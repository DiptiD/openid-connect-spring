package com.gracenote.content.auth.domain.user;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;

import com.gracenote.content.auth.domain.application.ApplicationService;
import com.gracenote.content.auth.dto.UserDTO;
import com.gracenote.content.auth.exception.ResourceCreationException;
import com.gracenote.content.auth.persistence.entity.Application;
import com.gracenote.content.auth.persistence.entity.ApplicationAuthority;
import com.gracenote.content.auth.persistence.entity.User;
import com.gracenote.content.auth.util.Constants;
import com.gracenote.content.auth.util.Util;
import com.gracenote.content.auth.util.Validation;
import com.sun.media.sound.InvalidDataException;

/**
 * User service class, perform business logic for user and interacting with
 * repository for db operation
 *
 * @author deepak on 12/9/17.
 */

@Service
public class UserService {

	/**
	 * repository interacting with db
	 */
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ApplicationService applicationService;

	@Autowired
	UserDetailsService userDetailsService;

	private final Logger log = LoggerFactory.getLogger(UserService.class);

	/**
	 * Create new user
	 *
	 * @param user
	 */
	public void create(User user) {
		validateUserData(user);

		User user1 = null;
		user1 = userRepository.save(user);

		if (user1 == null)
			throw new ResourceCreationException("Unable to create user");
	}

	/**
	 * Create new external user
	 *
	 * @param externalUser
	 * @param applicationName
	 */
	public User createExternal(String email, ExternalUser externalUser, String applicationName)
			throws InvalidDataException {
		User user = null;
		if (!checkIsAdminAuthority(applicationName)) {
			throw new AccessDeniedException("Dont have admin authority");
		}

		if (Util.isValidString(email) && Util.isValidString(applicationName)) {
			/** Existing user */
			User existingUser = userRepository.findByEmail(email);
			if (null == existingUser) {
				throw new InvalidDataException("email id not found! incorrect email id");
			}

			if (existingUser.getIsDeleted()) {
				throw new InvalidDataException("User is not valid");
			}

			Application application = applicationService.getApplication(applicationName);
			if (null == application) {
				throw new InvalidDataException("Invalid application name");
			}

			boolean isAuthority = false;
			ApplicationAuthority applicationAuthority = applicationService
					.getDefaultAuthority(application.getApplicationId());

			if (null == applicationAuthority) {
				throw new InvalidDataException("Default application authority is not valid");
			} else {
				isAuthority = existingUser.getApplicationAuthorities().add(applicationAuthority);
			}

			if (isAuthority) {
				userRepository.save(existingUser);
				return existingUser;
			} else {
				throw new InvalidDataException("Authority is not added.");
			}

		} else if (null != externalUser && Util.isValidString(applicationName)) {
			/** New USER */
			validateExternalUserData(externalUser);

			if (null != userRepository.findByEmail(externalUser.getEmail())) {
				throw new InvalidDataException("User already register!");
			}

			user = setExternalUser(externalUser, applicationName);
			userRepository.save(user);
		} else {
			throw new InvalidDataException("Provide valid user data");
		}
		return user;
	}

	/**
	 * Get list of all users.
	 *
	 * @return list of Users
	 */
	public List<User> getAll() {
		return userRepository.findAllByIsDeletedFalse();
	}

	/**
	 * Get user by username
	 *
	 * @param username
	 * @return User if user with username is present in db
	 */
	public User getUser(String username) {
		if (!Util.isValidString(username))
			throw new IllegalArgumentException("username must be required");

		return userRepository.findByIsDeletedFalseAndUsernameIgnoreCase(username);
	}

	/**
	 * Get user by user id
	 *
	 * @param id
	 * @return User if user with id is present in db
	 */
	public User getUser(int id) {

		if (id <= 0)
			throw new IllegalArgumentException("id must be positive number");

		return userRepository.findByUserId(id);
	}

	/**
	 * Get user by email
	 *
	 * @param email
	 * @return User if user with email is present in db
	 */
	public User getUserByEmail(String email) {
		if (!Util.isValidString(email))
			throw new IllegalArgumentException("email must be required");

		return userRepository.findByEmail(email);
	}

