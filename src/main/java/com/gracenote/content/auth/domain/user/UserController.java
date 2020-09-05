package com.gracenote.content.auth.domain.user;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gracenote.content.auth.dto.UserDTO;
import com.gracenote.content.auth.persistence.entity.User;
import com.gracenote.content.auth.util.Util;
import com.sun.media.sound.InvalidDataException;

/**
 * Rest end point of User service of content auth.
 *
 * @author deepak on 14/8/17.
 */

@RestController
@RequestMapping("api/v1/users")
@EnableResourceServer
public class UserController {

    private final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserService userservice;
    
    /**
     * Get list of all User
     *
     * @return list of User.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity getAllUserDetails() {
        List<User> users = userservice.getAll();

        return new ResponseEntity(users, HttpStatus.OK);
    }

    /**
     * Get User Details from context
     *
     * @return UserDTO created if success.
     */
    @RequestMapping(value = "/myprofile", method = RequestMethod.GET)
    public UserDTO currentUserDetail() {
            UserDTO userdto = userservice.getLoggedInUserDetail();
            return userdto;
    }
    
    /**
     * Create new User rest end point
     *
     * @param user
     * @return User created if success.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(method = POST, consumes = APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity createUser(@RequestBody User user) {
        log.debug("Receive request to create the {0}", user);
        try {
            userservice.create(user);
            return new ResponseEntity(true, HttpStatus.CREATED);
        } catch (DataAccessException e) {
            log.debug("Error in user creation: "+e.getMessage());
            return Util.exceptionHandler("Error occurred in creation", "Exception Occurred "+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * Get user of id provided in path variable
     *
     * @param userId
     * @return User if id match.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
    @RequestMapping(value = "/{userId}", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getUser(@PathVariable int userId) {
        log.debug("Receive request to get User with id {}", userId);

        try {
            User user = userservice.getUser(userId);
            if (user == null) {
                return Util.exceptionHandler("Invalid User", "User not found", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity(user, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.error("Exception occurred at get User id {0}",userId+" : "+e.getMessage());
            return Util.exceptionHandler("Invalid User ID", "Exception Occurred "+e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    /**
     * Get user of name provided in path variable
     *
     * @param userName
     * @return user if userName match.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "name/{userName}", method = GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getUserByName(@PathVariable String userName) {
        log.debug("Receive request to get user with name {}", userName);

        try {
            User user = userservice.getUser(userName);
            if (user == null) {
                log.debug("User with name {0} not found", userName);
                return Util.exceptionHandler("Invalid User ID", "User not found", HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity(user, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.error("Error to get User with name {0}", userName+" : "+e.getMessage());
            return Util.exceptionHandler("Invalid User Name", "Exception Occurred "+e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }
    }


    /**
     * Delete User by id provided in path variable
     *
     * @param userId
     * @return true if Delete User success.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
    @RequestMapping(value = "/{userId}", method = DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity deleteUser(@PathVariable int userId) {
        log.debug("Receive request to delete user with id {}", userId);

        try {
            boolean isDelete = userservice.deleteUser(userId);
            if (!isDelete) {
                return Util.exceptionHandler("Invalid user Id","User not found", HttpStatus.NOT_ACCEPTABLE);
            }
            log.info("User deleted successful : "+userId);
            return new ResponseEntity(true, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.error("Error occurred in user deleting id {0} : "+userId+" : "+e.getMessage());
            return Util.exceptionHandler("Invalid user Id", "Exception Occurred "+e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }
    }

    /**
     * Get list of all Users to specific application
     *
     * @param applicationId
     * @return list of User.
     */
    @RequestMapping(value = "/application/{applicationId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity getAllApplicationUser(@PathVariable int applicationId) {
        log.debug("Receive request to get user list for application id {}", applicationId);
        try {
            List<User> users = userservice.getAllApplicationUser(applicationId);

            if(users == null)
                return new ResponseEntity(null, HttpStatus.NOT_FOUND);

            return new ResponseEntity(users, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.error("Error occurred in get user list for application id {0} : "+applicationId+" : "+e.getMessage());
            return Util.exceptionHandler("Invalid application Id", "Exception Occurred "+e.getMessage(), HttpStatus.NOT_ACCEPTABLE);
        }
    }


    /**
     * Get list of all Users to specific application
     *
     * @param applicationId
     * @return list of User.
     */
    @RequestMapping(value = "/application/{applicationId}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity updateUserRoleForApplication(@RequestBody User user, @PathVariable int applicationId) {
        log.info("Receive request to update user role for application id {}", applicationId);
        try {
            User updatedUser = userservice.updateUserRole(user, applicationId);
            if (updatedUser != null) {
                return new ResponseEntity(updatedUser, HttpStatus.OK);
            }

        } catch (DataAccessException e) {
            log.error("Authority update failure for application id: {} and user id: {}", applicationId, user.getUser_id());
            return Util.exceptionHandler("Invalid params","Err", HttpStatus.NOT_ACCEPTABLE);
        }

        return new ResponseEntity(null, HttpStatus.NOT_FOUND);
    }
    
    /**
     * AddOrUpdate User with role to specific application
     *
     * @param applicationName
     * @param username
     * @param userrole
     * @return boolean Flag.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/internal", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity addApplicationUser(@RequestParam String username,@RequestParam String userrole, @RequestParam String applicationName,@RequestParam String gracenoteId) {
        log.info("Receive request to update user role for application name {}", applicationName);
        User user=null;
        try {
        	 user=userservice.addApplicationUser(username.toLowerCase(),userrole,applicationName,gracenoteId);
        	 return new ResponseEntity(user, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Authority update failure for application name: {} and user name: {}", applicationName, username);
            return Util.exceptionHandler("Invalid params","Err", HttpStatus.NOT_ACCEPTABLE);
        }
    }
    
    /**
     * Delete User Mapping with role to specific application
     *
     * @param applicationName
     * @param username
     * @param userrole
     * @return boolean Flag.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/application/deleteuser", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity deleteUserForApplication(@RequestParam String username,@RequestParam String userrole, @RequestParam String applicationName) {
        log.info("Receive request to delete user role for application name {}", applicationName);
        try {
        	userservice.deleteUserForApplication(username,applicationName,userrole);
        	return new ResponseEntity(true, HttpStatus.OK);        	
        } catch (Exception e) {
            log.error("Authority delete failure for application name: {} and user name: {}", applicationName, username);
            return Util.exceptionHandler("Invalid params","Err", HttpStatus.NOT_ACCEPTABLE);
        }
    }

    /**
     * Create new External User rest end point
     *
     * @param externalUser
     * @return User created if success.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @RequestMapping(value = "/external", method = POST, consumes = APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity createExternalUser(@RequestBody ExternalUser externalUser, @RequestParam String username, @RequestParam String applicationName) {

        log.debug("Receive request to create the {0}", externalUser);
        try {
            User user=userservice.createExternal(username.toLowerCase(), externalUser, applicationName);
            return new ResponseEntity(user, HttpStatus.CREATED);
        } catch (DataAccessException e) {
            log.debug("Error in user creation: "+e.getMessage());
            return Util.exceptionHandler("Error occurred in creation", "Exception Occurred "+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (InvalidDataException e) {
            log.error("Add external user failure for application: {} and user name: {}", applicationName, username);
            return Util.exceptionHandler("Invalid params","Err", HttpStatus.NOT_ACCEPTABLE);
        }
    }
    
    
    /**
     * Add multiple user with user role to requested application
     *
     * @param userdatalist
     * @return boolean Flag.
     * @throws Exception 
     */
    @SuppressWarnings({ "rawtypes", "unchecked", "restriction" })
	@RequestMapping(value = "/internal/adduser", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity addUserForApplication(@RequestBody String userdatalist) throws Exception {
        log.info("Receive request to add new user {}"+userdatalist);
        Map<String,List<String>> responseObject=new HashMap();
        boolean responseFlag = false;
        List<String> successList=new ArrayList();
        List<String> failureList=new ArrayList();
        try {
        	if(userdatalist!=null && !("").equalsIgnoreCase(userdatalist)) {
            	List<Map<String, String>> userlist = new Gson().fromJson(
            			userdatalist, new TypeToken<List<HashMap<String, String>>>() {}.getType()
            		);
            	Iterator<Map<String,String>> iterator=(Iterator<Map<String, String>>) userlist.iterator();
            	while(iterator.hasNext()) {
            		Map<String,String> userdata=iterator.next();
            		String username=userdata.get("username");
            		String userrole=userdata.get("userrole");
            		if(username!=null && !("").equalsIgnoreCase(username) && userrole!=null && !("").equalsIgnoreCase(userrole)) {
            			responseFlag =userservice.addUserForApplication(username.toLowerCase(),userrole);
            			if(responseFlag) {
            				successList.add(username);
            			}else {
            				failureList.add(username);
            			}
            		}
            		responseObject.put("success",successList);
            		responseObject.put("failure", failureList);
            	}
        	}else {
        		throw new InvalidDataException("Invalid data params");
        	}
	        return new ResponseEntity(responseObject, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.debug("Error in add user: "+e.getMessage());
            return Util.exceptionHandler("Error occurred in adding users", "Exception Occurred "+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        }
    /**
     * Delete multiple User to requested application
     *
     * @param userdatalist
     * @return boolean Flag.
     * @throws Exception 
     */
    @SuppressWarnings({ "rawtypes", "unchecked", "restriction" })
	@RequestMapping(value = "/internal/deleteuser", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity deleteUserForApplication(@RequestBody String userdatalist) throws Exception {
        log.info("Receive request to delete user - "+userdatalist);
        boolean responseFlag=false;
        Map<String,List<String>> responseObject=new HashMap();
        List<String> successList=new ArrayList();
        List<String> failureList=new ArrayList();
        try {
        	if(userdatalist!=null && !("").equalsIgnoreCase(userdatalist)) {
        		Map<String,List<String>> userlist = new Gson().fromJson(
        				userdatalist, new TypeToken<HashMap<String,List<String>>>() {}.getType()
            		);
        		List<String> usernames=userlist.get("username");
        		for (String username : usernames) {
        			if(username!=null && !("").equalsIgnoreCase(username)) {
        				responseFlag=userservice.deleteUserForApplication(username.toLowerCase());
        				if(responseFlag) {
        					successList.add(username);
        				}
        				else {
        					failureList.add(username);
        				}
        			}
            	}
        		responseObject.put("success", successList);
        		responseObject.put("failure",failureList);
        	}else {
        		throw new InvalidDataException("Invalid data params");
        	}
	        return new ResponseEntity(responseObject, HttpStatus.OK);
        } catch (DataAccessException e) {
            log.debug("Error in delete user: "+e.getMessage());
            return Util.exceptionHandler("Error occurred in deleting users", "Exception Occurred "+e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        }
    }