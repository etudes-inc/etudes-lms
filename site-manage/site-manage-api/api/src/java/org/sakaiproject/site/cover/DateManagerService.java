/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/site-manage/site-manage-api/api/src/java/org/sakaiproject/site/cover/DateManagerService.java $
 * $Id: DateManagerService.java 4863 2013-05-16 18:03:43Z mallikamt $
 ***********************************************************************************
 *
 * Copyright (c) 2009, 2012 Etudes, Inc.
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

import java.util.Date;
import java.util.List;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.site.api.ToolDateRange;

/**
 * <p>
 * DateManagerService is a static Cover for the {@link org.sakaiproject.site.api DateManagerService}; see that interface for usage details.
 * </p>
 */
public class DateManagerService
{
	public static java.lang.String SERVICE_NAME = org.sakaiproject.site.api.DateManagerService.SERVICE_NAME;

	private static org.sakaiproject.site.api.DateManagerService m_instance = null;

	/**
	 * Apply base date changes to all items(assessments,modules,forums,announcements,calendar events) If any tool fails, changes are rolled back for all tools.
	 *
	 * @param course_id
	 *        The context(course id).
	 * @param currentBaseDate
	 *        The current base date specified by the user or the earliest start date of the content items, if unchanged by the user
	 * @param newBaseDate
	 *        The new base date specified
	 * @returns true if the dates were applied successfully, false if a tool fails
	 *
	 */
	public static boolean applyBaseDate(String course_id, Date currentBaseDate, Date newBaseDate)
	{
		org.sakaiproject.site.api.DateManagerService service = getInstance();
		if (service == null) return false;
		return service.applyBaseDate(course_id, currentBaseDate, newBaseDate);
	}

	/**
	 * Get a list of tool names, current date range and adjusted date range (if available)
	 *
	 * @param course_id
	 *        The course id.
	 * @param currentBaseDate
	 *        The current base date specified by the user or the earliest start date of the content items, if unchanged by the user
	 * @param newBaseDate
	 *        The new base date specified *
	 *
	 * @return Returns list of tool names, current date range and adjust date range. If tools don't exist returns null.
	 */
	public static List<ToolDateRange> getAdjDateRanges(String course_id, Date currentBaseDate, Date newBaseDate)
	{
		org.sakaiproject.site.api.DateManagerService service = getInstance();
		if (service == null) return null;

		return service.getAdjDateRanges(course_id, currentBaseDate, newBaseDate);
	}

	/**
	 * Get a list of tool names and current date range
	 *
	 * @param course_id
	 *        The course id.
	 * @return Returns list of tool names and current date ranges returns null.
	 */
	public static List<ToolDateRange> getDateRanges(String course_id)
	{
		org.sakaiproject.site.api.DateManagerService service = getInstance();
		if (service == null) return null;

		return service.getDateRanges(course_id);
	}

	/**
	 * Access the component instance: special cover only method.
	 *
	 * @return the component instance.
	 */
	public static org.sakaiproject.site.api.DateManagerService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.site.api.DateManagerService) ComponentManager.get(org.sakaiproject.site.api.DateManagerService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.site.api.DateManagerService) ComponentManager.get(org.sakaiproject.site.api.DateManagerService.class);
		}
	}

	/**
	 * Get the earliest start date of items(assessments,modules,forums,announcements,calendar events) from the db
	 *
	 * @param course_id
	 *        The course id.
	 * @return Returns the minimum start date. If no items exist or if all items are open and have no dates, returns null.
	 */
	public static Date getMinStartDate(String course_id)
	{
		org.sakaiproject.site.api.DateManagerService service = getInstance();
		if (service == null) return null;

		return service.getMinStartDate(course_id);
	}
}
