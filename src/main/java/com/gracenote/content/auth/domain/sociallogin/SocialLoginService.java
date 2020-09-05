package com.gracenote.content.auth.domain.sociallogin;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.gracenote.content.auth.persistence.entity.SocialLogin;

@Service
public class SocialLoginService {

	/***
     * Create new Application
     *
     * @param application
     * */
	@Autowired
	SocialLoginRepository socialloginrepository;
	
	private final Logger log = LoggerFactory.getLogger(SocialLoginService.class);
	
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public SocialLogin save(SocialLogin sociallogin) throws DataAccessException {

        sociallogin.setCreatedTime(new Timestamp(System.currentTimeMillis()));
        try {
        	sociallogin = socialloginrepository.save(sociallogin);
        } catch (RuntimeException e) {
            log.error("Error occurred at the time of saving social login user data: " + e.getMessage());
            throw new DataIntegrityViolationException("Database exception");
        }
        return sociallogin;
    }
}
