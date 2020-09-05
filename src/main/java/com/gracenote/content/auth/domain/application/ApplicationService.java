package com.gracenote.content.auth.domain.application;

import com.gracenote.content.auth.config.LoginAPI;
import com.gracenote.content.auth.domain.horizontal.HorizontalService;
import com.gracenote.content.auth.domain.user.UserService;
import com.gracenote.content.auth.domain.vertical.VerticalService;
import com.gracenote.content.auth.dto.UserDTO;
import com.gracenote.content.auth.persistence.entity.Application;
import com.gracenote.content.auth.persistence.entity.ApplicationAuthority;
import com.gracenote.content.auth.persistence.entity.Authority;
import com.gracenote.content.auth.persistence.entity.HorizontalApplication;
import com.gracenote.content.auth.persistence.entity.Vertical;
import com.gracenote.content.auth.util.Constants;
import com.gracenote.content.auth.util.ExcelPO;
import com.gracenote.content.auth.util.S3ImageFileUploadAndDownload;
import com.gracenote.content.auth.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Application service class, perform business logic for application
 * and interacting with repository for db operations.
 *
 * @author deepak on 12/9/17.
 */
@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationAuthorityService applicationAuthorityService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    LoginAPI loginAPI;
    
    @Autowired
    VerticalService verticalService;
    
    @Autowired
    HorizontalService horizontalService;

    private final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    /***
     * Create new Application
     *
     * @param application
     * */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Application save(Application application) throws DataAccessException {

        validateApplicationData(application);
        validateApplicationAuthority(application.getApplicationAuthority());

        application.setCreatedTime(new Timestamp(System.currentTimeMillis()));
        try {
            application = applicationRepository.save(application);
        } catch (RuntimeException e) {
            log.error("Error occurred at the time of saving Application: " + e.getMessage());
            throw new DataIntegrityViolationException("Database exception");
        }

        // saving application authorities
        Set<ApplicationAuthority> applicationAuthoritySet = new HashSet<>();

        Iterator itr = application.getApplicationAuthority().iterator();
        while (itr.hasNext()) {
            ApplicationAuthority applicationAuthority = (ApplicationAuthority) itr.next();

            ApplicationAuthority applicationAuthority1 = new ApplicationAuthority();
            applicationAuthority1.setApplication_id(application.getApplicationId());
            applicationAuthority1.setRole_name(applicationAuthority.getRole_name());
            applicationAuthority1.setIsDefaultRole(applicationAuthority.getIsDefaultRole());
            applicationAuthority1.setCreatedTime(new Timestamp(System.currentTimeMillis()));

            Authority authority = new Authority();
            authority.setName(applicationAuthority.getRole_name());
            applicationAuthority1.setAuthority(authority);

            applicationAuthoritySet.add(applicationAuthority1);

            try {
                applicationAuthorityService.save(applicationAuthority1);
            } catch (RuntimeException e) {
                log.error("Error occurred at the time of saving Application Authority: " + e.getMessage());
                throw new DataIntegrityViolationException("Database exception");
            }
        }

        application.setApplicationAuthority(applicationAuthoritySet);

        return application;
    }

    /**
     * Get application by using application name
     *
     * @param applicationName
     * @return Application if application name is present in db
     */
    public Application getApplication(String applicationName) {
        if (!Util.isValidString(applicationName))
            throw new IllegalArgumentException("Application name must be required");

        try {
            return applicationRepository.findApplicationByApplicationName(applicationName);
        } catch (RuntimeException e) {
            log.error("Error occurred to get application {name}: " + applicationName + " : " + e.getMessage());
            throw new DataIntegrityViolationException("Database exception");
        }

    }
    
    /**
     * Get application by using client Id
     *
     * @param clientId
     * @return Application if client Id is present in db
     */
    public Application getApplicationByClientId(String clientId) {
        if (!Util.isValidString(clientId))
            throw new IllegalArgumentException("Client Id must be required");

        try {
            return applicationRepository.findApplicationByClientId(clientId);
        } catch (RuntimeException e) {
            log.error("Error occurred to get client {Id}: " + clientId + " : " + e.getMessage());
            throw new DataIntegrityViolationException("Database exception");
        }

    }

    /**
     * Get application by using application name
     *
     * @param applicationId
     * @return Application if application id is present in db
     */
    public Application getApplication(int applicationId) {
        if (applicationId <= 0)
            throw new IllegalArgumentException("Application id must be positive");

        try {
            return applicationRepository.findApplicationByApplicationId(applicationId);
        } catch (RuntimeException e) {
            log.error("Error occurred at get application {Id}: " + applicationId + " : " + e.getMessage());
            throw new DataIntegrityViolationException("Database exception");
        }
    }
    
    /**
     * Get application and client details by using application Id
     *
     * @param applicationId
     * @return List if application id is present in db
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public List getApplicationClientDetails(int applicationId) {
    	List clientApplicationDetails=new ArrayList<>();
        if (applicationId <= 0)
            throw new IllegalArgumentException("Application id must be positive");
        try {
            Application application = getApplication(applicationId);
            ClientDetails authenticatedClient = clientDetailsService.loadClientByClientId(application.getClientId());
            clientApplicationDetails.add(application);
            clientApplicationDetails.add(authenticatedClient);
            return clientApplicationDetails;
        } catch (RuntimeException e) {
            log.error("Error occurred at get application {Id}: " + applicationId + " : " + e.getMessage());
            throw new DataIntegrityViolationException("Database exception");
        }
    }


    /**
     * Get application by using application id
     *
     * @param applicationID
     * @return Application if application id is present in db
     */
    public ApplicationAuthority getDefaultAuthority(int applicationID) {
        if (applicationID <= 0)
            throw new IllegalArgumentException("application id must be positive number");

        try {
            return applicationRepository.findDefaultApplicationAutority(applicationID);
        } catch (RuntimeException e) {
            log.error("Error occurred at get application {Id}: " + applicationID + " : " + e.getMessage());
            throw new DataIntegrityViolationException("Database exception");
        }
    }

    /**
     * Create set of application Authority
     */
    public Set<ApplicationAuthority> createApplicationAuthoritySet(List<ApplicationAuthority> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("One application atuthority must be required");
        }

        Set<ApplicationAuthority> authoritySet = new HashSet<ApplicationAuthority>();
        authoritySet.addAll(list);
        return authoritySet;
    }

    /**
     * Validate application data
     *
     * @param application
     * @throws IllegalArgumentException if invalid application data provided.
     */
    public void validateApplicationData(Application application) {

        if (application == null)
            throw new IllegalArgumentException("Application can't be null");

        if (!Util.isValidString(application.getApplicationName()))
            throw new IllegalArgumentException("application name must be required");

        if (!Util.isValidString(application.getImageUrl()))
            throw new IllegalArgumentException("Application Image url must be required");
    }

    public void validateApplicationAuthority(Set<ApplicationAuthority> authoritySet) {
        if (authoritySet == null || authoritySet.isEmpty()) {
            throw new IllegalArgumentException("Application must have atleast one authority");
        } else {
            //check for only one default authority set
            List list =
                    authoritySet.stream()
                            .filter(s -> s.getIsDefaultRole() == true)
                            .collect(Collectors.toList());

            if (list.size() != 1) {
                throw new IllegalArgumentException("Only one authority has to be selected is default");
            }

            for (ApplicationAuthority a : authoritySet) {
                if (!Util.isValidString(a.getRole_name()))
                    throw new IllegalArgumentException("application authority name must be required");

            }
        }


    }

    /**
     * Get all application data
     *
     * @return list restricted and unrestricted applications from database
     */
    public Map<String, List<Application>> getAllApplications() {
        List<Application> applications = applicationRepository.findAllByIsDeletedFalseOrderByApplicationName();
        List<Application> userAccessApplication=null; 
        UserDTO userDTO = userService.getLoggedInUserDetail();
        if(userDTO.isExternalUser()){
        	userAccessApplication= getExternalUserApplications();
        }
        else {
        	userAccessApplication = getInternalUserApplications();
        }
		Map<String, List<Application>> applicationMap = new HashMap<>();
        List<Application> restrictedList = new ArrayList<>();
        for (Application application : applications) {
            if (!userAccessApplication.contains(application)) {
                	restrictedList.add(application);
            }
        }
		applicationMap.put("unrestricted", userAccessApplication);
		applicationMap.put("restricted", restrictedList);
		return applicationMap;

    }

    /**
     * Delete application
     *
     * @return true on successful delete
     */
    public boolean deleteApplication(int id) {

        boolean isDelete = false;

        //Delete application
        Application application = applicationRepository.findApplicationByApplicationId(id);
        if (application == null)
            throw new DataRetrievalFailureException("Application not Found");

        try {
            applicationRepository.deleteById(id);
            isDelete = true;
        } catch (DataAccessException e) {
            log.error("Error in deleting Application with Application Id" + id + " : " + e.getMessage());
            throw new DataIntegrityViolationException("Error in deleting application");
        }

        return isDelete;
    }

    /**
     * Get all applications with admin access to logged in user
     *
     * @return list applications with admin access.
     */
    List<Application> getAllAdminApplications() {

    	List<Application> applications = applicationRepository.findAllByIsDeletedFalseOrderByApplicationName();
    	
        Set<ApplicationAuthority> authoritySet = userService.getLoggedInUserAuthority();
        List<Integer> ids = new ArrayList<>();
        if (null != authoritySet || authoritySet.isEmpty()) {
            authoritySet.stream()
                    .filter(s-> s.getIsDeleted() == false)
                    .forEach(s-> ids.add(s.getApplicationAuthorityId()));

             List<Application> adminApplication=getAdminApplicationsByIds(ids);
             Iterator<Application> ltr=adminApplication.listIterator();
             while(ltr.hasNext()) {
            	 Application app=(Application) ltr.next();
            	 String appName=app.getApplicationName();
            	 if(appName.equalsIgnoreCase(Constants.DASHBOARD_CLIENT)) {
            		 String roleName=app.getApplicationAuthority().iterator().next().getRole_name();
            		 if(roleName.equalsIgnoreCase(Constants.ROLE_SSO_ADMIN)) {
            			 applications = applicationRepository.findAllByOrderByApplicationName();
            			 return applications;
            		 }
            	 }
             }
             return adminApplication;
        }

        return new ArrayList<Application>();
    }

    public List<Application> getAdminApplicationsByIds(List<Integer> ids) {
        if(ids!=null && !ids.isEmpty())
            return applicationRepository.getAdminUserApplication(ids);

        else
            return null;
    }


    /***
     * Update Application
     *
     * @param application
     * */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Application update(Application application,String verticalhorizontal) throws DataAccessException {
    	
    	
        validateApplicationData(application);
        validateApplicationAuthority(application.getApplicationAuthority());

        Application oldApplication = getApplication(application.getApplicationId());

        if (oldApplication == null) {
            throw new IllegalArgumentException("Incorrect data to update application");
        }

        // saving application authorities
        Set<ApplicationAuthority> oldApplicationAuthoritySet =
                applicationAuthorityService.getApplicationAuthority(application.getApplicationId());

        Set<ApplicationAuthority> newApplicationAuthority = application.getApplicationAuthority();

        Set<ApplicationAuthority> authoritySet = new HashSet<>();
        try {
            // Save the changes in application authority if any.
            authoritySet = saveAuthorityChange(oldApplicationAuthoritySet, newApplicationAuthority);

        } catch (DataAccessException e) {
            log.error("Error in deleting Application with Application Id : " + application.getApplicationId() + " : " + e.getMessage());
            throw new DataIntegrityViolationException("Error in updating application");
        }

        oldApplication.setDescription(application.getDescription());
        oldApplication.setImageUrl(application.getImageUrl());
        oldApplication.setIsRestricted(application.getIsRestricted());

        oldApplication.setApplicationAuthority(authoritySet);
        applicationRepository.save(oldApplication);
        verticalService.updateVerticalHorizontalApplication(verticalhorizontal, application.getApplicationId());
        return oldApplication;
    }

    Set<ApplicationAuthority> saveAuthorityChange(Set<ApplicationAuthority> oldApplicationAuthoritySet, Set<ApplicationAuthority> newApplicationAuthority) {

        Set<ApplicationAuthority> tempSet = new HashSet<>();

        int applicationId = 0;
        for (ApplicationAuthority oldAuthority : oldApplicationAuthoritySet) {
            String oldRoleName = oldAuthority.getRole_name();
            applicationId = oldAuthority.getApplication_id();
            int count = 0;

            for (ApplicationAuthority newAuthority : newApplicationAuthority) {
                String newRoleName = newAuthority.getRole_name();

                //for same Role_Name
                if (oldRoleName.equalsIgnoreCase(newRoleName)) {
                    count++;
                    tempSet.add(newAuthority); // add those authority which updating.

                    // if default role
                    if (newAuthority.getIsDefaultRole() != oldAuthority.getIsDefaultRole()) {
                        oldAuthority.setIsDefaultRole(newAuthority.getIsDefaultRole());
                        applicationAuthorityService.save(oldAuthority);
                    }
                }
            }

            // if old Role_Name not present in new authority set, then mark delete from table
            if (count == 0) {
                oldAuthority.setIsDeleted(true);
                applicationAuthorityService.save(oldAuthority);
            }
        }

        // Remove updated authorities, which is in tempSet.
        newApplicationAuthority.removeAll(tempSet);
        Set<ApplicationAuthority> newSet = new HashSet<>();
        if (!newApplicationAuthority.isEmpty()) {

            for (ApplicationAuthority newlyAdded : newApplicationAuthority) {
                newlyAdded.setApplication_id(applicationId);
                newlyAdded.setIsDefaultRole(newlyAdded.getIsDefaultRole());
                newlyAdded.setRole_name(newlyAdded.getRole_name());
                newlyAdded.setCreatedTime(new Timestamp(System.currentTimeMillis()));

                ApplicationAuthority authority = applicationAuthorityService.save(newlyAdded);
                newSet.add(authority);
            }
        }

        return newSet;
    }

    /**
     * Get all applications on which external user have allowed access.
     *
     * @return list applications for external user.
     */
    List<Application> getExternalUserApplications() {

        Set<ApplicationAuthority> authoritySet = userService.getLoggedInUserAuthority();
        List<Integer> ids = new ArrayList<>();
        if (null != authoritySet || authoritySet.isEmpty()) {
            authoritySet.stream()
                    .filter(s-> s.getIsDeleted() == false)
                    .forEach(s-> ids.add(s.getApplicationAuthorityId()));

            return applicationRepository.getExternalUserApplication(ids);
        }
        return null;
    }

    /**
     * Get all open applications & restricted application which user have allowed access.
     *
     * @return list applications for internal user.
     */
    List<Application> getInternalUserApplications() {

        Set<ApplicationAuthority> authoritySet = userService.getLoggedInUserAuthority();
        List<Integer> ids = new ArrayList<>();
        if (authoritySet != null || authoritySet.isEmpty()) {
            authoritySet.stream()
                    .filter(s-> s.getIsDeleted() == false)
                    .forEach(s-> ids.add(s.getApplicationAuthorityId()));

            return applicationRepository.getInternalUserApplication(ids);
        }
        return null;
    }

    /**
     * Add new application from user interface given
     * @return true if application added successful.
     * //@param appImg
     * @param applicationName
     * @param description
     * @param expiryTime
     * @param clientId
     * @param clientSecret
     * @param redirectUrl
     * @param isRestricted*/
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    void addApplication(/*MultipartFile appImg,*/ String applicationName, String description,MultipartFile file, int expiryTime,
                        String clientId, String clientSecret, String redirectUrl, boolean isRestricted,boolean isDeleted,
                        String engPoc,String baPoc,String supportPoc,String fullForm,
                        String appFamily,String applicationType,boolean gnActiveDirectory,String verticalhorizontal) throws Exception {

        //String imagePath = "/var/www/html/content-auth/assets/img";

        Application application = setApplication(/*appImg,*/ applicationName, description,file, expiryTime, clientId,
                clientSecret, redirectUrl, isRestricted, false,isDeleted,engPoc,baPoc,supportPoc,fullForm,
                appFamily,applicationType,gnActiveDirectory);
        Application createdApplication = save(application);
        if(null == createdApplication) {
            log.error("Failed application creation.");
            throw new DataIntegrityViolationException("Exception occured to save application.");
        }
        else {
        	verticalService.updateVerticalHorizontalApplication(verticalhorizontal,createdApplication.getApplicationId());
        	if(file!=null && !file.isEmpty()) {
				/*
				 * boolean isUpload =
				 * S3ImageFileUploadAndDownload.uploadImageFile(file);//Util.uploadImage(appImg,
				 * imagePath); if(!isUpload) { throw new Exception("Failed to upload an image");
				 * }
				 */
        	}
            log.info("Application created successfully");
            loginAPI.addClient(clientId, clientSecret, expiryTime, redirectUrl);
        }
    }

    private Application setApplication(String applicationName, String description,MultipartFile file, int expiryTime,
                                       String clientId, String clientSecret, String redirectUrl, boolean isRestricted, boolean expiryStatus, boolean isDeleted,
                                       String engPoc,String baPoc,String supportPoc,String fullForm,
                                       String appFamily,String applicationType,boolean gnActiveDirectory) {

        validateAppData(applicationName, description, expiryTime, clientId, clientSecret, redirectUrl,supportPoc,appFamily,applicationType);
        String imageName =Constants.DEFAULT_IMAGENAME; 
        String[] imageNames=null;
		  if(file!=null && !file.isEmpty()) {
			  imageName= file.getOriginalFilename();
			  imageNames=imageName.split(".png"); 
		  }
        Application application = new Application();
        application.setApplicationName(applicationName);
        application.setDescription(description);
        application.setExpiry(expiryStatus);
        application.setClientId(clientId);
        application.setIsRestricted(isRestricted);
        application.setImageUrl(imageNames!=null && imageName.length()>0 ?imageNames[0]:imageName);
        application.setIsDeleted(isDeleted);
        application.setEngPoc(engPoc);
        application.setBaPoc(baPoc);
        application.setSupportPoc(supportPoc);
        application.setApplicationFullForm(fullForm);
        application.setAppFamily(appFamily);
        application.setApplicationType(applicationType);
        application.setGnActiveDirectory(gnActiveDirectory);
        application.setCreatedTime(new Timestamp(System.currentTimeMillis()));

        String encoding = Base64.getEncoder().encodeToString((clientId+":"+clientSecret).getBytes()); //.getBytes(‌"UTF‌​-8"));
        application.setHeaders("Basic " + encoding);

        Set<ApplicationAuthority> set = getInitialsAuthorities();
        application.setApplicationAuthority(set);

        return application;
    }

    private Set<ApplicationAuthority> getInitialsAuthorities() {
        Set<ApplicationAuthority> set = new HashSet<>();
        ApplicationAuthority authority = new ApplicationAuthority();
        authority.setRole_name(Constants.ROLE_USER);
        authority.setIsDefaultRole(true);

        ApplicationAuthority authority1 = new ApplicationAuthority();
        authority1.setRole_name(Constants.ROLE_ADMIN);
        authority1.setIsDefaultRole(false);

        set.add(authority1);
        set.add(authority);

        return set;
    }

    void validateAppData(/*MultipartFile appImg,*/ String applicationName, String description, int expiryTime,
                                 String clientId, String clientSecret, String redirectUrl,String supportPoc,String appFamily,String applicationType) {
//        if(!appImg.getContentType().equalsIgnoreCase("image/png")) {
//            throw new IllegalArgumentException("Please provide png image");
//        }
        if(!Util.isValidString(applicationName)) {
            throw new IllegalArgumentException("Invalid application name");
        }
        if(!Util.isValidString(description)) {
            throw new IllegalArgumentException("Please provide description");
        }
        if(!Util.isValidString(clientId)) {
            throw new IllegalArgumentException("Invalid client ID");
        }
        if(!Util.isValidString(clientSecret)) {
            throw new IllegalArgumentException("Invalid client secret");
        }
        if(!Util.isValidString(redirectUrl)) {
            throw new IllegalArgumentException("Invalid redirect url");
        }
        if(expiryTime < 1800) {
            throw new IllegalArgumentException("Expiry time must be more that 1800 seconds");
        }
        if(!Util.isValidString(supportPoc)) {
            throw new IllegalArgumentException("Invalid support poc");
        }
        if(!Util.isValidString(appFamily)) {
            throw new IllegalArgumentException("Invalid tool family");
        }
        if(!Util.isValidString(applicationType)) {
            throw new IllegalArgumentException("Invalid hosting type");
        }

    }


    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    Application updateApplication(int applicationId, String description,MultipartFile file, int expiryTime,
                        boolean isDeleted, String redirectUrl, boolean isRestricted,String engPoc,
                        String baPoc,String supportPoc,String fullForm,String appFamily,
                        String applicationType,boolean gnActiveDirectory,String verticalhorizontal) throws Exception {

        log.info("Updating application: "+applicationId);
        Application oldApplication = getApplication(applicationId);
        
        String imageName ="";
        String[] imageNames=null;
		  if(file!=null && !file.isEmpty()) {
			  imageName= file.getOriginalFilename();
		   imageNames=imageName.split(".png"); 
		  }
        Application updatedApplication = null;
        if(null != oldApplication) {
            oldApplication.setDescription(description);
            oldApplication.setIsDeleted(isDeleted);
            oldApplication.setIsRestricted(isRestricted);
            oldApplication.setEngPoc(engPoc);
            oldApplication.setSupportPoc(supportPoc);
            oldApplication.setBaPoc(baPoc);
            oldApplication.setAppFamily(appFamily);
            oldApplication.setApplicationFullForm(fullForm);
            oldApplication.setApplicationType(applicationType);
            oldApplication.setGnActiveDirectory(gnActiveDirectory);
            if(!"".equalsIgnoreCase(imageName)) {
            	oldApplication.setImageUrl(imageNames!=null && !("").equalsIgnoreCase(imageName) && imageName.length()>0 ?imageNames[0]:imageName);
            }updatedApplication = update(oldApplication,verticalhorizontal);

            if(null == updatedApplication) {
                log.error("Failed update application.");
                throw new DataIntegrityViolationException("Exception occured to update application.");
            }
            else {
            	if(file!=null && !file.isEmpty()) {
					/*
					 * boolean isUpload =
					 * S3ImageFileUploadAndDownload.uploadImageFile(file);//Util.uploadImage(appImg,
					 * imagePath); if(!isUpload) { throw new Exception("Failed to upload an image");
					 * }
					 */
            	}
                log.info("Application updated successfully");
                loginAPI.updateClient(updatedApplication.getClientId(), expiryTime, redirectUrl);

                log.info("Client updated, now remove tokens of client : "+updatedApplication.getClientId());
                loginAPI.removeTokens(updatedApplication.getClientId());

                return updatedApplication;
            }
        }else {
            throw new IllegalArgumentException("Invalid application");
        }

    }
    
    /**
     * Get all application data
     *
     * @return list restricted and unrestricted applications from database
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public List<ExcelPO> getAllExcelApplicationsData() {
        List<Application> applications = applicationRepository.findAllByIsDeletedFalseOrderByApplicationName();
        List<ExcelPO> accessApplicationList=new ArrayList();
        ExcelPO excelpo=null;
    	for (Application application : applications) {
    		excelpo=new ExcelPO();
    		excelpo.setApplicationName(application.getApplicationName()!=null?application.getApplicationName():"");
    		excelpo.setDescription(application.getDescription()!=null?application.getDescription():"");
    		excelpo.setAppFamily(application.getAppFamily()!=null?application.getAppFamily():"");
    		excelpo.setApplicationType(application.getApplicationType()!=null?application.getApplicationType():"");
    		excelpo.setSupportPoc(application.getSupportPoc()!=null?application.getSupportPoc():"");
    		excelpo.setBaPoc(application.getBaPoc()!=null?application.getBaPoc():"");
    		excelpo.setEngPoc(application.getEngPoc()!=null?application.getEngPoc():"");
    		excelpo.setUserRoles(application.getUserRoles()!=null?application.getUserRoles():"");
    		excelpo.setGnActiveDirectory(application.isGnActiveDirectory());
    		excelpo.setIsRestricted(application.getIsRestricted());
    		excelpo.setApplicationFullForm(application.getApplicationFullForm()!=null?application.getApplicationFullForm():"");
    		List<HorizontalApplication> horizontalApplicationList=horizontalService.getHorizontalApplication(application.getApplicationId());
    		Set<String> horizontalName=new HashSet();
    		Set<String> verticalName=new HashSet();
    		for (HorizontalApplication horizontalApplication : horizontalApplicationList) {
    			horizontalName.add(horizontalApplication.getHorizontal().getHorizontalName());
    			List<Vertical> verticalList=verticalService.getVertical(horizontalApplication.getId());
    			if(!verticalList.isEmpty() && verticalList.get(0).getVerticalName()!=null)
    				verticalName.add(verticalList.get(0).getVerticalName());
			}
    		ClientDetails authenticatedClient = clientDetailsService.loadClientByClientId(application.getClientId());
    		excelpo.setRedirectUrl(authenticatedClient.getRegisteredRedirectUri().iterator().next());
    		excelpo.setHorizontalName(horizontalName.toString());
    		excelpo.setVerticalName(verticalName.toString());
    		accessApplicationList.add(excelpo);
		}
        return accessApplicationList;

    }

}