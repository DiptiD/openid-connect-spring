package com.gracenote.content.auth.ldap;


public abstract class AuthenticationFactory {

	  private static AuthenticationFactory instance = null;

	  @SuppressWarnings("rawtypes")
	  public static AuthenticationFactory getInstance() throws Exception{
	    if (instance == null) {
	      String classname = System.getProperty("AuthenticationFactory.classname");
	      if(classname == null || classname.length() < 1) {
	        classname = "com.gracenote.content.auth.ldap.LdapAuthentication";
	      }
	      Class c = null;
	      c = Class.forName(classname);
	      instance = (AuthenticationFactory)c.newInstance();
	    }
	    System.out.println("LdapLoginApI Authentication : Authentication Factory Instance is Created");
	    return instance;
	  }

	  public abstract boolean authenticate(String user, String password, String org) throws Exception;

	}
