/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/user/user-impl/impl/src/java/org/sakaiproject/user/impl/DbUserService.java $
 * $Id: DbUserService.java 5778 2013-08-30 23:24:47Z ggolden $
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.BaseDbFlatStorage;
import org.sakaiproject.util.StorageUser;
import org.sakaiproject.util.StringUtil;

/**
 * <p>
 * DbCachedUserService is an extension of the BaseUserService with a database storage backed up by an in-memory cache.
 * </p>
 */
public abstract class DbUserService extends BaseUserDirectoryService
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(DbUserService.class);

	/** Table name for users. */
	protected String m_tableName = "SAKAI_USER";

	/** Table name for properties. */
	protected String m_propTableName = "SAKAI_USER_PROPERTY";

	/** ID field. */
	protected String m_idFieldName = "USER_ID";

	/** SORT field 1. */
	protected String m_sortField1 = "LAST_NAME";

	/** SORT field 2. */
	protected String m_sortField2 = "FIRST_NAME";

	/** All fields. */
	protected String[] m_fieldNames = { "USER_ID", "EMAIL", "EMAIL_LC", "FIRST_NAME", "LAST_NAME", "TYPE", "PW", "CREATEDBY",
			"MODIFIEDBY", "CREATEDON", "MODIFIEDON" };

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the MemoryService collaborator.
	 */
	protected abstract SqlService sqlService();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Configuration
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Configuration: set the table name
	 * 
	 * @param path
	 *        The table name.
	 */
	public void setTableName(String name)
	{
		m_tableName = name;
	}

	/** If true, we do our locks in the remote database, otherwise we do them here. */
	protected boolean m_useExternalLocks = true;

	/**
	 * Configuration: set the external locks value.
	 * 
	 * @param value
	 *        The external locks value.
	 */
	public void setExternalLocks(String value)
	{
		m_useExternalLocks = new Boolean(value).booleanValue();
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
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_user");

				// load the 2.1.0.004 email_lc conversion
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_user_2_1_0_004");

				// load the 2.1.0 postmaster password conversion
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_user_2_1_0");

				// load the 2.2 id-eid map table conversion
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_user_2_2_map");
			}

			super.init();

			M_log.info("init(): table: " + m_tableName + " external locks: " + m_useExternalLocks);

		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * BaseUserService extensions
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct a Storage object.
	 * 
	 * @return The new storage object.
	 */
	protected Storage newStorage()
	{
		return new DbStorage(this);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Storage implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Covers for the BaseXmlFileStorage, providing User and UserEdit parameters
	 */
	protected class DbStorage extends BaseDbFlatStorage implements Storage, SqlReader
	{
		/** A prior version's storage model. */
		protected Storage m_oldStorage = null;

		/**
		 * Construct.
		 * 
		 * @param user
		 *        The StorageUser class to call back for creation of Resource and Edit objects.
		 */
		public DbStorage(StorageUser user)
		{
			super(m_tableName, m_idFieldName, m_fieldNames, m_propTableName, m_useExternalLocks, null, sqlService());
			setSortField(m_sortField1, m_sortField2);

			m_reader = this;
		}

		public boolean check(String id)
		{
			boolean rv = super.checkResource(id);

			return rv;
		}

		public UserEdit getById(String id)
		{
			UserEdit rv = (UserEdit) super.getResource(id);

			return rv;
		}

//		public UserEdit getByEid(String eid)
//		{
//			// find id from mapping - if not found, we don't have the record
//			String id = checkMapForId(eid);
//			if (id == null) return null;
//
//			UserEdit rv = (UserEdit) super.getResource(id);
//
//			return rv;
//		}

		public List getAll()
		{
			// let the db do range selection
			List all = super.getAllResources();
			return all;
		}

		public List getAll(int first, int last)
		{
			// let the db do range selection
			List all = super.getAllResources(first, last);
			return all;
		}

		public int count()
		{
			return super.countAllResources();
		}

		public UserEdit put(String id, String eid)
		{
			// check for already exists
			if (check(id)) return null;

			// assure mapping
			if (!putMap(id, eid)) return null;

			BaseUserEdit rv = (BaseUserEdit) super.putResource(id, fields(id, null, false));
			if (rv != null) rv.activate();
			return rv;
		}

		public UserEdit edit(String id)
		{
			BaseUserEdit rv = (BaseUserEdit) super.editResource(id);

			if (rv != null) rv.activate();
			return rv;
		}

		public boolean commit(UserEdit edit)
		{
			// update the mapping - fail if that does not succeed
			if (!updateMap(edit.getId(), edit.getEid())) return false;

			super.commitResource(edit, fields(edit.getId(), edit, true), edit.getProperties());
			return true;
		}

		public void cancel(UserEdit edit)
		{
			super.cancelResource(edit);
		}

		public void remove(UserEdit edit)
		{
			unMap(edit.getId());
			removeIid(edit.getId());
			super.removeResource(edit);
		}

		public List search(String criteria, int first, int last)
		{
			String search = "%" + criteria + "%";
			Object[] fields = new Object[4];
			fields[0] = criteria;
			fields[1] = search;
			fields[2] = search;
			fields[3] = search;

			String sql = "select " + fieldList(m_resourceTableReadFields, null) + " from " + m_resourceTableName
					+ " where SAKAI_USER.USER_ID = ? OR EMAIL_LC LIKE ? OR FIRST_NAME LIKE ? OR LAST_NAME LIKE ?"
					+ " order by LAST_NAME ASC";

			List all = m_sql.dbRead(sql, fields, m_reader);

			return all;
		}

		public int countSearch(String criteria)
		{
			String search = "%" + criteria + "%";
			Object[] fields = new Object[4];
			fields[0] = criteria;
			fields[1] = search;
			fields[2] = search;
			fields[3] = search;

			String sql = "select count(SAKAI_USER.USER_ID) from "
					+ m_resourceTableName
					+ " where SAKAI_USER.USER_ID = ? OR EMAIL_LC LIKE ? OR FIRST_NAME LIKE ? OR LAST_NAME LIKE ?";

			List results = m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						int count = result.getInt(1);
						return new Integer(count);
					}
					catch (SQLException ignore)
					{
						return null;
					}
				}
			});

			if (results.isEmpty()) return 0;

			return ((Integer) results.get(0)).intValue();
		}

		/**
		 * {@inheritDoc}
		 */
		public Collection findUsersByEmail(String email)
		{
			Collection rv = new Vector();

			// search for it
			Object[] fields = new Object[1];
			fields[0] = email.toLowerCase();
			List users = super.getSelectedResources("EMAIL_LC = ?", fields);
			if (users != null)
			{
				rv.addAll(users);
			}

			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public User getByIid(String institutionCode, String iid)
		{
			Collection rv = new Vector();

			// search for it
			Object[] fields = new Object[2];
			fields[0] = institutionCode;
			fields[1] = iid;
			List users = super.getSelectedResources("INST_CODE = ? AND IID = ?", fields);
			if ((users != null) && (users.size() == 1))
			{
				return (User) users.get(0);
			}
			
			return null;
		}

		/**
		 * Read properties from storage into the edit's properties.
		 * 
		 * @param edit
		 *        The user to read properties for.
		 */
		public void readProperties(UserEdit edit, ResourcePropertiesEdit props)
		{
			super.readProperties(edit, props);
		}

		/**
		 * Get the fields for the database from the edit for this id, and the id again at the end if needed
		 * 
		 * @param id
		 *        The resource id
		 * @param edit
		 *        The edit (may be null in a new)
		 * @param idAgain
		 *        If true, include the id field again at the end, else don't.
		 * @return The fields for the database.
		 */
		protected Object[] fields(String id, UserEdit edit, boolean idAgain)
		{
			Object[] rv = new Object[idAgain ? 12 : 11];
			rv[0] = caseId(id);
			if (idAgain)
			{
				rv[11] = rv[0];
			}

			if (edit == null)
			{
				String attribUser = sessionManager().getCurrentSessionUserId();

				// if no current user, since we are working up a new user record, use the user id as creator...
				if ((attribUser == null) || (attribUser.length() == 0)) attribUser = (String) rv[0];

				Time now = timeService().newTime();
				rv[1] = "";
				rv[2] = "";
				rv[3] = "";
				rv[4] = "";
				rv[5] = "";
				rv[6] = "";
				rv[7] = attribUser;
				rv[8] = attribUser;
				rv[9] = now;
				rv[10] = now;
			}

			else
			{
				rv[1] = StringUtil.trimToZero(edit.getEmail());
				rv[2] = StringUtil.trimToZero(edit.getEmail().toLowerCase());
				rv[3] = StringUtil.trimToZero(edit.getFirstName());
				rv[4] = StringUtil.trimToZero(edit.getLastName());
				rv[5] = StringUtil.trimToZero(edit.getType());
				rv[6] = StringUtil.trimToZero(((BaseUserEdit) edit).m_pw);

				// for creator and modified by, if null, make it the id
				rv[7] = StringUtil.trimToNull(((BaseUserEdit) edit).m_createdUserId);
				if (rv[7] == null)
				{
					rv[7] = rv[0];
				}
				rv[8] = StringUtil.trimToNull(((BaseUserEdit) edit).m_lastModifiedUserId);
				if (rv[8] == null)
				{
					rv[8] = rv[0];
				}

				rv[9] = edit.getCreatedTime();
				rv[10] = edit.getModifiedTime();
			}

			return rv;
		}

		/**
		 * Read from the result one set of fields to create a Resource.
		 * 
		 * @param result
		 *        The Sql query result.
		 * @return The Resource object.
		 */
		public Object readSqlResultRecord(ResultSet result)
		{
			try
			{
				String id = result.getString(1);
				String email = result.getString(2);
				String email_lc = result.getString(3);
				String firstName = result.getString(4);
				String lastName = result.getString(5);
				String type = result.getString(6);
				String pw = result.getString(7);
				String createdBy = result.getString(8);
				String modifiedBy = result.getString(9);
				Time createdOn = timeService().newTime(result.getTimestamp(10, sqlService().getCal()).getTime());
				Time modifiedOn = timeService().newTime(result.getTimestamp(11, sqlService().getCal()).getTime());

				// find the eid from the mapping
				String eid = checkMapForEid(id);
				if (eid == null)
				{
					M_log.warn("readSqlResultRecord: null eid for id: " + id);
				}

				// create the Resource from these fields
				return new BaseUserEdit(id, eid, email, firstName, lastName, type, pw, createdBy, createdOn, modifiedBy, modifiedOn);
			}
			catch (SQLException e)
			{
				M_log.warn("readSqlResultRecord: " + e);
				return null;
			}
		}

		/**
		 * Create a mapping between the id and eid.
		 * 
		 * @param id
		 *        The user id.
		 * @param eid
		 *        The user eid.
		 * @return true if successful, false if not (id or eid might be in use).
		 */
		public boolean putMap(String id, String eid)
		{
			// if we are not doing separate id/eid, do nothing
			if (!m_separateIdEid) return true;

			String statement = "insert into SAKAI_USER_ID_MAP (USER_ID, EID) values (?,?)";

			Object fields[] = new Object[2];
			fields[0] = id;
			fields[1] = eid;

			return m_sql.dbWrite(statement, fields);
		}

		/**
		 * Update the mapping
		 * 
		 * @param id
		 *        The user id.
		 * @param eid
		 *        The user eid.
		 * @return true if successful, false if not (id or eid might be in use).
		 */
		protected boolean updateMap(String id, String eid)
		{
			// if we are not doing separate id/eid, do nothing
			if (!m_separateIdEid) return true;

			// do we have this id mapped?
			String eidAlready = checkMapForEid(id);

			// if not, add it
			if (eidAlready == null)
			{
				return putMap(id, eid);
			}

			// we have a mapping, is it what we want?
			if (eidAlready.equals(eid)) return true;

			// we have a mapping that needs to be updated
			String statement = "update SAKAI_USER_ID_MAP set EID=? where USER_ID=?";

			Object fields[] = new Object[2];
			fields[0] = eid;
			fields[1] = id;

			return m_sql.dbWrite(statement, fields);
		}

		/**
		 * Remove the mapping for this id
		 * 
		 * @param id
		 *        The user id.
		 */
		protected void unMap(String id)
		{
			// if we are not doing separate id/eid, do nothing
			if (!m_separateIdEid) return;

			String statement = "delete from SAKAI_USER_ID_MAP where USER_ID=?";

			Object fields[] = new Object[1];
			fields[0] = id;

			m_sql.dbWrite(statement, fields);
		}

		/**
		 * Remove the IID information for this user id
		 * 
		 * @param id
		 *        The user id.
		 */
		protected void removeIid(String id)
		{
			String statement = "delete from SAKAI_USER_IID where USER_ID=?";

			Object fields[] = new Object[1];
			fields[0] = id;

			m_sql.dbWrite(statement, fields);
		}

		/**
		 * Check the id -> eid mapping: lookup this id and return the eid if found
		 * 
		 * @param id
		 *        The user id to lookup.
		 * @return The eid mapped to this id, or null if none.
		 */
		public String checkMapForEid(String id)
		{
			// if we are not doing separate id/eid, return the id
			if (!m_separateIdEid) return id;

			String statement = "select EID from SAKAI_USER_ID_MAP where USER_ID=?";
			Object fields[] = new Object[1];
			fields[0] = id;
			List rv = sqlService().dbRead(statement, fields, null);

			if (rv.size() > 0)
			{
				String eid = (String) rv.get(0);
				return eid;
			}

			return null;
		}

		/**
		 * Check the id -> eid mapping: lookup this eid and return the id if found
		 * 
		 * @param eid
		 *        The user eid to lookup.
		 * @return The id mapped to this eid, or null if none or multiple.
		 */
		public String checkMapForId(String eid)
		{
			// if we are not doing separate id/eid, do nothing
			if (!m_separateIdEid) return eid;

			String statement = "select USER_ID from SAKAI_USER_ID_MAP where EID=?";
			Object fields[] = new Object[1];
			fields[0] = eid;
			List rv = sqlService().dbRead(statement, fields, null);

			// if multiple user ids are mapped to this EID, we cannot return a single id, so we return nothing -ggolden
			if (rv.size() == 1)
			{
				String id = (String) rv.get(0);
				return id;
			}

			return null;
		}

		/**
		 * Check the id -> eid mapping: lookup this eid and return all of the ids found.
		 * 
		 * @param eid
		 *        The user eid to lookup.
		 * @return The ids mapped to this eid, or empty if none.
		 */
		public List<String> checkMapForMultipleIds(String eid)
		{
			List<String> rv = new ArrayList<String>();
			
			// if we are not doing separate id/eid, do nothing
			if (!m_separateIdEid) return rv;

			String statement = "select USER_ID from SAKAI_USER_ID_MAP where EID=?";
			Object fields[] = new Object[1];
			fields[0] = eid;
			List ids = sqlService().dbRead(statement, fields, null);

			// if multiple user ids are mapped to this EID, we cannot return a single id, so we return nothing -ggolden
			for (Object id : ids)
			{
				rv.add((String) id);
			}

			return rv;
		}
		/**
		 * Check if the user with this id has one or more IIDs defined.
		 * 
		 * @param id
		 *        The user id.
		 * @return true if the user has some IIDs, false if not.
		 */
		public boolean hasIid(String id)
		{
			String statement = "select ID from SAKAI_USER_IID where USER_ID=?";
			Object fields[] = new Object[1];
			fields[0] = id;
			List rv = sqlService().dbRead(statement, fields, null);

			return (rv.size() > 0);
		}
		
		/**
		 * Find the user id that has this IID
		 * 
		 * @param institutionCode
		 *        The institution code.
		 * @param iid
		 *        The user IID.
		 * @return The id mapped to this IID, or null if none.
		 */
		public String getUserIdForIid(String institutionCode, String iid)
		{
			String statement = "select USER_ID from SAKAI_USER_IID where IID=? and INST_CODE=?";
			Object fields[] = new Object[2];
			fields[0] = iid;
			fields[1] = institutionCode;
			List rv = sqlService().dbRead(statement, fields, null);

			if (rv.size() > 0)
			{
				String id = (String) rv.get(0);
				return id;
			}

			return null;
		}
		
		/**
		 * Find the user id that has this IID, with any institution code, if there is only one (i.e. if the iid is unique to this user across institutions).
		 * 
		 * @param iid
		 *        The user IID.
		 * @return The id mapped to this IID, or null if none.
		 */
		public String getUserIdForIid(String iid)
		{
			String statement = "select distinct USER_ID from SAKAI_USER_IID where IID=?";
			Object fields[] = new Object[1];
			fields[0] = iid;
			List rv = sqlService().dbRead(statement, fields, null);

			if (rv.size() == 1)
			{
				String id = (String) rv.get(0);
				return id;
			}

			return null;
		}

		/**
		 * Set this user's IID information. If one already exists, add this.
		 * 
		 * @param id
		 *        The user internal id.
		 * @param institutionCode
		 *        The institutionCode.
		 * @param iid
		 *        The IID.
		 */
		public void setIid(final String id, final String institutionCode, final String iid)
		{
			// TODO: check if this iid/institutionCode is already in use?  And what if it is?
			// getUserIdForIid(iid, institutionCode) should be null...
			sqlService().transact(new Runnable()
			{
				public void run()
				{
					setIidTx(id, institutionCode, iid);
				}
			}, "setIid: " + id);
		}
		
		/**
		 * Remove the set of IID information for this user.
		 * 
		 * @param id
		 *        The user internal id.
		 */
		public void clearIid(String id)
		{
			removeIid(id);
		}

		/**
		 * Get any IIDs set for this user.
		 * 
		 * @param id
		 *        The user id.
		 * @return A list of strings, each of the form userId@instCode.
		 */
		public List<String> getIid(String id)
		{
			final List<String> rv = new ArrayList<String>();

			String statement = "select INST_CODE, IID from SAKAI_USER_IID where USER_ID=? order by INST_CODE asc";
			Object fields[] = new Object[1];
			fields[0] = id;

			sqlService().dbRead(statement, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						String instCode = StringUtil.trimToNull(result.getString(1));
						String iid = StringUtil.trimToNull(result.getString(2));
						rv.add(iid + "@" + instCode);

						return null;
					}
					catch (SQLException e)
					{
						M_log.warn("getIid: " + e);
						return null;
					}
				}
			});

			return rv;
		}

		/**
		 * Set this user's IID information. If one already exists, add this.  Transaction code.
		 * 
		 * @param id
		 *        The user internal id.
		 * @param institutionCode
		 *        The institutionCode.
		 * @param iid
		 *        The IID.
		 */
		protected void setIidTx(String id, String institutionCode, String iid)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO SAKAI_USER_IID (USER_ID, INST_CODE, IID) VALUES(?,?,?)");

			Object[] fields = new Object[3];
			int i = 0;
			fields[i++] = id;
			fields[i++] = institutionCode;
			fields[i++] = iid;

			Long key = sqlService().dbInsert(null, sql.toString(), fields, "ID");
			if (key == null)
			{
				throw new RuntimeException("setIidTx: dbInsert failed");
			}
		}
	}
}
