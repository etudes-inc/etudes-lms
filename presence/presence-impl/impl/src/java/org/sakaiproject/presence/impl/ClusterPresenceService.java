/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/presence/presence-impl/impl/src/java/org/sakaiproject/presence/impl/ClusterPresenceService.java $
 * $Id: ClusterPresenceService.java 8184 2014-06-07 22:05:39Z ggolden $
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;

/**
 * <p>
 * ClusterPresenceService extends the BasePresenceService with a Storage model that keeps track of presence for a cluster of Sakai app servers, backed by a shared DB table.
 * </p>
 */
public class ClusterPresenceService extends BasePresenceService
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(ClusterPresenceService.class);

	/**
	 * Allocate a new storage object.
	 * 
	 * @return A new storage object.
	 */
	protected Storage newStorage()
	{
		return new ClusterStorage();
	}

	/** Dependency: SqlService */
	protected SqlService m_sqlService = null;

	/**
	 * Dependency: SqlService.
	 * 
	 * @param service
	 *        The SqlService.
	 */
	public void setSqlService(SqlService service)
	{
		m_sqlService = service;
	}

	/** Configuration: to run the ddl on init or not. */
	protected boolean m_autoDdl = false;

	/**
	 * Configuration: to run the ddl on init or not.
	 * 
	 * @param value
	 *        the auto ddl value.
	 */
	public void setAutoDdl(String value)
	{
		m_autoDdl = new Boolean(value).booleanValue();
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
			// if we are auto-creating our schema, check and create
			if (m_autoDdl)
			{
				m_sqlService.ddl(this.getClass().getClassLoader(), "sakai_presence");
			}

			super.init();
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage
	 *********************************************************************************************************************************************************************************************************************************************************/

	protected class ClusterStorage implements Storage
	{
		/**
		 * {@inheritDoc}
		 */
		public Map<String, Integer> getLocationCounts()
		{
			final Map<String, Integer> rv = new HashMap<String, Integer>();

			// Note: this counts sessions per location - we want USERS per location - if a user has two or more sessions going in the same location, we want a count of just 1
			// String sql = "SELECT LOCATION_ID, COUNT(1) FROM SAKAI_PRESENCE GROUP BY LOCATION_ID";
			String sql = "SELECT X.LOCATION_ID, COUNT(1) FROM ( SELECT DISTINCT P.LOCATION_ID, S.SESSION_USER FROM SAKAI_PRESENCE P JOIN SAKAI_SESSION S ON P.SESSION_ID = S.SESSION_ID ) X GROUP BY X.LOCATION_ID";

			m_sqlService.dbRead(sql, null, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						String locationId = m_sqlService.readString(result, 1);
						Integer count = m_sqlService.readInteger(result, 2);
						
						rv.put(locationId, count);

						return null;
					}
					catch (SQLException e)
					{
						M_log.warn("getLocationCounts: " + e);
						return null;
					}
				}
			});

			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public void setPresence(String sessionId, String locationId)
		{
			// send this to the database
			String statement = "insert into SAKAI_PRESENCE (SESSION_ID,LOCATION_ID) values ( ?, ?)";

			// collect the fields
			Object fields[] = new Object[2];
			fields[0] = sessionId;
			fields[1] = locationId;

			// process the insert
			boolean ok = m_sqlService.dbWrite(statement, fields);
			if (!ok)
			{
				M_log.warn("setPresence(): dbWrite failed");
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void removePresence(String sessionId, String locationId)
		{
			// form the SQL delete statement
			String statement = "delete from SAKAI_PRESENCE" + " where ( SESSION_ID = ? and LOCATION_ID = ?)";

			// setup the fields
			Object[] fields = new Object[1];
			fields[0] = sessionId;

			// process the remove
			boolean ok = m_sqlService.dbWrite(statement, fields, locationId);
			if (!ok)
			{
				M_log.warn("removePresence(): dbWrite failed");
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public List getSessions(String locationId)
		{
			// Note: this assumes
			// 1) the UsageSessionService has a db component selected.
			// 2) the presence table and the session table are in the same db.

			// to join the presence table to the session
			String joinTable = "SAKAI_PRESENCE";
			String joinAlias = "A";
			String joinColumn = "SESSION_ID";
			String joinCriteria = "A.LOCATION_ID = ?";

			// send in the locationId
			Object[] fields = new Object[1];
			fields[0] = locationId;

			// get these from usage session
			List sessions = m_usageSessionService.getSessions(joinTable, joinAlias, joinColumn, joinCriteria, fields);

			return sessions;
		}

		/**
		 * {@inheritDoc}
		 */
		public List getSessionsPartialId(String locationId)
		{
			// Note: this assumes
			// 1) the UsageSessionService has a db component selected.
			// 2) the presence table and the session table are in the same db.

			// to join the presence table to the session
			String joinTable = "SAKAI_PRESENCE";
			String joinAlias = "A";
			String joinColumn = "SESSION_ID";
			String joinCriteria = "A.LOCATION_ID LIKE ?";

			// send in the locationId
			Object[] fields = new Object[1];
			fields[0] = locationId + "%";

			// get these from usage session
			List sessions = m_usageSessionService.getSessions(joinTable, joinAlias, joinColumn, joinCriteria, fields);

			return sessions;
		}

		/**
		 * {@inheritDoc}
		 */
		public List getLocations()
		{
			// form the SQL query
			String statement = "select DISTINCT LOCATION_ID from SAKAI_PRESENCE";

			List locs = m_sqlService.dbRead(statement);

			return locs;
		}
	}
}
