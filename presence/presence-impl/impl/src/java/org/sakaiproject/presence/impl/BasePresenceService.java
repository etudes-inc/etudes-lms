/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/presence/presence-impl/impl/src/java/org/sakaiproject/presence/impl/BasePresenceService.java $
 * $Id: BasePresenceService.java 8184 2014-06-07 22:05:39Z ggolden $
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

package org.sakaiproject.presence.impl;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.api.UsageSessionService;
import org.sakaiproject.presence.api.PresenceService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * <p>
 * Implements the PresenceService, all but a Storage model.
 * </p>
 */
public abstract class BasePresenceService implements PresenceService, Runnable
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(BasePresenceService.class);

	/** SessionState key. */
	protected final static String SESSION_KEY = "sakai.presence.service";

	/** Storage. */
	protected Storage m_storage = null;

	/**
	 * Allocate a new storage object.
	 * 
	 * @return A new storage object.
	 */
	protected abstract Storage newStorage();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Constructors, Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Dependency: SessionManager */
	protected SessionManager m_sessionManager = null;

	/**
	 * Dependency: SessionManager.
	 * 
	 * @param service
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager service)
	{
		m_sessionManager = service;
	}

	/** Dependency: UsageSessionService */
	protected UsageSessionService m_usageSessionService = null;

	/**
	 * Dependency: UsageSessionService.
	 * 
	 * @param service
	 *        The UsageSessionService.
	 */
	public void setUsageSessionService(UsageSessionService service)
	{
		m_usageSessionService = service;
	}

	/** Dependency: UserDirectoryService */
	protected UserDirectoryService m_userDirectoryService = null;

	/**
	 * Dependency: UserDirectoryService.
	 * 
	 * @param service
	 *        The UserDirectoryService.
	 */
	public void setUserDirectoryService(UserDirectoryService service)
	{
		m_userDirectoryService = service;
	}

	/** Dependency: EventTrackingService */
	protected EventTrackingService m_eventTrackingService = null;

	/**
	 * Dependency: EventTrackingService.
	 * 
	 * @param service
	 *        The EventTrackingService.
	 */
	public void setEventTrackingService(EventTrackingService service)
	{
		m_eventTrackingService = service;
	}

	/** Configuration: milliseconds till a non-refreshed presence entry times out. */
	protected int m_timeout = 60000;

	/**
	 * Configuration: SECONDS till a non-refreshed presence entry times out.
	 * 
	 * @param value
	 *        timeout seconds.
	 */
	public void setTimeoutSeconds(String value)
	{
		try
		{
			m_timeout = Integer.parseInt(value) * 1000;
		}
		catch (Exception ignore)
		{
		}
	}

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
			// storage
			m_storage = newStorage();

			start();

			M_log.info("init()");
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		// stop the maintenance thread
		stop();

		m_storage = null;
		M_log.info("destroy()");
	}

	/** The maintenance thread quit flag. */
	protected boolean threadStop = false;

	/** How long to wait (ms) between maintenance thread runs. */
	protected long maintencePeriod = 1000L * 60;

	/** The maintenance thread. */
	protected Thread maintenanceThread = null;

	protected Map<String, Integer> presenceCounts = new HashMap<String, Integer>();

	/**
	 * Start the clean and report thread.
	 */
	protected void start()
	{
		this.threadStop = false;

		this.maintenanceThread = new Thread(this, getClass().getName());
		this.maintenanceThread.start();
	}

	/**
	 * Stop the clean and report thread.
	 */
	protected void stop()
	{
		if (this.maintenanceThread == null) return;

		// signal the thread to stop
		this.threadStop = true;

		// wake up the thread
		this.maintenanceThread.interrupt();

		this.maintenanceThread = null;
	}

	/**
	 * Run the expiration checking thread.
	 */
	public void run()
	{
		// since we might be running while the component manager is still being created and populated,
		// such as at server startup, wait here for a complete component manager
		ComponentManager.waitTillConfigured();

		// loop till told to stop
		while ((!threadStop) && (!Thread.currentThread().isInterrupted()))
		{
			try
			{
				// get a new map of locations
				Map<String, Integer> newCounts = m_storage.getLocationCounts();

				// swap
				this.presenceCounts = newCounts;
			}
			catch (Throwable e)
			{
				M_log.warn("run: will continue: ", e);
			}
			finally
			{
				// clear out any current current bindings
				// this.threadLocalManager.clear();
			}

			// take a small nap
			try
			{
				Thread.sleep(this.maintencePeriod);
			}
			catch (Exception ignore)
			{
			}
		}
	}
	
	/**********************************************************************************************************************************************************************************************************************************************************
	 * PresenceService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public String presenceReference(String id)
	{
		return REFERENCE_ROOT + Entity.SEPARATOR + id;

	} // presenceReference

	/**
	 * {@inheritDoc}
	 */
	protected String presenceId(String ref)
	{
		String start = presenceReference("");
		int i = ref.indexOf(start);
		if (i == -1) return ref;
		String id = ref.substring(i + start.length());
		return id;

	} // presenceId

	/**
	 * {@inheritDoc}
	 */
	public void setPresence(String locationId)
	{
		if (locationId == null) return;

		if (!checkPresence(locationId, true))
		{
			// presence relates a usage session (the current one) with a location
			UsageSession curSession = m_usageSessionService.getSession();
			if (curSession == null) return;

			// update the storage
			m_storage.setPresence(curSession.getId(), locationId);

			// generate the event
			Event event = m_eventTrackingService.newEvent(EVENT_PRESENCE, presenceReference(locationId), true);
			m_eventTrackingService.post(event, curSession);

			// create a presence for tracking

			// bind a presence tracking object to the sakai session for auto-cleanup when logout or inactivity invalidates the sakai session
			Session session = m_sessionManager.getCurrentSession();
			ToolSession ts = session.getToolSession(SESSION_KEY);
			Presence p = new Presence(curSession, locationId);
			ts.setAttribute(locationId, p);
		}

		// retire any expired presence
		checkPresenceForExpiration();

	} // setPresence

	/**
	 * {@inheritDoc}
	 */
	public void removePresence(String locationId)
	{
		if (locationId == null) return;

		if (checkPresence(locationId, false))
		{
			UsageSession curSession = m_usageSessionService.getSession();

			// tell maintenance
			m_storage.removePresence(curSession.getId(), locationId);

			// generate the event
			Event event = m_eventTrackingService.newEvent(EVENT_ABSENCE, presenceReference(locationId), true);
			m_eventTrackingService.post(event, curSession);

			// remove from state
			Session session = m_sessionManager.getCurrentSession();
			ToolSession ts = session.getToolSession(SESSION_KEY);
			Presence p = (Presence) ts.getAttribute(locationId);
			if (p != null)
			{
				p.deactivate();
				ts.removeAttribute(locationId);
			}
		}

	} // removePresence

	/**
	 * {@inheritDoc}
	 */
	public List getPresence(String locationId)
	{
		// get the sessions at this location
		List sessions = m_storage.getSessions(locationId);

		// sort
		Collections.sort(sessions);

		return sessions;

	} // getPresence

	/**
	 * {@inheritDoc}
	 */
	public Integer countPresence(String locationId)
	{
		// read from out last read map
		Integer rv = this.presenceCounts.get(locationId);

		// 0 if missing
		if (rv == null) rv = Integer.valueOf(0);

		return rv;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getPresentUsersPartialId(String locationId)
	{
		// get the sessions that have location ids that start with locationId
		List sessions = m_storage.getSessionsPartialId(locationId);

		// form a list of user ids
		List userIds = new Vector();
		for (Iterator i = sessions.iterator(); i.hasNext();)
		{
			UsageSession s = (UsageSession) i.next();

			if (!userIds.contains(s.getUserId()))
			{
				userIds.add(s.getUserId());
			}
		}

		// get the users for these ids
		List users = m_userDirectoryService.getUsers(userIds);

		// sort
		Collections.sort(users);

		return users;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getPresentUsers(String locationId)
	{
		// get the sessions
		List sessions = m_storage.getSessions(locationId);

		// form a list of user ids
		List userIds = new Vector();
		for (Iterator i = sessions.iterator(); i.hasNext();)
		{
			UsageSession s = (UsageSession) i.next();

			if (!userIds.contains(s.getUserId()))
			{
				userIds.add(s.getUserId());
			}
		}

		// get the users for these ids
		List users = m_userDirectoryService.getUsers(userIds);

		// sort
		Collections.sort(users);

		return users;
	}

	/**
	 * {@inheritDoc}
	 */
	public List getLocations()
	{
		List locations = m_storage.getLocations();

		Collections.sort(locations);

		return locations;

	} // getLocations

	/**
	 * {@inheritDoc}
	 */
	public String locationId(String site, String page, String tool)
	{
		// TODO: remove
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLocationDescription(String location)
	{
		// TODO: get a description for a placement!
		return "location: " + location;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getTimeout()
	{
		return m_timeout / 1000;
	}

	/**
	 * Check if the current session is present at the location - optionally refreshing it
	 * 
	 * @param locationId
	 *        The location to check.
	 * @param refresh
	 *        If true, refresh the timeout on the presence if found
	 * @return True if the current session is present at that location, false if not.
	 */
	protected boolean checkPresence(String locationId, boolean refresh)
	{
		Session session = m_sessionManager.getCurrentSession();
		ToolSession ts = session.getToolSession(SESSION_KEY);
		Presence p = (Presence) ts.getAttribute(locationId);

		if ((p != null) && refresh)
		{
			p.setActive();
		}

		return (p != null);
	}

	/**
	 * Check current session presences and remove any expired ones
	 */
	public void checkPresenceForExpiration()
	{
		Session session = m_sessionManager.getCurrentSession();
		ToolSession ts = session.getToolSession(SESSION_KEY);
		Enumeration locations = ts.getAttributeNames();
		while (locations.hasMoreElements())
		{
			String location = (String) locations.nextElement();

			Presence p = (Presence) ts.getAttribute(location);
			if (p.isExpired())
			{
				ts.removeAttribute(location);
			}
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected interface Storage
	{
		/**
		 * Add this session id's presence at this location, if not already there.
		 * 
		 * @param sessionId
		 *        The session id.
		 * @param locationId
		 *        The location id.
		 */
		void setPresence(String sessionId, String locationId);

		/**
		 * Remove this sessions id's presence at this location.
		 * 
		 * @param sessionId
		 *        The session id.
		 * @param locationId
		 *        The location id.
		 */
		void removePresence(String sessionId, String locationId);

		/**
		 * Access the List of UsageSessions present at this location.
		 * 
		 * @param locationId
		 *        The location id.
		 * @return The List of sessions (UsageSession) present at this location.
		 */
		List getSessions(String locationId);

		/**
		 * Access the List of UsageSessions present at the locations that start with this location id.
		 * 
		 * @param locationId
		 *        The location id root.
		 * @return The List of sessions (UsageSession) present at these locations.
		 */
		List getSessionsPartialId(String locationId);

		/**
		 * Access the List of all known location ids.
		 * 
		 * @return The List (String) of all known locations.
		 */
		List getLocations();
		
		/**
		 * @return The current counts of sessions at each location, mapped by location id.
		 */
		Map<String, Integer> getLocationCounts();
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Presence
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected class Presence implements SessionBindingListener
	{
		/** The session. */
		protected UsageSession m_session = null;

		/** The location id. */
		protected String m_locationId = null;

		/** If true, process the unbound. */
		protected boolean m_active = true;

		/** Time to expire. */
		protected long m_expireTime = 0;

		public Presence(UsageSession session, String locationId)
		{
			m_session = session;
			m_locationId = locationId;
			m_expireTime = System.currentTimeMillis() + m_timeout;
		}

		public void deactivate()
		{
			m_active = false;
		}

		/**
		 * Reset the timeout based on current activity
		 */
		public void setActive()
		{
			m_expireTime = System.currentTimeMillis() + m_timeout;
		}

		/**
		 * Has this presence timed out?
		 * 
		 * @return true if expired, false if not.
		 */
		public boolean isExpired()
		{
			return System.currentTimeMillis() > m_expireTime;
		}

		/**
		 * {@inheritDoc}
		 */
		public void valueBound(SessionBindingEvent event)
		{
		}

		/**
		 * {@inheritDoc}
		 */
		public void valueUnbound(SessionBindingEvent evt)
		{
			if (m_active)
			{
				m_storage.removePresence(m_session.getId(), m_locationId);

				// generate the event
				Event event = m_eventTrackingService.newEvent(EVENT_ABSENCE, presenceReference(m_locationId), true);
				m_eventTrackingService.post(event, m_session);
			}
		}
	}
}
