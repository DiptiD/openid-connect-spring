package com.gracenote.content.auth.domain.history;

import com.gracenote.content.auth.domain.user.UserService;
import com.gracenote.content.auth.dto.UserDTO;
import com.gracenote.content.auth.persistence.entity.Application;
import com.gracenote.content.auth.persistence.entity.User;
import com.gracenote.content.auth.persistence.entity.UserApplicationRecent;
import com.gracenote.content.auth.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents information about user recently access application.
 *
 * @author deepak on 4/10/17.
 */

@Service
public class UserApplicationRecentService {

    private final Logger log = LoggerFactory.getLogger(UserApplicationRecentService.class);

    @Autowired
    UserApplicationRecentRepository applicationRecentRepository;

    @Autowired
    UserApplicationHistoryService historyService;

    @Autowired
    UserService userService;

    /**
     * Add entry for user recently accessing application.
     *
     * @param user, appliction.
     * @return boolean on success.
     */
    public boolean addRecentApplicationAccess(User user, Application application) throws DataAccessException {

        if (user == null || application == null) {

            log.error("user or application not found");
            throw new IllegalArgumentException("User and application can not be null");
        }

        log.info("Add user application recent User: {}, application: {}", user.getUser_id(), application.getApplicationId());

        //List<UserApplicationRecent> recentList = getUserApplicationRecent(user);
        List<UserApplicationRecent> recentList = getUserApplicationRecent(user.getUser_id());

        UserApplicationRecent userApplicationRecent = new UserApplicationRecent();
        userApplicationRecent.setLoginTime(new Timestamp(System.currentTimeMillis()));
        userApplicationRecent.setUser(user);
        userApplicationRecent.setApplication(application);


        /**
         * Add user, application entry to history table, except for dashboard.
         * For dashboard we are making entry from DashboardEntryFilter
         * */
        historyService.save(user.getUser_id(), application.getApplicationId());

        // If does not have any record, add it and return.
        if (recentList == null || recentList.isEmpty()) {
            log.info("Add user history for first time User: {}, application: {}", user.getUser_id(), application.getApplicationId());
            insert(userApplicationRecent);
            return true;
        }

        List<UserApplicationRecent> hasList = null;
        hasList = recentList.stream()
                .filter(s -> s.getApplication().getApplicationId() == application.getApplicationId())
                .collect(Collectors.toList());

        // If already has entry for accessing application update login time
        if (hasList != null && !hasList.isEmpty()) {
            log.info("Already has record, update user's login time User: {}, application: {} ", user.getUser_id(), application.getApplicationId());
            UserApplicationRecent updateUserApplication = hasList.get(0);
            updateUserApplication.setLoginTime(new Timestamp(System.currentTimeMillis()));

            insert(updateUserApplication);
            return true;
        }

        // If having less than specified entries (eg.3) then add it.
        if (recentList.size() < Constants.RECENT_APP_LIMIT) {
            log.info("Adding record User: {}, application: {}", user.getUser_id(), application.getApplicationId());
            insert(userApplicationRecent);
            return true;
        } else if (recentList.size() == Constants.RECENT_APP_LIMIT) {

            /* if number of record for user and applications are upto the spcified limit,
                delete the older record and make new entry with application.
            */

            log.info("deleting record old application{} ", recentList.get(0).getApplication().getApplicationId());
            log.info("adding record User: {}, application: {}", user.getUser_id(), application.getApplicationId());
            int firstEntry = recentList.get(0).getId();
            applicationRecentRepository.deleteById(firstEntry);

            insert(userApplicationRecent);
            return true;
        }

        return false;
    }


    public List<UserApplicationRecent> getUserApplicationRecent(int userId) {

        if (userId <= 0)
            throw new IllegalArgumentException("Incorrect user id");

        try {
            return applicationRecentRepository.getUserApplicationRecent(userId);

        } catch (RuntimeException e) {
            log.error("Error occurred at the get user application recent : " + e.getMessage());
            throw new DataIntegrityViolationException("Database exception");
        }
    }


    /**
     * Get the list of user recent application access by specific user
     *
     * @return list of UserApplicationRecent
     */
    public List<Application> getRecentApplication() {

        UserDTO userDTO = userService.getLoggedInUserDetail();

        try {
            return applicationRecentRepository.getRecentApplications(userDTO.getUserId());

        } catch (RuntimeException e) {
            log.error("Error occurred at the get user application recent : " + e.getMessage());
            throw new DataIntegrityViolationException("Database exception");
        }
    }


    /**
     * Insert the record in user_application_recent
     *
     * @param userApplicationRecent
     */
    public void insert(UserApplicationRecent userApplicationRecent) {
        try {
            applicationRecentRepository.save(userApplicationRecent);
        } catch (RuntimeException e) {
            log.error("Error occurred at the time of saving User Application Recent : " + e.getMessage());
            throw new DataIntegrityViolationException("Database exception");
        }
    }

}
