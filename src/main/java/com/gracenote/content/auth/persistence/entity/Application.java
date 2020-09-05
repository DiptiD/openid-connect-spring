package com.gracenote.content.auth.persistence.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 * Represents the available application details in database.
 *
 * @author Vasudevan on 07/09/17.
 */

@Entity
@Table(name = "application")
public class Application extends BaseEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "application_id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int applicationId;

    @Column(updatable = false, nullable = false, name = "application_name")
    @Size(min = 0, max = 50)
    private String applicationName;

    @Column(name = "is_restricted", nullable = false)
    private boolean isRestricted;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "expiry_status", nullable = false)
    private boolean expiry;

    @Column(name = "description")
    private String description;

    @Size(min = 0, max = 50)
    @Column(name = "image_url")
    private String imageUrl;
    
    @Size(min = 0,max = 50)
    @Column(name = "client_id")
    private String clientId;
    
    @Size(min = 0,max = 256)
    @Column(name = "headers")
    private String headers;
    
    @Size(min =0,max = 100)
    @Column(name = "ba_poc")
    private String baPoc;
    
    @Size(min =0,max = 100)
    @Column(name = "eng_poc")
    private String engPoc;
    
    @Size(min = 0,max = 100)
    @Column(name = "application_type")
    private String applicationType;
    
    @Size(min = 0,max = 100)
    @Column(name = "app_family")
    private String appFamily;
    
    @Size(min =0,max =100)
    @Column(name = "support_poc")
    private String supportPoc;
    
    @Column(name = "gn_active_directory", nullable = false)
    private boolean gnActiveDirectory;
    
    @Size(min =0,max =45)
    @Column(name = "user_roles")
    private String userRoles;
    
    @Size(min =0,max =200)
    @Column(name = "application_full_form")
    private String applicationFullForm;

  //@OneToMany(mappedBy = "application", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, orphanRemoval = true)
    @OneToMany(mappedBy = "application", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<ApplicationAuthority> applicationAuthority = new HashSet<ApplicationAuthority>();

	public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public Set<ApplicationAuthority> getApplicationAuthority() {
        return applicationAuthority;
    }

    public void setApplicationAuthority(Set<ApplicationAuthority> applicationAuthority) {
        this.applicationAuthority = applicationAuthority;
    }
    
	public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public boolean getIsRestricted() {
        return isRestricted;
    }

    public void setIsRestricted(boolean isRestricted) {
        this.isRestricted = isRestricted;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public boolean getExpiry() {
        return expiry;
    }

    public void setExpiry(boolean expiry) {
        this.expiry = expiry;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getHeaders() {
		return headers;
	}

	public void setHeaders(String headers) {
		this.headers = headers;
	}
	
	public String getBaPoc() {
		return baPoc;
	}

	public void setBaPoc(String baPoc) {
		this.baPoc = baPoc;
	}

	public String getEngPoc() {
		return engPoc;
	}

	public void setEngPoc(String engPoc) {
		this.engPoc = engPoc;
	}

	public String getApplicationType() {
		return applicationType;
	}

	public void setApplicationType(String applicationType) {
		this.applicationType = applicationType;
	}

	public String getAppFamily() {
		return appFamily;
	}

	public void setAppFamily(String appFamily) {
		this.appFamily = appFamily;
	}

	public String getSupportPoc() {
		return supportPoc;
	}

	public void setSupportPoc(String supportPoc) {
		this.supportPoc = supportPoc;
	}

	public boolean isGnActiveDirectory() {
		return gnActiveDirectory;
	}

	public void setGnActiveDirectory(boolean gnActiveDirectory) {
		this.gnActiveDirectory = gnActiveDirectory;
	}

	public String getUserRoles() {
		return userRoles;
	}

	public void setUserRoles(String userRoles) {
		this.userRoles = userRoles;
	}

	public String getApplicationFullForm() {
		return applicationFullForm;
	}

	public void setApplicationFullForm(String applicationFullForm) {
		this.applicationFullForm = applicationFullForm;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + applicationId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Application other = (Application) obj;
		if (applicationId != other.applicationId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "{\"applicationId\":" + applicationId + ", \"applicationName\":\"" + applicationName
				+ "\", \"isRestricted\":" + isRestricted + ", \"isDeleted\":" + isDeleted + ", \"expiry\":" + expiry
				+ ", \"description\":\"" + description + "\", \"imageUrl\":\"" + imageUrl + "\", \"clientId\":\"" + clientId + "\", \"headers\":\""
				+ headers + "\", \"baPoc\":\"" + baPoc + "\", \"engPoc\":\"" + engPoc + "\", \"applicationType\":\"" + applicationType + "\", \"appFamily\":\"" + appFamily + "\", \"applicationAuthority\":" + applicationAuthority + "}";
	}
}