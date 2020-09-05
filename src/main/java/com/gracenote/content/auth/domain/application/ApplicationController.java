package com.gracenote.content.auth.domain.application;


import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.gracenote.content.auth.persistence.entity.Application;
import com.gracenote.content.auth.util.ExcelPO;

/**
 * Rest end point of Application service of content auth.
 */

@RestController
@EnableResourceServer
@RequestMapping(value = "api/v1/applications")
public class ApplicationController {

    @Autowired
    ApplicationService applicationservice;
    
    private final Logger log = LoggerFactory.getLogger(ApplicationController.class);

    /**
     * Get list of all application
     *
     * @return list of application.
     */
    @RequestMapping(value = "", method = GET)
    public ResponseEntity getApplications() {
        try {
            Map<String, List<Application>> applications = applicationservice.getAllApplications();
            //List<Application> applications = applicationservice.getAllApplications();

            if (applications == null || applications.isEmpty())
                return exceptionHandller("{\"Response\":\"No content\"}", HttpStatus.NO_CONTENT);

            return new ResponseEntity(applications, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.error("Exception occurred at get all applications: " + e.getMessage());
            return exceptionHandller("{\"Response\":\"No application found\"}", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Create new Application rest end point
     *
     * @param application
     * @return application created if success.
     */
    @RequestMapping(method = POST, consumes = APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity createApplication(@RequestBody final Application application) {
        log.debug("Receive request to create the {0}", application);

        try {
            Application createdApplication = applicationservice.save(application);

            if (createdApplication == null)
                return new ResponseEntity("{\"Response\":\"Invalid data input\"}", HttpStatus.NOT_ACCEPTABLE);

            return new ResponseEntity(createdApplication, HttpStatus.CREATED);
        } catch (DataAccessException e) {
            log.debug("Error in application creation: " + e.getMessage());
            return exceptionHandller("{\"Response\":\"Error occurred in creation\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * Get application of id provided in path variable
     *
     * @param applicationId
     * @return application if id match.
     */
    @ResponseBody
    @RequestMapping(value = "/{applicationId}", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getApplication(@PathVariable final int applicationId) {
        log.debug("Receive request to get application with id {}", applicationId);

        try {
            Application application = applicationservice.getApplication(applicationId);

            if (application == null)
                return exceptionHandller("{\"Response\":\"Application not found\"}", HttpStatus.NOT_FOUND);

            return new ResponseEntity(application, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.error("Exception occurred at get application id {0}", applicationId + " : " + e.getMessage());
            return exceptionHandller("{\"Response\":\"Invalid application ID\"}", HttpStatus.NOT_ACCEPTABLE);
        }
    }

    
    /**
     * Get application and client details of id provided in path variable
     *
     * @param applicationId
     * @return List if id match.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
    @RequestMapping(value = "/client/{applicationId}", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getApplicationWithClientDetails(@PathVariable final int applicationId) {
        log.debug("Receive request to get application with id {}", applicationId);
        try {
        	List applicationClientDetails=applicationservice.getApplicationClientDetails(applicationId);
            if (applicationClientDetails == null || applicationClientDetails.isEmpty())
                return exceptionHandller("{\"Response\":\"Application not found\"}", HttpStatus.NOT_FOUND);

            return new ResponseEntity(applicationClientDetails, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.error("Exception occurred at get application id {0}", applicationId + " : " + e.getMessage());
            return exceptionHandller("{\"Response\":\"Invalid application ID\"}", HttpStatus.NOT_ACCEPTABLE);
        }
    }
    
    
    /**
     * Get application of name provided in path variable
     *
     * @param applicationName
     * @return application if id match.
     */
    @RequestMapping(value = "name/{applicationName}", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getApplicationByName(@PathVariable final String applicationName) {
        log.debug("Receive request to get application with name {}", applicationName);

        try {
            Application application = applicationservice.getApplication(applicationName);

            if (application == null) {
                log.debug("Application with name {0} not found", applicationName);
                return exceptionHandller("{\"Response\":\"Application not found\"}", HttpStatus.NOT_FOUND);
            }

            return new ResponseEntity(application, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.error("Error to get Application with name {0}", applicationName + " : " + e.getMessage());
            return exceptionHandller("{\"Response\":\"Invalid application NAME\"}", HttpStatus.NOT_ACCEPTABLE);
        }
    }


    /**
     * Delete application by id provided in path variable
     *
     * @param applicationId
     * @return true if Delete Application success.
     */
    @ResponseBody
    @RequestMapping(value = "/{applicationId}", method = DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteApplication(@PathVariable final int applicationId) {
        log.debug("Receive request to delete applicaiton with id {}", applicationId);

        try {
            boolean isDelete = applicationservice.deleteApplication(applicationId);

            if (!isDelete)
                return exceptionHandller("{\"Response\":\"Application not found\"}", HttpStatus.NOT_FOUND);

            log.info("Application deleted successful : " + applicationId);
            return new ResponseEntity(true, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.error("Error occurred in application deleting id {0} : " + applicationId + " : " + e.getMessage());
            return exceptionHandller("{\"Response\":\"Invalid application Id\"}", HttpStatus.NOT_ACCEPTABLE);
        }
    }

    @ExceptionHandler(value = DataAccessException.class)
    public ResponseEntity exceptionHandller(String errMsg, HttpStatus status) {
        return new ResponseEntity(errMsg, status);
    }

    @RequestMapping(value = "/admin", method = GET)
    public ResponseEntity getAdminApplications() {
        try {
            List<Application> applications = applicationservice.getAllAdminApplications();

            if (applications == null || applications.isEmpty())
                return exceptionHandller("{\"Response\":\"You don't have admin access to any application\"}", HttpStatus.NO_CONTENT);

            return new ResponseEntity(applications, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.error("Exception occurred at get all admin applications: " + e.getMessage());
            return exceptionHandller("{\"Response\":\"No admin application found\"}", HttpStatus.NOT_FOUND);
        }
    }


    /**
     * Update application by id provided in path variable
     *
     * @param application
     * @return true if Update Application success.
     */
    @ResponseBody
    @RequestMapping(value = "/update", method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateApplication(@RequestBody final Application application) {
        log.debug("Receive request to update application ");

        try {
            Application updatedApplication = applicationservice.update(application,"");

            if (updatedApplication != null) {
                log.info("Application update successful : " + application.getApplicationId());
                return new ResponseEntity(updatedApplication, HttpStatus.OK);
            }

            log.error("Application update fails ");
            return exceptionHandller("{\"Response\":\"Application update fails\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (DataAccessException e) {
            log.error("Error occurred in updating application: " + e.getMessage());
            return exceptionHandller("{\"Response\":\"Invalid application Id\"}", HttpStatus.NOT_ACCEPTABLE);
        }
    }


    @ResponseBody
    @RequestMapping(value = "/add", method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity addApplication(/*@RequestParam("appImg") MultipartFile appImg
            ,*/ @RequestParam String applicationName, @RequestParam String description,@RequestParam(value="file",required=false) MultipartFile imageFilePath, @RequestParam int expiryTime,
                               @RequestParam String clientId, @RequestParam String clientSecret,
                               @RequestParam String redirectUrl, @RequestParam boolean isRestricted,boolean isDeleted,
                               @RequestParam String engPoc,@RequestParam String baPoc,@RequestParam String supportPoc,@RequestParam String fullForm,
                               @RequestParam String appFamily,@RequestParam String applicationType,@RequestParam boolean gnActiveDirectory,@RequestParam("verticalhorizontal") String verticalhorizontal ) {
        log.debug("Receive request to update application ");

        try{
            applicationservice.addApplication(/*appImg,*/ applicationName, description,imageFilePath,
                    expiryTime, clientId, clientSecret, redirectUrl, isRestricted,isDeleted,engPoc,baPoc,supportPoc,fullForm,
                    appFamily,applicationType,gnActiveDirectory,verticalhorizontal);
            
            return new ResponseEntity(true, HttpStatus.OK);
        }catch (DataAccessException e) {
            log.error("Error occurred in add application: " + e.getMessage());
            return exceptionHandller("{\"Response\":\"Invalid application data\"}", HttpStatus.NOT_ACCEPTABLE);
        } catch (Exception e) {
            log.error("Error occurred in add application: " + e.getMessage());
            return exceptionHandller("{\"Response\":"+e.getMessage()+"}", HttpStatus.NOT_ACCEPTABLE);
        }

    }


    @ResponseBody
    @RequestMapping(value = "/update/{applicationId}", method = POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity updateApplication(@PathVariable final int applicationId, @RequestParam String description,@RequestParam(value="file",required=false) MultipartFile file, @RequestParam int expiryTime,
                                         @RequestParam boolean isDeleted, @RequestParam String redirectUrl, @RequestParam boolean isRestricted,
                                         @RequestParam String engPoc,@RequestParam String baPoc,@RequestParam String supportPoc,@RequestParam String fullForm,
                                         @RequestParam String appFamily,@RequestParam String applicationType,@RequestParam boolean gnActiveDirectory,@RequestParam("verticalhorizontal") String verticalhorizontal) {
        log.debug("Receive request to update application ");

        try{
            Application updatedApplication = applicationservice.updateApplication(applicationId, description,file,
                    expiryTime, isDeleted, redirectUrl, isRestricted,engPoc,baPoc,supportPoc,fullForm,appFamily,applicationType,gnActiveDirectory,verticalhorizontal);

            return new ResponseEntity(updatedApplication, HttpStatus.OK);
        }catch (DataAccessException e) {
            log.error("Error occurred in add application: " + e.getMessage());
            return exceptionHandller("{\"Response\":\"Invalid application data\"}", HttpStatus.NOT_ACCEPTABLE);
        } catch (Exception e) {
            log.error("Error occurred in add application: " + e.getMessage());
            return exceptionHandller("{\"Response\":"+e.getMessage()+"}", HttpStatus.NOT_ACCEPTABLE);
        }

    }
    
    /**
     * Get list of all application
     *
     * @return list of application.
     */
    @RequestMapping(value = "/excel", method = GET)
    public ResponseEntity getAllExcelApplicationsData() {
        try {
        	List<ExcelPO> applications = applicationservice.getAllExcelApplicationsData();
          
            if (applications == null || applications.isEmpty())
                return exceptionHandller("{\"Response\":\"No content\"}", HttpStatus.NO_CONTENT);

            return new ResponseEntity(applications, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.error("Exception occurred at get all applications: " + e.getMessage());
            return exceptionHandller("{\"Response\":\"No application found\"}", HttpStatus.NOT_FOUND);
        }
    }
    
    
}