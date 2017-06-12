/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/user/user-api/api/src/java/org/sakaiproject/user/api/UserDirectoryService.java $
 * $Id: UserDirectoryService.java 4294 2013-01-28 19:34:34Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.user.api;

import java.util.Collection;
import java.util.List;

import org.sakaiproject.entity.api.EntityProducer;
import org.sakaiproject.entity.api.ResourceProperties;
import org.w3c.dom.Element;

/**
 * <p>
 * UserDirectoryService manages the end-user modeling for Sakai.
 * </p>
 */
public interface UserDirectoryService extends EntityProducer
{
	/** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
	static final String APPLICATION_ID = "sakai:user";

	/** This string starts the references to resources in this service. */
	static final String REFERENCE_ROOT = "/user";

	/** Name for the event of adding a group. */
	static final String SECURE_ADD_USER = "user.add";

	/** Name for the event of removing a user. */
	static final String SECURE_REMOVE_USER = "user.del";

	/** Name for the event of updating any user info. */
	static final String SECURE_UPDATE_USER_ANY = "user.upd.any";

	/** Name for the event of updating one's own user info. */
	static final String SECURE_UPDATE_USER_OWN = "user.upd.own";
	
	/** Name for the ability for updating one's own name. */
	static final String SECURE_UPDATE_USER_OWN_NAME = "user.upd.own.name";
	
	/** Name for the ability for updating one's own email. */
	static final String SECURE_UPDATE_USER_OWN_EMAIL = "user.upd.own.email";

	/** Name for the ability for updating one's own password. */
	static final String SECURE_UPDATE_USER_OWN_PASSWORD = "user.upd.own.passwd";

	/** Name for the ability for updating one's own type. */
	static final String SECURE_UPDATE_USER_OWN_TYPE = "user.upd.own.type";

	
	
	/** User id for the admin user. */
	static final String ADMIN_ID = "admin";

	/** User eid for the admin user. */
	static final String ADMIN_EID = "admin";

	/**
	 * Add a new user to the directory. Must commitEdit() to make official, or cancelEdit() when done! Id is auto-generated.
	 * 
	 * @param id
	 *        The user uuid string. Leave null for auto-assignment.
	 * @param eid
	 *        The user eid.
	 * @return A locked UserEdit object (reserving the id).
	 * @exception UserIdInvalidException
	 *            if the user eid is invalid.
	 * @exception UserAlreadyDefinedException
	 *            if the user id or eid is already used.
	 * @exception UserPermissionException
	 *            if the current user does not have permission to add a user.
	 */
	UserEdit addUser(String id, String eid) throws UserIdInvalidException, UserAlreadyDefinedException, UserPermissionException;

	/**
	 * Add a new user to the directory, complete in one operation. Id is auto-generated.
	 * 
	 * @param id
	 *        The user uuid string. Leave null for auto-assignment.
	 * @param eid
	 *        The user eid.
	 * @param firstName
	 *        The user first name.
	 * @param lastName
	 *        The user last name.
	 * @param email
	 *        The user email.
	 * @param pw
	 *        The user password.
	 * @param type
	 *        The user type.
	 * @param properties
	 *        Other user properties.
	 * @return The User object created.
	 * @exception UserIdInvalidException
	 *            if the user eid is invalid.
	 * @exception UserAlreadyDefinedException
	 *            if the user eid is already used.
	 * @exception UserPermissionException
	 *            if the current user does not have permission to add a user.
	 */
	User addUser(String id, String eid, String firstName, String lastName, String email, String pw, String type, ResourceProperties properties)
			throws UserIdInvalidException, UserAlreadyDefinedException, UserPermissionException;

	/**
	 * check permissions for addUser().
	 * 
	 * @return true if the user is allowed to add a user, false if not.
	 */
	boolean allowAddUser();

	/**
	 * check permissions for removeUser().
	 * 
	 * @param id
	 *        The group id.
	 * @return true if the user is allowed to removeUser(id), false if not.
	 */
	boolean allowRemoveUser(String id);

	/**
	 * check permissions for editUser()
	 * 
	 * @param id
	 *        The user id.
	 * @return true if the user is allowed to update the user, false if not.
	 */
	boolean allowUpdateUser(String id);
	
	
	/**
	 * check permissions for editUser()
	 * 
	 * @param id
	 *        The user id.
	 * @return true if the user is allowed to update their own first and last names, false if not.
	 */
	public boolean allowUpdateUserName(String id);
	
