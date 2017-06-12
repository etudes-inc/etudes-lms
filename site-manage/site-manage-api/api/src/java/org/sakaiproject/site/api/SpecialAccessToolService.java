/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/site-manage/site-manage-api/api/src/java/org/sakaiproject/site/api/SpecialAccessToolService.java $
 * $Id: SpecialAccessToolService.java 11615 2015-09-15 19:23:10Z mallikamt $
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

package org.sakaiproject.site.api;

import java.util.List;

import org.etudes.api.app.melete.ModuleObjService;
import org.etudes.mneme.api.Assessment;


/**
 * <p>
 * SpecialAccessToolService allows the updating of special access in Mneme, Jforum and Melete
 * </p>
 */
public interface SpecialAccessToolService
{
	public static final String SERVICE_NAME = SpecialAccessToolService.class.getName();

	/**
	 * Adds, updates or deletes special access values for a user in a site
	 *
	 * @param option
	 *        save(for add/update special access) or delete
	 * @param userId
	 *        The user id
	 * @param siteId
	 *        The site id
	 * @param days
	 *        The number of days to set special access modifications by
	 * @param untimed
	 *        Set to true implies there there is no time limit
	 * @param timelimit
	 *        Only used to alter special access entries for timed tests as an extension
	 * @parame timemult
	 *        Only used to alter special access entries for timed tests as a multiplier       
	 * @return A string containing the names of the tools in which this user has access set
	 * @throws Exception
	 */
	public String processAccess(String option, String userId, String siteId, int days, boolean untimed, long timelimit, float timemult) throws Exception;

	/**
	 * Used to determine if this user has special access set in any of the tools
	 *
	 * @param userId
	 *        The user id
	 * @param siteId
	 *        The site id
	 * @return A string containing the names of the tools in which this user has access set
	 */
	public String getAccessTools(String userId, String siteId);

	/**
	 * Checks to see if this user has been assigned special access via Site Info for this site
	 *
	 * @param userId
	 *        The user id
	 * @param siteId
	 *        The site id
	 * @return UserSiteAccess object
	 */
	public UserSiteAccess findUserSiteAccess(String userId, String siteId);

	/**
	 * Determine if this user has special access set in assessments, modules or discussions
	 * @param userId The user id
	 * @param assessments List of assessments
	 * @param modules List of modules
	 * @param saList List of special access objects for discussions
	 * @return true if special access is set in atleast one tool, false if not
	 */
	public boolean userAccessSet(String userId, List<Assessment> assessments,List<ModuleObjService> modules,List<org.etudes.api.app.jforum.SpecialAccess> saList);

}
