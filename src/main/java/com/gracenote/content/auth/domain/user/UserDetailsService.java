package com.gracenote.content.auth.domain.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.gracenote.content.auth.domain.application.ApplicationService;
import com.gracenote.content.auth.domain.sociallogin.SocialLoginService;
import com.gracenote.content.auth.exception.UserNotActivatedException;
import com.gracenote.content.auth.ldap.LdapAuthentication;
import com.gracenote.content.auth.persistence.entity.Application;
import com.gracenote.content.auth.persistence.entity.ApplicationAuthority;
import com.gracenote.content.auth.persistence.entity.SocialLogin;
import com.gracenote.content.auth.persistence.entity.User;
import com.gracenote.content.auth.util.Constants;
import com.gracenote.content.auth.util.Crypto;

/**
 * Responsible for get the user details from DB and validate with the details supplied.
 *
 * @author deepak on 10/8/17.
 */

@Component("userDetailsService")
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(UserDetailsService.class);

    private Map userDetailsMap = new HashMap<>();

    @Autowired
    Gson gson;
    
    @Autowired
    SocialLoginService socialloginservice;
    
    @Autowired
    UserService userService;

    @Autowired
    LdapAuthentication ldapAuthentication;

    @Autowired
    ApplicationService applicationService;

    @Override
    public UserDetails loadUserByUsername(String username) {

    	if(username.contains("_sso_")) {
    		return ssoLogin(username);
    	}
    	else if(username.contains("social")){
    		return openIdLogin(username);
    	}
    	else {
    		return ssoLogin(username);
    	}
    }
    
    private UserDetails ssoLogin(String username) {
    	String[] str_array = username.split("_sso_");
        String uname = username;  // used this username at the time of PreAuthentications.
        String pass = null;
        String orignalPass = null;

        boolean ldapLogin = false;
        boolean nsLdapLogin = false;
        String org = null;

        if (str_array.length == 2) {
            uname = str_array[0];
            pass = str_array[1];

            if (uname != null && pass != null) {
                orignalPass = Crypto.dycryptData(pass);

                if (orignalPass == null) {
                    log.error("Error in encryption.");
                    throw new BadCredentialsException("Invalid credentials");
                }
            } else {
                log.error("Username or password can not be null: ");
                throw new BadCredentialsException("Invalid credentials");
            }
        }

        log.debug("Authenticating {}", uname);
        String lowercaseUname = uname.toLowerCase();

        User userFromDatabase;
        if (lowercaseUname.contains(Constants.AT)) {
            userFromDatabase = userService.getUserByEmail(lowercaseUname);
        } else {
            userFromDatabase = userService.getUser(lowercaseUname);
        }

        if (null != userFromDatabase && userFromDatabase.isExternalUser()) {
            // External users
        } else {
            try {
                ldapLogin = false;//ldapAuthentication.authenticate(uname, orignalPass, "gracenote");
                org = "gracenote";
                log.info("GN LDAP authentication successful for username " + uname);
            } catch (Exception e) {
                log.info("Gracenote LDAP authentication failed for username " + uname);
                e.printStackTrace();
            }

            if (!ldapLogin) {
                try {
                    nsLdapLogin = false;//ldapAuthentication.authenticate(uname, orignalPass, "nielsen");
                    org = "nielsen";
                    log.info("NS LDAP authentication successful for username " + uname);
                } catch (Exception e1) {
                    log.info("NS LDAP authentication failed for username " + uname);
                    e1.printStackTrace();
                }
            }

            if (userFromDatabase == null) {
                log.info("User is not present in database: " + lowercaseUname);
                if (ldapLogin || nsLdapLogin) {

                    UsernamePasswordAuthenticationToken authenticationToken =
                            (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
                    String clientName = authenticationToken.getName();

                /* Adding user to DB if user is GN user. */

                    Application application = applicationService.getApplicationByClientId(clientName);

                    if (application == null) {
                        log.error("Accessing invalid application: " + clientName);
                        throw new BadCredentialsException("Invalid credentials");
                    }

                    if (application.getIsRestricted()) {
                        log.error("Access denied to user for this application: " + uname + " : " + clientName);
                        throw new AccessDeniedException("Access denied");
                    }

                    // This will need to add entry in user_application_authority

                    ApplicationAuthority applicationAuthority = applicationService.getDefaultAuthority(application.getApplicationId());
                    List<ApplicationAuthority> list = new ArrayList<>();
                    if (applicationAuthority == null) {
                        log.info("Default authority not available for application: " + clientName);
                        throw new BadCredentialsException("Invalid application ");
                    }
                    list.add(applicationAuthority);
                    Set<ApplicationAuthority> authoritySet = applicationService.createApplicationAuthoritySet(list);

                    userFromDatabase = userService.setInternalUser(uname, authoritySet, org);
                    userService.create(userFromDatabase);

                    log.info("User added to database: " + uname + " : " + clientName);

                } else {
                    log.error("User not found in database: " + lowercaseUname);
                    throw new UsernameNotFoundException("User " + lowercaseUname + Constants.NOT_FOUND_IN_DB_MSG);
                }

            } else if (!userFromDatabase.isActivated()) {
                log.error("User is not activated: " + lowercaseUname);
                throw new UserNotActivatedException("User " + lowercaseUname + Constants.NOT_ACTIVATED_MSG);
            } else if (null == userFromDatabase.getFirstName() || userFromDatabase.getFirstName().trim().isEmpty()) {
                // To add firstname, lastname, email of existing Nilsen users

                userService.setEmailFnameLname(userFromDatabase);
                userService.create(userFromDatabase);
            }
            userDetailsMap.remove(uname);
        }

        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (ApplicationAuthority authority : userFromDatabase.getApplicationAuthorities()) {
            GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(authority.getRole_name());
            grantedAuthorities.add(grantedAuthority);
        }

        if (ldapLogin || nsLdapLogin) {
            /* If GN user match password with supplied password else match with password in database */
            return new org.springframework.security.core.userdetails.User(
                    userFromDatabase.getUsername(),
                    new BCryptPasswordEncoder().encode(pass), grantedAuthorities);
        } else {
            return new org.springframework.security.core.userdetails.User(
                    userFromDatabase.getUsername(),
                    userFromDatabase.getPassword(), grantedAuthorities);
        }
    }
    
    
    private UserDetails openIdLogin(String emailId) {
    	String[] str_array = emailId.split("_social_");
        String email = emailId;  // used this username at the time of PreAuthentications.
        String pass = null;
        String userdetails=null;
        boolean googleApiAuthentication=false;

        if (str_array.length == 3) {
            email = str_array[0];
            pass = str_array[1];
            userdetails= str_array[2];
        }
        String lowercaseUname = email.toLowerCase();

        User userFromDatabase=null;
        if (lowercaseUname.contains(Constants.NS_COM)) {
            userFromDatabase = userService.getUser(lowercaseUname);
            if(pass!=null && userdetails!=null) {
            	googleApiAuthentication=saveOpenIdData(email,pass,userdetails);
            }
        }
            if (userFromDatabase == null) {
                log.info("User is not present in database: " + lowercaseUname);
                if (googleApiAuthentication) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
                    String clientName = authenticationToken.getName();

                /* Adding user to DB if user is GN user. */

                    Application application = applicationService.getApplicationByClientId(clientName);

                    if (application == null) {
                        log.error("Accessing invalid application: " + clientName);
                        throw new BadCredentialsException("Invalid credentials");
                    }

                    if (application.getIsRestricted()) {
                        log.error("Access denied to user for this application: " + " : " + clientName);
                        throw new AccessDeniedException("Access denied");
                    }

                    // This will need to add entry in user_application_authority

                    ApplicationAuthority applicationAuthority = applicationService.getDefaultAuthority(application.getApplicationId());
                    List<ApplicationAuthority> list = new ArrayList<>();
                    if (applicationAuthority == null) {
                        log.info("Default authority not available for application: " + clientName);
                        throw new BadCredentialsException("Invalid application ");
                    }
                    list.add(applicationAuthority);
                    Set<ApplicationAuthority> authoritySet = applicationService.createApplicationAuthoritySet(list);
                    googleApiAuthentication=saveOpenIdData(email,pass,userdetails);
                    userFromDatabase = userService.setOpenIdInternalUser(email, authoritySet,userdetails,pass);
                    userService.create(userFromDatabase);
                    log.info("User added to database: " + email + " : " + clientName);

                } else {
                    log.error("User not found in database: " + lowercaseUname);
                    throw new UsernameNotFoundException("User " + lowercaseUname + Constants.NOT_FOUND_IN_DB_MSG);
                }

            } else if (!userFromDatabase.isActivated()) {
                log.error("User is not activated: " + lowercaseUname);
                throw new UserNotActivatedException("User " + lowercaseUname + Constants.NOT_ACTIVATED_MSG);
            }else {
            	//if (null != userFromDatabase.getFirstName() || userFromDatabase.getFirstName().trim().isEmpty()) {
                // To add firstname, lastname, email of existing Nilsen users

                userService.setEmailFnameLname(userFromDatabase);
                userService.create(userFromDatabase);
                userDetailsMap.remove(userFromDatabase.getUsername().toLowerCase());
            }

        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (ApplicationAuthority authority : userFromDatabase.getApplicationAuthorities()) {
            GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(authority.getRole_name());
            grantedAuthorities.add(grantedAuthority);
        }

        if (googleApiAuthentication) {
            /* If GN user match password with supplied password else match with password in database */
            return new org.springframework.security.core.userdetails.User(
                    userFromDatabase.getUsername(),
                    new BCryptPasswordEncoder().encode(pass), grantedAuthorities);
        } else {
            return new org.springframework.security.core.userdetails.User(
                    userFromDatabase.getUsername(),
                    userFromDatabase.getPassword(), grantedAuthorities);
        }

    }
    
	@SuppressWarnings("unchecked")
	public Map<String,String> getUserInfo(String emailId,String accessToken,String userDetails)
    {
        final String uri = Constants.GOOGLE_API_URL+"?access_token="+accessToken;
        RestTemplate restTemplate = new RestTemplate();
        Map<String,String> result = restTemplate.getForObject(uri,Map.class);
        return result;
    }
	
	private boolean saveOpenIdData(String emailId,String accessToken,String userDetails) {
		Map<String,String> result = getUserInfo(emailId,accessToken,userDetails);
		HashMap<String, String> userMap = new HashMap<>();
		if(result.get("email").equals(emailId)){
    		SocialLogin socialLogin=setSocialData(result,userDetails);
        	socialloginservice.save(socialLogin);
        	userMap.put("givenName", result.get("given_name"));
			userMap.put("sn", result.get("family_name"));
			userMap.put("mail", emailId);
			getUserDetailsMap().put(emailId,userMap);
        	return true;
        }
		return false;
	}
	
	
	private SocialLogin setSocialData(Map<String,String> result,String userDetails) {
		SocialLogin socialLogin=gson.fromJson(userDetails, SocialLogin.class);
    	socialLogin.setFirstName(result.get("given_name"));
    	socialLogin.setLastName(result.get("family_name"));
    	socialLogin.setGender(result.get("gender"));
    	socialLogin.setLocale(result.get("locale"));
    	socialLogin.setLink(result.get("link"));
    	socialLogin.setVerified_email(String.valueOf(result.get("verified_email")));
    	return socialLogin;
	}
    
    
	public Map getUserDetailsMap() {
		return userDetailsMap;
	}

	public void setUserDetailsMap(Map userDetailsMap) {
		this.userDetailsMap = userDetailsMap;
	}
}