	/**
	 * check permissions for editUser()
	 * 
	 * @param id
	 *        The user id.
	 * @return true if the user is allowed to update their own email address, false if not.
	 */
	public boolean allowUpdateUserEmail(String id);
	
	/**
	 * check permissions for editUser()
	 * 
	 * @param id
	 *        The user id.
	 * @return true if the user is allowed to update their own password, false if not.
	 */
	public boolean allowUpdateUserPassword(String id);
	
	/**
	 * check permissions for editUser()
	 * 
	 * @param id
	 *        The user id.
	 * @return true if the user is allowed to update their own type, false if not.
	 */
	public boolean allowUpdateUserType(String id);
	

	/**
	 * Check if this user eid and password combination is not unique among users.
	 * 
	 * @param eid
	 *        The user eid.
	 * @param password
	 *        The user password.
	 * @return true if there are multiple matches for this eid and password, false if there are not.
	 */
	boolean nonUniqueEid(String eid, String password);

	/**
	 * Authenticate a user / password.
	 * 
	 * @param eid
	 *        The user eid.
	 * @param password
	 *        The password.
	 * @return The User object of the authenticated user if successfull, null if not.
	 */
	User authenticate(String eid, String password) throws AuthenticationException;

	/**
	 * Authenticate a user / password / iid.
	 * 
	 * @param iid
	 *        The user institution id.
	 * @param password
	 *        The password.
	 * @param institutionCode
	 *        The institution code.
	 * @return The User object of the authenticated user if successful, null if not.
	 */
	User authenticateIid(String iid, String password, String institutionCode);

	/**
	 * Cancel the changes made to a UserEdit object, and release the lock. The UserEdit is disabled, and not to be used after this call.
	 * 
	 * @param user
	 *        The UserEdit object to commit.
	 */
	void cancelEdit(UserEdit user);

	/**
	 * Commit the changes made to a UserEdit object, and release the lock. The UserEdit is disabled, and not to be used after this call.
	 * 
	 * @param user
	 *        The UserEdit object to commit.
	 * @exception UserAlreadyDefinedException
	 *            if the User eid is already in use by another User object.
	 */
	void commitEdit(UserEdit user) throws UserAlreadyDefinedException;

	/**
	 * Count all the users that match this criteria in id or target, first or last name.
	 * 
	 * @return The count of all users matching the criteria.
	 */
	int countSearchUsers(String criteria);

	/**
	 * Count all the users.
	 * 
	 * @return The count of all users.
	 */
	int countUsers();

	/**
	 * Remove authentication for the current user.
	 */
	void destroyAuthentication();

	/**
	 * Get a locked user object for editing. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param id
	 *        The user id string.
	 * @return A UserEdit object for editing.
	 * @exception UserNotDefinedException
	 *            if not found, or if not an UserEdit object
	 * @exception UserPermissionException
	 *            if the current user does not have permission to mess with this user.
	 * @exception UserLockedException
	 *            if the User object is locked by someone else.
	 */
	UserEdit editUser(String id) throws UserNotDefinedException, UserPermissionException, UserLockedException;

	/**
	 * Find the user objects which have this email address.
	 * 
	 * @param email
	 *        The email address string.
	 * @return A Collection (User) of user objects which have this email address (may be empty).
	 */
	Collection findUsersByEmail(String email);

	/**
	 * Access the anonymous user object.
	 * 
	 * @return the anonymous user object.
	 */
	User getAnonymousUser();

	/**
	 * Access the user object associated with the "current" request.
	 * 
	 * @return The current user (may be anon).
	 */
	User getCurrentUser();

	/**
	 * Access a user object.
	 * 
	 * @param id
	 *        The user id string.
	 * @return A user object containing the user information
	 * @exception UserNotDefinedException
	 *            if not found
	 */
	User getUser(String id) throws UserNotDefinedException;

	/**
	 * Access a user object, given an enterprise id.
	 * 
	 * @param eid
	 *        The user eid string.
	 * @return A user object containing the user information
	 * @exception UserNotDefinedException
	 *            if not found
	 */
	User getUserByEid(String eid) throws UserNotDefinedException;

	/**
	 * Access all the user objects that have this enterprise id.
	 * 
	 * @param eid
	 *        The user eid string.
	 * @return A list of user object containing the user information, or empty if not found
	 */
	List<User> getUsersByEid(String eid);

