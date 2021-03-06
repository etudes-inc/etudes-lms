/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/user/user-impl/impl/src/java/org/sakaiproject/user/impl/BaseUserDirectoryService.java $
 * $Id: BaseUserDirectoryService.java 8503 2014-08-22 02:02:58Z ggolden $
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

package org.sakaiproject.user.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.activitymeter.api.SiteVisitService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.AuthzPermissionException;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.AuthenticationMultipleException;
import org.sakaiproject.user.api.DisplayAdvisorUDP;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryProvider;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserFactory;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserLockedException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.sakaiproject.user.api.UsersShareEmailUDP;
import org.sakaiproject.util.BaseResourceProperties;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.StorageUser;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * BaseUserDirectoryService is a Sakai user directory services implementation.
 * </p>
 * <p>
 * User records can be kept locally, in Sakai, externally, by a UserDirectoryProvider, or a mixture of both.
 * </p>
 * <p>
 * Each User that ever goes through Sakai is allocated a Sakai unique UUID. Even if we don't keep the User record in Sakai, we keep a map of this id to the external eid.
 * </p>
 */
public abstract class BaseUserDirectoryService implements UserDirectoryService, StorageUser, UserFactory
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(BaseUserDirectoryService.class);

	/** Storage manager for this service. */
	protected Storage m_storage = null;

	/** The initial portion of a relative access point URL. */
	protected String m_relativeAccessPoint = null;

	/** An anon. user. */
	protected User m_anon = null;

	/** A user directory provider. */
	protected UserDirectoryProvider m_provider = null;

	/** Key for current service caching of current user */
	protected final String M_curUserKey = getClass().getName() + ".currentUser";

	/** A cache of calls to the service and the results. */
	protected Cache m_callCache = null;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Abstractions, etc.
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct storage for this service.
	 */
	protected abstract Storage newStorage();

	/**
	 * Access the partial URL that forms the root of resource URLs.
	 * 
	 * @param relative
	 *        if true, form within the access path only (i.e. starting with /content)
	 * @return the partial URL that forms the root of resource URLs.
	 */
	protected String getAccessPoint(boolean relative)
	{
		return (relative ? "" : serverConfigurationService().getAccessUrl()) + m_relativeAccessPoint;
	}

	/**
	 * Access the internal reference which can be used to access the resource from within the system.
	 * 
	 * @param id
	 *        The user id string.
	 * @return The the internal reference which can be used to access the resource from within the system.
	 */
	public String userReference(String id)
	{
		// clean up the id
		id = cleanId(id);

		return getAccessPoint(true) + Entity.SEPARATOR + ((id == null) ? "" : id);
	}

	/**
	 * Access the user id extracted from a user reference.
	 * 
	 * @param ref
	 *        The user reference string.
	 * @return The the user id extracted from a user reference.
	 */
	protected String userId(String ref)
	{
		String start = getAccessPoint(true) + Entity.SEPARATOR;
		int i = ref.indexOf(start);
		if (i == -1) return ref;
		String id = ref.substring(i + start.length());
		return id;
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @return true if allowd, false if not
	 */
	protected boolean unlockCheck(String lock, String resource)
	{
		if (!securityService().unlock(lock, resource))
		{
			return false;
		}

		return true;
	}
	
	/**
	 * Check security permission.
	 * 
	 * @param lock
	 *        A list of lock strings to consider.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @return true if any of these locks are allowed, false if not
	 */
	protected boolean unlockCheck(List locks, String resource)
	{
		Iterator locksIterator = locks.iterator();
		
		while(locksIterator.hasNext()) {
			
			if(securityService().unlock((String) locksIterator.next(), resource))
					return true;
			
		}
	
		return false;
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock1
	 *        The lock id string.
	 * @param lock2
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @return true if either allowed, false if not
	 */
	protected boolean unlockCheck2(String lock1, String lock2, String resource)
	{
		if (!securityService().unlock(lock1, resource))
		{
			if (!securityService().unlock(lock2, resource))
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @exception UserPermissionException
	 *            Thrown if the user does not have access
	 * @return The lock id string that succeeded
	 */
	protected String unlock(String lock, String resource) throws UserPermissionException
	{
		
		if (!unlockCheck(lock, resource))
		{
			throw new UserPermissionException(sessionManager().getCurrentSessionUserId(), lock, resource);
		}
		
	    return lock;
	}

	/**
	 * Check security permission.
	 * 
	 * @param lock1
	 *        The lock id string.
	 * @param lock2
	 *        The lock id string.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @exception UserPermissionException
	 *            Thrown if the user does not have access to either.
	 */
	protected void unlock2(String lock1, String lock2, String resource) throws UserPermissionException
	{
		if (!unlockCheck2(lock1, lock2, resource))
		{
			throw new UserPermissionException(sessionManager().getCurrentSessionUserId(), lock1 + "/" + lock2, resource);
		}
	}
	
	/**
	 * Check security permission.
	 * 
	 * 
	 * @param locks
	 *        The list of lock strings.
	 * @param resource
	 *        The resource reference string, or null if no resource is involved.
	 * @exception UserPermissionException
	 *            Thrown if the user does not have access to either.
	 * @return A list of the lock strings that the user has access to.
	 */
	
	protected List unlock(List locks, String resource) throws UserPermissionException
	{
		List locksSucceeded = new ArrayList();
		String locksFailed = "";
		
		Iterator locksIterator = locks.iterator();
		
		while (locksIterator.hasNext()) {
		
			String lock = (String) locksIterator.next();
			
			if (unlockCheck(lock, resource))
			{
				locksSucceeded.add(lock);
				
			} else {
				
				locksFailed += lock + " ";
			}
			
		}
			
		if (locksSucceeded.size() < 1) {
			throw new UserPermissionException(sessionManager().getCurrentSessionUserId(), locksFailed, resource);
		}
		
		return locksSucceeded;
	}

	/**
	 * Make sure we have a good uuid for a user record. If id is specified, use that. Otherwise get one from the provider or allocate a uuid.
	 * 
	 * @param id
	 *        The proposed id.
	 * @param eid
	 *        The proposed eid.
	 * @return The id to use as the User's uuid.
	 */
	protected String assureUuid(String id, String eid)
	{
		// if we are not using separate id and eid, return the eid
		if (!m_separateIdEid) return eid;

		if (id != null) return id;

		// TODO: let the provider have a chance to set this? -ggolden

		// allocate a uuid
		id = idManager().createUuid();

		return id;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Configuration
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Configuration: set the user directory provider helper service.
	 * 
	 * @param provider
	 *        the user directory provider helper service.
	 */
	public void setProvider(UserDirectoryProvider provider)
	{
		m_provider = provider;
	}

	/** The # seconds to cache gets. 0 disables the cache. */
	protected int m_cacheSeconds = 0;

	/**
	 * Set the # minutes to cache a get.
	 * 
	 * @param time
	 *        The # minutes to cache a get (as an integer string).
	 */
	public void setCacheMinutes(String time)
	{
		m_cacheSeconds = Integer.parseInt(time) * 60;
	}

	/** The # seconds to cache gets. 0 disables the cache. */
	protected int m_cacheCleanerSeconds = 0;

	/**
	 * Set the # minutes between cache cleanings.
	 * 
	 * @param time
	 *        The # minutes between cache cleanings. (as an integer string).
	 */
	public void setCacheCleanerMinutes(String time)
	{
		m_cacheCleanerSeconds = Integer.parseInt(time) * 60;
	}

	/** Configuration: case sensitive user eid. */
	protected boolean m_caseSensitiveEid = false;

	/**
	 * Configuration: case sensitive user eid
	 * 
	 * @param value
	 *        The case sensitive user eid.
	 */
	public void setCaseSensitiveId(String value)
	{
		m_caseSensitiveEid = new Boolean(value).booleanValue();
	}

	/** Configuration: use a different id and eid for each record (otherwise make them the same value). */
	protected boolean m_separateIdEid = false;

	/**
	 * Configuration: to use a separate value for id and eid for each user record, or not.
	 * 
	 * @param value
	 *        The separateIdEid setting.
	 */
	public void setSeparateIdEid(String value)
	{
		m_separateIdEid = new Boolean(value).booleanValue();
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected abstract ServerConfigurationService serverConfigurationService();

	/**
	 * @return the EntityManager collaborator.
	 */
	protected abstract EntityManager entityManager();

	/**
	 * @return the SecurityService collaborator.
	 */
	protected abstract SecurityService securityService();

	/**
	 * @return the FunctionManager collaborator.
	 */
	protected abstract FunctionManager functionManager();

	/**
	 * @return the SessionManager collaborator.
	 */
	protected abstract SessionManager sessionManager();

	/**
	 * @return the SiteService collaborator.
	 */
	protected abstract SiteService siteService();

	/**
	 * @return the MemoryService collaborator.
	 */
	protected abstract MemoryService memoryService();

	/**
	 * @return the EventTrackingService collaborator.
	 */
	protected abstract EventTrackingService eventTrackingService();

	/**
	 * @return the ThreadLocalManager collaborator.
	 */
	protected abstract ThreadLocalManager threadLocalManager();

	/**
	 * @return the AuthzGroupService collaborator.
	 */
	protected abstract AuthzGroupService authzGroupService();

	/**
	 * @return the TimeService collaborator.
	 */
	protected abstract TimeService timeService();

	/**
	 * @return the IdManager collaborator.
	 */
	protected abstract IdManager idManager();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			m_relativeAccessPoint = REFERENCE_ROOT;

			// construct storage and read
			m_storage = newStorage();
			m_storage.open();

			// make an anon. user
			m_anon = new BaseUserEdit("");

			// <= 0 indicates no caching desired
			if ((m_cacheSeconds > 0) && (m_cacheCleanerSeconds > 0))
			{
				// build a synchronized map for the call cache, automatiaclly checking for expiration every 15 mins, expire on user events, too.
				m_callCache = memoryService().newHardCache(m_cacheCleanerSeconds, userReference(""));
			}

			// register as an entity producer
			entityManager().registerEntityProducer(this, REFERENCE_ROOT);

			// register functions
			functionManager().registerFunction(SECURE_ADD_USER);
			functionManager().registerFunction(SECURE_REMOVE_USER);
			functionManager().registerFunction(SECURE_UPDATE_USER_OWN);
			functionManager().registerFunction(SECURE_UPDATE_USER_OWN_NAME);
			functionManager().registerFunction(SECURE_UPDATE_USER_OWN_EMAIL);
			functionManager().registerFunction(SECURE_UPDATE_USER_OWN_PASSWORD);
			functionManager().registerFunction(SECURE_UPDATE_USER_OWN_TYPE);
			functionManager().registerFunction(SECURE_UPDATE_USER_ANY);
			
			// if no provider was set, see if we can find one
			if (m_provider == null)
			{
				m_provider = (UserDirectoryProvider) ComponentManager.get(UserDirectoryProvider.class.getName());
			}

			M_log.info("init(): provider: " + ((m_provider == null) ? "none" : m_provider.getClass().getName())
					+ " - caching minutes: " + m_cacheSeconds / 60 + " - cache cleaner minutes: " + m_cacheCleanerSeconds / 60
					+ " separateIdEid: " + m_separateIdEid);
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}
	}

	/**
	 * Returns to uninitialized state. You can use this method to release resources thet your Service allocated when Turbine shuts down.
	 */
	public void destroy()
	{
		m_storage.close();
		m_storage = null;
		m_provider = null;
		m_anon = null;

		M_log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * UserDirectoryService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public User getUserByIid(String institutionCode, String iid) throws UserNotDefinedException
	{
		String id = null;
		if (institutionCode == null)
		{
			id = this.m_storage.getUserIdForIid(iid);
		}
		else
		{
			id = this.m_storage.getUserIdForIid(institutionCode, iid);
		}

		if (id == null) throw new UserNotDefinedException(iid + "@" + institutionCode);
		
		return getUser(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasIid(String id)
	{
		id = cleanId(id);
		return this.m_storage.hasIid(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setIid(String id, String institutionCode, String iid) throws UserPermissionException
	{
		id = cleanId(id);
		this.m_storage.setIid(id, institutionCode, iid);
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearIid(String id) throws UserPermissionException
	{
		id = cleanId(id);
		this.m_storage.clearIid(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<String> getIid(String id)
	{
		id = cleanId(id);
		return this.m_storage.getIid(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserEid(String id) throws UserNotDefinedException
	{
		id = cleanId(id);

		// first, check our map
		String eid = m_storage.checkMapForEid(id);
		if (eid != null) return eid;

		throw new UserNotDefinedException(id);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getUserId(String eid) throws UserNotDefinedException
	{
		eid = cleanEid(eid);

		// first, check our map
		String id = m_storage.checkMapForId(eid);
		if (id != null) return id;

		// if we don't have an id yet for this eid, and there's a provider that recognizes this eid, allocate an id
		if (m_provider != null)
		{
			// allocate the id to use if this succeeds
			id = assureUuid(null, eid);

			// make a new edit to hold the provider's info, hoping it will be filled in
			BaseUserEdit user = new BaseUserEdit((String) null);
			user.m_id = id;
			user.m_eid = eid;

			// check with the provider
			if (m_provider.getUser(user))
			{
				// record the id -> eid mapping (could possibly fail if somehow this eid was mapped since our test above)
				m_storage.putMap(user.getId(), user.getEid());

				return user.getId();
			}
		}

		// not found
		throw new UserNotDefinedException(eid);
	}

	/**
	 * @inheritDoc
	 */
	public User getUser(String id) throws UserNotDefinedException
	{
		// clean up the id
		id = cleanId(id);

		if (id == null) throw new UserNotDefinedException("null");

		// see if we've done this already in this thread
		String ref = userReference(id);
		UserEdit user = (UserEdit) threadLocalManager().get(ref);

		// if not
		if (user == null)
		{
			// check the cache
			if ((m_callCache != null) && (m_callCache.containsKey(ref)))
			{
				user = (UserEdit) m_callCache.get(ref);
			}

			else
			{
				// find our user record, and use it if we have it
				user = m_storage.getById(id);

				// let the provider provide if needed
				if ((user == null) && (m_provider != null))
				{
					// we need the eid for the provider - if we can't find an eid, we throw UserNotDefinedException
					String eid = getUserEid(id);

					// make a new edit to hold the provider's info, hoping it will be filled in
					user = new BaseUserEdit((String) null);
					((BaseUserEdit) user).m_id = id;
					((BaseUserEdit) user).m_eid = eid;

					if (!m_provider.getUser(user))
					{
						// it was not provided for, so clear back to null
						user = null;
					}
				}

				// if found, save it for later use in this thread
				if (user != null)
				{
					threadLocalManager().set(ref, user);

					// cache
					if (m_callCache != null) m_callCache.put(ref, user, m_cacheSeconds);
				}
			}
		}

		// if not found
		if (user == null)
		{
			throw new UserNotDefinedException(id);
		}

		return user;
	}

	/**
	 * @inheritDoc
	 */
	public User getUserByEid(String eid) throws UserNotDefinedException
	{
		User rv = null;

		// clean up the eid
		eid = cleanEid(eid);
		if (eid == null) throw new UserNotDefinedException("null");

		// find the user id mapped to this eid and get that record (internally or by provider)
		String id = getUserId(eid);

		// now we can get by id
		rv = getUser(id);

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public List<User> getUsersByEid(String eid)
	{
		List<User> rv = new ArrayList<User>();

		// clean up the eid
		eid = cleanEid(eid);
		if (eid == null) return rv;

		// only check our map - Note: ignoring providers -ggolden
		List<String> ids = m_storage.checkMapForMultipleIds(eid);
		for (String id : ids)
		{
			try
			{
				User user = getUser(id);
				rv.add(user);
			}
			catch (UserNotDefinedException e)
			{
			}
		}

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public List getUsers(Collection ids)
	{
		// User objects to return
		List rv = new Vector();

		// a list of User (edits) setup to check with the provider
		Collection fromProvider = new Vector();

		// for each requested id
		for (Iterator i = ids.iterator(); i.hasNext();)
		{
			String id = (String) i.next();

			// clean up the id
			id = cleanId(id);

			// skip nulls
			if (id == null) continue;

			// see if we've done this already in this thread
			String ref = userReference(id);
			UserEdit user = (UserEdit) threadLocalManager().get(ref);

			// if not
			if (user == null)
			{
				// check the cache
				if ((m_callCache != null) && (m_callCache.containsKey(ref)))
				{
					user = (UserEdit) m_callCache.get(ref);
				}

				else
				{
					// find our user record
					user = m_storage.getById(id);

					// if we didn't find it locally, collect a list of externals to get
					if ((user == null) && (m_provider != null))
					{
						try
						{
							// get the eid for this user so we can ask the provider
							String eid = getUserEid(id);

							// make a new edit to hold the provider's info; the provider will either fill this in, if known, or remove it from the collection
							BaseUserEdit providerUser = new BaseUserEdit((String) null);
							providerUser.m_id = id;
							providerUser.m_eid = eid;
							fromProvider.add(providerUser);
						}
						catch (UserNotDefinedException e)
						{
							// this user is not internally defined, and we can't find an eid for it, so we skip it
							M_log.warn("getUsers: cannot find eid for user id: " + id);
						}
					}

					// if found, save it for later use in this thread
					if (user != null)
					{
						// cache
						threadLocalManager().set(ref, user);
						if (m_callCache != null) m_callCache.put(ref, user, m_cacheSeconds);
					}
				}
			}

			// if we found a user for this id, add it to the return
			if (user != null)
			{
				rv.add(user);
			}
		}

		// check the provider, all at once
		if ((m_provider != null) && (!fromProvider.isEmpty()))
		{
			m_provider.getUsers(fromProvider);

			// for each User in the collection that was filled in (and not removed) by the provider, cache and return it
			for (Iterator i = fromProvider.iterator(); i.hasNext();)
			{
				User user = (User) i.next();

				// cache, thread and call cache
				String ref = user.getReference();
				threadLocalManager().set(ref, user);
				if (m_callCache != null) m_callCache.put(ref, user, m_cacheSeconds);

				// add to return
				rv.add(user);
			}
		}

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public User getCurrentUser()
	{
		String id = sessionManager().getCurrentSessionUserId();

		// check current service caching - discard if the session user is different
		User rv = (User) threadLocalManager().get(M_curUserKey);
		if ((rv != null) && (rv.getId().equals(id))) return rv;

		try
		{
			rv = getUser(id);
		}
		catch (UserNotDefinedException e)
		{
			rv = getAnonymousUser();
		}

		// cache in the current service
		threadLocalManager().set(M_curUserKey, rv);

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public boolean allowUpdateUser(String id)
	{
		// clean up the id
		id = cleanId(id);
		if (id == null) return false;

		// special override: the "helpdesk" user may
		if (this.getCurrentUser().getId().equals("helpdesk")) return true;

		// is this the user's own?
		if (id.equals(sessionManager().getCurrentSessionUserId()))
		{
			ArrayList locks = new ArrayList();
			locks.add(SECURE_UPDATE_USER_OWN);
			locks.add(SECURE_UPDATE_USER_ANY);
			locks.add(SECURE_UPDATE_USER_OWN_NAME);
			locks.add(SECURE_UPDATE_USER_OWN_EMAIL);
			locks.add(SECURE_UPDATE_USER_OWN_PASSWORD);
			locks.add(SECURE_UPDATE_USER_OWN_TYPE);
			
			// own or any
			return unlockCheck(locks, userReference(id));
		}

		else
		{
			// just any
			return unlockCheck(SECURE_UPDATE_USER_ANY, userReference(id));
		}
	}
	
	/**
	 * @inheritDoc
	 */
	public boolean allowUpdateUserName(String id)
	{
		// clean up the id
		id = cleanId(id);
		if (id == null) return false;
		
		// special override: the "helpdesk" user may not
		if (this.getCurrentUser().getId().equals("helpdesk")) return false;

		//		 is this the user's own?
		if (id.equals(sessionManager().getCurrentSessionUserId()))
		{
			ArrayList locks = new ArrayList();
			locks.add(SECURE_UPDATE_USER_OWN);
			locks.add(SECURE_UPDATE_USER_ANY);
			locks.add(SECURE_UPDATE_USER_OWN_NAME);
			
			
			// own or any
			return unlockCheck(locks, userReference(id));
		}

		else
		{
			// just any
			return unlockCheck(SECURE_UPDATE_USER_ANY, userReference(id));
		}
		
	}
	
	/**
	 * @inheritDoc
	 */
	public boolean allowUpdateUserEmail(String id)
	{
		// clean up the id
		id = cleanId(id);
		if (id == null) return false;
		
		// special override: the "helpdesk" user may
		if (this.getCurrentUser().getId().equals("helpdesk")) return true;

		//		 is this the user's own?
		if (id.equals(sessionManager().getCurrentSessionUserId()))
		{
			ArrayList locks = new ArrayList();
			locks.add(SECURE_UPDATE_USER_OWN);
			locks.add(SECURE_UPDATE_USER_ANY);
			locks.add(SECURE_UPDATE_USER_OWN_EMAIL);
			
			
			// own or any
			return unlockCheck(locks, userReference(id));
		}

		else
		{
			// just any
			return unlockCheck(SECURE_UPDATE_USER_ANY, userReference(id));
		}
		
	}
	
	/**
	 * @inheritDoc
	 */
	public boolean allowUpdateUserPassword(String id)
	{
		// clean up the id
		id = cleanId(id);
		if (id == null) return false;
		
		// special override: the "helpdesk" user may
		if (this.getCurrentUser().getId().equals("helpdesk")) return true;

		//		 is this the user's own?
		if (id.equals(sessionManager().getCurrentSessionUserId()))
		{
			ArrayList locks = new ArrayList();
			locks.add(SECURE_UPDATE_USER_OWN);
			locks.add(SECURE_UPDATE_USER_ANY);
			locks.add(SECURE_UPDATE_USER_OWN_PASSWORD);
			
			
			// own or any
			return unlockCheck(locks, userReference(id));
		}

		else
		{
			// just any
			return unlockCheck(SECURE_UPDATE_USER_ANY, userReference(id));
		}
		
	}
	

	/**
	 * @inheritDoc
	 */
	public boolean allowUpdateUserType(String id)
	{
		// clean up the id
		id = cleanId(id);
		if (id == null) return false;
		
		// special override: the "helpdesk" user may not
		if (this.getCurrentUser().getId().equals("helpdesk")) return false;

		//		 is this the user's own?
		if (id.equals(sessionManager().getCurrentSessionUserId()))
		{
			ArrayList locks = new ArrayList();
			locks.add(SECURE_UPDATE_USER_OWN);
			locks.add(SECURE_UPDATE_USER_ANY);
			locks.add(SECURE_UPDATE_USER_OWN_TYPE);
			
			
			// own or any
			return unlockCheck(locks, userReference(id));
		}

		else
		{
			// just any
			return unlockCheck(SECURE_UPDATE_USER_ANY, userReference(id));
		}
		
	}

	
	/**
	 * @inheritDoc
	 */
	public UserEdit editUser(String id) throws UserNotDefinedException, UserPermissionException, UserLockedException
	{
		
		// clean up the id
		id = cleanId(id);

		if (id == null) throw new UserNotDefinedException("null");

		List locksSucceeded = new ArrayList();
		String function = null;

		// special helpdesk security
		if (this.getCurrentUser().getId().equals("helpdesk"))
		{
			locksSucceeded.add(SECURE_UPDATE_USER_ANY);
			function = SECURE_UPDATE_USER_ANY;
		}

		else
		{
			// is this the user's own?
			if (id.equals(sessionManager().getCurrentSessionUserId()))
			{
				// own or any
				List locks = new ArrayList();
				locks.add(SECURE_UPDATE_USER_OWN);
				locks.add(SECURE_UPDATE_USER_OWN_NAME);
				locks.add(SECURE_UPDATE_USER_OWN_EMAIL);
				locks.add(SECURE_UPDATE_USER_OWN_PASSWORD);
				locks.add(SECURE_UPDATE_USER_OWN_TYPE);
				locks.add(SECURE_UPDATE_USER_ANY);
				
				locksSucceeded = unlock(locks, userReference(id));
				function = SECURE_UPDATE_USER_OWN;
			}
			else
			{
				// just any
				locksSucceeded.add(unlock(SECURE_UPDATE_USER_ANY, userReference(id)));
				function = SECURE_UPDATE_USER_ANY;
			}
		}

		// check for existance
		if (!m_storage.check(id))
		{
			throw new UserNotDefinedException(id);
		}

		// ignore the cache - get the user with a lock from the info store
		UserEdit user = m_storage.edit(id);
		if (user == null) throw new UserLockedException(id);
		
		if (this.getCurrentUser().getId().equals("helpdesk"))
		{
			user.restrictEditFirstName();
		    user.restrictEditLastName();
			user.restrictEditType();			
		}
	
		if(!locksSucceeded.contains(SECURE_UPDATE_USER_ANY) && !locksSucceeded.contains(SECURE_UPDATE_USER_OWN)) {
			
			// current session does not have permission to edit all properties for this user
			// lock the properties the user does not have access to edit
			
			if(!locksSucceeded.contains(SECURE_UPDATE_USER_OWN_NAME)) {
				user.restrictEditFirstName();
			    user.restrictEditLastName();
			}
			
			if(!locksSucceeded.contains(SECURE_UPDATE_USER_OWN_EMAIL)) {
				user.restrictEditEmail();
			}
			
			if(!locksSucceeded.contains(SECURE_UPDATE_USER_OWN_PASSWORD)) {
				user.restrictEditPassword();
			}
			
			if(!locksSucceeded.contains(SECURE_UPDATE_USER_OWN_TYPE)) {
				user.restrictEditType();
			}
			
		}

		((BaseUserEdit) user).setEvent(function);

		return user;
	}

	/**
	 * @inheritDoc
	 */
	public void commitEdit(UserEdit user) throws UserAlreadyDefinedException
	{
		// check for closed edit
		if (!user.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				M_log.warn("commitEdit(): closed UserEdit", e);
			}
			return;
		}

		// update the properties
		addLiveUpdateProperties((BaseUserEdit) user);

		// complete the edit
		if (!m_storage.commit(user))
		{
			m_storage.cancel(user);
			((BaseUserEdit) user).closeEdit();
			throw new UserAlreadyDefinedException(user.getEid());
		}

		// track it
		eventTrackingService().post(eventTrackingService().newEvent(((BaseUserEdit) user).getEvent(), user.getReference(), true));

		// close the edit object
		((BaseUserEdit) user).closeEdit();
		
		// clear out our thread-local cache of this user
		String key = userReference(user.getId());
		threadLocalManager().set(key, null);
	}

	/**
	 * @inheritDoc
	 */
	public void cancelEdit(UserEdit user)
	{
		// check for closed edit
		if (!user.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				M_log.warn("cancelEdit(): closed UserEdit", e);
			}
			return;
		}

		// release the edit lock
		m_storage.cancel(user);

		// close the edit object
		((BaseUserEdit) user).closeEdit();
	}

	/**
	 * @inheritDoc
	 */
	public List getUsers()
	{
		List users = m_storage.getAll();
		return users;
	}

	/**
	 * @inheritDoc
	 */
	public List getUsers(int first, int last)
	{
		List all = m_storage.getAll(first, last);

		return all;
	}

	/**
	 * @inheritDoc
	 */
	public int countUsers()
	{
		return m_storage.count();
	}

	/**
	 * @inheritDoc
	 */
	public List searchUsers(String criteria, int first, int last)
	{
		return m_storage.search(criteria, first, last);
	}

	/**
	 * @inheritDoc
	 */
	public int countSearchUsers(String criteria)
	{
		return m_storage.countSearch(criteria);
	}

	/**
	 * @inheritDoc
	 */
	public Collection findUsersByEmail(String email)
	{
		// check internal users
		Collection users = m_storage.findUsersByEmail(email);

		// add in provider users
		if (m_provider != null)
		{
			// support UDP that has multiple users per email
			if (m_provider instanceof UsersShareEmailUDP)
			{
				Collection udpUsers = ((UsersShareEmailUDP) m_provider).findUsersByEmail(email, this);
				if (udpUsers != null)
				{
					for (Iterator i = udpUsers.iterator(); i.hasNext();)
					{
						BaseUserEdit u = (BaseUserEdit) i.next();

						// find the user id mapped to this eid and get that record (internally or by provider)
						try
						{
							// lookup and set the id for this user from the eid set by the provider
							u.setId(getUserId(u.getEid()));
							users.add((u));
						}
						catch (UserNotDefinedException e)
						{
							M_log.warn("findUserByEmail: user returnd by provider not recognized by provider: eid: " + u.getEid());
						}
					}
				}
			}

			// check for one
			else
			{
				// make a new edit to hold the provider's info
				BaseUserEdit edit = new BaseUserEdit((String) null);
				if (m_provider.findUserByEmail(edit, email))
				{
					// find the user id mapped to this eid and get that record (internally or by provider)
					try
					{
						// lookup and set the id for this user from the eid set by the provider
						edit.setId(getUserId(edit.getEid()));
						users.add((edit));
					}
					catch (UserNotDefinedException e)
					{
						M_log.warn("findUserByEmail: user returnd by provider not recognized by provider: eid: " + edit.getEid());
					}
				}
			}
		}

		return users;
	}

	/**
	 * @inheritDoc
	 */
	public User getAnonymousUser()
	{
		return m_anon;
	}

	/**
	 * @inheritDoc
	 */
	public boolean allowAddUser()
	{
		// special override: the "helpdesk" user may not
		if (this.getCurrentUser().getId().equals("helpdesk")) return false;

		return unlockCheck(SECURE_ADD_USER, userReference(""));
	}

	/**
	 * @inheritDoc
	 */
	public UserEdit addUser(String id, String eid) throws UserIdInvalidException, UserAlreadyDefinedException,
			UserPermissionException
	{
		// clean the ids
		id = cleanId(id);
		eid = cleanEid(eid);

		// make sure we have an id
		id = assureUuid(id, eid);

		// check security (throws if not permitted)
		unlock(SECURE_ADD_USER, userReference(id));

		// reserve a user with this id from the info store - if it's in use, this will return null
		UserEdit user = m_storage.put(id, eid);
		if (user == null)
		{
			throw new UserAlreadyDefinedException(id + " -" + eid);
		}

		((BaseUserEdit) user).setEvent(SECURE_ADD_USER);

		return user;
	}

	/**
	 * @inheritDoc
	 */
	public User addUser(String id, String eid, String firstName, String lastName, String email, String pw, String type,
			ResourceProperties properties) throws UserIdInvalidException, UserAlreadyDefinedException, UserPermissionException
	{
		// get it added
		UserEdit edit = addUser(id, eid);

		// fill in the fields
		edit.setLastName(lastName);
		edit.setFirstName(firstName);
		edit.setEmail(email);
		edit.setPassword(pw);
		edit.setType(type);

		ResourcePropertiesEdit props = edit.getPropertiesEdit();
		if (properties != null)
		{
			props.addAll(properties);
		}

		// no live props!

		// get it committed - no further security check
		if (!m_storage.commit(edit))
		{
			m_storage.cancel(edit);
			((BaseUserEdit) edit).closeEdit();
			throw new UserAlreadyDefinedException(edit.getEid());
		}

		// track it
		eventTrackingService().post(eventTrackingService().newEvent(((BaseUserEdit) edit).getEvent(), edit.getReference(), true));

		// close the edit object
		((BaseUserEdit) edit).closeEdit();

		return edit;
	}

	/**
	 * @inheritDoc
	 */
	public UserEdit mergeUser(Element el) throws UserIdInvalidException, UserAlreadyDefinedException, UserPermissionException
	{
		// construct from the XML
		User userFromXml = new BaseUserEdit(el);

		// check for a valid eid
		Validator.checkResourceId(userFromXml.getEid());

		// check security (throws if not permitted)
		unlock(SECURE_ADD_USER, userFromXml.getReference());

		// reserve a user with this id from the info store - if it's in use, this will return null
		UserEdit user = m_storage.put(userFromXml.getId(), userFromXml.getEid());
		if (user == null)
		{
			throw new UserAlreadyDefinedException(userFromXml.getId() + " - " + userFromXml.getEid());
		}

		// transfer from the XML read user object to the UserEdit
		((BaseUserEdit) user).set(userFromXml);

		((BaseUserEdit) user).setEvent(SECURE_ADD_USER);

		return user;
	}

	/**
	 * @inheritDoc
	 */
	public boolean allowRemoveUser(String id)
	{
		// clean up the id
		id = cleanId(id);
		if (id == null) return false;

		// special override: the "helpdesk" user may not
		if (this.getCurrentUser().getId().equals("helpdesk")) return false;

		return unlockCheck(SECURE_REMOVE_USER, userReference(id));
	}

	/**
	 * @inheritDoc
	 */
	public void removeUser(UserEdit user) throws UserPermissionException
	{
		// check for closed edit
		if (!user.isActiveEdit())
		{
			try
			{
				throw new Exception();
			}
			catch (Exception e)
			{
				M_log.warn("removeUser(): closed UserEdit", e);
			}
			return;
		}

		// check security (throws if not permitted)
		unlock(SECURE_REMOVE_USER, user.getReference());

		// complete the edit
		m_storage.remove(user);

		// track it
		eventTrackingService().post(eventTrackingService().newEvent(SECURE_REMOVE_USER, user.getReference(), true));

		// close the edit object
		((BaseUserEdit) user).closeEdit();

		// remove any realm defined for this resource
		try
		{
			authzGroupService().removeAuthzGroup(authzGroupService().getAuthzGroup(user.getReference()));
		}
		catch (AuthzPermissionException e)
		{
			M_log.warn("removeUser: removing realm for : " + user.getReference() + " : " + e);
		}
		catch (GroupNotDefinedException ignore)
		{
		}
		
		// remove site visit data for the service (i.e. login data)
		siteVisitService().removeUserServiceVisits(user.getId());
	}

	/**
	 * @return The SiteVisitService, via the component manager.
	 */
	protected SiteVisitService siteVisitService()
	{
		return (SiteVisitService) ComponentManager.get(SiteVisitService.class);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean nonUniqueEid(String eid, String password)
	{	
		List<User> users = this.getUsersByEid(eid);
		int count = 0;
		for (User u : users)
		{
			// pick only one with the matching password
			if (u.checkPassword(password))
			{
				count++;
			}
		}
		
		return count > 1;
	}

	/**
	 * Split the source into two strings at the last occurrence of the splitter.<br />
	 * Previous occurrences are not treated specially, and may be part of the first string.
	 * 
	 * @param source
	 *        The string to split
	 * @param splitter
	 *        The string that forms the boundary between the two strings returned.
	 * @return An array of two strings split from source by splitter.
	 */
	protected String[] splitLast(String source, String splitter)
	{
		String start = null;
		String end = null;

		// find last splitter in source
		int pos = source.lastIndexOf(splitter);

		// if not found, return null
		if (pos == -1)
		{
			return null;
		}

		// take up to the splitter for the start
		start = source.substring(0, pos);

		// and the rest after the splitter
		end = source.substring(pos + splitter.length(), source.length());

		String[] rv = new String[2];
		rv[0] = start;
		rv[1] = end;

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public User authenticate(String eid, String password) throws AuthenticationException
	{
		// Note: skipping all provider logic - works only with internally defined users -ggolden

		// clean up the eid
		eid = cleanEid(eid);
		if (eid == null) return null;

		// do we have a record for this user? Note: there may be many records with the EID
		List<User> users = this.getUsersByEid(eid);
		UserEdit user = null;
		int count = 0;
		for (User u : users)
		{
			// count the ones with the matching password
			if (u.checkPassword(password))
			{
				count++;
				user = (UserEdit) u;
			}
		}

		// reject if we have multiple matches with an exception
		if (count > 1)
		{
			throw new AuthenticationMultipleException("invalid login");
		}

		// even if we found a user by eid, check for one by IID as well

		// next try by IID @ inst_code
		User iidUser = null;

		// isolate the IID
		String[] parts = splitLast(eid, "@");
		if (parts != null)
		{
			try
			{
				iidUser = (UserEdit) this.getUserByIid(parts[1], parts[0]);
			}
			catch (UserNotDefinedException e)
			{
			}
		}

		// next try by just IID
		if (iidUser == null)
		{
			try
			{
				iidUser = (UserEdit) this.getUserByIid(null, eid);
			}
			catch (UserNotDefinedException e)
			{
			}
		}

		// if we found by iid, check the password
		if (iidUser != null)
		{
			if (!iidUser.checkPassword(password))
			{
				// if this is not a match, ignore the found record
				iidUser = null;
			}
		}

		// user or iidUser may be set - if either are set, we found a user by eid or iid that matches the password. We authenticate if we find exactly one.

		// reject if no user records found
		if ((user == null) && (iidUser == null)) return null;

		// reject if multiple user records found with an exception
		if ((user != null) && (iidUser != null))
		{
			// if the user has the same iid and eid, we could find the same user twice - this is find
			if (user.equals(iidUser)) return user;

			throw new AuthenticationMultipleException("invalid login");
		}

		// return the one not null
		if (iidUser != null) return iidUser;
		return user;
	}

	/**
	 * {@inheritDoc}
	 */
	public User authenticateIid(String iid, String password, String instCode)
	{
		// Note: skipping all provider logic - works only with internally defined users -ggolden

		iid = StringUtil.trimToNull(iid);
		password = StringUtil.trimToNull(password);
		instCode = StringUtil.trimToNull(instCode);
		if (iid == null || password == null || instCode == null) return null;

		try
		{
			// do we have a record for this user?  Search by the iid
			User user = this.getUserByIid(instCode, iid);

			// check the password
			boolean authenticated = user.checkPassword(password);

			// if authenticated, get the user record to return - we might already have it
			User rv = null;
			if (authenticated)
			{
				rv = user;
				if (m_callCache != null) m_callCache.put(userReference(rv.getId()), rv, m_cacheSeconds);
			}

			return rv;
		}
		catch (UserNotDefinedException e)
		{
			return null;
		}
	}

	/**
	 * @inheritDoc
	 */
	public void destroyAuthentication()
	{
		// let the provider know
		if (m_provider != null)
		{
			m_provider.destroyAuthentication();
		}
	}

	/**
	 * Create the live properties for the user.
	 */
	protected void addLiveProperties(BaseUserEdit edit)
	{
		String current = sessionManager().getCurrentSessionUserId();

		edit.m_createdUserId = current;
		edit.m_lastModifiedUserId = current;

		Time now = timeService().newTime();
		edit.m_createdTime = now;
		edit.m_lastModifiedTime = (Time) now.clone();
	}

	/**
	 * Update the live properties for a user for when modified.
	 */
	protected void addLiveUpdateProperties(BaseUserEdit edit)
	{
		String current = sessionManager().getCurrentSessionUserId();

		edit.m_lastModifiedUserId = current;
		edit.m_lastModifiedTime = timeService().newTime();
	}

	/**
	 * Adjust the id - trim it to null. Note: eid case insensitive option does NOT apply to id.
	 * 
	 * @param id
	 *        The id to clean up.
	 * @return A cleaned up id.
	 */
	protected String cleanId(String id)
	{
		// if we are not doing separate id and eid, use the eid rules
		if (!m_separateIdEid) return cleanEid(id);

		return StringUtil.trimToNull(id);
	}

	/**
	 * Adjust the eid - trim it to null, and lower case IF we are case insensitive.
	 * 
	 * @param eid
	 *        The eid to clean up.
	 * @return A cleaned up eid.
	 */
	protected String cleanEid(String eid)
	{
		if (!m_caseSensitiveEid)
		{
			return StringUtil.trimToNullLower(eid);
		}

		return StringUtil.trimToNull(eid);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * EntityProducer implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public String getLabel()
	{
		return "user";
	}

	/**
	 * @inheritDoc
	 */
	public boolean willArchiveMerge()
	{
		return false;
	}

	/**
	 * @inheritDoc
	 */
	public HttpAccess getHttpAccess()
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		// for user access
		if (reference.startsWith(REFERENCE_ROOT))
		{
			String id = null;

			// we will get null, service, userId
			String[] parts = StringUtil.split(reference, Entity.SEPARATOR);

			if (parts.length > 2)
			{
				id = parts[2];
			}

			ref.set(APPLICATION_ID, null, id, null, null);

			return true;
		}

		return false;
	}

	/**
	 * @inheritDoc
	 */
	public String getEntityDescription(Reference ref)
	{
		// double check that it's mine
		if (APPLICATION_ID != ref.getType()) return null;

		String rv = "User: " + ref.getReference();

		try
		{
			User user = getUser(ref.getId());
			rv = "User: " + user.getDisplayName();
		}
		catch (UserNotDefinedException e)
		{
		}
		catch (NullPointerException e)
		{
		}

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Entity getEntity(Reference ref)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		// double check that it's mine
		if (APPLICATION_ID != ref.getType()) return null;

		Collection rv = new Vector();

		// for user access: user and template realms
		try
		{
			rv.add(userReference(ref.getId()));

			ref.addUserTemplateAuthzGroup(rv, userId);
		}
		catch (NullPointerException e)
		{
			M_log.warn("getEntityRealms(): " + e);
		}

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	public String getEntityUrl(Reference ref)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		return "";
	}

	/**
	 * @inheritDoc
	 */
	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport)
	{
		return "";
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * UserFactory implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public UserEdit newUser()
	{
		return new BaseUserEdit((String) null);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * UserEdit implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * <p>
	 * BaseUserEdit is an implementation of the UserEdit object.
	 * </p>
	 */
	public class BaseUserEdit implements UserEdit, SessionBindingListener
	{
		/** The event code for this edit. */
		protected String m_event = null;

		/** Active flag. */
		protected boolean m_active = false;

		/** The user id. */
		protected String m_id = null;

		/** The user eid. */
		protected String m_eid = null;

		/** The user first name. */
		protected String m_firstName = null;

		/** The user last name. */
		protected String m_lastName = null;

		/** The user email address. */
		protected String m_email = null;

		/** The user password. */
		protected String m_pw = null;

		/** The properties. */
		protected ResourcePropertiesEdit m_properties = null;

		/** The user type. */
		protected String m_type = null;

		/** The created user id. */
		protected String m_createdUserId = null;

		/** The last modified user id. */
		protected String m_lastModifiedUserId = null;

		/** The time created. */
		protected Time m_createdTime = null;

		/** The time last modified. */
		protected Time m_lastModifiedTime = null;
		
		/** If editing the first name is restricted **/ 
		protected boolean m_restrictedFirstName = false;
		
		/** If editing the last name is restricted **/ 
		protected boolean m_restrictedLastName = false;
		
		
		/** If editing the email is restricted **/ 
		protected boolean m_restrictedEmail = false;
		
		/** If editing the password is restricted **/ 
		protected boolean m_restrictedPassword = false;
		
		/** If editing the type is restricted **/ 
		protected boolean m_restrictedType = false;

		/**
		 * Construct.
		 * 
		 * @param id
		 *        The user id.
		 */
		public BaseUserEdit(String id)
		{
			m_id = id;

			// setup for properties
			ResourcePropertiesEdit props = new BaseResourcePropertiesEdit();
			m_properties = props;

			// if the id is not null (a new user, rather than a reconstruction)
			// and not the anon (id == "") user,
			// add the automatic (live) properties
			if ((m_id != null) && (m_id.length() > 0)) addLiveProperties(this);
		}

		/**
		 * Construct from another User object.
		 * 
		 * @param user
		 *        The user object to use for values.
		 */
		public BaseUserEdit(User user)
		{
			setAll(user);
		}

		/**
		 * Construct from information in XML.
		 * 
		 * @param el
		 *        The XML DOM Element definining the user.
		 */
		public BaseUserEdit(Element el)
		{
			// setup for properties
			m_properties = new BaseResourcePropertiesEdit();

			m_id = cleanId(el.getAttribute("id"));
			m_eid = cleanEid(el.getAttribute("eid"));
			m_firstName = StringUtil.trimToNull(el.getAttribute("first-name"));
			m_lastName = StringUtil.trimToNull(el.getAttribute("last-name"));
			setEmail(StringUtil.trimToNull(el.getAttribute("email")));
			m_pw = el.getAttribute("pw");
			m_type = StringUtil.trimToNull(el.getAttribute("type"));
			m_createdUserId = StringUtil.trimToNull(el.getAttribute("created-id"));
			m_lastModifiedUserId = StringUtil.trimToNull(el.getAttribute("modified-id"));

			String time = StringUtil.trimToNull(el.getAttribute("created-time"));
			if (time != null)
			{
				m_createdTime = timeService().newTimeGmt(time);
			}

			time = StringUtil.trimToNull(el.getAttribute("modified-time"));
			if (time != null)
			{
				m_lastModifiedTime = timeService().newTimeGmt(time);
			}

			// the children (roles, properties)
			NodeList children = el.getChildNodes();
			final int length = children.getLength();
			for (int i = 0; i < length; i++)
			{
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE) continue;
				Element element = (Element) child;

				// look for properties
				if (element.getTagName().equals("properties"))
				{
					// re-create properties
					m_properties = new BaseResourcePropertiesEdit(element);

					// pull out some properties into fields to convert old (pre 1.38) versions
					if (m_createdUserId == null)
					{
						m_createdUserId = m_properties.getProperty("CHEF:creator");
					}
					if (m_lastModifiedUserId == null)
					{
						m_lastModifiedUserId = m_properties.getProperty("CHEF:modifiedby");
					}
					if (m_createdTime == null)
					{
						try
						{
							m_createdTime = m_properties.getTimeProperty("DAV:creationdate");
						}
						catch (Exception ignore)
						{
						}
					}
					if (m_lastModifiedTime == null)
					{
						try
						{
							m_lastModifiedTime = m_properties.getTimeProperty("DAV:getlastmodified");
						}
						catch (Exception ignore)
						{
						}
					}
					m_properties.removeProperty("CHEF:creator");
					m_properties.removeProperty("CHEF:modifiedby");
					m_properties.removeProperty("DAV:creationdate");
					m_properties.removeProperty("DAV:getlastmodified");
				}
			}
		}

		/**
		 * ReConstruct.
		 * 
		 * @param id
		 *        The id.
		 * @param eid
		 *        The eid.
		 * @param email
		 *        The email.
		 * @param firstName
		 *        The first name.
		 * @param lastName
		 *        The last name.
		 * @param type
		 *        The type.
		 * @param pw
		 *        The password.
		 * @param createdBy
		 *        The createdBy property.
		 * @param createdOn
		 *        The createdOn property.
		 * @param modifiedBy
		 *        The modified by property.
		 * @param modifiedOn
		 *        The modified on property.
		 */
		public BaseUserEdit(String id, String eid, String email, String firstName, String lastName, String type, String pw, String createdBy,
				Time createdOn, String modifiedBy, Time modifiedOn)
		{
			m_id = id;
			m_eid = eid;
			m_firstName = firstName;
			m_lastName = lastName;
			m_type = type;
			setEmail(email);
			m_pw = pw;
			m_createdUserId = createdBy;
			m_lastModifiedUserId = modifiedBy;
			m_createdTime = createdOn;
			m_lastModifiedTime = modifiedOn;

			// setup for properties, but mark them lazy since we have not yet established them from data
			BaseResourcePropertiesEdit props = new BaseResourcePropertiesEdit();
			props.setLazy(true);
			m_properties = props;
		}

		/**
		 * Take all values from this object.
		 * 
		 * @param user
		 *        The user object to take values from.
		 */
		protected void setAll(User user)
		{
			m_id = user.getId();
			m_eid = user.getEid();
			m_firstName = user.getFirstName();
			m_lastName = user.getLastName();
			m_type = user.getType();
			setEmail(user.getEmail());
			m_pw = ((BaseUserEdit) user).m_pw;
			m_createdUserId = ((BaseUserEdit) user).m_createdUserId;
			m_lastModifiedUserId = ((BaseUserEdit) user).m_lastModifiedUserId;
			if (((BaseUserEdit) user).m_createdTime != null) m_createdTime = (Time) ((BaseUserEdit) user).m_createdTime.clone();
			if (((BaseUserEdit) user).m_lastModifiedTime != null)
				m_lastModifiedTime = (Time) ((BaseUserEdit) user).m_lastModifiedTime.clone();

			m_properties = new BaseResourcePropertiesEdit();
			m_properties.addAll(user.getProperties());
			((BaseResourcePropertiesEdit) m_properties).setLazy(((BaseResourceProperties) user.getProperties()).isLazy());
		}

		/**
		 * @inheritDoc
		 */
		public Element toXml(Document doc, Stack stack)
		{
			Element user = doc.createElement("user");

			if (stack.isEmpty())
			{
				doc.appendChild(user);
			}
			else
			{
				((Element) stack.peek()).appendChild(user);
			}

			stack.push(user);

			user.setAttribute("id", getId());
			user.setAttribute("eid", getEid());
			if (m_firstName != null) user.setAttribute("first-name", m_firstName);
			if (m_lastName != null) user.setAttribute("last-name", m_lastName);
			if (m_type != null) user.setAttribute("type", m_type);
			user.setAttribute("email", getEmail());
			user.setAttribute("pw", m_pw);
			user.setAttribute("created-id", m_createdUserId);
			user.setAttribute("modified-id", m_lastModifiedUserId);
			user.setAttribute("created-time", m_createdTime.toString());
			user.setAttribute("modified-time", m_lastModifiedTime.toString());

			// properties
			getProperties().toXml(doc, stack);

			stack.pop();

			return user;
		}

		/**
		 * @inheritDoc
		 */
		public String getId()
		{
			if (m_id == null) return "";
			return m_id;
		}

		/**
		 * @inheritDoc
		 */
		public String getEid()
		{
			if (m_eid == null) return "";
			return m_eid;
		}

		/**
		 * @inheritDoc
		 */
		public String getUrl()
		{
			return getAccessPoint(false) + m_id;
		}

		/**
		 * @inheritDoc
		 */
		public String getReference()
		{
			return userReference(m_id);
		}

		/**
		 * @inheritDoc
		 */
		public String getReference(String rootProperty)
		{
			return getReference();
		}

		/**
		 * @inheritDoc
		 */
		public String getUrl(String rootProperty)
		{
			return getUrl();
		}

		/**
		 * @inheritDoc
		 */
		public ResourceProperties getProperties()
		{
			// if lazy, resolve
			if (((BaseResourceProperties) m_properties).isLazy())
			{
				((BaseResourcePropertiesEdit) m_properties).setLazy(false);
				m_storage.readProperties(this, m_properties);
			}

			return m_properties;
		}

		/**
		 * @inheritDoc
		 */
		public User getCreatedBy()
		{
			try
			{
				return getUser(m_createdUserId);
			}
			catch (Exception e)
			{
				return getAnonymousUser();
			}
		}

		/**
		 * @inheritDoc
		 */
		public User getModifiedBy()
		{
			try
			{
				return getUser(m_lastModifiedUserId);
			}
			catch (Exception e)
			{
				return getAnonymousUser();
			}
		}

		/**
		 * @inheritDoc
		 */
		public Time getCreatedTime()
		{
			return m_createdTime;
		}

		/**
		 * @inheritDoc
		 */
		public Time getModifiedTime()
		{
			return m_lastModifiedTime;
		}

		/**
		 * @inheritDoc
		 */
		public String getDisplayName()
		{
			String rv = null;

			// let the provider handle it, if we have that sort of provider, and it wants to handle this
			if ((m_provider != null) && (m_provider instanceof DisplayAdvisorUDP))
			{
				rv = ((DisplayAdvisorUDP) m_provider).getDisplayName(this);
			}
			
			if (rv == null)
			{
				// or do it this way
				StringBuffer buf = new StringBuffer(128);
				if (m_firstName != null) buf.append(m_firstName);
				if (m_lastName != null)
				{
					buf.append(" ");
					buf.append(m_lastName);
				}
	
				if (buf.length() == 0)
				{
					rv = getEid();
				}
				
				else
				{
					rv = buf.toString();
				}
			}

			return rv;
		}

		/**
		 * @inheritDoc
		 */
		public String getDisplayId()
		{
			String rv = null;

			// let the provider handle it, if we have that sort of provider, and it wants to handle this
			if ((m_provider != null) && (m_provider instanceof DisplayAdvisorUDP))
			{
				rv = ((DisplayAdvisorUDP) m_provider).getDisplayId(this);
			}

			if (rv == null)
			{
				// use IID if set
				List<String> iids = getIid(getId());
				StringBuilder buf = new StringBuilder();
				for (String iid : iids)
				{
					buf.append(iid);
					buf.append(" ");
				}
				rv = StringUtil.trimToNull(buf.toString());
			}

			if (rv == null)
			{
				// use eid if no iid
				rv = getEid();
			}

			return StringUtil.trimToZero(rv);
		}

		/**
		 * @inheritDoc
		 */
		public String getFirstName()
		{
			if (m_firstName == null) return "";
			return m_firstName;
		}

		/**
		 * @inheritDoc
		 */
		public String getLastName()
		{
			if (m_lastName == null) return "";
			return m_lastName;
		}

		/**
		 * @inheritDoc
		 */
		public String getSortName()
		{
			StringBuffer buf = new StringBuffer(128);
			if (m_lastName != null) buf.append(m_lastName);
			if (m_firstName != null)
			{
				buf.append(", ");
				buf.append(m_firstName);
			}

			if (buf.length() == 0) return getEid();

			return buf.toString();
		}

		/**
		 * @inheritDoc
		 */
		public String getEmail()
		{
			if (m_email == null) return "";
			return m_email;
		}

		/**
		 * @inheritDoc
		 */
		public String getType()
		{
			return m_type;
		}

		/**
		 * @inheritDoc
		 */
		public String getIidDisplay()
		{
			List<String> iids = getIid(this.m_id);
			StringBuilder buf = new StringBuilder();
			for(String iid : iids)
			{
				buf.append(iid);
				buf.append(" ");
			}

			return StringUtil.trimToZero(buf.toString());
		}

		/**
		 * @inheritDoc
		 */
		public String getIidInContext(String context)
		{
			if (context == null) throw new IllegalArgumentException();

			// get the context's institution code (it may not have one)
			String institutionCode = siteService().getInstitutionCode(context);
			if (institutionCode != null)
			{
				// get the user's iids
				List<String> iids = getIid(this.m_id);
				for (String iid : iids)
				{
					// separate the iid@inst - iid may contain @
					String parts[] = splitLast(iid, "@");
					if ((parts != null) && (parts.length == 2))
					{
						if (parts[1].equalsIgnoreCase(institutionCode))
						{
							return parts[0];
						}
					}
				}
			}

			return null;
		}

		/**
		 * @inheritDoc
		 */
		public boolean checkPassword(String pw)
		{
			pw = StringUtil.trimToNull(pw);

			// if we have no password, or none is given, we fail
			if ((m_pw == null) || (pw == null)) return false;

			// if we have a truncated (i.e. pre SAK-5922) password, deal with it
			if (m_pw.length() == 20)
			{
				// encode this password truncated
				String encoded = OneWayHash.encode(pw, true);

				if (m_pw.equals(encoded)) return true;
			}
			
			// otherwise deal with the full encoding
			else
			{
				// encode this password
				String encoded = OneWayHash.encode(pw, false);

				if (m_pw.equals(encoded)) return true;
			}

			return false;
		}

		/**
		 * @inheritDoc
		 */
		public boolean equals(Object obj)
		{
			if (!(obj instanceof User)) return false;
			return ((User) obj).getId().equals(getId());
		}

		/**
		 * @inheritDoc
		 */
		public int hashCode()
		{
			return getId().hashCode();
		}

		/**
		 * @inheritDoc
		 */
		public int compareTo(Object obj)
		{
			if (!(obj instanceof User)) throw new ClassCastException();

			// if the object are the same, say so
			if (obj == this) return 0;

			// start the compare by comparing their sort names
			int compare = getSortName().compareTo(((User) obj).getSortName());

			// if these are the same
			if (compare == 0)
			{
				// sort based on (unique) eid
				compare = getEid().compareTo(((User) obj).getEid());
			}

			return compare;
		}

		/**
		 * Clean up.
		 */
		protected void finalize()
		{
			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancelEdit(this);
			}
		}

		/**
		 * @inheritDoc
		 */
		public void setId(String id)
		{
			// set once only!
			if (m_id == null)
			{
				m_id = id;
			}
		}

		/**
		 * @inheritDoc
		 */
		public void setEid(String eid)
		{
			m_eid = eid;
		}

		/**
		 * @inheritDoc
		 */
		public void setFirstName(String name)
		{
		    if(!m_restrictedFirstName) { 	
		    	m_firstName = name;	 
		    }
		}

		/**
		 * @inheritDoc
		 */
		public void setLastName(String name)
		{
			if(!m_restrictedLastName) { 
		    	m_lastName = name;
		    }
		}

		/**
		 * @inheritDoc
		 */
		public void setEmail(String email)
		{	
			if(!m_restrictedEmail) { 
				m_email = email;
			}
		}

		/**
		 * @inheritDoc
		 */
		public void setPassword(String pw)
		{
			
			if(!m_restrictedPassword) { 
		
				// to clear it
				if (pw == null)
				{
					m_pw = null;
				}
				
				// else encode the new one
				else
				{
					// encode this password
					String encoded = OneWayHash.encode(pw, false);
					m_pw = encoded;
				}
			}
		}

		/**
		 * @inheritDoc
		 */
		public void setType(String type)
		{
			if(!m_restrictedType) {
		
				m_type = type;
			
			}
		}
		
		public void restrictEditFirstName() {
			
			m_restrictedFirstName = true;
			
		}
		
		public void restrictEditLastName() {
			
			m_restrictedLastName = true;
			
		}
		
		
		
		public void restrictEditEmail() {
			
			m_restrictedEmail = true;
			
		}
		
		public void restrictEditPassword() {
			
			m_restrictedPassword = true;
			
		}
		
		public void restrictEditType() {
			
			m_restrictedType = true;
			
		}

		/**
		 * Take all values from this object.
		 * 
		 * @param user
		 *        The user object to take values from.
		 */
		protected void set(User user)
		{
			setAll(user);
		}

		/**
		 * Access the event code for this edit.
		 * 
		 * @return The event code for this edit.
		 */
		protected String getEvent()
		{
			return m_event;
		}

		/**
		 * Set the event code for this edit.
		 * 
		 * @param event
		 *        The event code for this edit.
		 */
		protected void setEvent(String event)
		{
			m_event = event;
		}

		/**
		 * @inheritDoc
		 */
		public ResourcePropertiesEdit getPropertiesEdit()
		{
			// if lazy, resolve
			if (((BaseResourceProperties) m_properties).isLazy())
			{
				((BaseResourcePropertiesEdit) m_properties).setLazy(false);
				m_storage.readProperties(this, m_properties);
			}

			return m_properties;
		}

		/**
		 * Enable editing.
		 */
		protected void activate()
		{
			m_active = true;
		}

		/**
		 * @inheritDoc
		 */
		public boolean isActiveEdit()
		{
			return m_active;
		}

		/**
		 * Close the edit object - it cannot be used after this.
		 */
		protected void closeEdit()
		{
			m_active = false;
		}

		/**
		 * Check this User object to see if it is selected by the criteria.
		 * 
		 * @param criteria
		 *        The critera.
		 * @return True if the User object is selected by the criteria, false if not.
		 */
		protected boolean selectedBy(String criteria)
		{
			if (StringUtil.containsIgnoreCase(getSortName(), criteria) || StringUtil.containsIgnoreCase(getDisplayName(), criteria)
					|| StringUtil.containsIgnoreCase(getEid(), criteria) || StringUtil.containsIgnoreCase(getEmail(), criteria))
			{
				return true;
			}

			return false;
		}

		/******************************************************************************************************************************************************************************************************************************************************
		 * SessionBindingListener implementation
		 *****************************************************************************************************************************************************************************************************************************************************/

		/**
		 * @inheritDoc
		 */
		public void valueBound(SessionBindingEvent event)
		{
		}

		/**
		 * @inheritDoc
		 */
		public void valueUnbound(SessionBindingEvent event)
		{
			if (M_log.isDebugEnabled()) M_log.debug("valueUnbound()");

			// catch the case where an edit was made but never resolved
			if (m_active)
			{
				cancelEdit(this);
			}
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected interface Storage
	{
		/**
		 * Open.
		 */
		public void open();

		/**
		 * Close.
		 */
		public void close();

		/**
		 * Check if a user by this id exists.
		 * 
		 * @param id
		 *        The user id.
		 * @return true if a user by this id exists, false if not.
		 */
		public boolean check(String id);

		/**
		 * Get the user with this id, or null if not found.
		 * 
		 * @param id
		 *        The user id.
		 * @return The user with this id, or null if not found.
		 */
		public UserEdit getById(String id);

//		/**
//		 * Get the user with this eid, or null if not found.
//		 * 
//		 * @param eid
//		 *        The user eid.
//		 * @return The user with this eid, or null if not found.
//		 */
//		public UserEdit getByEid(String eid);

		/**
		 * Get the user that has this institution id
		 * 
		 * @param institutionCode
		 *        The institution code.
		 * @param iid
		 *        The institution id.
		 * @return The user id for the user found, or null if not found.
		 */
		public User getByIid(String institutionCode, String iid);

		/**
		 * Get the users with this email, or return empty if none found.
		 * 
		 * @param id
		 *        The user email.
		 * @return The Collection (User) of users with this email, or an empty collection if none found.
		 */
		public Collection findUsersByEmail(String email);

		/**
		 * Get all users.
		 * 
		 * @return The List (UserEdit) of all users.
		 */
		public List getAll();

		/**
		 * Get all the users in record range.
		 * 
		 * @param first
		 *        The first record position to return.
		 * @param last
		 *        The last record position to return.
		 * @return The List (BaseUserEdit) of all users.
		 */
		public List getAll(int first, int last);

		/**
		 * Count all the users.
		 * 
		 * @return The count of all users.
		 */
		public int count();

		/**
		 * Search for users with id or email, first or last name matching criteria, in range.
		 * 
		 * @param criteria
		 *        The search criteria.
		 * @param first
		 *        The first record position to return.
		 * @param last
		 *        The last record position to return.
		 * @return The List (BaseUserEdit) of all alias.
		 */
		public List search(String criteria, int first, int last);

		/**
		 * Count all the users with id or email, first or last name matching criteria.
		 * 
		 * @param criteria
		 *        The search criteria.
		 * @return The count of all aliases with id or target matching criteria.
		 */
		public int countSearch(String criteria);

		/**
		 * Add a new user with this id and eid.
		 * 
		 * @param id
		 *        The user id.
		 * @param eid
		 *        The user eid.
		 * @return The locked User object with this id and eid, or null if the id is in use.
		 */
		public UserEdit put(String id, String eid);

		/**
		 * Get a lock on the user with this id, or null if a lock cannot be gotten.
		 * 
		 * @param id
		 *        The user id.
		 * @return The locked User with this id, or null if this records cannot be locked.
		 */
		public UserEdit edit(String id);

		/**
		 * Commit the changes and release the lock.
		 * 
		 * @param user
		 *        The user to commit.
		 * @return true if successful, false if not (eid may not be unique).
		 */
		public boolean commit(UserEdit user);

		/**
		 * Cancel the changes and release the lock.
		 * 
		 * @param user
		 *        The user to commit.
		 */
		public void cancel(UserEdit user);

		/**
		 * Remove this user.
		 * 
		 * @param user
		 *        The user to remove.
		 */
		public void remove(UserEdit user);

		/**
		 * Read properties from storage into the edit's properties.
		 * 
		 * @param edit
		 *        The user to read properties for.
		 */
		public void readProperties(UserEdit edit, ResourcePropertiesEdit props);

		/**
		 * Create a mapping between the id and eid.
		 * 
		 * @param id
		 *        The user id.
		 * @param eid
		 *        The user eid.
		 * @return true if successful, false if not (id or eid might be in use).
		 */
		public boolean putMap(String id, String eid);

		/**
		 * Check the id -> eid mapping: lookup this id and return the eid if found
		 * 
		 * @param id
		 *        The user id to lookup.
		 * @return The eid mapped to this id, or null if none.
		 */
		public String checkMapForEid(String id);

		/**
		 * Check the id -> eid mapping: lookup this eid and return the id if found
		 * 
		 * @param eid
		 *        The user eid to lookup.
		 * @return The id mapped to this eid, or null if none.
		 */
		public String checkMapForId(String eid);

		/**
		 * Check the id -> eid mapping: lookup this eid and return all of the ids found.
		 * 
		 * @param eid
		 *        The user eid to lookup.
		 * @return The ids mapped to this eid, or empty if none.
		 */
		public List<String> checkMapForMultipleIds(String eid);

		/**
		 * Check if the user with this id has one or more IIDs defined.
		 * 
		 * @param id
		 *        The user id.
		 * @return true if the user has some IIDs, false if not.
		 */
		public boolean hasIid(String id);
		
		/**
		 * Find the user id that has this IID, with any institution code, if there is only one (i.e. if the iid is unique across institutions).
		 * 
		 * @param iid
		 *        The user IID.
		 * @return The id mapped to this IID, or null if none.
		 */
		public String getUserIdForIid(String iid);

		/**
		 * Find the user id that has this IID
		 * 
		 * @param institutionCode
		 *        The institution code.
		 * @param iid
		 *        The user IID.
		 * @return The id mapped to this IID, or null if none.
		 */
		public String getUserIdForIid(String institutionCode, String iid);
		
		/**
		 * Set this user's IID information. If one already exists, add this.
		 * 
		 * @param id
		 *        The user internal id.
		 * @param iid
		 *        The IID.
		 * @param institutionCode
		 *        The institutionCode.
		 */
		public void setIid(String id, String institutionCode, String iid);

		/**
		 * Remove the set of IID information for this user.
		 * 
		 * @param id
		 *        The user internal id.
		 */
		public void clearIid(String id);

		/**
		 * Get any IIDs set for this user.
		 * 
		 * @param id
		 *        The user id.
		 * @return A list of strings, each of the form instCode@userId.
		 */
		public List<String> getIid(String id);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * StorageUser implementation (no container)
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public Entity newContainer(String ref)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Entity newContainer(Element element)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Entity newContainer(Entity other)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Entity newResource(Entity container, String id, Object[] others)
	{
		return new BaseUserEdit(id);
	}

	/**
	 * @inheritDoc
	 */
	public Entity newResource(Entity container, Element element)
	{
		return new BaseUserEdit(element);
	}

	/**
	 * @inheritDoc
	 */
	public Entity newResource(Entity container, Entity other)
	{
		return new BaseUserEdit((User) other);
	}

	/**
	 * @inheritDoc
	 */
	public Edit newContainerEdit(String ref)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Edit newContainerEdit(Element element)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Edit newContainerEdit(Entity other)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Edit newResourceEdit(Entity container, String id, Object[] others)
	{
		BaseUserEdit e = new BaseUserEdit(id);
		e.activate();
		return e;
	}

	/**
	 * @inheritDoc
	 */
	public Edit newResourceEdit(Entity container, Element element)
	{
		BaseUserEdit e = new BaseUserEdit(element);
		e.activate();
		return e;
	}

	/**
	 * @inheritDoc
	 */
	public Edit newResourceEdit(Entity container, Entity other)
	{
		BaseUserEdit e = new BaseUserEdit((User) other);
		e.activate();
		return e;
	}

	/**
	 * @inheritDoc
	 */
	public Object[] storageFields(Entity r)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public boolean isDraft(Entity r)
	{
		return false;
	}

	/**
	 * @inheritDoc
	 */
	public String getOwnerId(Entity r)
	{
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Time getDate(Entity r)
	{
		return null;
	}
}
