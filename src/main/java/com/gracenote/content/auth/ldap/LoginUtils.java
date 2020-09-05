package com.gracenote.content.auth.ldap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource(value ="classpath:application.properties")
public class LoginUtils {

	@Autowired
	Environment env;

	private static final String DEFAULT_DOMAINS = "gracenote,gracenote";
	private static final String DEFAULT_CONTROLLERS = "qbdc02.gracenote.gracenote.com,qbdc03.gracenote.gracenote.com";
	private static final String DEFAULT_WINDOW_DOMAIN="gracenote";
	private static final String DEFAULT_WINDOW_CONTROLLERS="qbdc02.gracenote.gracenote.com";

	/**
     * Get the map of domain details with domain controller and domains
     * @return the domain details 
     */ 
	public static Map<String,String[]> getDomainAndDomainController(String domain,String domainController) throws Exception {
		Map<String,String[]> domainDetail=new HashMap<String,String[]>();
	 	String domains[];
        String domainControllers[];
	 	if (domain == null || domainController == null) {
	 		throw new Exception("Could not get domain controller or Windows domain name");
	 	}
        if(domain.indexOf(',') != -1) {
			domains = domain.split("\\,");
		} else {
          domains = new String[] {domain};
        }
        domainDetail.put("domains", domains);
        if(domainController.indexOf(',') != -1) {
			domainControllers = domainController.split("\\,");
        } else {
          domainControllers = new String[] {domainController};
        }
        domainDetail.put("domainControllers", domainControllers);
        if(domains == null || domainControllers == null) {
          throw new Exception("Authentication Login Exception: Property com.gracenote.LdapLoginApi.WindowsAuthentication.windows-domain does not properly specify a domain or domain controller.");
        }

        if(domains.length != domainControllers.length) {
          throw new Exception("Authentication Login Exception: Property com.gracenote.LdapLoginApi.WindowsAuthentication.windows-domain-contollers count doesn't match windows-domain count.");
        }
        return domainDetail;
	}
	
	/**
     * Get the domain name 
     * @return the domain name 
     */ 
	public static String getDomain() {
		String domain = System.getProperty("com.gracenote.LdapLoginApi.LdapAuthentication.windows-domain");
		if(domain == null || domain.length() < 1) {
			domain = DEFAULT_DOMAINS;
			System.out.println("WARNING:  Defaulting to the windows domain: " + domain);
		}
		return domain;
	}

	/**
     * Get the domain controller we want to authenticate against 
     * @return the name of the domain controller 
     */ 
	public static String getDomainController() {
		String domainController = System.getProperty("com.gracenote.LdapLoginApi.LdapAuthentication.windows-domain-controller");
		if(domainController == null || domainController.length() < 1) {
			domainController = DEFAULT_CONTROLLERS;
			System.out.println("WARNING:  Defaulting the windows domain controller to be: " + domainController);
		}
		return domainController;
	}
	
	/**
     * Get the window domain name 
     * @return the domain name 
     */ 
	public static String getWindowsDomain() {
	    String domain = System.getProperty("com.gracenote.LdapLoginApi.WindowsAuthentication.windows-domain");
	    if(domain == null || domain.length() < 1) {
	      domain = DEFAULT_WINDOW_DOMAIN;
	      System.out.println("WARNING:  Defaulting to the windows domain: " + domain);
	    }
	    return domain;
	  }

	/**
     * Get the Window domain controller we want to authenticate against 
     * @return the name of the domain controller 
     */ 
	public static String getWindowDomainController() {
	    String domainController = System.getProperty("com.gracenote.LdapLoginApi.WindowsAuthentication.windows-domain-controller");
	    if(domainController == null || domainController.length() < 1) {
	      domainController = DEFAULT_WINDOW_CONTROLLERS;
	      System.out.println("WARNING:  Defaulting the windows domain controller to be: " + domainController);
	    }
	    return domainController;
	  }

	public String getNSDomain() {
		System.out.println(env.getProperty("NS_DEFAULT_DOMAINS"));
		return env.getProperty("NS_DEFAULT_DOMAINS");
	}

	public String getNSDomainController() {
		System.out.println(env.getProperty("NS_DEFAULT_CONTROLLERS"));
		return env.getProperty("NS_DEFAULT_CONTROLLERS");
	}

}
