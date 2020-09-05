package com.gracenote.content.auth.domain.application;

import com.gracenote.content.auth.persistence.entity.ApplicationAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * ApplicationAuthority class perform business logic for Application's authorities
 * and interacting with repository for db operations.
 *
 * @author deepak on 18/9/17.
 */
@Service
public class ApplicationAuthorityService {

    private final Logger log = LoggerFactory.getLogger(ApplicationAuthorityService.class);

    @Autowired
    private ApplicationAuthorityRepository applicationAuthorityRepository;

    /**
     * Save authority specific to application
     *
     * @param applicationAuthority
     * @return ApplicationAuthority
     */
    ApplicationAuthority save(ApplicationAuthority applicationAuthority) {
        log.info("Save application authority");
        return applicationAuthorityRepository.save(applicationAuthority);
    }

    /**
     * Get application authority specific to application
     *
     * @param applicationId
     * @return Set of ApplicationAuthority
     */
    Set<ApplicationAuthority> getApplicationAuthority(int applicationId) {
        log.info("Get application authority");
        return applicationAuthorityRepository.getApplicationsAuthorities(applicationId);
    }

    /**
     * Get default application authority specific to application
     *
     * @param applicationId
     * @return Default ApplicationAuthority of Application
     */
    ApplicationAuthority getDefaultApplicationAuthority(int applicationId) {
        log.info("Get default application authority");
        return applicationAuthorityRepository.getApplicationAuthorityIsDefaultTrue(applicationId);
    }
}