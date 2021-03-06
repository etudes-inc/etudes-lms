/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/db/db-util/storage/src/java/org/sakaiproject/util/BaseDbSingleStorage.java $
 * $Id: BaseDbSingleStorage.java 8597 2014-08-31 20:55:03Z ggolden $
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

package org.sakaiproject.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.time.cover.TimeService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * BaseDbSingleStorage is a class that stores Resources (of some type) in a database, <br />
 * provides locked access, and generally implements a services "storage" class. The <br />
 * service's storage class can extend this to provide covers to turn Resource and <br />
 * Edit into something more type specific to the service.
 * </p>
 * <p>
 * Note: the methods here are all "id" based, with the following assumptions: <br /> - just the Resource Id field is enough to distinguish one Resource from another <br /> - a resource's reference is based on no more than the resource id <br /> - a
 * resource's id cannot change.
 * </p>
 * <p>
 * In order to handle Unicode characters properly, the SQL statements executed by this class <br />
 * should not embed Unicode characters into the SQL statement text; rather, Unicode values <br />
 * should be inserted as fields in a PreparedStatement. Databases handle Unicode better in fields.
 * </p>
 */
public class BaseDbSingleStorage
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(BaseDbSingleStorage.class);

	/** Table name for resource records. */
	protected String m_resourceTableName = null;

	/** The field in the resource table that holds the resource id. */
	protected String m_resourceTableIdField = null;

	/** The additional field names in the resource table that go between the two ids and the xml */
	protected String[] m_resourceTableOtherFields = null;

	/** The xml tag name for the element holding each actual resource entry. */
	protected String m_resourceEntryTagName = null;

	/** The StorageUser to callback for new Resource and Edit objects. */
	protected StorageUser m_user = null;

	/** If set, we treat reasource ids as case insensitive. */
	protected boolean m_caseInsensitive = false;

	/** Injected (by constructor) SqlService. */
	protected SqlService m_sql = null;

	/**
	 * Construct.
	 * 
	 * @param resourceTableName
	 *        Table name for resources.
	 * @param resourceTableIdField
	 *        The field in the resource table that holds the id.
	 * @param resourceTableOtherFields
	 *        The other fields in the resource table (between the two id and the xml fields).
	 * @param locksInDb
	 *        If true, we do our locks in the remote database, otherwise we do them here.
	 * @param resourceEntryName
	 *        The xml tag name for the element holding each actual resource entry.
	 * @param user
	 *        The StorageUser class to call back for creation of Resource and Edit objects.
	 * @param sqlService
	 *        The SqlService.
	 */
	public BaseDbSingleStorage(String resourceTableName, String resourceTableIdField, String[] resourceTableOtherFields,
			boolean locksInDb, String resourceEntryName, StorageUser user, SqlService sqlService)
	{
		m_resourceTableName = resourceTableName;
		m_resourceTableIdField = resourceTableIdField;
		m_resourceTableOtherFields = resourceTableOtherFields;
		m_resourceEntryTagName = resourceEntryName;
		m_user = user;
		m_sql = sqlService;
	}

	/**
	 * Open and be ready to read / write.
	 */
	public void open()
	{
	}

	/**
	 * Close.
	 */
	public void close()
	{
	}

	/**
	 * Read one Resource from xml
	 * 
	 * @param xml
	 *        An string containing the xml which describes the resource.
	 * @return The Resource object created from the xml.
	 */
	protected Entity readResource(String xml)
	{
		try
		{
			// read the xml
			Document doc = Xml.readDocumentFromString(xml);

			// verify the root element
			Element root = doc.getDocumentElement();
			if (!root.getTagName().equals(m_resourceEntryTagName))
			{
				M_log.warn("readResource(): not = " + m_resourceEntryTagName + " : " + root.getTagName());
				return null;
			}

			// re-create a resource
			Entity entry = m_user.newResource(null, root);

			return entry;
		}
		catch (Exception e)
		{
			M_log.debug("readResource(): ", e);
			return null;
		}
	}

	/**
	 * Check if a Resource by this id exists.
	 * 
	 * @param id
	 *        The id.
	 * @return true if a Resource by this id exists, false if not.
	 */
	public boolean checkResource(String id)
	{
		// just see if the record exists
		String sql = "select " + m_resourceTableIdField + " from " + m_resourceTableName + " where ( " + m_resourceTableIdField
				+ " = ? )";

		Object fields[] = new Object[1];
		fields[0] = caseId(id);
		List ids = m_sql.dbRead(sql, fields, null);

		return (!ids.isEmpty());
	}

	/**
	 * Get the Resource with this id, or null if not found.
	 * 
	 * @param id
	 *        The id.
	 * @return The Resource with this id, or null if not found.
	 */
	public Entity getResource(String id)
	{
		Entity entry = null;

		// get the user from the db
		String sql = "select XML from " + m_resourceTableName + " where ( " + m_resourceTableIdField + " = ? )";

		Object fields[] = new Object[1];
		fields[0] = caseId(id);
		List xml = m_sql.dbRead(sql, fields, null);
		if (!xml.isEmpty())
		{
			// create the Resource from the db xml
			entry = readResource((String) xml.get(0));
		}

		return entry;
	}

	public boolean isEmpty()
	{
		// count
		int count = countAllResources();
		return (count == 0);
	}

	public List getAllResources()
	{
		List all = new Vector();

		// read all users from the db
		String sql = "select XML from " + m_resourceTableName;
		// %%% + "order by " + m_resourceTableOrderField + " asc";

		List xml = m_sql.dbRead(sql);

		// process all result xml into user objects
		if (!xml.isEmpty())
		{
			for (int i = 0; i < xml.size(); i++)
			{
				Entity entry = readResource((String) xml.get(i));
				if (entry != null) all.add(entry);
			}
		}

		return all;
	}

	public List getAllResources(int first, int last)
	{
		String sql;
		Object[] fields = null;
		if ("oracle".equals(m_sql.getVendor()))
		{
			// use Oracle RANK function
			sql = "select XML from" + " (select XML" + " ,RANK() OVER" + " (order by " + m_resourceTableIdField + ") as rank"
					+ " from " + m_resourceTableName + " order by " + m_resourceTableIdField + " asc)"
					+ " where rank between ? and ?";
			fields = new Object[2];
			fields[0] = new Long(first);
			fields[1] = new Long(last);
		}
		else if ("mysql".equals(m_sql.getVendor()))
		{
			// use MySQL SQL LIMIT clause
			sql = "select XML from " + m_resourceTableName + " order by " + m_resourceTableIdField + " asc " + " limit "
					+ (last - first + 1) + " offset " + (first - 1);
		}
		else
		// if ("hsqldb".equals(m_sql.getVendor()))
		{
			// use SQL2000 LIMIT clause
			sql = "select " + "limit " + (first - 1) + " " + (last - first + 1) + " " + "XML from " + m_resourceTableName
					+ " order by " + m_resourceTableIdField + " asc";
		}

		List xml = m_sql.dbRead(sql, fields, null);
		List rv = new Vector();

		// process all result xml into user objects
		if (!xml.isEmpty())
		{
			for (int i = 0; i < xml.size(); i++)
			{
				Entity entry = readResource((String) xml.get(i));
				if (entry != null) rv.add(entry);
			}
		}

		return rv;
	}

	public int countAllResources()
	{
		List all = new Vector();

		// read all count
		String sql = "select count(1) from " + m_resourceTableName;

		List results = m_sql.dbRead(sql, null, new SqlReader()
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

	public int countSelectedResourcesWhere(String sqlWhere)
	{
		List all = new Vector();

		// read all where count
		String sql = "select count(1) from " + m_resourceTableName + " " + sqlWhere;
		List results = m_sql.dbRead(sql, null, new SqlReader()
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
	 * Get all Resources where the given field matches the given value.
	 * 
	 * @param field
	 *        The db field name for the selection.
	 * @param value
	 *        The value to select.
	 * @return The list of all Resources that meet the criteria.
	 */
	public List getAllResourcesWhere(String field, String value)
	{
		// read all users from the db
		String sql = "select XML from " + m_resourceTableName + " where " + field + " = ?";
		Object[] fields = new Object[1];
		fields[0] = value;
		// %%% + "order by " + m_resourceTableOrderField + " asc";

		return loadResources(sql, fields);
	}

	protected List loadResources(String sql, Object[] fields)
	{
		List all = m_sql.dbRead(sql, fields, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					// create the Resource from the db xml
					String xml = result.getString(1);
					Entity entry = readResource(xml);
					return entry;
				}
				catch (SQLException ignore)
				{
					return null;
				}
			}
		});
		return all;
	}

	public List getAllResourcesWhereLike(String field, String value)
	{
		String sql = "select XML from " + m_resourceTableName + " where " + field + " like ?";
		Object[] fields = new Object[1];
		fields[0] = value;
		// %%% + "order by " + m_resourceTableOrderField + " asc";

		return loadResources(sql, fields);
	}

	/**
	 * Get selected Resources, filtered by a test on the id field
	 * 
	 * @param filter
	 *        A filter to select what gets returned.
	 * @return The list of selected Resources.
	 */
	public List getSelectedResources(final Filter filter)
	{
		List all = new Vector();

		// read all users from the db
		String sql = "select " + m_resourceTableIdField + ", XML from " + m_resourceTableName;
		// %%% + "order by " + m_resourceTableOrderField + " asc";

		List xml = m_sql.dbRead(sql, null, new SqlReader()
		{
			public Object readSqlResultRecord(ResultSet result)
			{
				try
				{
					// read the id m_resourceTableIdField
					String id = result.getString(1);

					// read the xml
					String xml = result.getString(2);

					if (!filter.accept(caseId(id))) return null;

					return xml;
				}
				catch (SQLException ignore)
				{
					return null;
				}
			}
		});

		// process all result xml into user objects
		if (!xml.isEmpty())
		{
			for (int i = 0; i < xml.size(); i++)
			{
				Entity entry = readResource((String) xml.get(i));
				if (entry != null) all.add(entry);
			}
		}

		return all;
	}

	/**
	 * Get selected Resources, using a supplied where clause
	 * 
	 * @param sqlWhere
	 *        The SQL where clause.
	 * @return The list of selected Resources.
	 */
	public List getSelectedResourcesWhere(String sqlWhere)
	{
		List all = new Vector();

		// read all users from the db
		String sql = "select XML from " + m_resourceTableName + " " + sqlWhere;
		// %%% + "order by " + m_resourceTableOrderField + " asc";

		List xml = m_sql.dbRead(sql);

		// process all result xml into user objects
		if (!xml.isEmpty())
		{
			for (int i = 0; i < xml.size(); i++)
			{
				Entity entry = readResource((String) xml.get(i));
				if (entry != null) all.add(entry);
			}
		}

		return all;
	}

	/**
	 * Add a new Resource with this id.
	 * 
	 * @param id
	 *        The id.
	 * @param others
	 *        Other fields for the newResource call
	 * @return The locked Resource object with this id, or null if the id is in use.
	 */
	public Edit putResource(String id, Object[] others)
	{
		// create one with just the id, and perhaps some other fields as well
		Entity entry = m_user.newResource(null, id, others);

		// form the XML and SQL for the insert
		Document doc = Xml.createDocument();
		entry.toXml(doc, new Stack());
		String xml = Xml.writeDocumentToString(doc);
		String statement = "insert into " + m_resourceTableName
				+ insertFields(m_resourceTableIdField, m_resourceTableOtherFields, "XML") + " values ( ?, "
				+ valuesParams(m_resourceTableOtherFields) + " ? )";

		Object[] flds = m_user.storageFields(entry);
		if (flds == null) flds = new Object[0];
		Object[] fields = new Object[flds.length + 2];
		System.arraycopy(flds, 0, fields, 1, flds.length);
		fields[0] = caseId(entry.getId());
		fields[fields.length - 1] = xml;

		// process the insert
		boolean ok = m_sql.dbWrite(statement, fields);

		// if this failed, assume a key conflict (i.e. id in use)
		if (!ok) return null;

		// now get a lock on the record for edit
		Edit edit = editResource(id);
		if (edit == null)
		{
			M_log.warn("putResource(): didn't get a lock!");
			return null;
		}

		return edit;
	}

	/** store the record in content_resource_delete table along with resource_uuid and date */
	public Edit putDeleteResource(String id, String uuid, String userId, Object[] others)
	{
		Entity entry = m_user.newResource(null, id, others);

		// form the XML and SQL for the insert
		Document doc = Xml.createDocument();
		entry.toXml(doc, new Stack());
		String xml = Xml.writeDocumentToString(doc);
		String statement = "insert into "
				+ m_resourceTableName
				+ insertDeleteFields(m_resourceTableIdField, m_resourceTableOtherFields, "RESOURCE_UUID", "DELETE_DATE",
						"DELETE_USERID", "XML") + " values ( ?, " + valuesParams(m_resourceTableOtherFields) + " ? ,? ,? ,?)";

		Object[] flds = m_user.storageFields(entry);
		if (flds == null) flds = new Object[0];
		Object[] fields = new Object[flds.length + 5];
		System.arraycopy(flds, 0, fields, 1, flds.length);
		fields[0] = caseId(entry.getId());
		// uuid added here
		fields[fields.length - 4] = uuid;
		// date added here
		fields[fields.length - 3] = TimeService.newTime();// .toStringLocalDate();

		// userId added here
		fields[fields.length - 2] = userId;
		fields[fields.length - 1] = xml;

		// process the insert
		boolean ok = m_sql.dbWrite(statement, fields);

		// if this failed, assume a key conflict (i.e. id in use)
		if (!ok) return null;

		// now get a lock on the record for edit
		Edit edit = editResource(id);
		if (edit == null)
		{
			M_log.warn("putResourceDelete(): didn't get a lock!");
			return null;
		}

		return edit;
	}

	/** Construct the SQL statement */
	protected String insertDeleteFields(String before, String[] fields, String uuid, String date, String userId, String after)
	{
		StringBuffer buf = new StringBuffer();
		buf.append(" (");
		buf.append(before);
		buf.append(",");
		if (fields != null)
		{
			for (int i = 0; i < fields.length; i++)
			{
				buf.append(fields[i] + ",");
			}
		}
		buf.append(uuid);
		buf.append(",");
		buf.append(date);
		buf.append(",");
		buf.append(userId);
		buf.append(",");
		buf.append(after);
		buf.append(")");

		return buf.toString();
	}

	/** update XML attribute on properties and remove locks */
	public void commitDeleteResource(Edit edit, String uuid)
	{
		// form the SQL statement and the var w/ the XML
		Document doc = Xml.createDocument();
		edit.toXml(doc, new Stack());
		String xml = Xml.writeDocumentToString(doc);
		Object[] flds = m_user.storageFields(edit);
		if (flds == null) flds = new Object[0];
		Object[] fields = new Object[flds.length + 2];
		System.arraycopy(flds, 0, fields, 0, flds.length);
		fields[fields.length - 2] = xml;
		fields[fields.length - 1] = uuid;// caseId(edit.getId());

		String statement = "update " + m_resourceTableName + " set " + updateSet(m_resourceTableOtherFields) + " XML = ?"
				+ " where ( RESOURCE_UUID = ? )";

		// process the update
		m_sql.dbWrite(statement, fields);
	}

	/**
	 * Get a lock on the Resource with this id, or null if a lock cannot be gotten.
	 * 
	 * @param id
	 *        The user id.
	 * @return The locked Resource with this id, or null if this records cannot be locked.
	 */
	public Edit editResource(String id)
	{
		Edit edit = null;

		// read the record - fail if not there
		Entity entry = getResource(id);
		if (entry == null) return null;

		// we got the lock! - make the edit from the Resource
		edit = m_user.newResourceEdit(null, entry);

		return edit;
	}

	/**
	 * Commit the changes and release the lock.
	 * 
	 * @param user
	 *        The Edit to commit.
	 */
	public void commitResource(Edit edit)
	{
		// form the SQL statement and the var w/ the XML
		Document doc = Xml.createDocument();
		edit.toXml(doc, new Stack());
		String xml = Xml.writeDocumentToString(doc);
		Object[] flds = m_user.storageFields(edit);
		if (flds == null) flds = new Object[0];
		Object[] fields = new Object[flds.length + 2];
		System.arraycopy(flds, 0, fields, 0, flds.length);
		fields[fields.length - 2] = xml;
		fields[fields.length - 1] = caseId(edit.getId());

		String statement = "update " + m_resourceTableName + " set " + updateSet(m_resourceTableOtherFields) + " XML = ?"
				+ " where ( " + m_resourceTableIdField + " = ? )";

		// process the update
		m_sql.dbWrite(statement, fields);
	}

	/**
	 * Cancel the changes and release the lock.
	 * 
	 * @param user
	 *        The Edit to cancel.
	 */
	public void cancelResource(Edit edit)
	{
	}

	/**
	 * Remove this (locked) Resource.
	 * 
	 * @param user
	 *        The Edit to remove.
	 */
	public void removeResource(Edit edit)
	{
		// form the SQL delete statement
		String statement = "delete from " + m_resourceTableName + " where ( " + m_resourceTableIdField + " = ? )";

		Object fields[] = new Object[1];
		fields[0] = caseId(edit.getId());

		// process the delete statement
		m_sql.dbWrite(statement, fields);
	}

	/**
	 * Form a string of n question marks with commas, for sql value statements, one for each item in the values array, or an empty string if null.
	 * 
	 * @param values
	 *        The values to be inserted into the sql statement.
	 * @return A sql statement fragment for the values part of an insert, one for each value in the array.
	 */
	protected String valuesParams(String[] fields)
	{
		if ((fields == null) || (fields.length == 0)) return "";
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < fields.length; i++)
		{
			buf.append(" ?,");
		}
		return buf.toString();
	}

	/**
	 * Form a string of n name=?, for sql update set statements, one for each item in the values array, or an empty string if null.
	 * 
	 * @param values
	 *        The values to be inserted into the sql statement.
	 * @return A sql statement fragment for the values part of an insert, one for each value in the array.
	 */
	protected String updateSet(String[] fields)
	{
		if ((fields == null) || (fields.length == 0)) return "";
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < fields.length; i++)
		{
			buf.append(fields[i] + " = ?,");
		}
		return buf.toString();
	}

	/**
	 * Form a string of (field, field, field), for sql insert statements, one for each item in the fields array, plus one before, and one after.
	 * 
	 * @param before
	 *        The first field name.
	 * @param values
	 *        The extra field names, in the middle.
	 * @param after
	 *        The last field name.
	 * @return A sql statement fragment for the insert fields.
	 */
	protected String insertFields(String before, String[] fields, String after)
	{
		StringBuffer buf = new StringBuffer();
		buf.append(" (");

		buf.append(before);
		buf.append(",");

		if (fields != null)
		{
			for (int i = 0; i < fields.length; i++)
			{
				buf.append(fields[i] + ",");
			}
		}

		buf.append(after);

		buf.append(")");

		return buf.toString();
	}

	/**
	 * Fix the case of resource ids to support case insensitive ids if enabled
	 * 
	 * @param The
	 *        id to fix.
	 * @return The id, case modified as needed.
	 */
	protected String caseId(String id)
	{
		if (m_caseInsensitive)
		{
			return id.toLowerCase();
		}

		return id;
	}

	/**
	 * Enable / disable case insensitive ids.
	 * 
	 * @param setting
	 *        true to set case insensitivity, false to set case sensitivity.
	 */
	protected void setCaseInsensitivity(boolean setting)
	{
		m_caseInsensitive = setting;
	}

	/**
	 * Return a record ID to use internally in the database. This is needed for databases (MySQL) that have limits on key lengths. The hash code ensures that the record ID will be unique, even if the DB only considers a prefix of a very long record ID.
	 * 
	 * @param recordId
	 * @return The record ID to use internally in the database
	 */
	private String internalRecordId(String recordId)
	{
		if ("mysql".equals(m_sql.getVendor()))
		{
			if (recordId == null) recordId = "null";
			return recordId.hashCode() + " - " + recordId;
		}
		else
		// oracle, hsqldb
		{
			return recordId;
		}
	}
}