	/**
	 * Set the internal user with username and authority and default values.
	 *
	 * @param username,
	 * @param authoritySet
	 * @return user if proper internal user details.
	 * @throws IllegalArgumentException if invalid username or authoritySet
	 *                                  provided.
	 */
	public User setOpenIdInternalUser(String email, Set<ApplicationAuthority> authoritySet, String userDetails,
			String accessToken) {

		if (!Util.isValidString(email))
			throw new IllegalArgumentException("email must be required");

		if (authoritySet == null)
			throw new IllegalArgumentException("Application authority must be required");

		User user = new User();

		Map<String, String> result = userDetailsService.getUserInfo(email, accessToken, userDetails);
		user.setFirstName(result.get("given_name"));
		user.setLastName(result.get("family_name"));
		user.setEmail(email);
		user.setCreatedTime(new Timestamp(System.currentTimeMillis()));
		user.setActivated(true);
		user.setIsAdmin(false);
		user.setIsDeleted(false);
		user.setIsExternalUser(false);
		user.setResetPasswordKey(null);
		user.setUsername(email);

		user.setApplicationAuthorities(authoritySet);
		user.setPassword(Constants.NS_INTERNAL_USER_PASS);
		return user;
	}

	/**
	 * Set the internal user with username and authority and default values.
	 *
	 * @param username,
	 * @param authoritySet
	 * @return user if proper internal user details.
	 * @throws IllegalArgumentException if invalid username or authoritySet
	 *                                  provided.
	 */
	public User setInternalUser(String username, Set<ApplicationAuthority> authoritySet, String org) {

		if (!Util.isValidString(username))
			throw new IllegalArgumentException("username must be required");

		if (authoritySet == null)
			throw new IllegalArgumentException("Application authority must be required");

		User user = new User();
		user = setEmailFnameLname(user);
		user.setCreatedTime(new Timestamp(System.currentTimeMillis()));
		user.setActivated(true);
		/*
		 * user.setEmail((String) userDetailsMap.get("mail"));
		 * user.setFirstName((String) userDetailsMap.get("givenName"));
		 * user.setLastName((String) userDetailsMap.get("sn"));
		 */
		user.setIsAdmin(false);
		user.setIsDeleted(false);
		user.setIsExternalUser(false);
		user.setResetPasswordKey(null);
		user.setUsername(username);

		user.setApplicationAuthorities(authoritySet);

		// map initialised if authenticate with Nielsen credentials.
		if (null != org && org.equalsIgnoreCase("nielsen")) {
			user.setPassword(Constants.NS_INTERNAL_USER_PASS);
		} else {
			user.setPassword(Constants.INTERNAL_USER_PASS);
		}

		return user;
	}

	public User setExternalUser(ExternalUser externalUser, String applicationName) {

		if (!Util.isValidString(applicationName))
			throw new IllegalArgumentException("application name must be required");

		Application application = applicationService.getApplication(applicationName);
		if (null == application) {
			throw new IllegalArgumentException("Application not found!");
		}

		Application dashboardApplication = applicationService.getApplication(Constants.DASHBOARD_CLIENT);

		ApplicationAuthority dashboardAuthority = applicationService
				.getDefaultAuthority(dashboardApplication.getApplicationId());
		ApplicationAuthority applicationAuthority = applicationService
				.getDefaultAuthority(application.getApplicationId());

		if (null == dashboardAuthority || null == applicationAuthority) {
			throw new IllegalArgumentException("Invalid default application authority");
		}

		Set<ApplicationAuthority> authoritySet = new HashSet<>();
		authoritySet.add(dashboardAuthority);
		authoritySet.add(applicationAuthority);

		User user = new User();
		user.setCreatedTime(new Timestamp(System.currentTimeMillis()));
		user.setActivated(true);
		user.setEmail(externalUser.getEmail());
		user.setFirstName(externalUser.getFirstName());
		user.setLastName(externalUser.getLastName());
		user.setIsAdmin(false);
		user.setIsDeleted(false);
		user.setIsExternalUser(true);
		user.setResetPasswordKey(null);
		user.setUsername(externalUser.getUsername());
		user.setPassword(BCrypt.hashpw(externalUser.getPassword(), BCrypt.gensalt(4)));
		user.setApplicationAuthorities(authoritySet);

		return user;
	}