	/**
	 * Access a user object, given an institution code and the user's id within the institution
	 * 
	 * @param institutionCode
	 *        The unique code given to the user's institution.
	 * @param iid
	 *        The unique id of the user within the institution.
	 * @return A user object containing the user information
	 * @exception UserNotDefinedException
	 *            if not found
	 */
	User getUserByIid(String institutionCode, String iid) throws UserNotDefinedException;

	/**
	 * Check if the user with this internal id has one or more institution id (s) set.
	 * @param id The user internal id.
	 * @return true if this user has one or more IIDs, false if not.
	 */
	boolean hasIid(String id);

	/**
	 * Set the IID for this user. If one exists already, add this as an additional one.
	 * 
	 * @param id
	 *        The user's in-institution identity string.
	 * @param institutionCode
	 *        The institution code that generated this iid.
	 * @param iid
	 *        The institution
	 * @throws UserPermissionException
	 *         if the current user does not have permission.
	 */
	void setIid(String id, String institutionCode, String iid) throws UserPermissionException;
	
	/**
	 * Remove the set of IID information for this user.
	 * 
	 * @param id
	 *        The user id.
	 * @throws UserPermissionException
	 *         if the current user does not have permission.
	 */
	void clearIid(String id) throws UserPermissionException;

	/**
	 * Get any IIDs set for this user.
	 * 
	 * @param id
	 *        The user id.
	 * @return A list of strings, each of the form userId@instCode.
	 */
	List<String> getIid(String id);

	/**
	 * Find the user eid from a user id.
	 * 
	 * @param id
	 *        The user id.
	 * @return The eid for the user with this id.
	 * @exception UserNotDefinedException
	 *            if we don't know anything about the user with this id.
	 */
	String getUserEid(String id) throws UserNotDefinedException;

	/**
	 * Find the user id from a user eid.
	 * 
	 * @param eid
	 *        The user eid.
	 * @return The id for the user with this eid.
	 * @exception UserNotDefinedException
	 *            if we don't know anything about the user with this eid.
	 */
	String getUserId(String eid) throws UserNotDefinedException;

	/**
	 * Access all user objects - known to us (not from external providers).
	 * 
	 * @return A list of user objects containing each user's information.
	 * @exception IdUnusedException
	 *            if not found.
	 */
	List getUsers();

	/**
	 * Access a bunch of user object.
	 * 
	 * @param ids
	 *        The Collection (String) of user ids.
	 * @return A List (User) of user objects for valid ids.
	 */
	List getUsers(Collection ids);

	/**
	 * Find all the users within the record range given (sorted by sort name).
	 * 
	 * @param first
	 *        The first record position to return.
	 * @param last
	 *        The last record position to return.
	 * @return A list (User) of all the users within the record range given (sorted by sort name).
	 */
	List getUsers(int first, int last);

	/**
	 * Add a new user to the directory, from a definition in XML. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param el
	 *        The XML DOM Element defining the user.
	 * @return A locked UserEdit object (reserving the id).
	 * @exception UserIdInvalidException
	 *            if the user id is invalid.
	 * @exception UserAlreadyDefinedException
	 *            if the user id is already used.
	 * @exception UserPermissionException
	 *            if the current user does not have permission to add a user.
	 */
	UserEdit mergeUser(Element el) throws UserIdInvalidException, UserAlreadyDefinedException, UserPermissionException;

	/**
	 * Remove this user's information from the directory - it must be a user with a lock from editUser(). The UserEdit is disabled, and not to be used after this call.
	 * 
	 * @param user
	 *        The locked user object to remove.
	 * @exception UserPermissionException
	 *            if the current user does not have permission to remove this user.
	 */
	void removeUser(UserEdit user) throws UserPermissionException;

	/**
	 * Search all the users that match this criteria in id or email, first or last name, returning a subset of records within the record range given (sorted by sort name).
	 * 
	 * @param criteria
	 *        The search criteria.
	 * @param first
	 *        The first record position to return.
	 * @param last
	 *        The last record position to return.
	 * @return A list (User) of all the aliases matching the criteria, within the record range given (sorted by sort name).
	 */
	List searchUsers(String criteria, int first, int last);

	/**
	 * Access the internal reference which can be used to access the resource from within the system.
	 * 
	 * @param id
	 *        The user id string.
	 * @return The the internal reference which can be used to access the resource from within the system.
	 */
	String userReference(String id);
}
