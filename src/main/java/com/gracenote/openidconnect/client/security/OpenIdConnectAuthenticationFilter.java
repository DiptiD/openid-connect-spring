package com.gracenote.openidconnect.client.security;

import static java.util.Optional.empty;

import java.io.IOException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Custom authentication filter for using OpenID Connect.
 */
@SuppressWarnings("deprecation")
public class OpenIdConnectAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private OAuth2RestOperations restTemplate;

	@Value("${google.openidconnect.client_id}")
	private String clientId;
	
	@Value("${google.jwkUrl}")
	private String jwkUrl;  

	public OpenIdConnectAuthenticationFilter(String defaultFilterProcessesUrl) {
		super(defaultFilterProcessesUrl);
		setAuthenticationManager(new NoOpAuthenticationManager());
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		OAuth2AccessToken accessToken;

		try {
			accessToken = restTemplate.getAccessToken();
			log.info("AccessToken: value: " + accessToken.getValue());
			log.info("AccessToken: additionalInfo: " + accessToken.getAdditionalInformation());
			log.info("AccessToken: tokenType: " + accessToken.getTokenType());
			log.info("AccessToken: expiration: " + accessToken.getExpiration());
			log.info("AccessToken: expiresIn: " + accessToken.getExpiresIn());
			log.info("AccessToken: refreshToken: " + accessToken.getRefreshToken());

		} catch (OAuth2Exception e) {
			throw new BadCredentialsException("Could not obtain Access Token", e);
		}

		try {
			final String idToken = accessToken.getAdditionalInformation().get("id_token").toString();
			log.info("Encoded id_token from accessToken.additionalInformation: " + idToken);

			String kid = JwtHelper.headers(idToken).get("kid");
			final Jwt tokenDecoded = JwtHelper.decodeAndVerify(idToken, verifier(kid));
			//final Jwt tokenDecoded = JwtHelper.decode(idToken);
			log.info("Decoded JWT id_token: " + tokenDecoded);
			log.info("Decoded JWT id_token -> claims: " + tokenDecoded.getClaims());

			final Map<String, String> authInfo = new ObjectMapper().readValue(tokenDecoded.getClaims(), Map.class);

			verifyClaims(authInfo);

			final OpenIdConnectUserDetails userDetails = new OpenIdConnectUserDetails(authInfo, accessToken);
			log.info("OpenIdConnectUserDetails -> userId: " + userDetails.getUsername());

			return new PreAuthenticatedAuthenticationToken(userDetails, empty(), userDetails.getAuthorities());

		} catch (Exception e) {
			log.debug("Could not obtain user details from Access Token", e.getMessage());
			throw new BadCredentialsException("Could not obtain user details from Access Token", e);
		}
	}

	private RsaVerifier verifier(String kid) throws Exception {
	    JwkProvider provider = new UrlJwkProvider(new URL(jwkUrl));
	    Jwk jwk = provider.get(kid);
	    return new RsaVerifier((RSAPublicKey) jwk.getPublicKey());
	}

	public void verifyClaims(Map claims) {
		int exp = (int) claims.get("exp");
		Date expireDate = new Date(exp * 1000L);
		Date now = new Date();
		String issuer = "accounts.google.com";
		if (expireDate.before(now) || !claims.get("iss").equals(issuer) || !claims.get("aud").equals(clientId)) {
			throw new RuntimeException("Invalid claims");
		}

	}

	public void setRestTemplate(OAuth2RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	private static class NoOpAuthenticationManager implements AuthenticationManager {
		@Override
		public Authentication authenticate(Authentication authentication) {
			throw new UnsupportedOperationException("No authentication should be done with this AuthenticationManager");
		}
	}
}