	/**
	 * Validate user data
	 *
	 * @param user
	 * @throws IllegalArgumentException if invalid user data provided.
	 */
	public void validateUserData(User user) {

		if (user == null)
			throw new IllegalArgumentException("User can't be null");

		if (!Util.isValidString(user.getUsername()))
			throw new IllegalArgumentException("username must be required");

		if (user.getApplicationAuthorities() == null)
			throw new IllegalArgumentException("Application authority must be required");

	}

	/**
	 * Validate external user data
	 *
	 * @param externalUser
	 * @throws IllegalArgumentException if invalid user data provided.
	 */
	public void validateExternalUserData(ExternalUser externalUser) {

		if (!Util.isValidString(externalUser.getEmail()))
			throw new IllegalArgumentException("Email must not be null");

		if (null != externalUser.getEmail() && !Validation.validate(externalUser.getEmail(), Validation.EMAIL_REGEX))
			throw new IllegalArgumentException("Email is not valid!");

		if (!Util.isValidString(externalUser.getPassword()))
			throw new IllegalArgumentException("User password must be required");

		if (externalUser.getPassword().length() < 6) {
			throw new IllegalArgumentException("User password required at least 6 characters");
		}

		if (!externalUser.getConfirmPassword().equals(externalUser.getPassword())) {
			throw new IllegalArgumentException("Password and confirm password must be same");
		}

		if (!Validation.validate(externalUser.getFirstName(), Validation.onlyAlphaRegex))
			throw new IllegalArgumentException("First name is not valid");

		if (!Validation.validate(externalUser.getLastName(), Validation.onlyAlphaRegex))
			throw new IllegalArgumentException("Last name is not valid");

	}

	/**
	 * Get User Object by username
	 *
	 * @return User if user with username is present in db
	 */
	public UserDTO getLoggedInUserDetail() {

		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {

			UserDTO userdto = new UserDTO();
			User user = userRepository.findByIsDeletedFalseAndUsernameIgnoreCase(authentication.getName());
			if (user != null) {
				userdto.setUserId(user.getUser_id());
				userdto.setEmailAddress(user.getEmail());
				userdto.setUserName(user.getUsername());
				userdto.setDate(user.getCreatedTime());
				userdto.setExternalUser(user.isExternalUser());
				userdto.setFirstName(user.getFirstName());
				userdto.setLastName(user.getLastName());
				userdto.setGracenoteId(user.getGracenoteId());
				return userdto;
			}
		}
		return null;
	}

	/**
	 * Delete user
	 *
	 * @return true on successful delete
	 */
	public boolean deleteUser(int id) {

		// Delete User
		User user = userRepository.findByUserId(id);
		if (user == null)
			throw new DataRetrievalFailureException("User not Found");

		try {
			// userRepository.delete(id);
			user.setIsDeleted(true);
			userRepository.save(user);

		} catch (DataAccessException e) {
			log.error("Error in deleting User with User Id" + id + " : " + e.getMessage());
			throw new DataIntegrityViolationException("Error in deleting user");
		}

		return true;
	}

	/**
	 * Get ApplicationAuthority of loggedin users
	 *
	 * @return set of ApplicationAuthority
	 */
	public Set<ApplicationAuthority> getLoggedInUserAuthority() {

		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (!(authentication instanceof AnonymousAuthenticationToken)) {
			User user = userRepository.findByIsDeletedFalseAndUsernameIgnoreCase(authentication.getName());

			if (user != null) {
				if (user.getApplicationAuthorities() != null || user.getApplicationAuthorities().isEmpty()) {
					return user.getApplicationAuthorities();
				}
			}
		}
		return null;
	}

