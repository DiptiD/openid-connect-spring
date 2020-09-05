package com.gracenote.openidconnect.client.controllers;

import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("http://localhost:4200")
@RestController
@RequestMapping("/prajakta")
public class GreetingController {
	
	private static final String template = "Hello, %s";
	private static AtomicLong counter = new AtomicLong();
	
	@RequestMapping("/token")
	public Greeting greeting(@RequestParam(value = "name" ,defaultValue = "World") String name) {
		System.out.println("Inside Greetings>>>>>>>>>>>>>>>>>>");
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}
	
}
