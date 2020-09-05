package com.gracenote.content.auth.util;

/**
* Represents to export the application details in excel format.
*
* @author Vasudevan on 07/09/17.
*/
public class ExcelPO {

	    private static final long serialVersionUID = 1L;

	    private String applicationName;

	    private boolean isRestricted;

	    private boolean isDeleted;

	    private String description;

	    private String imageUrl;
	    
	    private String baPoc;
	    
	    private String engPoc;
	    
	    private String applicationType;
	    
	    private String appFamily;
	    
	    private String supportPoc;
	    
	    private boolean gnActiveDirectory;
	    
	    private String userRoles;
	    
	    private String applicationFullForm;
	    
	    private String horizontalName;
	    
	    private String verticalName;
	    
	    private String redirectUrl;

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
		
		public String getHorizontalName() {
			return horizontalName;
		}

		public void setHorizontalName(String horizontalName) {
			this.horizontalName = horizontalName;
		}

		public String getVerticalName() {
			return verticalName;
		}

		public void setVerticalName(String verticalName) {
			this.verticalName = verticalName;
		}

		public String getRedirectUrl() {
			return redirectUrl;
		}

		public void setRedirectUrl(String redirectUrl) {
			this.redirectUrl = redirectUrl;
		}
}
