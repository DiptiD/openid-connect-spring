package com.gracenote.content.auth.domain.user;

import com.gracenote.content.auth.persistence.entity.User;

/**
 * Represent pojo for external user
 *
 * @author deepak on 11/7/18.
 */
public class ExternalUser extends User {

    private static final long serialVersionUID = 1L;

    private String confirmPassword;

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