	/**
	 * Get list of all users.
	 *
	 * @return list of Users
	 */
	public List<User> getAllApplicationUser(int applicationId) {

		Application application = applicationService.getApplication(applicationId);

		if (application == null)
			throw new DataRetrievalFailureException("Invalid application id");

		Set<ApplicationAuthority> authoritySet = application.getApplicationAuthority();

		List<Integer> authorityIds = new ArrayList<>();

		authoritySet.stream().filter(s -> s.getIsDeleted() == false)
				.forEach(s -> authorityIds.add(s.getApplicationAuthorityId()));

		if (authorityIds == null || authorityIds.isEmpty())
			return null;

		return userRepository.findByApplicationAuthoritiesApplicationAuthorityIdIn(authorityIds);
	}

	/**
	 * Get user by user id
	 *
	 * @param user, applicationId
	 * @return User if user with id is present in db
	 */
	public User updateUserRole(User user, int applicationId) {

		if (applicationId <= 0)
			throw new IllegalArgumentException("id must be positive number");

		User oldUser = userRepository.findByUserId(user.getUser_id());

		if (oldUser == null)
			throw new IllegalArgumentException("Invalid user");

		Set<ApplicationAuthority> oldAuthorities = oldUser.getApplicationAuthorities();
		Optional<ApplicationAuthority> matchObj = oldAuthorities.stream()
				.filter(o -> o.getApplication_id() == applicationId).findFirst();

		ApplicationAuthority oldAuthority = matchObj.get();

		Set<ApplicationAuthority> newAuthorities = user.getApplicationAuthorities();
		Optional<ApplicationAuthority> matchObj2 = newAuthorities.stream()
				.filter(n -> n.getApplication_id() == applicationId).findFirst();

		ApplicationAuthority newAuthority = matchObj2.get();

		if (oldAuthority.getRole_name().equalsIgnoreCase(newAuthority.getRole_name())) {
			return oldUser;
		} else {
			oldAuthorities.remove(oldAuthority);
			oldAuthorities.add(newAuthority);

			oldUser.setApplicationAuthorities(oldAuthorities);
			userRepository.save(oldUser);
		}

		return oldUser;
	}

	/**
	 * Set the existing internal user's firstname lastname and email.
	 * 
	 * @param user,
	 * @return user if proper internal user details.
	 * @throws IllegalArgumentException if invalid username or authoritySet
	 *                                  provided.
	 */
	public User setEmailFnameLname(User user) {

		if (null == user)
			throw new IllegalArgumentException("User must not be null");

		Map userDetailsMap = (Map) userDetailsService.getUserDetailsMap().get(user.getUsername().toLowerCase());
		// map initialised if authenticate with Nielsen credentials.
		if (null != userDetailsMap && !userDetailsMap.isEmpty()) {
			if (user.getEmail() == null || user.getEmail().equalsIgnoreCase(""))
				user.setEmail((String) userDetailsMap.get("mail"));
			if (user.getFirstName() == null || user.getFirstName().equalsIgnoreCase(""))
				user.setFirstName((String) userDetailsMap.get("givenName"));
			if (user.getLastName() == null || user.getLastName().equalsIgnoreCase(""))
				user.setLastName((String) userDetailsMap.get("sn"));
		}
		return user;
	}

	/**
	 * Update User role to specific application
	 *
	 * @param applicationName
	 * @param user
	 * @param userrole
	 * @return User.
	 */

	public User updateUserRoleForApplication(User user, String applicationName, String userrole, String gracenoteId) {
		Application application = applicationService.getApplication(applicationName);
		Iterator<ApplicationAuthority> itr = application.getApplicationAuthority().iterator();
		while (itr.hasNext()) {
			ApplicationAuthority applicationAuthority = (ApplicationAuthority) itr.next();
			if (applicationAuthority.getRole_name().equalsIgnoreCase(userrole)) {
				Set<ApplicationAuthority> tempSet = new HashSet(user.getApplicationAuthorities());
				Iterator<ApplicationAuthority> iterator = tempSet.iterator();
				while (iterator.hasNext()) {
					ApplicationAuthority oldapplicationAuthority = (ApplicationAuthority) iterator.next();
					if (oldapplicationAuthority.getApplication_id() == application.getApplicationId()) {
						user.getApplicationAuthorities().remove(oldapplicationAuthority);
						user.getApplicationAuthorities().add(applicationAuthority);
					} else {
						user.getApplicationAuthorities().add(applicationAuthority);
					}
				}

			}
		}
		if (gracenoteId != null && !("").equalsIgnoreCase(gracenoteId)
				&& Validation.validate(gracenoteId, Validation.alphaNumericRegex)) {
			user.setGracenoteId(gracenoteId);
		}
		userRepository.save(user);
		return user;
	}

