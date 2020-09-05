package com.gracenote.content.auth.domain.history;

import com.gracenote.content.auth.domain.application.ApplicationService;
import com.gracenote.content.auth.domain.user.UserService;
import com.gracenote.content.auth.persistence.entity.Application;
import com.gracenote.content.auth.persistence.entity.User;
import com.gracenote.content.auth.persistence.entity.UserApplicationHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * This service class, performs business logic for User application history record
 * and interacting with repository.
 *
 * @author deepak on 6/10/17.
 */

@Service
public class UserApplicationHistoryService {

    private final Logger log = LoggerFactory.getLogger(UserApplicationHistory.class);

    @Autowired
    private UserApplicationHistoryRepository historyRepository;

    @Autowired
    UserService userService;

    @Autowired
    ApplicationService applicationService;

    @Async
    public boolean save(int userId, int applicationId) throws DataAccessException {

        if(userId <= 0 || applicationId <= 0)
            throw new IllegalArgumentException("user id or application id can not be null");

        log.info("add history for user {} & application {}", userId, applicationId);

        UserApplicationHistory userApplicationHistory = new UserApplicationHistory();
        userApplicationHistory.setUserId(userId);
        userApplicationHistory.setApplicationId(applicationId);
        userApplicationHistory.setLoginTime(new Timestamp(System.currentTimeMillis()));

        try {
            historyRepository.save(userApplicationHistory);
            return true;
        } catch (RuntimeException e) {

            log.error("Error occurred at the time add history : " + e.getMessage());
            throw new DataIntegrityViolationException("Database exception");
        }
    }

    @Async
    public boolean save(String username, String applicationName) {
        User user = userService.getUser(username);
        Application application = applicationService.getApplication(applicationName);

        if(user != null && application != null) {
            return save(user.getUser_id(), application.getApplicationId());
        }

        return false;
    }
}
