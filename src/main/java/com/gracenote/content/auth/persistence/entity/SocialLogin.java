package com.gracenote.content.auth.persistence.entity;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Size;

/**
 * Represents Social login user details in database.
 *
 * @author Vasudevan on 20/03/19.
 */

@Entity
public class SocialLogin extends BaseEntity{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @Column(updatable = false, nullable = false)
    private String id;
    
    @Column(updatable = false, nullable = false)
    @Size(min = 0, max = 45)
    private String name;

    @Size(min = 0, max = 45)
    private String email;

    @Size(min = 0, max = 200)
    @Column(name = "photoUrl")
    private String photoUrl;

    @Size(min = 0, max = 45)
    @Column(name = "firstName")
    private String firstName;

    @Size(min = 0, max = 45)
    @Column(name = "lastName")
    private String lastName;

    @Size(min = 0, max = 500)
    @Column(name = "authToken")
    private String authToken;
    
    @Size(min = 0, max = 45)
    @Column(name = "provider")
    private String provider;
    
    @Size(min = 0, max = 10)
    @Column(name = "verified_email")
    private String verified_email;
    
    @Size(min = 0, max = 100)
    @Column(name = "link")
    private String link;
    
    @Size(min = 0, max = 50)
    @Column(name = "gender")
    private String gender;

    @Size(min = 0, max = 50)
    @Column(name = "locale")
    private String locale;
    
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhotoUrl() {
		return photoUrl;
	}

	public void setPhotoUrl(String photoUrl) {
		this.photoUrl = photoUrl;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String isVerified_email() {
		return verified_email;
	}

	public void setVerified_email(String verified_email) {
		this.verified_email = verified_email;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}
}
