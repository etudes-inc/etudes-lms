/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/site/site-impl/impl/src/java/org/sakaiproject/site/impl/DbSiteService.java $
 * $Id: DbSiteService.java 6322 2013-11-14 20:13:42Z ggolden $
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

package org.sakaiproject.site.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.javax.PagingPosition;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.util.BaseDbFlatStorage;
import org.sakaiproject.util.BaseResourcePropertiesEdit;
import org.sakaiproject.util.StringUtil;

/**
 * <p>
 * DbSiteService is an extension of the BaseSiteService with a database storage.
 * </p>
 */
public abstract class DbSiteService extends BaseSiteService
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(DbSiteService.class);

	/** Table name for sites. */
	protected String m_siteTableName = "SAKAI_SITE";

	/** Table name for site properties. */
	protected String m_sitePropTableName = "SAKAI_SITE_PROPERTY";

	/** ID field for site. */
	protected String m_siteIdFieldName = "SITE_ID";

	/** Site sort field. */
	protected String m_siteSortField = "TITLE";

	/** All fields for site. */
	protected String[] m_siteFieldNames = { "SITE_ID", "TITLE", "TYPE", "SHORT_DESC", "DESCRIPTION", "ICON_URL", "INFO_URL",
			"SKIN", "PUBLISHED", "JOINABLE", "PUBVIEW", "JOIN_ROLE", "IS_SPECIAL", "IS_USER", "CREATEDBY", "MODIFIEDBY",
			"CREATEDON", "MODIFIEDON" };
	private static final String SORTED_BY_CREATION_DATE = "creationdate";
	private static final String SORTED_BY_TITLE = "title";


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
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_site");

				// also load the 2.1 new site database tables
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_site_group");

				// also load the 2.1.0.003 field insertion
				sqlService().ddl(this.getClass().getClassLoader(), "sakai_site_2_1_0_003");
			}

			super.init();

			M_log.info("init(): site table: " + m_siteTableName + " external locks: " + m_useExternalLocks);
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * BaseSiteService extensions
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

	protected class DbStorage extends BaseDbFlatStorage implements Storage, SqlReader
	{
		/** A prior version's storage model. */
		protected Storage m_oldStorage = null;

		/** The service. */
		protected BaseSiteService m_service = null;

		/**
		 * Construct.
		 * 
		 * @param user
		 *        The StorageUser class to call back for creation of Resource and Edit objects.
		 */
		public DbStorage(BaseSiteService service)
		{
			super(m_siteTableName, m_siteIdFieldName, m_siteFieldNames, m_sitePropTableName, m_useExternalLocks, null, sqlService());
			m_reader = this;

			m_service = service;

			setSortField(m_siteSortField, null);

			// no locking
			setLocking(false);
		}

		public boolean check(String id)
		{
			return super.checkResource(id);
		}

		public Site get(String id)
		{
			return (Site) super.getResource(id);
		}

		public Site getByTitle(String title)
		{
			String sql = "SELECT S.SITE_ID FROM SAKAI_SITE S WHERE S.TITLE = ?";
			Object[] fields = new Object[1];
			fields[0] = title;
			List results = m_sql.dbRead(sql, fields, null);
			if (results.size() > 0)
			{
				String siteId = (String) results.get(0);
				return get(siteId);
			}

			return null;
		}

		public List getAll()
		{
			return super.getAllResources();
		}

		public Site put(String id)
		{
			// check for already exists
			if (check(id)) return null;

			BaseSite rv = (BaseSite) super.putResource(id, fields(id, null, false));
			if (rv != null) rv.activate();
			return rv;
		}

		public void save(Site edit)
		{
			save(null, edit);
		}

		/**
		 * Commit with optional connection to use.
		 * 
		 * @param conn
		 *        Optional connection to use.
		 * @param edit
		 *        Edit to commit.
		 */
		protected void save(Connection conn, Site edit)
		{
			// write the pages, tools, properties,
			// and then commit the site and release the lock, all in one transaction
			Connection connection = null;
			boolean wasCommit = true;
			try
			{
				// use the connection given if given.
				if (conn != null)
				{
					connection = conn;
				}

				else
				{
					connection = m_sql.borrowConnection();
					wasCommit = connection.getAutoCommit();
					connection.setAutoCommit(false);
				}

				// delete the pages, tools, page properties, tool properties
				Object fields[] = new Object[1];
				fields[0] = caseId(edit.getId());

				String statement = "delete from SAKAI_SITE_TOOL_PROPERTY where SITE_ID = ?";
				m_sql.dbWrite(connection, statement, fields);

				statement = "delete from SAKAI_SITE_TOOL where SITE_ID = ?";
				m_sql.dbWrite(connection, statement, fields);

				statement = "delete from SAKAI_SITE_PAGE_PROPERTY where SITE_ID = ?";
				m_sql.dbWrite(connection, statement, fields);

				statement = "delete from SAKAI_SITE_PAGE where SITE_ID = ?";
				m_sql.dbWrite(connection, statement, fields);

				statement = "delete from SAKAI_SITE_GROUP_PROPERTY where SITE_ID = ?";
				m_sql.dbWrite(connection, statement, fields);

				statement = "delete from SAKAI_SITE_GROUP where SITE_ID = ?";
				m_sql.dbWrite(connection, statement, fields);

				// since we've already deleted the old values, don't delete them again.
				boolean deleteAgain = false;

				// add each page
				int pageOrder = 1;
				for (Iterator iPages = edit.getPages().iterator(); iPages.hasNext();)
				{
					SitePage page = (SitePage) iPages.next();

					// write the page
					statement = "insert into SAKAI_SITE_PAGE (PAGE_ID, SITE_ID, TITLE, LAYOUT, POPUP, SITE_ORDER)"
							+ " values (?,?,?,?,?,?)";

					fields = new Object[6];
					fields[0] = page.getId();
					fields[1] = caseId(edit.getId());
					fields[2] = page.getTitle();
					fields[3] = Integer.toString(page.getLayout());
					fields[4] = ((((BaseSitePage) page).m_popup) ? "1" : "0");
					fields[5] = new Integer(pageOrder++);
					m_sql.dbWrite(connection, statement, fields);

					// write the page's properties
					writeProperties(connection, "SAKAI_SITE_PAGE_PROPERTY", "PAGE_ID", page.getId(), "SITE_ID",
							caseId(edit.getId()), page.getProperties(), deleteAgain);

					// write the tools
					int toolOrder = 1;
					for (Iterator iTools = page.getTools().iterator(); iTools.hasNext();)
					{
						ToolConfiguration tool = (ToolConfiguration) iTools.next();

						// write the tool
						statement = "insert into SAKAI_SITE_TOOL (TOOL_ID, PAGE_ID, SITE_ID, REGISTRATION, PAGE_ORDER, TITLE, LAYOUT_HINTS)"
								+ " values (?,?,?,?,?,?,?)";

						fields = new Object[7];
						fields[0] = tool.getId();
						fields[1] = page.getId();
						fields[2] = caseId(edit.getId());
						fields[3] = tool.getToolId();
						fields[4] = new Integer(toolOrder++);
						fields[5] = tool.getTitle();
						fields[6] = tool.getLayoutHints();
						m_sql.dbWrite(connection, statement, fields);

						// write the tool's properties
						writeProperties(connection, "SAKAI_SITE_TOOL_PROPERTY", "TOOL_ID", tool.getId(), "SITE_ID", caseId(edit
								.getId()), tool.getPlacementConfig(), deleteAgain);
					}
				}

				// add each group
				for (Iterator iGroups = edit.getGroups().iterator(); iGroups.hasNext();)
				{
					Group group = (Group) iGroups.next();

					// write the group
					statement = "insert into SAKAI_SITE_GROUP (GROUP_ID, SITE_ID, TITLE, DESCRIPTION)" + " values (?,?,?,?)";

					fields = new Object[4];
					fields[0] = group.getId();
					fields[1] = caseId(edit.getId());
					fields[2] = group.getTitle();
					fields[3] = group.getDescription();
					m_sql.dbWrite(connection, statement, fields);

					// write the group's properties
					writeProperties(connection, "SAKAI_SITE_GROUP_PROPERTY", "GROUP_ID", group.getId(), "SITE_ID", caseId(edit
							.getId()), group.getProperties(), deleteAgain);
				}

				// write the site and properties, releasing the lock
				super.commitResource(connection, edit, fields(edit.getId(), edit, true), edit.getProperties(), null);

				// if the connection is new, commit
				if (conn == null)
				{
					connection.commit();
				}
			}
			catch (Exception e)
			{
				if ((connection != null) && (conn == null))
				{
					try
					{
						connection.rollback();
					}
					catch (Exception ee)
					{
						M_log.warn("commit, while rolling back: " + ee);
					}
				}
				M_log.warn("commit: " + e);
			}
			finally
			{
				if ((connection != null) && (conn == null))
				{
					try
					{
						connection.setAutoCommit(wasCommit);
					}
					catch (Exception e)
					{
						M_log.warn("commit, while setting auto commit: " + e);
					}
					m_sql.returnConnection(connection);
				}
			}
		}

		/**
		 * @inheritDoc
		 */
		public void saveInfo(String siteId, String description, String infoUrl)
		{
			String statement = "update " + m_siteTableName + " set DESCRIPTION = ?, INFO_URL = ? where SITE_ID = ?";

			Object fields[] = new Object[3];
			fields[0] = description;
			fields[1] = infoUrl;
			fields[2] = caseId(siteId);

			m_sql.dbWrite(statement, fields);
		}

		/**
		 * @inheritDoc
		 */
		public void saveToolConfig(Connection conn, ToolConfiguration tool)
		{
			Connection connection = null;
			boolean wasCommit = true;
			try
			{
				// use the connection given if given.
				if (conn != null)
				{
					connection = conn;
				}

				else
				{
					connection = m_sql.borrowConnection();
					wasCommit = connection.getAutoCommit();
					connection.setAutoCommit(false);
				}

				// delete this tool and tool properties
				Object fields[] = new Object[2];
				fields[0] = caseId(tool.getSiteId());
				fields[1] = caseId(tool.getId());

				String statement = "delete from SAKAI_SITE_TOOL_PROPERTY where SITE_ID = ? and TOOL_ID = ?";
				m_sql.dbWrite(connection, statement, fields);

				statement = "delete from SAKAI_SITE_TOOL where SITE_ID = ? and TOOL_ID = ?";
				m_sql.dbWrite(connection, statement, fields);

				// write the tool
				statement = "insert into SAKAI_SITE_TOOL (TOOL_ID, PAGE_ID, SITE_ID, REGISTRATION, PAGE_ORDER, TITLE, LAYOUT_HINTS)"
						+ " values (?,?,?,?,?,?,?)";

				fields = new Object[7];
				fields[0] = tool.getId();
				fields[1] = tool.getPageId();
				fields[2] = caseId(tool.getSiteId());
				fields[3] = tool.getToolId();
				fields[4] = new Integer(tool.getPageOrder());
				fields[5] = tool.getTitle();
				fields[6] = tool.getLayoutHints();
				m_sql.dbWrite(connection, statement, fields);

				// write the tool's properties
				writeProperties(connection, "SAKAI_SITE_TOOL_PROPERTY", "TOOL_ID", tool.getId(), "SITE_ID",
						caseId(tool.getSiteId()), tool.getPlacementConfig());

				// if the connection is new, commit
				if (conn == null)
				{
					connection.commit();
				}
			}
			catch (Exception e)
			{
				if ((connection != null) && (conn == null))
				{
					try
					{
						connection.rollback();
					}
					catch (Exception ee)
					{
						M_log.warn("commitToolConfig, while rolling back: " + ee);
					}
				}
				M_log.warn("commitToolConfig: " + e);
			}
			finally
			{
				if ((connection != null) && (conn == null))
				{
					try
					{
						connection.setAutoCommit(wasCommit);
					}
					catch (Exception e)
					{
						M_log.warn("commitToolConfig, while setting auto commit: " + e);
					}
					m_sql.returnConnection(connection);
				}
			}
		}

		public void remove(Site edit)
		{
			// delete all the pages, tools, properties, permissions
			// and then the site and release the lock, all in one transaction
			Connection connection = null;
			boolean wasCommit = true;
			try
			{
				connection = m_sql.borrowConnection();
				wasCommit = connection.getAutoCommit();
				connection.setAutoCommit(false);

				// delete the pages, tools, page properties, tool properties, permissions
				Object fields[] = new Object[1];
				fields[0] = caseId(edit.getId());

				String statement = "delete from SAKAI_SITE_TOOL_PROPERTY where SITE_ID = ?";
				m_sql.dbWrite(connection, statement, fields);

				statement = "delete from SAKAI_SITE_TOOL where SITE_ID = ?";
				m_sql.dbWrite(connection, statement, fields);

				statement = "delete from SAKAI_SITE_PAGE_PROPERTY where SITE_ID = ?";
				m_sql.dbWrite(connection, statement, fields);

				statement = "delete from SAKAI_SITE_PAGE where SITE_ID = ?";
				m_sql.dbWrite(connection, statement, fields);

				statement = "delete from SAKAI_SITE_USER where SITE_ID = ?";
				m_sql.dbWrite(connection, statement, fields);

				statement = "delete from SAKAI_SITE_GROUP_PROPERTY where SITE_ID = ?";
				m_sql.dbWrite(connection, statement, fields);

				statement = "delete from SAKAI_SITE_GROUP where SITE_ID = ?";
				m_sql.dbWrite(connection, statement, fields);

				// delete the site and properties
				super.removeResource(connection, edit, null);

				connection.commit();
			}
			catch (Exception e)
			{
				if (connection != null)
				{
					try
					{
						connection.rollback();
					}
					catch (Exception ee)
					{
						M_log.warn("remove, while rolling back: " + ee);
					}
				}
				M_log.warn("remove: " + e);
			}
			finally
			{
				if (connection != null)
				{
					try
					{
						connection.setAutoCommit(wasCommit);
					}
					catch (Exception e)
					{
						M_log.warn("remove, while setting auto commit: " + e);
					}
					m_sql.returnConnection(connection);
				}
			}

		}

		public int count()
		{
			return super.countAllResources();
		}

		/**
		 * {@inheritDoc}
		 */
		public List getSites(SelectionType type, Object ofType, String criteria, Integer term, String instPrefix, Map propertyCriteria, SortType sort,
				PagingPosition page)
		{
			// Note: super users are not treated any differently - they get only those sites they have permission for,
			// not based on super user status

			// if we are joining, start our where with the join clauses
			StringBuffer where = new StringBuffer();
			if ((type == SelectionType.ACCESS) || (type == SelectionType.UPDATE) || (type == SelectionType.MYSITES))
			{
				// join on site id and also select the proper user
				where.append("SAKAI_SITE.SITE_ID = SAKAI_SITE_USER.SITE_ID and SAKAI_SITE_USER.USER_ID = ? and ");
			}

			// ignore user sites
			if (type.isIgnoreUser()) where.append("SAKAI_SITE.IS_USER = '0' and ");
			// reject special sites
			if (type.isIgnoreSpecial()) where.append("SAKAI_SITE.IS_SPECIAL = '0' and ");
			// reject unpublished sites
			if (type.isIgnoreUnpublished()) where.append("SAKAI_SITE.PUBLISHED = 1 and ");

			if (term != null)
			{
				where.append("ARCHIVES_SITE_TERM.TERM_ID = ? and ");
			}

			if (ofType != null)
			{
				if (ofType.getClass().equals(String.class))
				{
					// type criteria is a simple String value
					where.append("SAKAI_SITE.TYPE = ? and ");
				}
				else if (ofType instanceof String[] || ofType instanceof List || ofType instanceof Set)
				{
					// more complex type criteria
					int size = 0;
					if (ofType instanceof String[])
					{
						size = ((String[]) ofType).length;
					}
					else if (ofType instanceof List)
					{
						size = ((List) ofType).size();
					}
					else if (ofType instanceof Set)
					{
						size = ((Set) ofType).size();
					}
					if (size > 0)
					{
						where.append("SAKAI_SITE.TYPE IN (?");
						for (int i = 1; i < size; i++)
						{
							where.append(",?");
						}
						where.append(") and ");
					}
				}
			}

			// reject non-joinable sites
			if (type == SelectionType.JOINABLE) where.append("SAKAI_SITE.JOINABLE = '1' and ");
			// check for pub view status
			if (type == SelectionType.PUBVIEW) where.append("SAKAI_SITE.PUBVIEW = '1' and ");
			// check criteria
			if (criteria != null) where.append("UPPER(SAKAI_SITE.TITLE) like UPPER(?) and ");
			// institution title prefix
			if (instPrefix != null) where.append("SAKAI_SITE.TITLE like ? and ");
			// update permission
			if (type == SelectionType.UPDATE) where.append("SAKAI_SITE_USER.PERMISSION <= -1 and ");
			// access permission
			if (type == SelectionType.ACCESS) where.append("SAKAI_SITE_USER.PERMISSION <= SAKAI_SITE.PUBLISHED and ");
			// joinable requires NOT access permission
			if (type == SelectionType.JOINABLE)
				where
						.append("SITE_ID not in (select SITE_ID from SAKAI_SITE_USER where USER_ID = ? and PERMISSION <= PUBLISHED) and ");

			// do we need a join?
			String join = "ARCHIVES_TERM,ARCHIVES_SITE_TERM";
			if ((type == SelectionType.ACCESS) || (type == SelectionType.UPDATE) || (type == SelectionType.MYSITES))
			{
				// join with the SITE_USER table
				join = "ARCHIVES_TERM,ARCHIVES_SITE_TERM,SAKAI_SITE_USER";
			}

			// add propertyCriteria if specified
			if ((propertyCriteria != null) && (propertyCriteria.size() > 0))
			{
				for (int i = 0; i < propertyCriteria.size(); i++)
				{
					where
							.append("SAKAI_SITE.SITE_ID in (select SITE_ID from SAKAI_SITE_PROPERTY where NAME = ? and UPPER(VALUE) like UPPER(?)) and ");
				}
			}

			where.append("SAKAI_SITE.SITE_ID = ARCHIVES_SITE_TERM.SITE_ID and ARCHIVES_SITE_TERM.TERM_ID = ARCHIVES_TERM.ID ");
			
			// add order by if needed
			String order = null;
			if (sort == SortType.ID_ASC)
			{
				order = "SAKAI_SITE.SITE_ID ASC";
			}
			else if (sort == SortType.ID_DESC)
			{
				order = "SAKAI_SITE.SITE_ID DESC";
			}
			else if (sort == SortType.TITLE_ASC)
			{
				order = "SAKAI_SITE.TITLE ASC";
			}
			else if (sort == SortType.TITLE_DESC)
			{
				order = "SAKAI_SITE.TITLE DESC";
			}
			else if (sort == SortType.TYPE_ASC)
			{
				order = "SAKAI_SITE.TYPE ASC";
			}
			else if (sort == SortType.TYPE_DESC)
			{
				order = "SAKAI_SITE.TYPE DESC";
			}
			else if (sort == SortType.PUBLISHED_ASC)
			{
				order = "SAKAI_SITE.PUBLISHED ASC";
			}
			else if (sort == SortType.PUBLISHED_DESC)
			{
				order = "SAKAI_SITE.PUBLISHED DESC";
			}
			else if (sort == SortType.CREATED_BY_ASC)
			{
				order = "SAKAI_SITE.CREATEDBY ASC";
			}
			else if (sort == SortType.CREATED_BY_DESC)
			{
				order = "SAKAI_SITE.CREATEDBY DESC";
			}
			else if (sort == SortType.MODIFIED_BY_ASC)
			{
				order = "SAKAI_SITE.MODIFIEDBY ASC";
			}
			else if (sort == SortType.MODIFIED_BY_DESC)
			{
				order = "SAKAI_SITE.MODIFIEDBY DESC";
			}
			else if (sort == SortType.TERM_ASC)
			{
				order = "ARCHIVES_TERM.ID ASC";
			}
			else if (sort == SortType.TERM_DESC)
			{
				order = "ARCHIVES_TERM.ID DESC";
			}
			else if (sort == SortType.CREATED_ON_ASC)
			{
				order = "SAKAI_SITE.CREATEDON ASC";
			}
			else if (sort == SortType.CREATED_ON_DESC)
			{
				order = "SAKAI_SITE.CREATEDON DESC";
			}
			else if (sort == SortType.MODIFIED_ON_ASC)
			{
				order = "SAKAI_SITE.MODIFIEDON ASC";
			}
			else if (sort == SortType.MODIFIED_ON_DESC)
			{
				order = "SAKAI_SITE.MODIFIEDON DESC";
			}

			int fieldCount = 0;
			if (term != null) fieldCount++;
			if (ofType != null)
			{
				if (ofType instanceof String)
				{
					// type criteria is a simple String value
					fieldCount++;
				}
				// more complex types
				else if (ofType instanceof String[])
				{
					fieldCount += ((String[]) ofType).length;
				}
				else if (ofType instanceof List)
				{
					fieldCount += ((List) ofType).size();
				}
				else if (ofType instanceof Set)
				{
					fieldCount += ((Set) ofType).size();
				}
			}
			if (criteria != null) fieldCount += 1;
			if (instPrefix != null) fieldCount ++;
			if ((type == SelectionType.JOINABLE) || (type == SelectionType.ACCESS) || (type == SelectionType.UPDATE) || (type == SelectionType.MYSITES)) fieldCount++;
			if (propertyCriteria != null) fieldCount += (2 * propertyCriteria.size());
			Object fields[] = null;
			if (fieldCount > 0)
			{
				fields = new Object[fieldCount];
				int pos = 0;
				if ((type == SelectionType.ACCESS) || (type == SelectionType.UPDATE) || (type == SelectionType.MYSITES))
				{
					fields[pos++] = sessionManager().getCurrentSessionUserId();
				}
				if (term != null)
				{
					fields[pos++] = term;
				}
				if (ofType != null)
				{
					if (ofType instanceof String)
					{
						// type criteria is a simple String value
						fields[pos++] = ofType;
					}
					else if (ofType instanceof String[])
					{
						for (int i = 0; i < ((String[]) ofType).length; i++)
						{
							// of type String[]
							fields[pos++] = (String) ((String[]) ofType)[i];
						}
					}
					else if (ofType instanceof List)
					{
						for (Iterator l = ((List) ofType).iterator(); l.hasNext();)
						{
							// of type List
							fields[pos++] = l.next();
						}
					}
					else if (ofType instanceof Set)
					{
						for (Iterator l = ((Set) ofType).iterator(); l.hasNext();)
						{
							// of type Set
							fields[pos++] = l.next();
						}
					}
				}
				if (criteria != null)
				{
					criteria = "%" + criteria + "%";
					fields[pos++] = criteria;
				}
				if (instPrefix != null)
				{
					instPrefix = instPrefix + " %";
					fields[pos++] = instPrefix;
				}
				if ((propertyCriteria != null) && (propertyCriteria.size() > 0))
				{
					for (Iterator i = propertyCriteria.entrySet().iterator(); i.hasNext();)
					{
						Map.Entry entry = (Map.Entry) i.next();
						String name = (String) entry.getKey();
						String value = (String) entry.getValue();
						fields[pos++] = name;
						fields[pos++] = "%" + value + "%";
					}
				}
				if (type == SelectionType.JOINABLE)
				{
					fields[pos++] = sessionManager().getCurrentSessionUserId();
				}
			}

			List rv = null;

			// where has a trailing 'and ' to remove
			if ((where.length() > 5) && (where.substring(where.length() - 5).equals(" and ")))
			{
				where.setLength(where.length() - 5);
			}

			// paging
			if (page != null)
			{
				// adjust to the size of the set found
				// page.validate(rv.size());
				rv = getSiteSelectedResources(where.toString(), order, fields, page.getFirst(), page.getLast(), join);
			}
			else
			{
				rv = getSiteSelectedResources(where.toString(), order, fields, join);
			}
			return rv;
		}
		
		protected List getSiteSelectedResources(String where, String order, Object[] values, String join)
		{
			if (order == null)
			{
				order = m_resourceTableName + "." + m_resourceTableSortField1
						+ (m_resourceTableSortField2 == null ? "" : "," + m_resourceTableName + "." + m_resourceTableSortField2);
			}
			if (order != null)
			{
				if (!order.trim().contains("SAKAI_SITE.TITLE"))
				{
					order = order + ",SAKAI_SITE.TITLE";
				}
			}
			if (where == null) where = "";

			// read all resources from the db with a where
			String sql = "select " + fieldList(m_resourceTableReadFields, null) + ",ARCHIVES_TERM.SUFFIX, ARCHIVES_TERM.ID from " + m_resourceTableName
					+ ((join == null) ? "" : ("," + join)) + ((where.length() > 0) ? (" where " + where) : "") + " order by " + order;
					
			List all = m_sql.dbRead(sql, values, m_reader);

			return all;
		}
		
		protected List getSiteSelectedResources(String where, String order, Object[] values, int first, int last, String join)
		{
			Object[] fields;
			String sql;

			if (order == null)
			{
				order = m_resourceTableName + "." + m_resourceTableSortField1
						+ (m_resourceTableSortField2 == null ? "" : "," + m_resourceTableName + "." + m_resourceTableSortField2);
			}

			if ("oracle".equals(m_sql.getVendor()))
			{
				if (values != null)
				{
					fields = new Object[2 + values.length];
					System.arraycopy(values, 0, fields, 0, values.length);
				}
				else
				{
					fields = new Object[2];
				}

				// use Oracle RANK function, adding the id field to the order to assure a unique ranking
				sql = "select " + /* fieldList(m_resourceTableReadFields, null) */"*" + " from" + " (select "
						+ fieldList(m_resourceTableReadFields, null) + " ,RANK() OVER" + " (order by " + order + ","
						+ m_resourceTableName + "." + m_resourceTableIdField + ") as rank" + " from " + m_resourceTableName
						+ ((join == null) ? "" : ("," + join)) + (((where != null) && (where.length() > 0)) ? (" where " + where) : "")
						+ " order by " + order + "," + m_resourceTableName + "." + m_resourceTableIdField + " )"
						+ " where rank between ? and ?";
				fields[fields.length - 2] = new Long(first);
				fields[fields.length - 1] = new Long(last);
			}
			else if ("mysql".equals(m_sql.getVendor()))
			{
				fields = values;
				if (order != null)
				{
					if (!order.trim().contains("SAKAI_SITE.TITLE"))
					{
						order = order + ",SAKAI_SITE.TITLE";
					}
				}
				// use MySQL LIMIT clause
				sql = "select " + fieldList(m_resourceTableReadFields, null) + ",ARCHIVES_TERM.SUFFIX, ARCHIVES_TERM.ID from " + m_resourceTableName
						+ ((join == null) ? "" : ("," + join)) + (((where != null) && (where.length() > 0)) ? (" where " + where) : "")
						+ " order by " + order
						+ (m_resourceTableSortField2 == null ? "" : "," + m_resourceTableName + "." + m_resourceTableSortField2)
						+ " limit " + (last - first + 1) + " offset " + (first - 1);
						
			}
			else
			// if ("hsqldb".equals(m_sql.getVendor()))
			{
				// use SQL2000 LIMIT clause
				fields = values;
				sql = "select " + "limit " + (first - 1) + " " + (last - first + 1) + " " + fieldList(m_resourceTableReadFields, null)
						+ " from " + m_resourceTableName + ((join == null) ? "" : ("," + join))
						+ (((where != null) && (where.length() > 0)) ? (" where " + where) : "") + " order by " + order + ","
						+ m_resourceTableName + "." + m_resourceTableSortField1
						+ (m_resourceTableSortField2 == null ? "" : "," + m_resourceTableName + "." + m_resourceTableSortField2);
			}

			List rv = m_sql.dbRead(sql, fields, m_reader);

			return rv;
		}			


		/**
		 * {@inheritDoc}
		 */
		public List getSiteTypes()
		{
			String statement = "select distinct TYPE from SAKAI_SITE order by TYPE";

			List rv = sqlService().dbRead(statement);

			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getSiteSkin(final String siteId)
		{
			if (siteId == null) return m_service.adjustSkin(null, true);

			// let the db do the work
			String statement = "select SKIN, PUBLISHED from SAKAI_SITE where SITE_ID = ?";
			Object fields[] = new Object[1];
			fields[0] = caseId(siteId);

			List rv = sqlService().dbRead(statement, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						String skin = result.getString(1);
						int published = result.getInt(2);

						// adjust the skin value
						skin = m_service.adjustSkin(skin, (published == 1));

						return skin;
					}
					catch (SQLException e)
					{
						M_log.warn("getSiteSkin: " + siteId + " : " + e);
						return null;
					}
				}
			});

			if ((rv != null) && (rv.size() > 0))
			{
				return (String) rv.get(0);
			}

			return m_service.adjustSkin(null, true);
		}

		/**
		 * {@inheritDoc}
		 */
		public int countSites(SelectionType type, Object ofType, String criteria, Integer term, String instPrefix, Map propertyCriteria)
		{
			// if we are joining, start our where with the join clauses
			StringBuffer where = new StringBuffer();
			if ((type == SelectionType.ACCESS) || (type == SelectionType.UPDATE) || (type == SelectionType.MYSITES))
			{
				// join on site id and also select the proper user
				where.append("SAKAI_SITE.SITE_ID = SAKAI_SITE_USER.SITE_ID and SAKAI_SITE_USER.USER_ID = ? and ");
			}

			// ignore user sites
			if (type.isIgnoreUser()) where.append("SAKAI_SITE.IS_USER = '0' and ");
			// reject special sites
			if (type.isIgnoreSpecial()) where.append("SAKAI_SITE.IS_SPECIAL = '0' and ");
			// reject unpublished sites
			if (type.isIgnoreUnpublished()) where.append("SAKAI_SITE.PUBLISHED = 1 and ");

			if (term != null)
			{
				where.append("ARCHIVES_SITE_TERM.TERM_ID = ? and ");
			}

			// reject unwanted site types
			if (ofType != null)
			{
				if (ofType instanceof String)
				{
					// type criteria is a simple String value
					where.append("SAKAI_SITE.TYPE = ? and ");
				}
				else if (ofType instanceof String[] || ofType instanceof List || ofType instanceof Set)
				{
					// more complex type criteria
					int size = 0;
					if (ofType instanceof String[])
					{
						size = ((String[]) ofType).length;
					}
					else if (ofType instanceof List)
					{
						size = ((List) ofType).size();
					}
					else if (ofType instanceof Set)
					{
						size = ((Set) ofType).size();
					}
					if (size > 0)
					{
						where.append("SAKAI_SITE.TYPE IN (?");
						for (int i = 1; i < size; i++)
						{
							where.append(",?");
						}
						where.append(") and ");
					}
				}
			}

			// reject non-joinable sites
			if (type == SelectionType.JOINABLE) where.append("SAKAI_SITE.JOINABLE = '1' and ");
			// check for pub view status
			if (type == SelectionType.PUBVIEW) where.append("SAKAI_SITE.PUBVIEW = '1' and ");
			// check criteria
			if (criteria != null) where.append("UPPER(SAKAI_SITE.TITLE) like UPPER(?) and ");
			// institution title prefix
			if (instPrefix != null) where.append("SAKAI_SITE.TITLE like ? and ");
			// update permission
			if (type == SelectionType.UPDATE) where.append("SAKAI_SITE_USER.PERMISSION <= -1 and ");
			// access permission
			if (type == SelectionType.ACCESS) where.append("SAKAI_SITE_USER.PERMISSION <= SAKAI_SITE.PUBLISHED and ");
			// joinable requires NOT access permission
			if (type == SelectionType.JOINABLE)
				where
						.append("SAKAI_SITE.SITE_ID not in (select SITE_ID from SAKAI_SITE_USER where USER_ID = ? and PERMISSION <= PUBLISHED) and ");

			// do we need a join?
			String join = "ARCHIVES_SITE_TERM";
			if ((type == SelectionType.ACCESS) || (type == SelectionType.UPDATE) || (type == SelectionType.MYSITES))
			{
				// join with the SITE_USER table
				join = "ARCHIVES_SITE_TERM, SAKAI_SITE_USER";
			}

			// add propertyCriteria if specified
			if ((propertyCriteria != null) && (propertyCriteria.size() > 0))
			{
				for (int i = 0; i < propertyCriteria.size(); i++)
				{
					where
							.append("SAKAI_SITE.SITE_ID in (select SITE_ID from SAKAI_SITE_PROPERTY where NAME = ? and UPPER(VALUE) like UPPER(?)) and ");
				}
			}

			where.append("SAKAI_SITE.SITE_ID = ARCHIVES_SITE_TERM.SITE_ID ");

			int fieldCount = 0;
			if (term != null) fieldCount++;
			if (ofType != null)
			{
				if (ofType instanceof String)
				{
					// type criteria is a simple String value
					fieldCount++;
				}
				// more complex types
				else if (ofType instanceof String[])
				{
					fieldCount += ((String[]) ofType).length;
				}
				else if (ofType instanceof List)
				{
					fieldCount += ((List) ofType).size();
				}
				else if (ofType instanceof Set)
				{
					fieldCount += ((Set) ofType).size();
				}
			}
			if (criteria != null) fieldCount += 1;
			if (instPrefix != null) fieldCount ++;
			if ((type == SelectionType.JOINABLE) || (type == SelectionType.ACCESS) || (type == SelectionType.UPDATE) || (type == SelectionType.MYSITES)) fieldCount++;
			if (propertyCriteria != null) fieldCount += (2 * propertyCriteria.size());
			Object fields[] = null;
			if (fieldCount > 0)
			{
				fields = new Object[fieldCount];
				int pos = 0;
				if ((type == SelectionType.ACCESS) || (type == SelectionType.UPDATE) || (type == SelectionType.MYSITES))
				{
					fields[pos++] = sessionManager().getCurrentSessionUserId();
				}
				if (term != null)
				{
					fields[pos++] = term;
				}
				if (ofType != null)
				{
					if (ofType instanceof String)
					{
						// type criteria is a simple String value
						fields[pos++] = ofType;
					}
					else if (ofType instanceof String[])
					{
						for (int i = 0; i < ((String[]) ofType).length; i++)
						{
							// of type String[]
							fields[pos++] = (String) ((String[]) ofType)[i];
						}
					}
					else if (ofType instanceof List)
					{
						for (Iterator l = ((List) ofType).iterator(); l.hasNext();)
						{
							// of type List
							fields[pos++] = l.next();
						}
					}
					else if (ofType instanceof Set)
					{
						for (Iterator l = ((Set) ofType).iterator(); l.hasNext();)
						{
							// of type Set
							fields[pos++] = l.next();
						}
					}
				}
				if (criteria != null)
				{
					criteria = "%" + criteria + "%";
					fields[pos++] = criteria;
				}
				if (instPrefix != null)
				{
					instPrefix = instPrefix + " %";
					fields[pos++] = instPrefix;
				}
				if ((propertyCriteria != null) && (propertyCriteria.size() > 0))
				{
					for (Iterator i = propertyCriteria.entrySet().iterator(); i.hasNext();)
					{
						Map.Entry entry = (Map.Entry) i.next();
						String name = (String) entry.getKey();
						String value = (String) entry.getValue();
						fields[pos++] = name;
						fields[pos++] = "%" + value + "%";
					}
				}
				if (type == SelectionType.JOINABLE)
				{
					fields[pos++] = sessionManager().getCurrentSessionUserId();
				}
			}

			// where has a trailing 'and ' to remove
			if ((where.length() > 5) && (where.substring(where.length() - 5).equals(" and ")))
			{
				where.setLength(where.length() - 5);
			}

			int rv = countSelectedResources(where.toString(), fields, join);

			return rv;
		}

		/**
		 * Access the ToolConfiguration that has this id, if one is defined, else return null. The tool may be on any SitePage in the site.
		 * 
		 * @param id
		 *        The id of the tool.
		 * @return The ToolConfiguration that has this id, if one is defined, else return null.
		 */
		public ToolConfiguration findTool(final String id)
		{
			String sql = "select REGISTRATION, SAKAI_SITE_TOOL.TITLE, LAYOUT_HINTS, SAKAI_SITE_TOOL.SITE_ID, PAGE_ID, SKIN, PUBLISHED, PAGE_ORDER from SAKAI_SITE_TOOL, SAKAI_SITE"
					+ " where SAKAI_SITE_TOOL.SITE_ID = SAKAI_SITE.SITE_ID" + " and TOOL_ID = ?";

			Object fields[] = new Object[1];
			fields[0] = id;

			List found = m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// get the fields
						String registration = result.getString(1);
						String title = result.getString(2);
						String layout = result.getString(3);
						String siteId = result.getString(4);
						String pageId = result.getString(5);
						String skin = result.getString(6);
						int published = result.getInt(7);
						int pageOrder = result.getInt(8);

						// adjust the skin value
						skin = m_service.adjustSkin(skin, (published == 1));

						// make the tool
						BaseToolConfiguration tool = new BaseToolConfiguration(id, registration, title, layout, pageId, siteId,
								skin, pageOrder);

						return tool;
					}
					catch (SQLException e)
					{
						M_log.warn("findTool: " + id + " : " + e);
						return null;
					}
				}
			});

			if (found.size() > 1)
			{
				M_log.warn("findTool: multiple results for tool id: " + id);
			}

			ToolConfiguration rv = null;
			if (found.size() > 0)
			{
				rv = (ToolConfiguration) found.get(0);
			}

			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public SitePage findPage(final String id)
		{
			String sql = "select PAGE_ID, SAKAI_SITE_PAGE.TITLE, LAYOUT, SAKAI_SITE_PAGE.SITE_ID, SKIN, PUBLISHED, POPUP "
					+ "from SAKAI_SITE_PAGE, SAKAI_SITE where SAKAI_SITE_PAGE.SITE_ID = SAKAI_SITE.SITE_ID " + "and PAGE_ID = ?";

			Object fields[] = new Object[1];
			fields[0] = id;

			List found = m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// get the fields
						String pageId = result.getString(1);
						String title = result.getString(2);
						String layout = result.getString(3);
						String siteId = result.getString(4);
						String skin = result.getString(5);
						int published = result.getInt(6);
						boolean popup = "1".equals(result.getString(7)) ? true : false;

						// adjust the skin value
						skin = m_service.adjustSkin(skin, (published == 1));

						// make the page
						BaseSitePage page = new BaseSitePage(pageId, title, layout, popup, siteId, skin);

						return page;
					}
					catch (SQLException e)
					{
						M_log.warn("findPage: " + id + " : " + e);
						return null;
					}
				}
			});

			if (found.size() > 1)
			{
				M_log.warn("findPage: multiple results for page id: " + id);
			}

			SitePage rv = null;
			if (found.size() > 0)
			{
				rv = (SitePage) found.get(0);
			}

			return rv;
		}

		/**
		 * Access the Site id for the page with this id.
		 * 
		 * @param id
		 *        The id of the page.
		 * @return The Site id for the page with this id, if the page is found, else null.
		 */
		public String findPageSiteId(String id)
		{
			String sql = "select SITE_ID from SAKAI_SITE_PAGE where PAGE_ID = ?";
			Object fields[] = new Object[1];
			fields[0] = id;

			List found = m_sql.dbRead(sql, fields, null);

			if (found.size() > 1)
			{
				M_log.warn("findPageSiteId: multiple results for page id: " + id);
			}

			String rv = null;
			if (found.size() > 0)
			{
				rv = (String) found.get(0);
			}

			return rv;
		}

		/**
		 * {@inheritDoc}
		 */
		public String findGroupSiteId(String id)
		{
			String sql = "select SS.SITE_ID from SAKAI_SITE_GROUP SS where SS.GROUP_ID = ?";
			Object fields[] = new Object[1];
			fields[0] = id;

			List found = m_sql.dbRead(sql, fields, null);

			if (found.size() > 1)
			{
				M_log.warn("findGroupSiteId: multiple results for page id: " + id);
			}

			String rv = null;
			if (found.size() > 0)
			{
				rv = (String) found.get(0);
			}

			return rv;
		}

		/**
		 * Access the Site id for the tool with this id.
		 * 
		 * @param id
		 *        The id of the tool.
		 * @return The Site id for the tool with this id, if the tool is found, else null.
		 */
		public String findToolSiteId(String id)
		{
			String sql = "select SITE_ID from SAKAI_SITE_TOOL where TOOL_ID = ?";
			Object fields[] = new Object[1];
			fields[0] = id;

			List found = m_sql.dbRead(sql, fields, null);

			if (found.size() > 1)
			{
				M_log.warn("findToolSiteId: multiple results for page id: " + id);
			}

			String rv = null;
			if (found.size() > 0)
			{
				rv = (String) found.get(0);
			}

			return rv;
		}

		/**
		 * Establish the internal security for this site. Previous security settings are replaced for this site. Assigning a user with update implies the two reads; assigning a user with unp read implies the other read.
		 * 
		 * @param siteId
		 *        The id of the site.
		 * @param updateUsers
		 *        The set of String User Ids who have update access.
		 * @param visitUnpUsers
		 *        The set of String User Ids who have visit unpublished access.
		 * @param visitUsers
		 *        The set of String User Ids who have visit access.
		 */
		public void setSiteSecurity(final String siteId, Set updateUsers, Set visitUnpUsers, Set visitUsers)
		{
			// normalize the input parameters - remove any user in more than one set

			// adjust visitUsers to remove any that are in visitUnpUsers or updateUsers
			Set targetVisit = new HashSet();
			targetVisit.addAll(visitUsers);
			targetVisit.removeAll(visitUnpUsers);
			targetVisit.removeAll(updateUsers);

			// adjust visitUnpUsers to remove any that are in updateUsers
			Set targetUnp = new HashSet();
			targetUnp.addAll(visitUnpUsers);
			targetUnp.removeAll(updateUsers);

			Set targetUpdate = updateUsers;

			// read existing
			String statement = "select USER_ID, PERMISSION from SAKAI_SITE_USER " + "where SITE_ID = ?";
			Object[] fields = new Object[1];
			fields[0] = caseId(siteId);

			// collect the current data in three sets, update, unp, visit
			final Set existingUpdate = new HashSet();
			final Set existingUnp = new HashSet();
			final Set existingVisit = new HashSet();

			m_sql.dbRead(statement, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						String userId = result.getString(1);
						int permission = result.getInt(2);
						if (permission == -1)
						{
							existingUpdate.add(userId);
						}
						else if (permission == 0)
						{
							existingUnp.add(userId);
						}
						else if (permission == 1)
						{
							existingVisit.add(userId);
						}
						else
						{
							M_log.warn("setSiteSecurity: invalid permission " + permission + " site: " + siteId + " user: "
									+ userId);
						}
					}
					catch (Throwable ignore)
					{
						return null;
					}
					return null;
				}
			});

			// compute the delete and insert sets for each of the three permissions

			// delete if the user is in targetUpdate, but it is already in one of the other categories
			Set updDeletes = new HashSet();
			updDeletes.addAll(existingUnp);
			updDeletes.addAll(existingVisit);
			updDeletes.retainAll(targetUpdate);

			// also delete if the user is in the existing and not in the target
			Set obsolete = new HashSet();
			obsolete.addAll(existingUpdate);
			obsolete.removeAll(targetUpdate);
			updDeletes.addAll(obsolete);

			// insert if the user is in targetUpdate, but is not already in update
			Set updInserts = new HashSet();
			updInserts.addAll(targetUpdate);
			updInserts.removeAll(existingUpdate);

			// delete if the user is in targetUnp, but it is already in one of the other categories
			Set unpDeletes = new HashSet();
			unpDeletes.addAll(existingUpdate);
			unpDeletes.addAll(existingVisit);
			unpDeletes.retainAll(targetUnp);

			// also delete if the user is in the existing and not in the target
			obsolete.clear();
			obsolete.addAll(existingUnp);
			obsolete.removeAll(targetUnp);
			unpDeletes.addAll(obsolete);

			// insert if the user is in targetUnp, but is not already in unp
			Set unpInserts = new HashSet();
			unpInserts.addAll(targetUnp);
			unpInserts.removeAll(existingUnp);

			// delete if the user is in targetVisit, but it is already in one of the other categories
			Set visitDeletes = new HashSet();
			visitDeletes.addAll(existingUpdate);
			visitDeletes.addAll(existingUnp);
			visitDeletes.retainAll(targetVisit);

			// also delete if the user is in the existing and not in the target
			obsolete.clear();
			obsolete.addAll(existingVisit);
			obsolete.removeAll(targetVisit);
			visitDeletes.addAll(obsolete);

			// insert if the user is in targetVisit, but is not already in visit
			Set visitInserts = new HashSet();
			visitInserts.addAll(targetVisit);
			visitInserts.removeAll(existingVisit);

			// if there's anything to do
			if (updDeletes.size() > 0 || updInserts.size() > 0 || unpDeletes.size() > 0 || unpInserts.size() > 0
					|| visitDeletes.size() > 0 || visitInserts.size() > 0)
			{
				// delete old, write new, each in it's own transaction to avoid possible deadlock
				// involving modifications to multiple rows in a transaction
				fields = new Object[2];
				fields[0] = caseId(siteId);

				// delete
				statement = "delete from SAKAI_SITE_USER where SITE_ID = ? and USER_ID = ?";
				for (Iterator i = updDeletes.iterator(); i.hasNext();)
				{
					String userId = (String) i.next();
					fields[1] = userId;
					m_sql.dbWrite(statement, fields);
				}
				for (Iterator i = unpDeletes.iterator(); i.hasNext();)
				{
					String userId = (String) i.next();
					fields[1] = userId;
					m_sql.dbWrite(statement, fields);
				}
				for (Iterator i = visitDeletes.iterator(); i.hasNext();)
				{
					String userId = (String) i.next();
					fields[1] = userId;
					m_sql.dbWrite(statement, fields);
				}

				// insert
				statement = "insert into SAKAI_SITE_USER (SITE_ID, USER_ID, PERMISSION) values (?, ?, ?)";
				fields = new Object[3];
				fields[0] = caseId(siteId);

				fields[2] = new Integer(-1);
				for (Iterator i = updInserts.iterator(); i.hasNext();)
				{
					String userId = (String) i.next();
					fields[1] = userId;
					m_sql.dbWrite(statement, fields);
				}

				fields[2] = new Integer(0);
				for (Iterator i = unpInserts.iterator(); i.hasNext();)
				{
					String userId = (String) i.next();
					fields[1] = userId;
					m_sql.dbWrite(statement, fields);
				}

				fields[2] = new Integer(1);
				for (Iterator i = visitInserts.iterator(); i.hasNext();)
				{
					String userId = (String) i.next();
					fields[1] = userId;
					m_sql.dbWrite(statement, fields);
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void setUserSecurity(final String userId, Set updateSites, Set visitUnpSites, Set visitSites)
		{
			// normalize the input parameters - remove any user in more than one set

			// adjust visitSites to remove any that are in visitUnpSites or updateSites
			Set targetVisit = new HashSet();
			targetVisit.addAll(visitSites);
			targetVisit.removeAll(visitUnpSites);
			targetVisit.removeAll(updateSites);

			// adjust visitUnpSites to remove any that are in updateSites
			Set targetUnp = new HashSet();
			targetUnp.addAll(visitUnpSites);
			targetUnp.removeAll(updateSites);

			Set targetUpdate = updateSites;

			// read existing
			String statement = "select SITE_ID, PERMISSION from SAKAI_SITE_USER " + "where USER_ID = ?";
			Object[] fields = new Object[1];
			fields[0] = userId;

			// collect the current data in three sets, update, unp, visit
			final Set existingUpdate = new HashSet();
			final Set existingUnp = new HashSet();
			final Set existingVisit = new HashSet();

			m_sql.dbRead(statement, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						String siteId = result.getString(1);
						int permission = result.getInt(2);
						if (permission == -1)
						{
							existingUpdate.add(siteId);
						}
						else if (permission == 0)
						{
							existingUnp.add(siteId);
						}
						else if (permission == 1)
						{
							existingVisit.add(siteId);
						}
						else
						{
							M_log.warn("setUserSecurity: invalid permission " + permission + " site: " + siteId + " user: "
									+ userId);
						}
					}
					catch (Throwable ignore)
					{
						return null;
					}
					return null;
				}
			});

			// compute the delete and insert sets for each of the three permissions

			// delete if the site is in targetUpdate, but it is already in one of the other categories
			Set updDeletes = new HashSet();
			updDeletes.addAll(existingUnp);
			updDeletes.addAll(existingVisit);
			updDeletes.retainAll(targetUpdate);

			// also delete if the user is in the existing and not in the target
			Set obsolete = new HashSet();
			obsolete.addAll(existingUpdate);
			obsolete.removeAll(targetUpdate);
			updDeletes.addAll(obsolete);

			// insert if the site is in targetUpdate, but is not already in update
			Set updInserts = new HashSet();
			updInserts.addAll(targetUpdate);
			updInserts.removeAll(existingUpdate);

			// delete if the site is in targetUnp, but it is already in one of the other categories
			Set unpDeletes = new HashSet();
			unpDeletes.addAll(existingUpdate);
			unpDeletes.addAll(existingVisit);
			unpDeletes.retainAll(targetUnp);

			// also delete if the user is in the existing and not in the target
			obsolete.clear();
			obsolete.addAll(existingUnp);
			obsolete.removeAll(targetUnp);
			unpDeletes.addAll(obsolete);

			// insert if the site is in targetUnp, but is not already in unp
			Set unpInserts = new HashSet();
			unpInserts.addAll(targetUnp);
			unpInserts.removeAll(existingUnp);

			// delete if the site is in targetVisit, but it is already in one of the other categories
			Set visitDeletes = new HashSet();
			visitDeletes.addAll(existingUpdate);
			visitDeletes.addAll(existingUnp);
			visitDeletes.retainAll(targetVisit);

			// also delete if the user is in the existing and not in the target
			obsolete.clear();
			obsolete.addAll(existingVisit);
			obsolete.removeAll(targetVisit);
			visitDeletes.addAll(obsolete);

			// insert if the site is in targetVisit, but is not already in visit
			Set visitInserts = new HashSet();
			visitInserts.addAll(targetVisit);
			visitInserts.removeAll(existingVisit);

			// if there's anything to do
			if (updDeletes.size() > 0 || updInserts.size() > 0 || unpDeletes.size() > 0 || unpInserts.size() > 0
					|| visitDeletes.size() > 0 || visitInserts.size() > 0)
			{
				// delete old, write new, each in it's own transaction to avoid possible deadlock
				// involving modifications to multiple rows in a transaction
				fields = new Object[2];
				fields[1] = userId;

				// delete
				statement = "delete from SAKAI_SITE_USER where SITE_ID = ? and USER_ID = ?";
				for (Iterator i = updDeletes.iterator(); i.hasNext();)
				{
					String siteId = (String) i.next();
					fields[0] = caseId(siteId);
					m_sql.dbWrite(statement, fields);
				}
				for (Iterator i = unpDeletes.iterator(); i.hasNext();)
				{
					String siteId = (String) i.next();
					fields[0] = caseId(siteId);
					m_sql.dbWrite(statement, fields);
				}
				for (Iterator i = visitDeletes.iterator(); i.hasNext();)
				{
					String siteId = (String) i.next();
					fields[0] = caseId(siteId);
					m_sql.dbWrite(statement, fields);
				}

				// insert
				statement = "insert into SAKAI_SITE_USER (SITE_ID, USER_ID, PERMISSION) values (?, ?, ?)";
				fields = new Object[3];
				fields[1] = userId;

				fields[2] = new Integer(-1);
				for (Iterator i = updInserts.iterator(); i.hasNext();)
				{
					String siteId = (String) i.next();
					fields[0] = caseId(siteId);
					m_sql.dbWrite(statement, fields);
				}

				fields[2] = new Integer(0);
				for (Iterator i = unpInserts.iterator(); i.hasNext();)
				{
					String siteId = (String) i.next();
					fields[0] = caseId(siteId);
					m_sql.dbWrite(statement, fields);
				}

				fields[2] = new Integer(1);
				for (Iterator i = visitInserts.iterator(); i.hasNext();)
				{
					String siteId = (String) i.next();
					fields[0] = caseId(siteId);
					m_sql.dbWrite(statement, fields);
				}
			}
		}

		/**
		 * Read site properties from storage into the site's properties.
		 * 
		 * @param edit
		 *        The user to read properties for.
		 */
		public void readSiteProperties(Site site, ResourcePropertiesEdit props)
		{
			super.readProperties(site, props);
		}

		/**
		 * Read site properties and all page and tool properties for the site from storage.
		 * 
		 * @param site
		 *        The site for which properties are desired.
		 */
		public void readAllSiteProperties(Site site)
		{
			// read and un-lazy the site properties
			readSiteProperties(site, ((BaseSite) site).m_properties);
			((BaseResourcePropertiesEdit) ((BaseSite) site).m_properties).setLazy(false);

			// read and unlazy the page properties for the entire site
			readSitePageProperties((BaseSite) site);
			for (Iterator i = site.getPages().iterator(); i.hasNext();)
			{
				BaseSitePage page = (BaseSitePage) i.next();
				((BaseResourcePropertiesEdit) page.m_properties).setLazy(false);
			}

			// read and unlazy the tool properties for the entire site
			readSiteToolProperties((BaseSite) site);
			for (Iterator i = site.getPages().iterator(); i.hasNext();)
			{
				BaseSitePage page = (BaseSitePage) i.next();
				for (Iterator t = page.getTools().iterator(); t.hasNext();)
				{
					BaseToolConfiguration tool = (BaseToolConfiguration) t.next();
					tool.m_configLazy = false;
				}
			}

			// read and unlazy the group properties for the entire site
			readSiteGroupProperties((BaseSite) site);
			for (Iterator i = site.getGroups().iterator(); i.hasNext();)
			{
				BaseGroup group = (BaseGroup) i.next();
				((BaseResourcePropertiesEdit) group.m_properties).setLazy(false);
			}
		}

		/**
		 * Read properties for all pages in the site
		 * 
		 * @param site
		 *        The site to read properties for.
		 */
		protected void readSitePageProperties(final BaseSite site)
		{
			// get the properties from the db for all pages in the site
			String sql = "select PAGE_ID, NAME, VALUE from SAKAI_SITE_PAGE_PROPERTY where ( SITE_ID = ? )";

			Object fields[] = new Object[1];
			fields[0] = site.getId();
			m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// read the fields
						String pageId = result.getString(1);
						String name = result.getString(2);
						String value = result.getString(3);

						// get the page
						BaseSitePage page = (BaseSitePage) site.getPage(pageId);
						if (page != null)
						{
							page.m_properties.addProperty(name, value);
						}

						// nothing to return
						return null;
					}
					catch (SQLException e)
					{
						M_log.warn("readSitePageProperties: " + e);
						return null;
					}
				}
			});
		}

		/**
		 * Read properties for all tools in the site
		 * 
		 * @param site
		 *        The site to read properties for.
		 */
		protected void readSiteToolProperties(final BaseSite site)
		{
			// get the properties from the db for all pages in the site
			String sql = "select TOOL_ID, NAME, VALUE from SAKAI_SITE_TOOL_PROPERTY where ( SITE_ID = ? )";

			Object fields[] = new Object[1];
			fields[0] = site.getId();
			m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// read the fields
						String toolId = result.getString(1);
						String name = result.getString(2);
						String value = result.getString(3);

						// get the page
						BaseToolConfiguration tool = (BaseToolConfiguration) site.getTool(toolId);
						if (tool != null)
						{
							tool.getMyConfig().setProperty(name, value);
						}

						// nothing to return
						return null;
					}
					catch (SQLException e)
					{
						M_log.warn("readSitePageProperties: " + e);
						return null;
					}
				}
			});
		}

		/**
		 * Read properties for all groups in the site
		 * 
		 * @param site
		 *        The site to read group properties for.
		 */
		protected void readSiteGroupProperties(final BaseSite site)
		{
			// get the properties from the db for all pages in the site
			String sql = "select GROUP_ID, NAME, VALUE from SAKAI_SITE_GROUP_PROPERTY where ( SITE_ID = ? )";

			Object fields[] = new Object[1];
			fields[0] = site.getId();
			m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// read the fields
						String groupId = result.getString(1);
						String name = result.getString(2);
						String value = result.getString(3);

						// get the group
						BaseGroup group = (BaseGroup) site.getGroup(groupId);
						if (group != null)
						{
							group.m_properties.addProperty(name, value);
						}

						// nothing to return
						return null;
					}
					catch (SQLException e)
					{
						M_log.warn("readSiteGroupProperties: " + e);
						return null;
					}
				}
			});
		}

		/**
		 * Read page properties from storage into the page's properties.
		 * 
		 * @param page
		 *        The page for which properties are desired.
		 */
		public void readPageProperties(SitePage page, ResourcePropertiesEdit props)
		{
			super.readProperties(null, "SAKAI_SITE_PAGE_PROPERTY", "PAGE_ID", page.getId(), props);
		}

		/**
		 * Read tool properties from storage into the tool's properties.
		 * 
		 * @param tool
		 *        The tool for which properties are desired.
		 */
		public void readToolProperties(ToolConfiguration tool, Properties props)
		{
			super.readProperties(null, "SAKAI_SITE_TOOL_PROPERTY", "TOOL_ID", tool.getId(), props);
		}

		/**
		 * Read group properties from storage into the group's properties.
		 * 
		 * @param group
		 *        The group for which properties are desired.
		 */
		public void readGroupProperties(Group group, Properties props)
		{
			super.readProperties(null, "SAKAI_SITE_GROUP_PROPERTY", "GROUP_ID", group.getId(), props);
		}

		/**
		 * Read site pages from storage into the site's pages.
		 * 
		 * @param site
		 *        The site for which pages are desired.
		 */
		public void readSitePages(final Site site, final ResourceVector pages)
		{
			// read all resources from the db with a where
			String sql = "select PAGE_ID, TITLE, LAYOUT, POPUP from SAKAI_SITE_PAGE" + " where SITE_ID = ?"
					+ " order by SITE_ORDER ASC";

			Object fields[] = new Object[1];
			fields[0] = site.getId();

			List all = m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// get the fields
						String id = result.getString(1);
						String title = result.getString(2);
						String layout = result.getString(3);
						boolean popup = "1".equals(result.getString(4)) ? true : false;

						// make the page
						BaseSitePage page = new BaseSitePage(site, id, title, layout, popup);

						// add it to the pages
						pages.add(page);

						return null;
					}
					catch (SQLException ignore)
					{
						return null;
					}
				}
			});
		}

		/**
		 * Read site page tools from storage into the page's tools.
		 * 
		 * @param page
		 *        The page for which tools are desired.
		 */
		public void readPageTools(final SitePage page, final ResourceVector tools)
		{
			// read all resources from the db with a where
			String sql = "select TOOL_ID, REGISTRATION, TITLE, LAYOUT_HINTS, PAGE_ORDER from SAKAI_SITE_TOOL"
					+ " where PAGE_ID = ?" + " order by PAGE_ORDER ASC";

			Object fields[] = new Object[1];
			fields[0] = page.getId();

			List all = m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// get the fields
						String id = result.getString(1);
						String registration = result.getString(2);
						String title = result.getString(3);
						String layout = result.getString(4);
						int pageOrder = result.getInt(5);

						// make the tool
						BaseToolConfiguration tool = new BaseToolConfiguration(page, id, registration, title, layout, pageOrder);

						// add it to the tools
						tools.add(tool);

						return null;
					}
					catch (SQLException ignore)
					{
						return null;
					}
				}
			});
		}

		/**
		 * Read tools for all pages from storage into the site's page's tools.
		 * 
		 * @param site
		 *        The site for which tools are desired.
		 */
		public void readSiteTools(final Site site)
		{
			// read all tools for the site
			String sql = "select TOOL_ID, PAGE_ID, REGISTRATION, TITLE, LAYOUT_HINTS, PAGE_ORDER from SAKAI_SITE_TOOL"
					+ " where SITE_ID = ?" + " order by PAGE_ID, PAGE_ORDER ASC";

			Object fields[] = new Object[1];
			fields[0] = site.getId();

			List all = m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// get the fields
						String id = result.getString(1);
						String pageId = result.getString(2);
						String registration = result.getString(3);
						String title = result.getString(4);
						String layout = result.getString(5);
						int pageOrder = result.getInt(6);

						// get the page
						BaseSitePage page = (BaseSitePage) site.getPage(pageId);
						if ((page != null) && (page.m_toolsLazy))
						{
							// make the tool
							BaseToolConfiguration tool = new BaseToolConfiguration(page, id, registration, title, layout, pageOrder);

							// add it to the tools
							page.m_tools.add(tool);
						}

						return null;
					}
					catch (SQLException ignore)
					{
						return null;
					}
				}
			});

			// unlazy the page tools
			for (Iterator i = site.getPages().iterator(); i.hasNext();)
			{
				BaseSitePage page = (BaseSitePage) i.next();
				page.m_toolsLazy = false;
			}
		}

		/**
		 * @inheritDoc
		 */
		public void readSiteGroups(final Site site, final Collection groups)
		{
			String sql = "select SS.GROUP_ID, SS.TITLE, SS.DESCRIPTION " + "from SAKAI_SITE_GROUP SS where SS.SITE_ID = ?";
			// TODO: order by? title? -ggolden

			Object fields[] = new Object[1];
			fields[0] = site.getId();

			List all = m_sql.dbRead(sql, fields, new SqlReader()
			{
				public Object readSqlResultRecord(ResultSet result)
				{
					try
					{
						// get the fields
						String groupId = result.getString(1);
						String title = result.getString(2);
						String description = result.getString(3);

						// make the group
						BaseGroup group = new BaseGroup(groupId, title, description, site);

						// add it to the groups
						groups.add(group);

						return null;
					}
					catch (SQLException e)
					{
						M_log.warn("readSiteGroups: " + site.getId() + " : " + e);
						return null;
					}
				}
			});
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
		protected Object[] fields(String id, Site edit, boolean idAgain)
		{
			Object[] rv = new Object[idAgain ? 19 : 18];
			rv[0] = caseId(id);
			if (idAgain)
			{
				rv[18] = rv[0];
			}

			if (edit == null)
			{
				String current = sessionManager().getCurrentSessionUserId();

				// if no current user, since we are working up a new user record, use the user id as creator...
				if (current == null) current = "";

				Time now = timeService().newTime();

				rv[1] = "";
				rv[2] = "";
				rv[3] = "";
				rv[4] = "";
				rv[5] = "";
				rv[6] = "";
				rv[7] = "";
				rv[8] = new Integer(0);
				rv[9] = "0";
				rv[10] = "0";
				rv[11] = "";
				rv[12] = isSpecialSite(id) ? "1" : "0";
				rv[13] = isUserSite(id) ? "1" : "0";
				rv[14] = current;
				rv[15] = current;
				rv[16] = now;
				rv[17] = now;
			}

			else
			{
				rv[1] = StringUtil.trimToZero(((BaseSite) edit).m_title);
				rv[2] = StringUtil.trimToZero(((BaseSite) edit).m_type);
				rv[3] = StringUtil.trimToZero(((BaseSite) edit).m_shortDescription);
				rv[4] = StringUtil.trimToZero(((BaseSite) edit).m_description);
				rv[5] = StringUtil.trimToZero(((BaseSite) edit).m_icon);
				rv[6] = StringUtil.trimToZero(((BaseSite) edit).m_info);
				rv[7] = StringUtil.trimToZero(((BaseSite) edit).m_skin);
				rv[8] = new Integer((((BaseSite) edit).m_published) ? 1 : 0);
				rv[9] = ((((BaseSite) edit).m_joinable) ? "1" : "0");
				rv[10] = ((((BaseSite) edit).m_pubView) ? "1" : "0");
				rv[11] = StringUtil.trimToZero(((BaseSite) edit).m_joinerRole);
				rv[12] = isSpecialSite(id) ? "1" : "0";
				rv[13] = isUserSite(id) ? "1" : "0";
				rv[14] = StringUtil.trimToZero(((BaseSite) edit).m_createdUserId);
				rv[15] = StringUtil.trimToZero(((BaseSite) edit).m_lastModifiedUserId);
				rv[16] = edit.getCreatedTime();
				rv[17] = edit.getModifiedTime();
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
			String termSuffix = null;
			String termId = null;
			try
			{
				String id = result.getString(1);
				String title = result.getString(2);
				String type = result.getString(3);
				String shortDesc = result.getString(4);
				String description = result.getString(5);
				String icon = result.getString(6);
				String info = result.getString(7);
				String skin = result.getString(8);
				boolean published = result.getInt(9) == 1;
				boolean joinable = "1".equals(result.getString(10)) ? true : false;
				boolean pubView = "1".equals(result.getString(11)) ? true : false;
				String joinRole = result.getString(12);
				boolean isSpecial = "1".equals(result.getString(13)) ? true : false;
				boolean isUser = "1".equals(result.getString(14)) ? true : false;
				String createdBy = result.getString(15);
				String modifiedBy = result.getString(16);
				java.sql.Timestamp ts = result.getTimestamp(17, sqlService().getCal());
				Time createdOn = null;
				if (ts != null)
				{
					createdOn = timeService().newTime(ts.getTime());
				}
				ts = result.getTimestamp(18, sqlService().getCal());
				Time modifiedOn = null;
				if (ts != null)
				{
					modifiedOn = timeService().newTime(ts.getTime());
				}
				ResultSetMetaData rsmd = result.getMetaData();
				int noOfColumns = rsmd.getColumnCount();
				if (noOfColumns >= 19)
				{
					if (result.getString(19) != null) termSuffix = result.getString(19);
				}
				if (noOfColumns >= 20)
				{
					if (result.getString(20) != null) termId = result.getString(20);
				}
				// create the Resource from these fields
				return new BaseSite(id, title, type, shortDesc, description, icon, info, skin, published, joinable, pubView, joinRole, isSpecial,
						isUser, createdBy, createdOn, modifiedBy, modifiedOn, termSuffix, termId);
			}
			catch (SQLException e)
			{
				M_log.warn("readSqlResultRecord: " + e);
				return null;
			}
		}
	}
	/**
		 * the SiteComparator class
		 */
		private class SiteComparator
			implements Comparator
		{
			/**
			 * the criteria
			 */
			String m_criterion = null;
			String m_asc = null;

			/**
			 * constructor
			 * @param criteria The sort criteria string
			 * @param asc The sort order string. TRUE_STRING if ascending; "false" otherwise.
			 */
			public SiteComparator (String criterion, String asc)
			{
				m_criterion = criterion;
				m_asc = asc;

			}	// constructor

			/**
			* implementing the Comparator compare function
			* @param o1 The first object
			* @param o2 The second object
			* @return The compare result. 1 is o1 < o2; -1 otherwise
			*/
			public int compare ( Object o1, Object o2)
			{
				int result = -1;

				if (m_criterion.equals (SORTED_BY_CREATION_DATE))
				{
					// sort by the site's creation date
					Time t1 = null;
					Time t2 = null;

					// get the times
					try
					{
						t1 = ((Site)o1).getProperties().getTimeProperty(ResourceProperties.PROP_CREATION_DATE);
					}
					catch (EntityPropertyNotDefinedException e)
					{
					}
					catch (EntityPropertyTypeException e)
					{
					}

					try
					{
						t2 = ((Site)o2).getProperties().getTimeProperty(ResourceProperties.PROP_CREATION_DATE);
					}
					catch (EntityPropertyNotDefinedException e)
					{
					}
					catch (EntityPropertyTypeException e)
					{
					}
					if (t1==null)
					{
						result = -1;
					}
					else if (t2==null)
					{
						result = 1;
					}
					else if (t1.before (t2))
					{
						result = -1;
					}
					else
					{
						result = 1;
					}
				}
				if (m_criterion.equals (SORTED_BY_TITLE))
				{
					// sorted by the worksite title
					String s1 = ((Site) o1).getTitle();
					String s2 = ((Site) o2).getTitle();
					result =  s1.compareToIgnoreCase (s2);
				}

				// sort ascending or descending
				if (m_asc.equals (Boolean.FALSE.toString ()))
				{
					result = -result;
				}

				return result;

			}	// compare

	} //SiteComparator
}