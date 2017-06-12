/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/site-manage/site-manage-api/api/src/java/org/sakaiproject/site/cover/PubDatesService.java $
 * $Id: PubDatesService.java 4863 2013-05-16 18:03:43Z mallikamt $
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2012 Etudes, Inc.
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

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteInfo;
import org.sakaiproject.time.api.Time;

/**
 * <p>
 * PubDatesService contains methods needed for site publishing with dates.
 * </p>
 */
public class PubDatesService
{
	/**
	 * Access the component instance: special cover only method.
	 *
	 * @return the component instance.
	 */
	public static org.sakaiproject.site.api.PubDatesService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.site.api.PubDatesService) ComponentManager
						.get(org.sakaiproject.site.api.PubDatesService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.site.api.PubDatesService) ComponentManager
					.get(org.sakaiproject.site.api.PubDatesService.class);
		}
	}

	private static org.sakaiproject.site.api.PubDatesService m_instance = null;

	public static java.lang.String SERVICE_NAME = org.sakaiproject.site.api.PubDatesService.SERVICE_NAME;

	/**
	 * Processes publish options for sites and new site that are being created
	 * @param publishUnpublish the publishing choice
	 * @param publishTime the publish time (may be null)
	 * @param unpublishTime the unpublish time (may be null)
	 * @param site the site object (may be null if site is being created)
	 * @param siteInfo the siteInfo object (may be null if an existing site is being edited)
	 * @return String error message if any, otherwise null
	 */
	public static String processPublishOptions(String publishUnpublish, Time publishTime, Time unpublishTime, Site site, SiteInfo siteInfo)
	{
		org.sakaiproject.site.api.PubDatesService service = getInstance();
		if (service == null) return null;

		return service.processPublishOptions(publishUnpublish, publishTime, unpublishTime, site, siteInfo/*, rb*/);
	}

	public static boolean checkDatesSet(ResourceProperties siteProperties)
	{
		org.sakaiproject.site.api.PubDatesService service = getInstance();
		if (service == null) return true;

		return service.checkDatesSet(siteProperties);
	}


	/**
	 * Invoked when import from site triggers, transfers publish settings and dates(if any) to new site
	 * Also creates entries in scheduling table if needed, does not affect site status
	 * @param fromContext site id of source site
	 * @param toSite destination site
	 */
	public static void transferDates(String fromContext, Site toSite)
	{
		org.sakaiproject.site.api.PubDatesService service = getInstance();
		if (service == null) return;
		service.transferDates(fromContext, toSite);
	}

	/**
	 * Invoked when base date changes happen, moves dates, changes site status
	 * Also creates entries in scheduling table if needed
	 * @param course_id site id site
	 * @param days_diff difference to be added/subtracted
	 */
	public void applyBaseDateTx(String course_id, int days_diff)
	{
		org.sakaiproject.site.api.PubDatesService service = getInstance();
		if (service == null) return;
		service.applyBaseDateTx(course_id, days_diff);
	}
}
