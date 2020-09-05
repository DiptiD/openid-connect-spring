package com.gracenote.content.auth.domain.history;

import com.gracenote.content.auth.persistence.entity.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Rest end point of User application recent service of content auth.
 * normally used for get list of user's recent application.
 *
 * @author deepak on 9/10/17.
 */

@RestController
@EnableResourceServer
@RequestMapping(value = "api/v1/recentApplications")
public class UserApplicationRecentController {

    @Autowired
    UserApplicationRecentService userApplicationRecentService;

    private final Logger log = LoggerFactory.getLogger(UserApplicationRecentController.class);

    /**
     * Get list of all user application recent
     *
     * @return list of user application recent.
     */
    @RequestMapping(value = "", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity getUserApplicationRecent() {
        log.info("Enter to get the user application recent list");

        try {
            List<Application> recentList = userApplicationRecentService.getRecentApplication();

            if (recentList == null || recentList.isEmpty())
                return new ResponseEntity("{\"Response\":\"No content\"}", HttpStatus.NO_CONTENT);

            return new ResponseEntity(recentList, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.error("Exception occurred at get all user application recent: " + e.getMessage());
            return new ResponseEntity("{\"Response\":\"No application found\"}", HttpStatus.NOT_FOUND);
        }
    }
}
