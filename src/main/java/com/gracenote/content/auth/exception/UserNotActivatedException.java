package com.gracenote.content.auth.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author deepak on 10/8/17.
 */
public class UserNotActivatedException extends AuthenticationException{

    public UserNotActivatedException(String msg, Throwable t) {
        super(msg, t);
    }

    public UserNotActivatedException(String msg) {
        super(msg);
    }

}
