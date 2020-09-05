package com.gracenote.content.auth.domain.sociallogin;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.gracenote.content.auth.domain.application.ApplicationController;
import com.gracenote.content.auth.persistence.entity.SocialLogin;

@RestController
@EnableResourceServer
@RequestMapping(value = "api/v1/sociallogin")
public class SocialLoginController {
	
	@Autowired
	SocialLoginService socialloginservice;

	private final Logger log = LoggerFactory.getLogger(SocialLoginController.class);
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value="/save", method = POST, consumes = APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity saveSocialLoginUserDetails(@RequestBody final SocialLogin sociallogin) {
        log.debug("Receive request to create the {0}", sociallogin);

        try {
            SocialLogin createdApplication = socialloginservice.save(sociallogin);

            if (createdApplication == null)
                return new ResponseEntity("{\"Response\":\"Invalid data input\"}", HttpStatus.NOT_ACCEPTABLE);

            return new ResponseEntity(createdApplication, HttpStatus.CREATED);
        } catch (DataAccessException e) {
            log.debug("Error in application creation: " + e.getMessage());
            return exceptionHandller("{\"Response\":\"Error occurred in creation\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ExceptionHandler(value = DataAccessException.class)
    public ResponseEntity exceptionHandller(String errMsg, HttpStatus status) {
        return new ResponseEntity(errMsg, status);
    }
}
