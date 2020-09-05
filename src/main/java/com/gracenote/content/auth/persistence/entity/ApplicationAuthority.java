package com.gracenote.content.auth.persistence.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents the available ApplicationAuthority details in database.
 *
 * @author Vasudevan on 07/09/17.
 */

@Entity
@Table(name = "application_authority")
public class ApplicationAuthority extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_authority_id", nullable = false, updatable = false)
    private int applicationAuthorityId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "application_id", insertable = false, updatable = false)
    private Application application;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_name", insertable = false, updatable = false)
    private Authority authority;
    
	private String role_name;

    private int application_id;

    @Column(name = "is_default_role")
    private boolean isDefaultRole;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Authority getAuthority() {
        return authority;
    }

    public void setAuthority(Authority authority) {
        this.authority = authority;
    }

    public boolean getIsDefaultRole() {
        return isDefaultRole;
    }

    public void setIsDefaultRole(boolean isDefaultRole) {
        this.isDefaultRole = isDefaultRole;
    }

    public String getRole_name() {
        return role_name;
    }

    public void setRole_name(String role_name) {
        this.role_name = role_name;
    }

    public int getApplication_id() {
        return application_id;
    }

    public void setApplication_id(int application_id) {
        this.application_id = application_id;
    }

    public int getApplicationAuthorityId() {
        return applicationAuthorityId;
    }

    public void setApplicationAuthorityId(int applicationAuthorityId) {
        this.applicationAuthorityId = applicationAuthorityId;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    @Override
	public String toString() {
		return "{\"applicationAuthorityId\":" + applicationAuthorityId + ", \"role_name\":\"" + role_name + "\", \"application_id\":" + application_id
				+ ", \"isDefaultRole\":" + isDefaultRole + ", \"isDeleted\":" + isDeleted + "}";
	}

}