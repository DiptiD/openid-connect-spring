package com.gracenote.content.auth.config;

import com.gracenote.content.auth.util.AuthenticateTokenVO;
import com.gracenote.content.auth.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.sql.DataSource;

@RestController
@EnableResourceServer
public class LoginAPI {

    private final Logger log = LoggerFactory.getLogger(LoginAPI.class);

    @Autowired
    private DataSource dataSource;
    
    private JdbcTokenStore jdbcTokenStore;
    
    private JdbcClientDetailsService jdbcClientDetailsStore;

    @Autowired
	ConsumerTokenServices consumerTokenServices;

	@Autowired
	ClientDetailsServiceConfigurer client;

    @Autowired
    JdbcTokenStore tokenStore() {
        return jdbcTokenStore = new JdbcTokenStore(dataSource);
    }
    
    @Autowired
    JdbcClientDetailsService clientStore() {
    	return jdbcClientDetailsStore=new JdbcClientDetailsService(dataSource);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/application/authenticateToken", method = RequestMethod.POST,consumes=MediaType.APPLICATION_JSON_VALUE,produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity authenticate(@RequestBody AuthenticateTokenVO authenticateTokenVO) {
        log.info("Login API Authentication called :: parameters :: authenticateTokenVO :: " + authenticateTokenVO);
    	OAuth2Authentication authentication = null;
        authentication = jdbcTokenStore.readAuthentication(authenticateTokenVO.getAccessToken());
        if(authentication!=null && authentication.getPrincipal()!=null) {
	        String username = ((User) authentication.getPrincipal()).getUsername();
	        String clientId = authentication.getOAuth2Request().getClientId();
	        if (!Util.isValidString(username)) {
	            log.error("User Name must be required " + username);
	            return Util.exceptionHandler("invalid_username","UserName is empty or null :"+authenticateTokenVO.getUsername(), HttpStatus.NOT_ACCEPTABLE);
	        }
	        else if (!Util.isValidString(clientId)) {
	            log.error("Client Id must be required " + clientId);
	            return Util.exceptionHandler("invalid_clientId","ClientId is empty or null : "+authenticateTokenVO.getClientId(), HttpStatus.NOT_ACCEPTABLE);
	        }
	        else if (!authenticateTokenVO.getUsername().equalsIgnoreCase(username)) {
	            log.error("User Name does not matched with the requested parameters " + username);
	            return Util.exceptionHandler("invalid_username","Incorrect username : "+authenticateTokenVO.getUsername(), HttpStatus.NOT_ACCEPTABLE);
	        }
	        else if (!authenticateTokenVO.getClientId().equalsIgnoreCase(clientId)) {
	            log.error("Client Id does not matched with the requested parameters " + clientId);
	            return Util.exceptionHandler("invalid_clientId","Incorrect clientId : "+authenticateTokenVO.getClientId(), HttpStatus.NOT_ACCEPTABLE);
	        }
	        else
	        {
	        	log.info("Login API Inside authenticate method :: parameters :: authenticateTokenVO :: " + authenticateTokenVO != null ? authenticateTokenVO.toString() : null);
	        	return new ResponseEntity(true, HttpStatus.OK);
	        }
        }
        else
        {
        	log.error("Invalid token " + authenticateTokenVO.getAccessToken());
            return Util.exceptionHandler("invalid_token","Invalid Access Token : "+authenticateTokenVO.getAccessToken(), HttpStatus.NOT_ACCEPTABLE);
        }
   }
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/application/updateAccessToken", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity updateAccessToken(String value) {
    	Collection<OAuth2AccessToken> accessTokens=tokenStore().findTokensByClientId(value);
    	ClientDetails clientdetails=clientStore().loadClientByClientId(value);
    	Set<String> redirectUri=clientdetails.getRegisteredRedirectUri();
    	Iterator<OAuth2AccessToken> itr=(Iterator<OAuth2AccessToken>) accessTokens.iterator();
    	while(itr.hasNext()) {
    		OAuth2AccessToken oauth2AccessToken=itr.next();
    		oauth2AccessToken.getAdditionalInformation().put("registeredRedirectUri",redirectUri.iterator().next());
    		String tokenId=oauth2AccessToken.getValue();
    		OAuth2Authentication oath2Authication=tokenStore().readAuthentication(tokenId);
    		tokenStore().storeAccessToken(oauth2AccessToken, oath2Authication);
    	}
    	return new ResponseEntity(true, HttpStatus.OK);
    }

    public void addClient(String clientId, String clientSecret, int expiryTime, String redirectUrl) throws Exception {

    	log.info("Start add client to database");
    	client.jdbc(dataSource).withClient(clientId)
				.authorizedGrantTypes("password", "refresh_token")
				.authorities("ROLE_USER","ROLE_ADMIN")
				.scopes("read","write")
				.resourceIds("authsapp")
				.redirectUris(redirectUrl)
				.secret(clientSecret)
				.accessTokenValiditySeconds(expiryTime)
				.refreshTokenValiditySeconds(30000)
				.and().build();
	}

	public void updateClient(String clientId, int expiryTime, String redirectUrl) throws Exception {

		log.info("Start update client to database : "+clientId);

		/**
		 * first get  client info & get its secrete then remove client by using client id
		 * then again add client with new params.*/

			ClientDetails clientDetails = clientStore().loadClientByClientId(clientId);
			String clientSecret = clientDetails.getClientSecret();
			clientStore().removeClientDetails(clientId);

			client.jdbc(dataSource).withClient(clientId)
					.authorizedGrantTypes("password", "refresh_token")
					.authorities("ROLE_USER", "ROLE_ADMIN")
					.scopes("read", "write")
					.resourceIds("authsapp")
					.redirectUris(redirectUrl)
					.secret(clientSecret)
					.accessTokenValiditySeconds(expiryTime)
					.refreshTokenValiditySeconds(30000)
					.and().build();
	}


	public void removeTokens(String clientId) {
		Collection<OAuth2AccessToken> tokens = jdbcTokenStore.findTokensByClientId(clientId);
		if (tokens!=null){
			for (OAuth2AccessToken token:tokens){
				try {
					log.info("Token remove : " + token.getValue());
					consumerTokenServices.revokeToken(token.getValue());
				} catch (Exception e) {
					log.error("Token not found : "+token.getValue());
				}
			}
		}
	}

}