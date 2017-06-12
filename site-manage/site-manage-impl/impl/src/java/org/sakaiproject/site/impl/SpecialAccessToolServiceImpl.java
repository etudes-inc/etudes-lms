/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/site-manage/site-manage-impl/impl/src/java/org/sakaiproject/site/impl/SpecialAccessToolServiceImpl.java $
 * $Id: SpecialAccessToolServiceImpl.java 11671 2015-09-18 17:00:24Z mallikamt $
 ***********************************************************************************
 *
 * Copyright (c) 2013, 2015 Etudes, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.site.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.api.app.jforum.Category;
import org.etudes.api.app.jforum.Forum;
import org.etudes.api.app.jforum.JForumCategoryService;
import org.etudes.api.app.jforum.JForumForumService;
import org.etudes.api.app.jforum.JForumPostService;
import org.etudes.api.app.jforum.JForumSpecialAccessService;
import org.etudes.api.app.jforum.JForumUserService;
import org.etudes.api.app.jforum.Topic;
import org.etudes.api.app.melete.ModuleObjService;
import org.etudes.api.app.melete.ModuleService;
import org.etudes.api.app.melete.SpecialAccessObjService;
import org.etudes.api.app.melete.SpecialAccessService;
import org.etudes.component.app.melete.SpecialAccess;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentAccess;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.util.SqlHelper;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SpecialAccessToolService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.UserSiteAccess;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.SessionManager;

/**
 * Special service implementation
 */
