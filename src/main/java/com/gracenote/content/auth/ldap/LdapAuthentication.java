package com.gracenote.content.auth.ldap;

import com.gracenote.content.auth.domain.user.UserDetailsService;
import com.gracenote.content.auth.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;

@Component
public class LdapAuthentication extends AuthenticationFactory {

	private final Logger log = LoggerFactory.getLogger(LdapAuthentication.class);

	@Autowired
	LoginUtils loginUtils;

	@Autowired
	UserDetailsService userDetailsService;
	/* (non-Javadoc)
	 * Authenticate a user. This is done by Ldap Authentication 
     * @param Username
     * @param password to authenticate with 
     * @return true if the given username and password matches with the 
     * ldap username and password for this user 
	 * @see com.gracenote.LdapLoginApI.AuthenticationFactory#authenticate(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean authenticate(String username, String password, String org) throws Exception {
		 boolean loginSuccess = false;
		if(username.startsWith("ituser") || username.startsWith("testuser") || "theload".equals(username)) {
	        String ituserPassword = System.getProperty("com.gracenote.LdapLoginApi.WindowsAuthenticationFactory.ituser-password");
	        String testuserPassword = System.getProperty("com.gracenote.LdapLoginApi.WindowsAuthenticationFactory.testuser-password");

	        if(!ituserPassword.equals(password)) {
	        	System.out.println("ituser password " + password + " doesn't match " + ituserPassword);
	        	throw new Exception("Invalid username or password");
	        }
	        else if(!testuserPassword.equals(password)) {
		    	System.out.println("testuser password " + password + " doesn't match " + testuserPassword);
		        throw new Exception("Invalid username or password");
		    }

	        throw new Exception("Invalid test username or password.");
	    }
		else {
			String domain = null;
			String domainController = null;
			if(org.equalsIgnoreCase("gracenote")) {
				domain = LoginUtils.getDomain();
				domainController = LoginUtils.getDomainController();
			}
			else if(org.equalsIgnoreCase("nielsen")) {
				domain = loginUtils.getNSDomain();
				domainController = loginUtils.getNSDomainController();
			}

			Map<String,String[]> domainValues=LoginUtils.getDomainAndDomainController(domain,domainController);
			String[] domainControllers=domainValues.get("domainControllers")!=null?domainValues.get("domainControllers"):null;
	        String[] domains=domainValues.get("domains")!=null?domainValues.get("domains"):null;
			Exception lastException = null;

	        if(domainControllers!=null && domains!=null) {
	        /**
	         * Loop through all the domains and the domain controllers, If any of
	         * the "tryLogin()" calls works without throwing an exception, then
	         * the user is authenticated.
	         */
				for (int i = 0; (i < domainControllers.length) && (loginSuccess == false); i++) {
					String tempDomainController = domainControllers[i];
				  String tempDomain = domains[i];
				  Exception e = tryLogin(tempDomainController, tempDomain, username, password, org);
				  if(e == null) {
					loginSuccess = true;
					System.out.println("LdapLoginApI Authentication : Successful login for " + username + " using domain controller " + tempDomainController + ". Domain " + tempDomain);
					return loginSuccess;
				  }
				}

		        throw new AuthenticationException("Invalid credentials");
	        }
	        else
	        {
	        	throw new Exception("Domains is not Specified for validate Successful Connection");
	        }
	      }

	}
	
	/**
	   * This is actually tries to login with ldap with provided valid parameters.
	   * If an exception is thrown, then the login was unsuccessful.
	   * @param domainController
	   * @param domain
	   * @param user
	   * @param password
	   * @return void which means authentication is successful.
	   */
	public void LdapConfiguration(String domainController, String domain, String user, String password, String org) throws NamingException{
		DirContext  dircontext = null;
		String securityPrincipal = domain + "\\" + user;
        System.out.println(securityPrincipal + " is trying to log in...");
		Hashtable<String,String> environment = new Hashtable<String,String>();   
		environment.put(Context.INITIAL_CONTEXT_FACTORY,"com.sun.jndi.ldap.LdapCtxFactory");   
		environment.put(Context.SECURITY_AUTHENTICATION, "simple");
		environment.put(Context.SECURITY_PRINCIPAL, securityPrincipal);   
		environment.put(Context.SECURITY_CREDENTIALS, password);   
		environment.put(Context.PROVIDER_URL, "ldap://" + domainController); 
		try {
			dircontext = new InitialDirContext(environment);
			System.out.println("Connection Successful.");
			SearchControls constraints = new SearchControls();
			constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
			String[] attrIDs = {
					"sn",
					"givenname",
					"mail"
			};
			constraints.setReturningAttributes(attrIDs);
			NamingEnumeration answer = null;
			if(null != org && org.equalsIgnoreCase("gracenote")) {
				answer = dircontext.search("DC=gracenote,DC=gracenote,DC=com", "sAMAccountName="
						+ user, constraints);
			} else if(null != org && org.equalsIgnoreCase("nielsen")) {
				answer = dircontext.search("DC=enterprisenet,DC=org", "sAMAccountName="
						+ user, constraints);
			}

			if (null != answer && answer.hasMore()) {
				Attributes attrs = ((SearchResult) answer.next()).getAttributes();
				HashMap<String, String> userMap = new HashMap<>();

				String fName = null==attrs.get("givenName") ? null :
						attrs.get("givenName").toString().replace("givenName: ","").trim();

				String lName = null==attrs.get("sn") ? null :
						attrs.get("sn").toString().replace("sn: ","").trim();

				String email = null==attrs.get("mail") ? null :
						attrs.get("mail").toString().replace("mail: ","").trim();

				log.info("First name : "+fName+" Last name : "+lName+ " Email : "+email);

				userMap.put("givenName", fName);
				userMap.put("sn", lName);
				userMap.put("mail", email);
				userDetailsService.getUserDetailsMap().put(user,userMap);
			}else{
				throw new AuthenticationException("Invalid User");
			}
		}
		finally {
			if(dircontext!=null) {
				dircontext.close();
			}
		}
        
	}
	/**
	   * This is the piece that actually tries to login.
	   * If an exception is thrown, then the login was unsuccessful, and the
	   * exception is returned to the caller.
	   * @param domainController
	   * @param domain
	   * @param user
	   * @param password
	   * @return The Exception that is encountered from Ldap.
	   */
	public Exception tryLogin(String domainController, String domain, String user, String password, String org) throws AuthenticationException {
		
		Exception exception = null;
		if(password != null && password.length() > 0) 
		{
			try {
					LdapConfiguration(domainController,domain,user,password, org);
		    	}
				catch(AuthenticationException ex) {
					ex.printStackTrace();
					exception = ex;
					System.err.println("Authentication not Success!");
				}
				catch(NamingException nex){
		            System.out.println("LDAP Connection: FAILED");
		            exception = nex;
		            nex.printStackTrace();   
		        }

		        return exception;
		}
		else
		{
			throw new AuthenticationException("Password is empty");
		}
	}
}