	/**
	 * AddOrUpdate User with role to specific application
	 *
	 * @param applicationName
	 * @param username
	 * @param userrole
	 * @return boolean Flag.
	 * @throws Exception if invalid requested parameters.
	 */

	public User addApplicationUser(String username, String userrole, String applicationName, String gracenoteId)
			throws Exception {
		User user = null;
		if (Validation.validate(username, Validation.NIELSEN_EMAIL_REGEX) && Validation.validateRole(userrole)
				&& Validation.validate(applicationName, Validation.onlyAlphaWithSpaceAllowed)
				&& checkIsAdminAuthority(applicationName)) {
			user = getUser(username);
			if (user == null) {
				user = createInternalUser(username, Constants.DASHBOARD_CLIENT);
			}
			user = updateUserRoleForApplication(user, applicationName, userrole, gracenoteId);
		} else {
			throw new InvalidDataException("Invalid Requested Parameters");
		}
		return user;
	}

	/**
	 * Delete User with role to specific application
	 *
	 * @param applicationName
	 * @param username
	 * @param userrole
	 * @return void.
	 * @throws Exception if invalid requested parameters.
	 */
	public void deleteUserForApplication(String username, String applicationName, String userrole) throws Exception {
		if (Validation.validateRole(userrole)
				&& Validation.validate(applicationName, Validation.onlyAlphaWithSpaceAllowed)
				&& checkIsAdminAuthority(applicationName)) {
			User user = getUser(username);
			Application application = applicationService.getApplication(applicationName);
			Iterator<ApplicationAuthority> itr = application.getApplicationAuthority().iterator();
			while (itr.hasNext()) {
				ApplicationAuthority applicationAuthority = (ApplicationAuthority) itr.next();
				if (applicationAuthority.getRole_name().equalsIgnoreCase(userrole)) {
					user.getApplicationAuthorities().remove(applicationAuthority);
				}
			}
			userRepository.save(user);
		} else {
			throw new InvalidDataException("Invalid Requested Parameters");
		}

	}

	/**
	 * Create internal user details.
	 * 
	 * @param username,
	 * @param applicationName
	 * @return user if proper internal user details saved.
	 */
	public User createInternalUser(String username, String applicationName) {
		User user = new User();
		Set<ApplicationAuthority> applicationAuthorities = new HashSet<ApplicationAuthority>();
		user.setCreatedTime(new Timestamp(System.currentTimeMillis()));
		user.setUsername(username);
		user.setEmail(username);
		user.setActivated(true);
		user.setIsAdmin(false);
		user.setIsDeleted(false);
		user.setIsExternalUser(false);
		user.setPassword(Constants.INTERNAL_USER_PASS);

		Application application = applicationService.getApplication(applicationName);
		Iterator<ApplicationAuthority> itr = application.getApplicationAuthority().iterator();
		while (itr.hasNext()) {
			ApplicationAuthority applicationAuthority = (ApplicationAuthority) itr.next();
			if (applicationAuthority.getRole_name().equalsIgnoreCase(Constants.DEFAULT_AUTHORITY)) {
				applicationAuthorities.add(applicationAuthority);
				user.setApplicationAuthorities(applicationAuthorities);
				// user.getApplicationAuthorities().add(applicationAuthority);
			}
		}
		create(user);
		return user;
	}

