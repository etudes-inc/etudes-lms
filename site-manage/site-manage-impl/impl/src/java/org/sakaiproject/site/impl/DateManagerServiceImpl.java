/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/site-manage/site-manage-impl/impl/src/java/org/sakaiproject/site/impl/DateManagerServiceImpl.java $
 * $Id: DateManagerServiceImpl.java 6665 2013-12-17 20:42:28Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2009, 2010, 2011, 2012 Etudes, Inc.
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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.api.app.jforum.JForumBaseDateService;
import org.etudes.api.app.melete.ModuleService;
import org.etudes.homepage.api.HomePageService;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.util.DateHelper;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.DateManagerService;
import org.sakaiproject.site.api.PubDatesService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.api.ToolDateRange;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.util.ResourceLoader;

/**
 * Date manager service implementation
 */
public class DateManagerServiceImpl implements DateManagerService
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(DateManagerServiceImpl.class);

	/** Dependency: gradebookService */
	private GradebookService gradebookService;

	/** Dependency: AnnouncementService */
	protected AnnouncementService announcementService;

	/** Dependency: AssessmentService */
	protected AssessmentService assessmentService;

	/**
	 * @return The HomePageService, via the component manager.
	 */
	private HomePageService homePageService()
	{
		return (HomePageService) ComponentManager.get(HomePageService.class);
	}

	/** Dependency: CalendarService */
	protected CalendarService calendarService;

	/** Dependency: JForumBaseDateService */
	protected JForumBaseDateService jforumBaseDateService;

	/** Dependency: ModuleService */
	protected ModuleService moduleService;

	/** Dependency: PubDatesService */
	protected PubDatesService pubDatesService;

	/** Dependency: SqlService. */
	protected SqlService sqlService = null;

	final int outrange_diff = 182;

	/**
	 * {@inheritDoc}
	 */
	public boolean applyBaseDate(final String course_id, Date currentBaseDate, Date newBaseDate)
	{
		boolean success = false;
		final int days_diff = daysBetween(currentBaseDate, newBaseDate);
		if ((course_id != null) && (days_diff != 0))
		{
			success = this.sqlService.transact(new Runnable()
			{
				public void run()
				{
					pubDatesService.applyBaseDateTx(course_id, days_diff);
					if (checkTool("sakai.melete", course_id)) moduleService.applyBaseDateTx(course_id, days_diff);
					if (checkTool("sakai.jforum.tool", course_id)) jforumBaseDateService.applyBaseDateTx(course_id, days_diff);
				}
			}, "applyBaseDate: " + course_id);
			// Announcement service, calendar service and Mneme do not use sqlService
			// so keep them separate from main transaction and only apply base
			// date changes to them if all other tools are successful.
			if (success)
			{
				try
				{
					if (checkTool("sakai.mneme", course_id)) assessmentService.applyBaseDateTx(course_id, days_diff);
					if (checkTool("sakai.announcements", course_id)) announcementService.applyBaseDateTx(course_id, days_diff);
					if (checkTool("sakai.schedule", course_id)) calendarService.applyBaseDateTx(course_id, days_diff);
					if (checkTool("sakai.gradebook.tool", course_id)) gradebookService.applyBaseDateTx(course_id, days_diff);
					if (checkTool("e3.homepage", course_id)) homePageService().applyBaseDateTx(course_id, days_diff);
				}
				catch (Exception e)
				{
					success = false;
					M_log.warn("applyBaseDate: base date changes failed in Mneme, Announcement Service, Calendar Service or Gradebook Service"
							+ course_id);
				}
			}
		}
		return success;
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ToolDateRange> getAdjDateRanges(String course_id, Date currentBaseDate, Date newBaseDate)
	{
		ResourceLoader rb = new ResourceLoader("PublishUnpublish");
		List<ToolDateRange> dateRangeList = null;
		String toolName;
		Date minDate = null, maxDate = null;
		final int days_diff = daysBetween(currentBaseDate, newBaseDate);
		if ((course_id != null) && (days_diff != 0))
		{
			dateRangeList = new ArrayList<ToolDateRange>();
			ToolDateRange tdr;
			toolName = rb.getString("java.pubunpubtitle");
			minDate = pubDatesService.getMinStartDate(course_id);
			maxDate = pubDatesService.getMaxStartDate(course_id);
			tdr = getAdjDateRange(course_id, toolName, minDate, maxDate, true, days_diff);
			if (tdr != null) dateRangeList.add(tdr);
			toolName = getToolName("sakai.melete", course_id);
			if (toolName != null)
			{
				minDate = moduleService.getMinStartDate(course_id);
				maxDate = moduleService.getMaxStartDate(course_id);
				tdr = getAdjDateRange(course_id, toolName, minDate, maxDate, true, days_diff);
				if (tdr != null) dateRangeList.add(tdr);
			}
			toolName = getToolName("sakai.jforum.tool", course_id);
			if (toolName != null)
			{
				minDate = jforumBaseDateService.getMinStartDate(course_id);
				maxDate = jforumBaseDateService.getMaxStartDate(course_id);
				tdr = getAdjDateRange(course_id, toolName, minDate, maxDate, true, days_diff);
				if (tdr != null) dateRangeList.add(tdr);
			}
			toolName = getToolName("sakai.mneme", course_id);
			if (toolName != null)
			{
				minDate = assessmentService.getMinStartDate(course_id);
				maxDate = assessmentService.getMaxStartDate(course_id);
				tdr = getAdjDateRange(course_id, toolName, minDate, maxDate, true, days_diff);
				if (tdr != null) dateRangeList.add(tdr);
			}
			toolName = getToolName("sakai.announcements", course_id);
			if (toolName != null)
			{
				minDate = announcementService.getMinStartDate(course_id);
				maxDate = announcementService.getMaxStartDate(course_id);
				tdr = getAdjDateRange(course_id, toolName, minDate, maxDate, true, days_diff);
				if (tdr != null) dateRangeList.add(tdr);
			}
			toolName = getToolName("sakai.schedule", course_id);
			if (toolName != null)
			{
				minDate = calendarService.getMinStartDate(course_id);
				maxDate = calendarService.getMaxStartDate(course_id);
				tdr = getAdjDateRange(course_id, toolName, minDate, maxDate, true, days_diff);
				if (tdr != null) dateRangeList.add(tdr);
			}
			toolName = getToolName("sakai.gradebook.tool", course_id);
			if (toolName != null)
			{
				minDate = gradebookService.getInternalAssignmentsMinDueDate(course_id);
				maxDate = gradebookService.getInternalAssignmentsMaxDueDate(course_id);
				tdr = getAdjDateRange(course_id, toolName, minDate, maxDate, true, days_diff);
				if (tdr != null) dateRangeList.add(tdr);
			}
			toolName = getToolName("e3.homepage", course_id);
			if (toolName != null)
			{
				minDate = homePageService().getMinStartDate(course_id);
				maxDate = homePageService().getMaxStartDate(course_id);
				tdr = getAdjDateRange(course_id, toolName, minDate, maxDate, true, days_diff);
				if (tdr != null) dateRangeList.add(tdr);
			}
		}
		return dateRangeList;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ToolDateRange> getDateRanges(String course_id)
	{
		ResourceLoader rb = new ResourceLoader("PublishUnpublish");
		String toolName;
		Date minDate = null, maxDate = null;
		ToolDateRange tdr = null;
		List<ToolDateRange> dateRangeList = new ArrayList<ToolDateRange>();
		List<Date> minDateList = new ArrayList<Date>();
		List<Date> maxDateList = new ArrayList<Date>();
		toolName = rb.getString("java.pubunpubtitle");
		minDate = pubDatesService.getMinStartDate(course_id);
		maxDate = pubDatesService.getMaxStartDate(course_id);
		if (minDate != null) minDateList.add(new java.util.Date(minDate.getTime()));
		if (maxDate != null) maxDateList.add(new java.util.Date(maxDate.getTime()));
		tdr = getAdjDateRange(course_id, toolName, minDate, maxDate, false, 0);
		if (tdr != null) dateRangeList.add(tdr);
		toolName = getToolName("sakai.melete", course_id);
		if (toolName != null)
		{
			minDate = moduleService.getMinStartDate(course_id);
			maxDate = moduleService.getMaxStartDate(course_id);
			if (minDate != null) minDateList.add(new java.util.Date(minDate.getTime()));
			if (maxDate != null) maxDateList.add(new java.util.Date(maxDate.getTime()));
			tdr = getAdjDateRange(course_id, toolName, minDate, maxDate, false, 0);
			if (tdr != null) dateRangeList.add(tdr);
		}
		toolName = getToolName("sakai.jforum.tool", course_id);
		if (toolName != null)
		{
			minDate = jforumBaseDateService.getMinStartDate(course_id);
			maxDate = jforumBaseDateService.getMaxStartDate(course_id);
			if (minDate != null) minDateList.add(new java.util.Date(minDate.getTime()));
			if (maxDate != null) maxDateList.add(new java.util.Date(maxDate.getTime()));
			tdr = getAdjDateRange(course_id, toolName, minDate, maxDate, false, 0);
			if (tdr != null) dateRangeList.add(tdr);
		}
		toolName = getToolName("sakai.mneme", course_id);
		if (toolName != null)
		{
			minDate = assessmentService.getMinStartDate(course_id);
			maxDate = assessmentService.getMaxStartDate(course_id);
			if (minDate != null) minDateList.add(new java.util.Date(minDate.getTime()));
			if (maxDate != null) maxDateList.add(new java.util.Date(maxDate.getTime()));
			tdr = getAdjDateRange(course_id, toolName, minDate, maxDate, false, 0);
			if (tdr != null) dateRangeList.add(tdr);
		}
		toolName = getToolName("sakai.announcements", course_id);
		if (toolName != null)
		{
			minDate = announcementService.getMinStartDate(course_id);
			maxDate = announcementService.getMaxStartDate(course_id);
			if (minDate != null) minDateList.add(new java.util.Date(minDate.getTime()));
			if (maxDate != null) maxDateList.add(new java.util.Date(maxDate.getTime()));
			tdr = getAdjDateRange(course_id, toolName, minDate, maxDate, false, 0);
			if (tdr != null) dateRangeList.add(tdr);
		}
		toolName = getToolName("sakai.schedule", course_id);
		if (toolName != null)
		{
			minDate = calendarService.getMinStartDate(course_id);
			maxDate = calendarService.getMaxStartDate(course_id);
			if (minDate != null) minDateList.add(new java.util.Date(minDate.getTime()));
			if (maxDate != null) maxDateList.add(new java.util.Date(maxDate.getTime()));
			tdr = getAdjDateRange(course_id, toolName, minDate, maxDate, false, 0);
			if (tdr != null) dateRangeList.add(tdr);
		}
		toolName = getToolName("sakai.gradebook.tool", course_id);
		if (toolName != null)
		{
			minDate = gradebookService.getInternalAssignmentsMinDueDate(course_id);
			maxDate = gradebookService.getInternalAssignmentsMaxDueDate(course_id);
			if (minDate != null) minDateList.add(new java.util.Date(minDate.getTime()));
			if (maxDate != null) maxDateList.add(new java.util.Date(maxDate.getTime()));
			tdr = getAdjDateRange(course_id, toolName, minDate, maxDate, false, 0);
			if (tdr != null) dateRangeList.add(tdr);
		}
		toolName = getToolName("e3.homepage", course_id);
		if (toolName != null)
		{
			minDate = homePageService().getMinStartDate(course_id);
			maxDate = homePageService().getMaxStartDate(course_id);
			if (minDate != null) minDateList.add(new java.util.Date(minDate.getTime()));
			if (maxDate != null) maxDateList.add(new java.util.Date(maxDate.getTime()));
			tdr = getAdjDateRange(course_id, toolName, minDate, maxDate, false, 0);
			if (tdr != null) dateRangeList.add(tdr);
		}
		flagOutsideRangeTools(dateRangeList, minDateList, maxDateList);
		return dateRangeList;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getMinStartDate(String course_id)
	{
		Date minDate = null, resDate = null;
		minDate = pubDatesService.getMinStartDate(course_id);
		if (checkTool("sakai.melete", course_id)) resDate = moduleService.getMinStartDate(course_id);
		minDate = getMin(minDate, resDate);
		if (checkTool("sakai.jforum.tool", course_id)) resDate = jforumBaseDateService.getMinStartDate(course_id);
		minDate = getMin(minDate, resDate);
		if (checkTool("sakai.announcements", course_id)) resDate = announcementService.getMinStartDate(course_id);
		minDate = getMin(minDate, resDate);
		if (checkTool("sakai.schedule", course_id)) resDate = calendarService.getMinStartDate(course_id);
		minDate = getMin(minDate, resDate);
		if (checkTool("sakai.mneme", course_id)) resDate = assessmentService.getMinStartDate(course_id);
		minDate = getMin(minDate, resDate);
		if (checkTool("sakai.gradebook.tool", course_id)) resDate = gradebookService.getInternalAssignmentsMinDueDate(course_id);
		minDate = getMin(minDate, resDate);
		if (checkTool("e3.homepage", course_id)) resDate = homePageService().getMinStartDate(course_id);
		minDate = getMin(minDate, resDate);

		return minDate;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		M_log.info("init()");
	}

	/**
	 * Dependency: AnnouncementService.
	 *
	 * @param announcementService
	 *        The AnnouncementService.
	 */
	public void setAnnouncementService(AnnouncementService announcementService)
	{
		this.announcementService = announcementService;
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

	/**
	 * Dependency: CalendarService.
	 *
	 * @param calendarService
	 *        The CalendarService.
	 */
	public void setCalendarService(CalendarService calendarService)
	{
		this.calendarService = calendarService;
	}

	/**
	 * Dependency: GradebookService.
	 *
	 * @param gradebookService
	 *        The GradebookService
	 */
	public void setGradebookService(GradebookService gradebookService)
	{
		this.gradebookService = gradebookService;
	}

	/**
	 * Dependency: JForumBaseDateService.
	 *
	 * @param jforumBaseDateService
	 *        The JForumBaseDateService.
	 */
	public void setJforumBaseDateService(JForumBaseDateService jforumBaseDateService)
	{
		this.jforumBaseDateService = jforumBaseDateService;
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

	/**
	 * Dependency: PubDatesService.
	 *
	 * @param pubDatesService
	 *        The PubDatesService.
	 */
	public void setPubDatesService(PubDatesService pubDatesService)
	{
		this.pubDatesService = pubDatesService;
	}

	/**
	 * Dependency: SqlService.
	 *
	 * @param service
	 *        The SqlService.
	 */
	public void setSqlService(SqlService service)
	{
		this.sqlService = service;
	}

	/**
	 * Check if a tool exists in the current site.
	 * 
	 * @param tool
	 *        The tool id.
	 * @return true if the tool exists in the current site, false, if not.
	 */
	protected boolean checkTool(String tool, String siteId)
	{
		try
		{
			Site site = SiteService.getSite(siteId);
			if (site.getToolForCommonId(tool) != null)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		catch (Exception e)
		{
			M_log.warn("checkTool: Exception thrown while getting site" + e.toString());
		}
		return false;
	}

	/**
	 * Compute the number of days between these two dates.
	 *
	 * @param d1
	 *        One date.
	 * @param d2
	 *        Another date.
	 * @return The number of days between these two dates.
	 */
	protected int daysBetween(Date d1, Date d2)
	{
		GregorianCalendar first = new GregorianCalendar();
		GregorianCalendar last = new GregorianCalendar();

		int factor = 1;

		if (d1.before(d2))
		{
			first.setTime(d1);
			last.setTime(d2);
		}
		else
		{
			first.setTime(d2);
			last.setTime(d1);
			factor = -1;
		}

		int days = 0;

		// if the years are different, add the days to complete the first year, the days of the years between,
		// and the days get to the date in the second year
		if (first.get(Calendar.YEAR) != last.get(Calendar.YEAR))
		{
			// add the days to complete the first year
			days = first.getActualMaximum(Calendar.DAY_OF_YEAR) - first.get(Calendar.DAY_OF_YEAR);

			// add the days of all the years between
			GregorianCalendar tmp = (GregorianCalendar) first.clone();
			for (int year = first.get(Calendar.YEAR) + 1; year < last.get(Calendar.YEAR); year++)
			{
				tmp.set(Calendar.YEAR, year);
				int maxDays = tmp.getActualMaximum(Calendar.DAY_OF_YEAR);
				days += maxDays;
			}

			// add the days to the last date in that year
			days += last.get(Calendar.DAY_OF_YEAR);
		}

		// otherwise, in the same year, set the days to the difference
		else
		{
			days = last.get(Calendar.DAY_OF_YEAR) - first.get(Calendar.DAY_OF_YEAR);
		}

		return days * factor;
	}

	/**
	 * Flags any toolDateRange objects that are out of range
	 *
	 * @param dateRangeList
	 *        List of ToolDateRange objects.
	 * @param minDateList
	 *        List of min dates of all tools.
	 * @param maxDateList
	 *        List of max dates of all tools
	 *
	 */
	protected void flagOutsideRangeTools(List<ToolDateRange> dateRangeList, List<Date> minDateList, List<Date> maxDateList)
	{
		Date lowMinDate = null, highMinDate = null, lowMaxDate = null, highMaxDate = null;
		long minDiff, maxDiff;
		if ((dateRangeList == null) || (dateRangeList.size() == 0)) return;

		// Sort minDateList and maxDateList
		Collections.sort(minDateList);
		Collections.sort(maxDateList);
		lowMinDate = (Date) minDateList.get(0);
		highMinDate = (Date) minDateList.get(minDateList.size() - 1);
		lowMaxDate = (Date) maxDateList.get(0);
		highMaxDate = (Date) maxDateList.get(maxDateList.size() - 1);
		minDiff = (highMinDate.getTime() - lowMinDate.getTime()) / (1000 * 60 * 60 * 24);
		maxDiff = (highMaxDate.getTime() - lowMaxDate.getTime()) / (1000 * 60 * 60 * 24);
		// If all dates are within range, nothing to flag
		if ((minDiff < outrange_diff) && (maxDiff < outrange_diff))
		{
			return;
		}
		Iterator<ToolDateRange> itr = dateRangeList.iterator();
		while (itr.hasNext())
		{
			ToolDateRange tdr = itr.next();
			// If min date of tool is out of range, flag and then continue to next tool
			if (tdr.getMinDate() != null)
			{
				if (((highMinDate.getTime() - tdr.getMinDate().getTime()) / (1000 * 60 * 60 * 24)) > outrange_diff)
				{
					tdr.outsideRangeFlag = true;
					continue;
				}
			}
			// If max date of tool is out of range, flag
			if (tdr.getMaxDate() != null)
			{
				if (((highMaxDate.getTime() - tdr.getMaxDate().getTime()) / (1000 * 60 * 60 * 24)) > outrange_diff)
				{
					tdr.outsideRangeFlag = true;
				}
			}
		}
	}

	protected ToolDateRange getAdjDateRange(String course_id, String toolName, Date minDate, Date maxDate, boolean adjust, int days_diff)
	{
		Date adjMinDate = null, adjMaxDate = null;
		ToolDateRange tdr = null;

		if ((minDate == null) && (maxDate == null)) return tdr;
		tdr = new ToolDateRange();
		tdr.toolName = toolName;
		tdr.minDate = minDate;
		tdr.maxDate = maxDate;
		if (minDate.equals(maxDate))
		{
			tdr.currentDateRange = DateHelper.formatDateOnly(minDate, null);
		}
		else
		{
			tdr.currentDateRange = DateHelper.formatDateOnly(minDate, null) + " - " + DateHelper.formatDateOnly(maxDate, null);
		}
		if (adjust)
		{
			if (minDate != null)
			{
				GregorianCalendar gc1 = new GregorianCalendar();
				gc1.setTime(minDate);
				gc1.add(java.util.Calendar.DATE, days_diff);
				adjMinDate = gc1.getTime();
			}
			if (maxDate != null)
			{
				GregorianCalendar gc2 = new GregorianCalendar();
				gc2.setTime(maxDate);
				gc2.add(java.util.Calendar.DATE, days_diff);
				adjMaxDate = gc2.getTime();
			}
			if (adjMinDate.equals(adjMaxDate))
			{
				tdr.adjDateRange = DateHelper.formatDateOnly(adjMinDate, null);
			}
			else
			{
				tdr.adjDateRange = DateHelper.formatDateOnly(adjMinDate, null) + " - " + DateHelper.formatDateOnly(adjMaxDate, null);
			}
		}
		return tdr;
	}

	/**
	 * Return the minimum of the two dates
	 *
	 * @param date1
	 *        One date.
	 * @param date2
	 *        The other date.
	 * @return the minimum of the two dates.
	 */
	protected Date getMin(Date date1, Date date2)
	{
		if (date1 == null) return date2;
		if (date2 == null) return date1;
		if (date1.before(date2))
			return date1;
		else
			return date2;
	}

	/**
	 * Get tool name.
	 *
	 * @param tool
	 *        The tool id.
	 * @return tool name if the tool exists in the current site, null, if not.
	 */
	protected String getToolName(String tool, String siteId)
	{
		Site site = null;
		try
		{
			site = SiteService.getSite(siteId);
		}
		catch (Exception e)
		{
			M_log.warn("getToolName: Exception thrown while getting site" + e.toString());
		}
		ToolConfiguration tConfig = site.getToolForCommonId(tool);
		if (tConfig != null)
		{
			return tConfig.getTitle();
		}
		else
		{
			return null;
		}
	}
}
