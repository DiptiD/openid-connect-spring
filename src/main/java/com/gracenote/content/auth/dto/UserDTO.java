package com.gracenote.content.auth.dto;

import java.sql.Timestamp;

public class UserDTO {

	private int userId;
	private String userName;
	private String emailAddress;
	private Timestamp Date;
	private boolean isExternalUser;
	private String firstName;
	private String lastName;
	private String gracenoteId;

	public String getGracenoteId() {
		return gracenoteId;
	}
	public void setGracenoteId(String gracenoteId) {
		this.gracenoteId = gracenoteId;
	}
	public boolean isExternalUser() {
		return isExternalUser;
	}
	public void setExternalUser(boolean externalUser) {
		isExternalUser = externalUser;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public Timestamp getDate() {
		return Date;
	}
	public void setDate(Timestamp date) {
		Date = date;
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
}