	public boolean checkIsAdminAuthority(String applicationName) {
		Set<ApplicationAuthority> authoritySet = getLoggedInUserAuthority();

		if (authoritySet != null && !authoritySet.isEmpty()) {

			Iterator itr = authoritySet.iterator();
			while (itr.hasNext()) {
				ApplicationAuthority authority = (ApplicationAuthority) itr.next();
				if (authority.getRole_name().contains("SSO")) {
					return true;
				} else if (authority.getApplication().getApplicationName().equalsIgnoreCase(applicationName)
						&& authority.getRole_name().contains("ADMIN") && authority.getIsDeleted() == false) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Update User role to specific application
	 *
	 * @param user
	 * @param application
	 * @param userrole
	 * @return User.
	 */

	public User updateUserRoleForApplication(User user, Application application, String userrole) {
		Iterator<ApplicationAuthority> itr = application.getApplicationAuthority().iterator();
		while (itr.hasNext()) {
			ApplicationAuthority applicationAuthority = (ApplicationAuthority) itr.next();
			if (applicationAuthority.getRole_name().equalsIgnoreCase(userrole)) {
				Set<ApplicationAuthority> tempSet = new HashSet(user.getApplicationAuthorities());
				Iterator<ApplicationAuthority> iterator = tempSet.iterator();
				while (iterator.hasNext()) {
					ApplicationAuthority oldapplicationAuthority = (ApplicationAuthority) iterator.next();
					if (oldapplicationAuthority.getApplication_id() == application.getApplicationId()) {
						user.getApplicationAuthorities().remove(oldapplicationAuthority);
						user.getApplicationAuthorities().add(applicationAuthority);
					} else {
						user.getApplicationAuthorities().add(applicationAuthority);
					}
				}
			}
		}
		userRepository.save(user);
		return user;
	}

	/**
	 * AddOrUpdate User with role to specific application
	 *
	 * @param applicationName
	 * @param username
	 * @param userrole
	 * @return boolean Flag.
	 * @throws Exception if invalid requested parameters.
	 */

	public boolean addUserForApplication(String username, String userrole) {
		boolean statusFlag = false;
		User user = null;
		OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext()
				.getAuthentication();
		String clientId = authentication.getOAuth2Request().getClientId();
		if (Validation.validate(username, Validation.NIELSEN_EMAIL_REGEX) && Validation.validateRole(userrole)
				&& checkIsAdminAuthorityWithClientId(clientId)) {
			user = getUser(username);
			if (user == null) {
				user = createInternalUser(username, Constants.DASHBOARD_CLIENT);
			}
			Application application = applicationService.getApplicationByClientId(clientId);
			updateUserRoleForApplication(user, application, userrole);
			statusFlag = true;

		}
		return statusFlag;
	}

	/**
	 * Delete User with role to specific application
	 *
	 * @param applicationName
	 * @param username
	 * @return void.
	 * @throws Exception if invalid requested parameters.
	 */
	public boolean deleteUserForApplication(String username) throws Exception {
		boolean statusFlag = false;
		OAuth2Authentication authentication = (OAuth2Authentication) SecurityContextHolder.getContext()
				.getAuthentication();
		String clientId = authentication.getOAuth2Request().getClientId();
		if ((Validation.validate(username, Validation.NIELSEN_EMAIL_REGEX)
				|| Validation.validate(username, Validation.EMAIL_REGEX))
				&& checkIsAdminAuthorityWithClientId(clientId)) {
			User user = getUser(username);
			if (user != null) {
				Application application = applicationService.getApplicationByClientId(clientId);
				Iterator<ApplicationAuthority> itr = application.getApplicationAuthority().iterator();
				while (itr.hasNext()) {
					ApplicationAuthority applicationAuthority = (ApplicationAuthority) itr.next();
					user.getApplicationAuthorities().remove(applicationAuthority);
				}
				userRepository.save(user);
				statusFlag = true;
			}

		}
		return statusFlag;
	}

	public boolean checkIsAdminAuthorityWithClientId(String clientId) {
		Set<ApplicationAuthority> authoritySet = getLoggedInUserAuthority();
		if (authoritySet != null && !authoritySet.isEmpty()) {
			Iterator itr = authoritySet.iterator();
			while (itr.hasNext()) {
				ApplicationAuthority authority = (ApplicationAuthority) itr.next();
				if (authority.getRole_name().contains("SSO")) {
					return true;
				} else if (authority.getApplication().getClientId().equalsIgnoreCase(clientId)
						&& authority.getRole_name().contains("ADMIN") && authority.getIsDeleted() == false) {
					return true;
				}
			}
		}
		return false;
	}
}