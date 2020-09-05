package com.gracenote.content.auth.util;

public class Validation {

	public static final String alphaNumericRegex= "^[a-zA-Z0-9]+$";
	public static final String onlyAlphaRegex="^[a-zA-Z]+$";
	public static final String onlyAlphaWithSpaceAllowed="^[a-zA-Z0-9 ]*$";
	public static final String roles="ROLE_ADMIN,ROLE_USER";
	public static final String EMAIL_REGEX = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";
	public static final String NIELSEN_EMAIL_REGEX= "^[\\w\\d](\\.?[\\w\\d]){3,}@nielsen\\.com$";
	//public static final String EMAIL_REGEX = "^([\\w-\\.]+){1,64}@([\\w&&[^_]]+){2,255}.[a-z]{2,}$";

	public static boolean validate(String value,String regexpattern){
		if(value!=null && !("").equalsIgnoreCase(value)) {
			return value.matches(regexpattern);
		}
		return false;
	}
	public static boolean validateRole(String role) {
		if(role!=null && !("").equalsIgnoreCase(role) && roles.contains(role)) {
			return true;
		}
		return false;
	}

}
