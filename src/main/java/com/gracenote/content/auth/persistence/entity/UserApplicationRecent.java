package com.gracenote.content.auth.persistence.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Represents the available UserApplicationRecent details in database.
 *
 * @author Vasudevan on 07/09/17.
 */
@Entity
@Table(name = "user_application_recent")
public class UserApplicationRecent {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(updatable = false, nullable = false, name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int Id;

    @ManyToOne
    @JoinColumn(name = "userId")
    private User user;

    @ManyToOne
    @JoinColumn(name = "application_id")
    private Application application;

    @Column(name = "login_time")
    Timestamp loginTime;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Timestamp getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Timestamp loginTime) {
        this.loginTime = loginTime;
    }
}
