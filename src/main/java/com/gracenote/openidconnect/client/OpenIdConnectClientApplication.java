package com.gracenote.openidconnect.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAutoConfiguration
@SpringBootApplication
public class OpenIdConnectClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(OpenIdConnectClientApplication.class, args);
	}
}