public class SpecialAccessToolServiceImpl implements SpecialAccessToolService
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(SpecialAccessToolServiceImpl.class);

	/** Dependency: AssessmentService */
	protected AssessmentService assessmentService;

	/** Dependency: JForumCategoryService */
	protected JForumCategoryService jforumCategoryService;

	/** Dependency: JForumForumService */
	protected JForumForumService jforumForumService;

	/** Dependency: JForumPostService */
	protected JForumPostService jforumPostService;

	/** Dependency: JForumSpecialAccessService */
	protected JForumSpecialAccessService jforumSpecialAccessService;

	protected JForumUserService jforumUserService;

	/** Dependency: SqlService */
	protected SqlService m_sqlService = null;

	/** Dependency: SpecialAccessService */
	protected org.etudes.api.app.melete.SpecialAccessService meleteSpecialAccessService;

	/** Dependency: ModuleService */
	protected ModuleService moduleService;

	/**
	 * {@inheritDoc}
	 */
	public UserSiteAccess findUserSiteAccess(String userId, String siteId)
	{
		if (userId == null || userId.trim().length() == 0) return null;
		if (siteId == null || siteId.trim().length() == 0) return null;

		M_log.debug("userId=" + userId + ", siteId=" + siteId);

		String sql = "SELECT * FROM SAKAI_USER_SITE_ACCESS WHERE USER_ID = ? AND SITE_ID = ?";

		Object[] fields = new Object[2];
		fields[0] = userId;
		fields[1] = siteId;

		List userSiteAccessList = m_sqlService.dbRead(sql, fields, new UserSiteAccessReader());
		if (userSiteAccessList.size() == 0) return null;
		return (UserSiteAccess) userSiteAccessList.get(0);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getAccessTools(String userId, String siteId)
	{
		Site site;
		ToolConfiguration toolConfiguration;
		StringBuffer toolsBuf = new StringBuffer();

		try
		{
			site = SiteService.getSite(siteId);
		}
		catch (Exception e)
		{
			return null;
		}

		if (checkTool("sakai.melete", siteId))
		{
			if (getAccessModules(userId, siteId))
			{
				toolConfiguration = site.getToolForCommonId("sakai.melete");
				toolsBuf.append(toolConfiguration.getTitle());
				toolsBuf.append(",");
			}
		}
		if (checkTool("sakai.jforum.tool", siteId))
		{
			if (getAccessDiscussions(userId, siteId))
			{
				toolConfiguration = site.getToolForCommonId("sakai.jforum.tool");
				toolsBuf.append(" ");
				toolsBuf.append(toolConfiguration.getTitle());
				toolsBuf.append(",");
			}
		}
		if (checkTool("sakai.mneme", siteId))
		{
			if (getAccessTests(userId, siteId))
			{
				toolConfiguration = site.getToolForCommonId("sakai.mneme");
				toolsBuf.append(" ");
				toolsBuf.append(toolConfiguration.getTitle());
			}
		}
		if (toolsBuf.length() == 0) return null;
		if (toolsBuf.length() > 0 && toolsBuf.toString().endsWith(","))
		{
			toolsBuf = toolsBuf.deleteCharAt(toolsBuf.length() - 1);
		}
		return toolsBuf.toString();
	}

	public JForumCategoryService getJforumCategoryService()
	{
		return jforumCategoryService;
	}

	public JForumForumService getJforumForumService()
	{
		return jforumForumService;
	}

	public JForumPostService getJforumPostService()
	{
		return jforumPostService;
	}

	public JForumSpecialAccessService getJforumSpecialAccessService()
	{
		return jforumSpecialAccessService;
	}

	public JForumUserService getJforumUserService()
	{
		return jforumUserService;
	}

	public org.etudes.api.app.melete.SpecialAccessService getMeleteSpecialAccessService()
	{
		return meleteSpecialAccessService;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		M_log.info("init()");
	}

	/**
	 * {@inheritDoc}
	 */
	public String processAccess(String option, String userId, String siteId, int days, boolean untimed, long timelimit, float timemult) throws Exception
	{
		Site site;
		ToolConfiguration toolConfiguration;
		StringBuffer toolsBuf = new StringBuffer();

		try
		{
			site = SiteService.getSite(siteId);
		}
		catch (Exception e)
		{
			return null;
		}

		if (option.equals("save"))
		{
			if (checkTool("sakai.melete", siteId))
			{
				if (setAccessModules(userId, siteId, days))
				{
					toolConfiguration = site.getToolForCommonId("sakai.melete");
					toolsBuf.append(toolConfiguration.getTitle());
					toolsBuf.append(",");
				}
			}
			if (checkTool("sakai.jforum.tool", siteId))
			{
				if (setAccessDiscussions(userId, siteId, days))
				{
					toolConfiguration = site.getToolForCommonId("sakai.jforum.tool");
					toolsBuf.append(" ");
					toolsBuf.append(toolConfiguration.getTitle());
					toolsBuf.append(",");
				}
			}
			if (checkTool("sakai.mneme", siteId))
			{
				if (setAccessTests(userId, siteId, days, untimed, timelimit, timemult))
				{
					toolConfiguration = site.getToolForCommonId("sakai.mneme");
					toolsBuf.append(" ");
					toolsBuf.append(toolConfiguration.getTitle());
				}
			}

			// Only create or update site access table records if access was updated
			// in tools
			if (toolsBuf.length() > 0)
			{
				if (findUserSiteAccess(userId, siteId) != null)
				{
					updateUserSiteAccess(userId, siteId, days, untimed, timelimit, timemult);
				}
				else
				{
					createUserSiteAccess(userId, siteId, days, untimed, timelimit, timemult);
				}
			}
		}

		else if (option.equals("delete"))
		{
			if (checkTool("sakai.melete", siteId))
			{
				if (deleteAccessModules(userId, siteId))
				{
					toolConfiguration = site.getToolForCommonId("sakai.melete");
					toolsBuf.append(toolConfiguration.getTitle());
					toolsBuf.append(",");
				}
			}
			if (checkTool("sakai.jforum.tool", siteId))
			{
				if (deleteAccessDiscussions(userId, siteId))
				{
					toolConfiguration = site.getToolForCommonId("sakai.jforum.tool");
					toolsBuf.append(" ");
					toolsBuf.append(toolConfiguration.getTitle());
					toolsBuf.append(",");
				}
			}
			if (checkTool("sakai.mneme", siteId))
			{
				// Note: set here works for delete as well
				if (setAccessTests(userId, siteId, 0, false, 0, 0))
				{
					toolConfiguration = site.getToolForCommonId("sakai.mneme");
					toolsBuf.append(" ");
					toolsBuf.append(toolConfiguration.getTitle());
				}
			}

			// the user wants special access deleted, so do so even if no tool cares!
			deleteUserSiteAccess(userId, siteId);
		}

		if (toolsBuf.length() == 0) return null;
		if (toolsBuf.length() > 0 && toolsBuf.toString().endsWith(","))
		{
			toolsBuf = toolsBuf.deleteCharAt(toolsBuf.length() - 1);
		}
		return toolsBuf.toString();
	}

	/**
	 * Dependency: AssessmentService.
	 * 
	 * @param assessmentService
	 *        The AssessmentService.
	 */
	public void setAssessmentService(AssessmentService assessmentService)
	{
		this.assessmentService = assessmentService;
	}

	public void setJforumCategoryService(JForumCategoryService jforumCategoryService)
	{
		this.jforumCategoryService = jforumCategoryService;
	}

	public void setJforumForumService(JForumForumService jforumForumService)
	{
		this.jforumForumService = jforumForumService;
	}

	public void setJforumPostService(JForumPostService jforumPostService)
	{
		this.jforumPostService = jforumPostService;
	}

	public void setJforumSpecialAccessService(JForumSpecialAccessService jforumSpecialAccessService)
	{
		this.jforumSpecialAccessService = jforumSpecialAccessService;
	}

	public void setJforumUserService(JForumUserService jforumUserService)
	{
		this.jforumUserService = jforumUserService;
	}

	public void setMeleteSpecialAccessService(org.etudes.api.app.melete.SpecialAccessService meleteSpecialAccessService)
	{
		this.meleteSpecialAccessService = meleteSpecialAccessService;
	}

	/**
	 * Dependency: ModuleService.
	 * 
	 * @param moduleService
	 *        The ModuleService.
	 */
	public void setModuleService(ModuleService moduleService)
	{
		this.moduleService = moduleService;
	}

	public void setSqlService(SqlService service)
	{
		m_sqlService = service;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean userAccessSet(String userId, List<Assessment> assessments, List<ModuleObjService> modules,
			List<org.etudes.api.app.jforum.SpecialAccess> saList)
	{
		SpecialAccessService meleteSpecialAccessService = (SpecialAccessService) ComponentManager.get(SpecialAccessService.class);
		JForumUserService jforumUserService = (JForumUserService) ComponentManager.get(JForumUserService.class);

		for (Assessment assessment : assessments)
		{
			// check for special access, but only one that we might care about (dates, time limit)
			AssessmentAccess special = assessment.getSpecialAccess().getUserAccess(userId);
			if (special != null)
			{
				if (!(special.getOverrideAcceptUntilDate() || special.getOverrideDueDate() || special.getOverrideTimeLimit())) special = null;
			}

			if (special != null)
			{
				return true;
			}
		}
		for (ModuleObjService module : modules)
		{
			int moduleId = module.getModuleId().intValue();
			List<SpecialAccessService> modulesSaList = meleteSpecialAccessService.getSpecialAccess(moduleId);

			List<Integer> accessIdList = new ArrayList();

			for (ListIterator i = modulesSaList.listIterator(); i.hasNext();)
			{
				SpecialAccess saObj = (SpecialAccess) i.next();

				if (saObj.getUsers().contains(userId)) return true;
			}
		}
		for (org.etudes.api.app.jforum.SpecialAccess sa : saList)
		{
			if (sa.getUserIds().contains(jforumUserService.getBySakaiUserId(userId).getId()))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Modifies special access for forums. Changes end date and allow until date
	 * 
	 * @param specialAccess
	 *        Special Access object
	 * @param forum
	 *        The forum object
	 * @param days
	 *        Number of days
	 * @param userId
	 *        User id
	 * @return Special access object that is modified
	 */
	private org.etudes.api.app.jforum.SpecialAccess assignAccessForumDates(org.etudes.api.app.jforum.SpecialAccess specialAccess, Forum forum,
			int days, String userId)
	{
		if (days == 0) return specialAccess;
		if (userId == null || userId.trim().length() == 0) return specialAccess;

		if (forum.getAccessDates().getOpenDate() == null)
		{
			specialAccess.setOverrideStartDate(true);
			specialAccess.setOverrideHideUntilOpen(true);
			specialAccess.getAccessDates().setHideUntilOpen(false);
		}
		else
		{
			specialAccess.getAccessDates().setOpenDate(forum.getAccessDates().getOpenDate());
		}

		if (forum.getAccessDates().getDueDate() == null)
		{
			specialAccess.setOverrideEndDate(true);
			specialAccess.setOverrideLockEndDate(true);
		}
		else
		{
			GregorianCalendar gcDue = new GregorianCalendar();
			gcDue.setTime(forum.getAccessDates().getDueDate());
			gcDue.add(java.util.Calendar.DATE, days);
			specialAccess.getAccessDates().setDueDate(gcDue.getTime());
			specialAccess.setOverrideEndDate(true);
		}

		if (forum.getAccessDates().getAllowUntilDate() == null)
		{
			specialAccess.setOverrideAllowUntilDate(true);
		}
		else
		{
			GregorianCalendar gcAllow = new GregorianCalendar();
			gcAllow.setTime(forum.getAccessDates().getAllowUntilDate());
			gcAllow.add(java.util.Calendar.DATE, days);
			specialAccess.getAccessDates().setAllowUntilDate(gcAllow.getTime());
			specialAccess.setOverrideAllowUntilDate(true);
		}

		List<Integer> users = new ArrayList<Integer>();
		users.add(jforumUserService.getBySakaiUserId(userId).getId());
		specialAccess.setUserIds(users);
		return specialAccess;
	}

	/**
	 * Modifies special access for topics. Changes end date and allow until date
	 * 
	 * @param specialAccess
	 *        Special Access object
	 * @param forum
	 *        The forum object
	 * @param days
	 *        Number of days
	 * @param userId
	 *        User id
	 * @return Special access object that is modified
	 */
	private org.etudes.api.app.jforum.SpecialAccess assignAccessTopicDates(org.etudes.api.app.jforum.SpecialAccess specialAccess, Topic topic,
			int days, String userId)
	{
		if (days == 0) return specialAccess;
		if (userId == null || userId.trim().length() == 0) return specialAccess;

		if (topic.getAccessDates().getOpenDate() == null)
		{
			specialAccess.setOverrideStartDate(true);
			specialAccess.setOverrideHideUntilOpen(true);
			specialAccess.getAccessDates().setHideUntilOpen(false);
		}
		else
		{
			specialAccess.getAccessDates().setOpenDate(topic.getAccessDates().getOpenDate());
		}

		if (topic.getAccessDates().getDueDate() == null)
		{
			specialAccess.setOverrideEndDate(true);
			specialAccess.setOverrideLockEndDate(true);
		}
		else
		{
			GregorianCalendar gcDue = new GregorianCalendar();
			gcDue.setTime(topic.getAccessDates().getDueDate());
			gcDue.add(java.util.Calendar.DATE, days);
			specialAccess.getAccessDates().setDueDate(gcDue.getTime());
			specialAccess.setOverrideEndDate(true);
		}

		if (topic.getAccessDates().getAllowUntilDate() == null)
		{
			specialAccess.setOverrideAllowUntilDate(true);
		}
		else
		{
			GregorianCalendar gcAllow = new GregorianCalendar();
			gcAllow.setTime(topic.getAccessDates().getAllowUntilDate());
			gcAllow.add(java.util.Calendar.DATE, days);
			specialAccess.getAccessDates().setAllowUntilDate(gcAllow.getTime());
			specialAccess.setOverrideAllowUntilDate(true);
		}

		List<Integer> users = new ArrayList<Integer>();
		users.add(jforumUserService.getBySakaiUserId(userId).getId());
		specialAccess.setUserIds(users);
		return specialAccess;
	}

	/**
	 * Creates an entry in the SAKAI_USER_SITE_ACCESS table
	 * 
	 * @param userId
	 *        The user id
	 * @param siteId
	 *        The site id
	 * @param days
	 *        Number of days
	 * @param untimed
	 *        If test needs to have no time limit for user, this is true       
	 * @param timelimit
	 *        Time limit (for tests)
	 * @param timemult
	 *        Time multiplier (for tests)
	 * @return true is record is inserted, false if not
	 */
	private boolean createUserSiteAccess(String userId, String siteId, int days, boolean untimed, long timelimit, float timemult)
	{
		if (userId == null || userId.trim().length() == 0) return false;
		if (siteId == null || siteId.trim().length() == 0) return false;
		if (days == 0 && timelimit == 0 && timemult == 0.0 && !untimed) return false;

		String sql = "INSERT INTO SAKAI_USER_SITE_ACCESS(USER_ID,SITE_ID,DAYS,UNTIMED,TIME_LIMIT,TIME_MULT) VALUES(?,?,?,?,?,?)";

		Object[] fields = new Object[6];

		fields[0] = userId;
		fields[1] = siteId;
		fields[2] = days;
		fields[3] = untimed ? "1":"0";
		fields[4] = timelimit;
		fields[5] = timemult;

		M_log.debug("SQL: " + sql);
		if (m_sqlService.dbWrite(sql, fields))
		{
			M_log.debug("Created new user site access: userId=" + userId);
			return true;
		}
		else
		{
			M_log.error("Failed to create new user site access: userId=" + userId + ", siteId=" + siteId);
			return false;
		}

	}

	/**
	 * Deletes special access entries for Discussion forums and topics
	 * 
	 * @param userId
	 *        The user id
	 * @param siteId
	 *        The site id
	 * @return true if special access is set for atleast one discussion, false if not
	 * @throws Exception
	 */
	private boolean deleteAccessDiscussions(String userId, String siteId) throws Exception
	{
		boolean accessSet = false;
		if (userId == null || userId.trim().length() == 0) return false;
		if (siteId == null || siteId.trim().length() == 0) return false;
		List<org.etudes.api.app.jforum.SpecialAccess> saList = jforumSpecialAccessService.getBySite(siteId);
		for (org.etudes.api.app.jforum.SpecialAccess sa : saList)
		{
			if (sa.getUserIds().size() == 1 && sa.getUserIds().contains(jforumUserService.getBySakaiUserId(userId).getId()))
			{
				accessSet = true;
				jforumSpecialAccessService.delete(sa.getId());
			}
			if (sa.getUserIds().size() > 1 && sa.getUserIds().contains(jforumUserService.getBySakaiUserId(userId).getId()))
			{
				accessSet = true;
				jforumSpecialAccessService.removeUserSpecialAccess(sa.getId(), jforumUserService.getBySakaiUserId(userId).getId());
			}
		}
		return accessSet;
	}

	/**
	 * Deletes all special access entries in modules for this user and site
	 * 
	 * @param userId
	 * @param siteId
	 * @return true if special access is set for atleast one discussion, false if not
	 * @throws Exception
	 */
	private boolean deleteAccessModules(String userId, String siteId) throws Exception
	{
		if (userId == null || userId.trim().length() == 0) return false;
		if (siteId == null || siteId.trim().length() == 0) return false;
		boolean accessSet = false;

		List<ModuleObjService> modules = this.moduleService.getModules(siteId);

		for (ModuleObjService module : modules)
		{
			int moduleId = module.getModuleId().intValue();
			List<SpecialAccessService> saList = meleteSpecialAccessService.getSpecialAccess(moduleId);

			List<Integer> accessIdList = new ArrayList();

			for (ListIterator i = saList.listIterator(); i.hasNext();)
			{
				SpecialAccess saObj = (SpecialAccess) i.next();
				String[] userIds = SqlHelper.decodeStringArray(saObj.getUsers());

				// If this is the only user, delete entry
				if (userIds.length == 1 && Arrays.asList(userIds).contains(userId))
				{
					accessSet = true;
					accessIdList.add(saObj.getAccessId());
					meleteSpecialAccessService.deleteSpecialAccess(accessIdList);
					break;
				}
				// If there are other users, update entry
				if (userIds.length > 1 && Arrays.asList(userIds).contains(userId))
				{
					accessSet = true;
					List<String> userIdList = new ArrayList<String>(Arrays.asList(userIds));
					userIdList.removeAll(Arrays.asList(userId));
					String[] newArray = new String[userIdList.size()];
					newArray = userIdList.toArray(newArray);
					saObj.setUsers(SqlHelper.encodeStringArray(newArray));
					meleteSpecialAccessService.insertSpecialAccess(saList, saObj, module);
					break;
				}
			}
		}
		return accessSet;
	}

	/**
	 * Delete the user site access record
	 * 
	 * @param userId
	 *        The user id
	 * @param siteId
	 *        The site id
	 */
	private void deleteUserSiteAccess(String userId, String siteId)
	{
		if (userId == null || userId.trim().length() == 0) return;
		if (siteId == null || siteId.trim().length() == 0) return;

		M_log.debug("userId=" + userId + ", siteId=" + siteId);

		String sql = "DELETE FROM SAKAI_USER_SITE_ACCESS WHERE USER_ID = ? AND SITE_ID = ?";

		Object[] fields = new Object[2];
		fields[0] = userId;
		fields[1] = siteId;

		M_log.debug("SQL: " + sql);
		if (m_sqlService.dbWrite(sql, fields))
		{
			M_log.debug("Removed all scheduled invocations matching: userId=" + userId + ", siteId=" + siteId);
		}
		else
		{
			M_log.error("Failure while attempting to remove invocations matching: userId=" + userId + ", siteId=" + siteId);
		}

	}

	/**
	 * Determine if this user has special access entries in discussions
	 * 
	 * @param userId
	 *        The user id
	 * @param siteId
	 *        The site id
	 * @return true if this user has special access entries in discussions, false if not
	 */
	private boolean getAccessDiscussions(String userId, String siteId)
	{
		if (userId == null || userId.trim().length() == 0) return false;
		if (siteId == null || siteId.trim().length() == 0) return false;

		List<org.etudes.api.app.jforum.SpecialAccess> saList = jforumSpecialAccessService.getBySite(siteId);
		for (org.etudes.api.app.jforum.SpecialAccess sa : saList)
		{
			if (sa.getUserIds().contains(jforumUserService.getBySakaiUserId(userId).getId()))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Determine if this user has special access entries in modules
	 * 
	 * @param userId
	 *        The user id
	 * @param siteId
	 *        The site id
	 * @return
	 */
	private boolean getAccessModules(String userId, String siteId)
	{
		if (userId == null || userId.trim().length() == 0) return false;
		if (siteId == null || siteId.trim().length() == 0) return false;

		List<ModuleObjService> modules = this.moduleService.getModules(siteId);

		for (ModuleObjService module : modules)
		{
			int moduleId = module.getModuleId().intValue();
			List<SpecialAccessService> saList = meleteSpecialAccessService.getSpecialAccess(moduleId);

			List<Integer> accessIdList = new ArrayList();

			for (ListIterator i = saList.listIterator(); i.hasNext();)
			{
				SpecialAccess saObj = (SpecialAccess) i.next();

				if (saObj.getUsers().contains(userId)) return true;
			}
		}
		return false;
	}

	/**
	 * Determine if this user has special access entries for tests
	 * 
	 * @param userId
	 *        The user id
	 * @param siteId
	 *        The site id
	 * @return true if the user has special access entries, false if not
	 */
	private boolean getAccessTests(String userId, String siteId)
	{
		if (userId == null || userId.trim().length() == 0) return false;
		if (siteId == null || siteId.trim().length() == 0) return false;

		List<Assessment> assessments = this.assessmentService.getContextAssessments(siteId, AssessmentService.AssessmentsSort.title_a, Boolean.FALSE);
		for (Assessment assessment : assessments)
		{
			// check for special access, but only one that we might care about (dates, time limit)
			AssessmentAccess special = assessment.getSpecialAccess().getUserAccess(userId);
			if (special != null)
			{
				if (!(special.getOverrideAcceptUntilDate() || special.getOverrideDueDate() || special.getOverrideTimeLimit())) special = null;
			}

			if (special != null)
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Set special access entries for this user for discussions in this site
	 * 
	 * @param userId
	 *        The user id
	 * @param siteId
	 *        The site id
	 * @param days
	 *        Number of days
	 * @return true if special access is set for atleast one discussion, false if not
	 * @throws Exception
	 */
	private boolean setAccessDiscussions(String userId, String siteId, int days) throws Exception
	{
		if (userId == null || userId.trim().length() == 0) return false;
		if (siteId == null || siteId.trim().length() == 0) return false;
		if (days == 0) return false;

		boolean accessSet = false;
		String instId = SessionManager.getCurrentSessionUserId();
		List<Category> categories = jforumCategoryService.getUserContextCategories(siteId, instId);
		org.etudes.api.app.jforum.SpecialAccess specialAccess;
		for (Category category : categories)
		{
			if (category.getAccessDates().getOpenDate() == null && category.getAccessDates().getDueDate() == null
					&& category.getAccessDates().getAllowUntilDate() == null)
			{
				List<Forum> forums = jforumForumService.getCategoryForums(category.getId());
				for (Forum forum : forums)
				{
					if (forum.getAccessDates().getDueDate() != null || forum.getAccessDates().getAllowUntilDate() != null)
					{
						List<org.etudes.api.app.jforum.SpecialAccess> saList = jforumSpecialAccessService.getByForum(forum.getId());
						boolean userPresent = false;
						if (saList != null && saList.size() > 0)
						{
							for (org.etudes.api.app.jforum.SpecialAccess sa : saList)
							{
								if (sa.getUserIds().contains(jforumUserService.getBySakaiUserId(userId).getId()))
								{
									specialAccess = jforumSpecialAccessService.newSpecialAccess(forum.getId(), 0);

									specialAccess = assignAccessForumDates(specialAccess, forum, days, userId);

									jforumSpecialAccessService.createForumSpecialAccess(specialAccess);
									accessSet = true;
									userPresent = true;
									break;
								}
							}
						}
						if (!userPresent)
						{
							specialAccess = jforumSpecialAccessService.newSpecialAccess(forum.getId(), 0);

							specialAccess = assignAccessForumDates(specialAccess, forum, days, userId);

							jforumSpecialAccessService.createForumSpecialAccess(specialAccess);
							accessSet = true;
						}
					}
					else
					{
						List<Topic> topics = jforumPostService.getTopics(forum.getId(), 0, 0, instId);
						for (Topic topic : topics)
						{
							if (topic.getAccessDates().getDueDate() != null || topic.getAccessDates().getAllowUntilDate() != null)
							{
								List<org.etudes.api.app.jforum.SpecialAccess> saList = jforumSpecialAccessService.getByTopic(forum.getId(),
										topic.getId());
								boolean userPresent = false;
								if (saList != null && saList.size() > 0)
								{
									for (org.etudes.api.app.jforum.SpecialAccess sa : saList)
									{
										if (sa.getUserIds().contains(jforumUserService.getBySakaiUserId(userId).getId()))
										{
											specialAccess = jforumSpecialAccessService.newSpecialAccess(topic.getForumId(), topic.getId());
											specialAccess = assignAccessTopicDates(specialAccess, topic, days, userId);

											jforumSpecialAccessService.createTopicSpecialAccess(specialAccess);
											userPresent = true;
											accessSet = true;
											break;
										}
									}
								}
								if (!userPresent)
								{
									specialAccess = jforumSpecialAccessService.newSpecialAccess(topic.getForumId(), topic.getId());
									specialAccess = assignAccessTopicDates(specialAccess, topic, days, userId);

									jforumSpecialAccessService.createTopicSpecialAccess(specialAccess);
									accessSet = true;
								}

							}
						}
					}
				}
			}
		}
		return accessSet;
	}

	/**
	 * Set special access entries for this user for modules in this site
	 * 
	 * @param userId
	 *        This user id
	 * @param siteId
	 *        The site id
	 * @param days
	 *        Number of days
	 * @return true if special access is set for atleast one module, false if not
	 * @throws Exception
	 */
	private boolean setAccessModules(String userId, String siteId, int days) throws Exception
	{
		if (userId == null || userId.trim().length() == 0) return false;
		if (siteId == null || siteId.trim().length() == 0) return false;
		if (days == 0) return false;

		boolean accessSet = false;
		List<ModuleObjService> modules = this.moduleService.getModules(siteId);

		for (ModuleObjService module : modules)
		{
			int moduleId = module.getModuleId().intValue();
			List<SpecialAccessService> saList = meleteSpecialAccessService.getSpecialAccess(moduleId);
			SpecialAccessObjService saObj = new SpecialAccess();
			saObj.setAccessId(0);
			saObj.setModuleId(moduleId);

			String[] valArray = new String[1];
			valArray[0] = userId;
			saObj.setUsers(SqlHelper.encodeStringArray(valArray));

			boolean datesExist = false;

			GregorianCalendar gcEnd = new GregorianCalendar();
			if (module.getModuleshdate().getEndDate() != null)
			{
				datesExist = true;
				gcEnd.setTime(module.getModuleshdate().getEndDate());
				gcEnd.add(java.util.Calendar.DATE, days);
				saObj.setEndDate(gcEnd.getTime());
			}

			GregorianCalendar gcAllowUntil = new GregorianCalendar();
			if (module.getModuleshdate().getAllowUntilDate() != null)
			{
				datesExist = true;
				gcAllowUntil.setTime((module.getModuleshdate().getAllowUntilDate()));
				gcAllowUntil.add(java.util.Calendar.DATE, days);
				saObj.setAllowUntilDate(gcAllowUntil.getTime());
			}
			saObj.setOverrideStart(false);
			saObj.setOverrideEnd(false);
			saObj.setOverrideAllowUntil(false);

			if (datesExist)
			{
				accessSet = true;
				saObj.setStartDate(module.getModuleshdate().getStartDate());
				meleteSpecialAccessService.insertSpecialAccess(saList, saObj, module);
			}
		}
		return accessSet;
	}

	/**
	 * Set special access entries for this user for tests in this site
	 * 
	 * @param userId
	 *        The user id
	 * @param siteId
	 *        The site id
	 * @param days
	 *        Number of days
	 * @param untimed
	 *        True means there is no time limit       
	 * @param timelimit
	 *        Timelimit as a millisecond value for timed assessments
	 * @param timemult
	 *        Time multiplier as a float value for timed assessments
	 * @return true if special access is set for atleast one test, false if not
	 * @throws Exception
	 */
	private boolean setAccessTests(String userId, String siteId, int days, boolean untimed, long timelimit, float timemult) throws Exception
	{
		if (userId == null || userId.trim().length() == 0) return false;
		if (siteId == null || siteId.trim().length() == 0) return false;

		boolean rv = false;
		List<Assessment> assessments = this.assessmentService.getContextAssessments(siteId, AssessmentService.AssessmentsSort.title_a, Boolean.FALSE);
		for (Assessment assessment : assessments)
		{
			// skip formal course evaluations, as they have no special access
			if (assessment.getFormalCourseEval()) continue;

			// we will be updating a current setting if it specifies dates or time limit
			AssessmentAccess currentAccess = assessment.getSpecialAccess().getUserAccess(userId);
			if (currentAccess != null)
			{
				if (!(currentAccess.getOverrideAcceptUntilDate() || currentAccess.getOverrideDueDate() || currentAccess.getOverrideTimeLimit()))
					currentAccess = null;
			}

			// extend due and accept until dates, if they exist, and if days > 0
			Date newAcceptUntilDate = null;
			Date newDueDate = null;
			if (days > 0)
			{
				if (assessment.getDates().getAcceptUntilDate() != null)
				{
					GregorianCalendar cal = new GregorianCalendar();
					cal.setTime((assessment.getDates().getAcceptUntilDate()));
					cal.add(java.util.Calendar.DATE, days);
					newAcceptUntilDate = cal.getTime();
				}

				if (assessment.getDates().getDueDate() != null)
				{
					GregorianCalendar cal = new GregorianCalendar();
					cal.setTime((assessment.getDates().getDueDate()));
					cal.add(java.util.Calendar.DATE, days);
					newDueDate = cal.getTime();
				}
			}

			// extend time limit if time limit or multiplier are set, and if the assessment has a time limit
			long newTimeLimit = 0;
			if ((timelimit > 0) || (timemult > 0))
			{
				if (assessment.getHasTimeLimit())
				{
					if (timelimit > 0)
					{
						newTimeLimit = assessment.getTimeLimit().longValue() + timelimit;
					}
					else if (timemult > 0)
					{
						newTimeLimit = (long) (assessment.getTimeLimit().floatValue() * timemult);
					}
				}
			}

			// if we have anything new, create a special access
			if ((newAcceptUntilDate != null) || (newDueDate != null) || (newTimeLimit != 0) || (newTimeLimit == 0 && untimed && assessment.getHasTimeLimit())|| (currentAccess != null))
			{
				AssessmentAccess newAccess = assessment.getSpecialAccess().assureUserAccess(userId);
				if (newAcceptUntilDate != null)
				{
					newAccess.setAcceptUntilDate(newAcceptUntilDate);
				}
				else
				{
					newAccess.setOverrideAcceptUntilDate(Boolean.FALSE);
				}
				if (newDueDate != null)
				{
					newAccess.setDueDate(newDueDate);
				}
				else
				{
					newAccess.setOverrideDueDate(Boolean.FALSE);
				}
				if (newTimeLimit != 0)
				{
					newAccess.setTimeLimit(newTimeLimit);
				}
				else
				{
					if (untimed) newAccess.setTimeLimit(null);
					else newAccess.setOverrideTimeLimit(Boolean.FALSE);
				}

				this.assessmentService.saveAssessment(assessment);

				rv = true;
			}
		}

		return rv;
	}

	/**
	 * Update the user site table
	 * 
	 * @param userId
	 *        The user id
	 * @param siteId
	 *        The site id
	 * @param days
	 *        Number of days
	 * @param untimed
	 *        Set to true if time limit is 0       
	 * @param timelimit
	 *        Time limit as a long value
	 * @param timemult
	 *        Time multiplier as a float value
	 * @return true if the update succeeds, false if not
	 */
	private boolean updateUserSiteAccess(String userId, String siteId, int days, boolean untimed, long timelimit, float timemult)
	{
		if (userId == null || userId.trim().length() == 0) return false;
		if (siteId == null || siteId.trim().length() == 0) return false;
		if (days == 0 && timelimit == 0 && timemult == 0.0 && !untimed) return false;

		M_log.debug("Updating user site access: userId:" + userId + " siteId: " + siteId);

		String sql = "UPDATE SAKAI_USER_SITE_ACCESS SET DAYS=?,UNTIMED=?,TIME_LIMIT=?,TIME_MULT=?  WHERE USER_ID = ? AND SITE_ID = ?";

		Object[] fields = new Object[6];

		fields[0] = days;
		fields[1] = untimed ? "1":"0";
		fields[2] = timelimit;
		fields[3] = timemult;
		fields[4] = userId;
		fields[5] = siteId;

		M_log.debug("SQL: " + sql);
		if (m_sqlService.dbWrite(sql, fields))
		{
			return true;
		}
		else
		{
			M_log.error("Failed to update user site access: userId=" + userId + ", siteId=" + siteId);
			return false;
		}

	}

	/**
	 * Check if a tool exists in the current site.
	 * 
	 * @param tool
	 *        The tool id.
	 * @param siteId
	 *        The site id.
	 * @return true if the tool exists in the current site, false, if not.
	 */
	protected boolean checkTool(String tool, String siteId)
	{
		Site site = null;
		try
		{
			site = SiteService.getSite(siteId);
		}
		catch (Exception e)
		{
			M_log.warn("checkTool: Exception thrown while getting site" + e.toString());
		}
		if (site.getToolForCommonId(tool) != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
