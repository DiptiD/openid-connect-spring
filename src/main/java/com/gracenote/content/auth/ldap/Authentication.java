package com.gracenote.content.auth.ldap;

public class Authentication {

	public static AuthenticationFactory getAuthenticationFactory() throws Exception {
	    return AuthenticationFactory.getInstance();
	  } 
}
