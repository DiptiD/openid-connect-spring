package com.gracenote.content.auth.persistence.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * To maintain the user's accessing application record.
 * It is just trailing table to add records.
 *
 * @author deepak on 6/10/17.
 */

@Entity
@Table(name = "user_application_history")
public class UserApplicationHistory {

    private static final long serialVersionUID = 1L;

    @javax.persistence.Id
    @Column(updatable = false, nullable = false, name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int Id;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "application_id")
    private int applicationId;

    @Column(name = "login_time")
    Timestamp loginTime;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public Timestamp getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Timestamp loginTime) {
        this.loginTime = loginTime;
    }
}
