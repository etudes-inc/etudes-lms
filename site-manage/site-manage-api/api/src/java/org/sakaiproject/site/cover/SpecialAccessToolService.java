/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/site-manage/site-manage-api/api/src/java/org/sakaiproject/site/cover/SpecialAccessToolService.java $
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
package org.sakaiproject.site.cover;

import java.util.List;

import org.etudes.api.app.melete.ModuleObjService;
import org.etudes.mneme.api.Assessment;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.site.api.UserSiteAccess;


/**
 * <p>
 * SpecialAccessToolService contains methods for updating special access in Mneme, Melete and Jforum.
 * </p>
 */
public class SpecialAccessToolService
{
	/**
	 * Access the component instance: special cover only method.
	 *
	 * @return the component instance.
	 */
	public static org.sakaiproject.site.api.SpecialAccessToolService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.site.api.SpecialAccessToolService) ComponentManager
						.get(org.sakaiproject.site.api.SpecialAccessToolService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.site.api.SpecialAccessToolService) ComponentManager
					.get(org.sakaiproject.site.api.SpecialAccessToolService.class);
		}
	}

	private static org.sakaiproject.site.api.SpecialAccessToolService m_instance = null;

	public static java.lang.String SERVICE_NAME = org.sakaiproject.site.api.SpecialAccessToolService.SERVICE_NAME;

	public static String processAccess(String option, String userId, String siteId, int days, boolean untimed, long timelimit, float timemult) throws Exception
	{
		org.sakaiproject.site.api.SpecialAccessToolService service = getInstance();
		if (service == null)
		{
			return null;
		}
		return service.processAccess(option, userId, siteId, days, untimed, timelimit, timemult);
	}

	public static String getAccessTools(String userId, String siteId)
	{
		org.sakaiproject.site.api.SpecialAccessToolService service = getInstance();
		if (service == null)
		{
			return null;
		}
		return service.getAccessTools(userId, siteId);
	}

	public static UserSiteAccess findUserSiteAccess(String userId, String siteId)
	{
		org.sakaiproject.site.api.SpecialAccessToolService service = getInstance();
		if (service == null)
		{
			return null;
		}
		return service.findUserSiteAccess(userId, siteId);
	}

	public static boolean userAccessSet(String userId, List<Assessment> assessments,List<ModuleObjService> modules,List<org.etudes.api.app.jforum.SpecialAccess> saList)
	{
		org.sakaiproject.site.api.SpecialAccessToolService service = getInstance();
		if (service == null)
		{
			return false;
		}
		return service.userAccessSet(userId, assessments, modules, saList);
	}
}
