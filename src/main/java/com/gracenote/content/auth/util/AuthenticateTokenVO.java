package com.gracenote.content.auth.util;

public class AuthenticateTokenVO {

	private String username;
	private String accessToken;
	private String clientId;
	private String expiryTime;
	private String endpointUrl;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getExpiryTime() {
		return expiryTime;
	}
	public void setExpiryTime(String expiryTime) {
		this.expiryTime = expiryTime;
	}
	public String getEndpointUrl() {
		return endpointUrl;
	}
	public void setEndpointUrl(String endpointUrl) {
		this.endpointUrl = endpointUrl;
	}
	@Override
	public String toString() {
		return "AuthenticateTokenVO [username=" + username + ", accessToken=" + accessToken + ", clientId=" + clientId
				+ ", expiryTime=" + expiryTime + ", endpointUrl=" + endpointUrl + "]";
	}
}