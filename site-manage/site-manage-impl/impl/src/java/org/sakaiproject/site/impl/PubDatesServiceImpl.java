/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/site-manage/site-manage-impl/impl/src/java/org/sakaiproject/site/impl/PubDatesServiceImpl.java $
 * $Id: PubDatesServiceImpl.java 5687 2013-08-21 21:27:22Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2012, 2013 Etudes, Inc.
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

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.util.DateHelper;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.scheduler.api.DbScheduleService;
import org.sakaiproject.site.api.PubDatesService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteInfo;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>
 * PubDatesServiceImpl is a base implementation of the PubDatesService.
 * </p>
 */
public class PubDatesServiceImpl implements PubDatesService
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(PubDatesServiceImpl.class);

	private final static String PROP_SITE_PUB_DATE = "pub-date";
	private final static String PROP_SITE_PUB_ID = "pub-id";
	private final static String PROP_SITE_UNPUB_DATE = "unpub-date";
	private final static String PROP_SITE_UNPUB_ID = "unpub-id";

	/** Dependency: SecurityService */
	protected SecurityService securityService = null;

	/** Dependency: SessionManager */
	protected SessionManager sessionManager = null;

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public void applyBaseDateTx(String course_id, int days_diff)
	{
		Site site = null;
		GregorianCalendar pubGc = new GregorianCalendar();
		GregorianCalendar unpubGc = new GregorianCalendar();
		Time pubGcTime = null, unpubGcTime = null;
		boolean datesSet = false;

		if (course_id == null)
		{
			M_log.warn("applyBaseDateTx: course_id is null");
		}
		if (days_diff == 0)
		{
			return;
		}
		try
		{
			site = SiteService.getSite(course_id);
			ResourcePropertiesEdit siteProperties = site.getPropertiesEdit();
			datesSet = checkDatesSet(siteProperties);

			if (datesSet)
			{
				if (siteProperties.getProperty(PROP_SITE_PUB_DATE) != null)
				{
					try
					{
						// These properties should be Time properties, but they were initially stored in the data entry format, default time zone -ggolden
						String pubDateValue = siteProperties.getProperty(PROP_SITE_PUB_DATE);
						Date pubDate = DateHelper.parseDateFromDefault(pubDateValue);
						pubGc.setTime(new Date(pubDate.getTime()));
						pubGc.add(Calendar.DATE, days_diff);
						pubGcTime = TimeService.newTime(pubGc);
					}
					catch (ParseException e)
					{
					}
				}
				if (siteProperties.getProperty(PROP_SITE_UNPUB_DATE) != null)
				{
					try
					{
						// These properties should be Time properties, but they were initially stored in the data entry format, default time zone -ggolden
						String unpubDateValue = siteProperties.getProperty(PROP_SITE_UNPUB_DATE);
						Date unpubDate = DateHelper.parseDateFromDefault(unpubDateValue);
						unpubGc.setTime(new Date(unpubDate.getTime()));
						unpubGc.add(Calendar.DATE, days_diff);
						unpubGcTime = TimeService.newTime(unpubGc);
					}
					catch (ParseException e)
					{
					}
				}
				boolean cleared = clearPreviousSettings(site);
				if (cleared)
				{
					processPublishLogic(pubGcTime, unpubGcTime, site, null);
				}
			}

			// get the current user
			final String userId = this.sessionManager.getCurrentSessionUserId() == null ? "admin" : this.sessionManager.getCurrentSessionUserId();

			try
			{
				// set the user into the thread
				Session s = sessionManager.getCurrentSession();
				if (s != null)
				{
					s.setUserId(userId);
				}

				pushAdvisor();
				try
				{
					SiteService.save(site);
				}
				catch (IdUnusedException e)
				{
					// TODO:
				}
				catch (PermissionException e)
				{
					// TODO:
				}
			}
			finally
			{
				popAdvisor();
			}
		}
		catch (IdUnusedException e)
		{
		}
	}

	/**
	 * @inheritDoc
	 */
	public boolean checkDatesSet(ResourceProperties siteProperties)
	{
		String publishTimeStr = null, unpublishTimeStr = null;

		boolean datesSet = false;
		if (siteProperties.getProperty(PROP_SITE_PUB_DATE) != null)
		{
			publishTimeStr = siteProperties.getProperty(PROP_SITE_PUB_DATE);
		}
		if (siteProperties.getProperty(PROP_SITE_UNPUB_DATE) != null)
		{
			unpublishTimeStr = siteProperties.getProperty(PROP_SITE_UNPUB_DATE);
		}

		if ((publishTimeStr != null && publishTimeStr.trim().length() > 0) || (unpublishTimeStr != null && unpublishTimeStr.trim().length() > 0))
			datesSet = true;
		return datesSet;
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**
	 * @inheritDoc
	 */
	public Date getMaxStartDate(String course_id)
	{
		Date pubDate = null, unpubDate = null;
		boolean datesSet = false;
		Site site = null;
		if (course_id == null)
		{
			M_log.warn("getMaxStartDate: course_id is null");
			return null;
		}
		try
		{
			site = SiteService.getSite(course_id);
			ResourcePropertiesEdit siteProperties = site.getPropertiesEdit();
			datesSet = checkDatesSet(siteProperties);
			if (datesSet)
			{
				if (siteProperties.getProperty(PROP_SITE_PUB_DATE) != null)
				{
					// These properties should be Time properties, but they were initially stored in the data entry format, default time zone -ggolden
					String pubDateValue = siteProperties.getProperty(PROP_SITE_PUB_DATE);
					pubDate = DateHelper.parseDateFromDefault(pubDateValue);
				}
				if (siteProperties.getProperty(PROP_SITE_UNPUB_DATE) != null)
				{
					// These properties should be Time properties, but they were initially stored in the data entry format, default time zone -ggolden
					String unpubDateValue = siteProperties.getProperty(PROP_SITE_UNPUB_DATE);
					unpubDate = DateHelper.parseDateFromDefault(unpubDateValue);
				}
				if (pubDate != null && unpubDate != null)
				{
					if (pubDate.after(unpubDate))
						return pubDate;
					else
						return unpubDate;
				}
				if (pubDate != null) return pubDate;
				if (unpubDate != null) return unpubDate;
			}
		}
		catch (IdUnusedException e)
		{
		}
		catch (ParseException e)
		{
		}
		return null;
	}

	/**
	 * @inheritDoc
	 */
	public Date getMinStartDate(String course_id)
	{
		Date pubDate = null, unpubDate = null;
		Site site = null;
		boolean datesSet = false;
		if (course_id == null)
		{
			M_log.warn("getMinStartDate: course_id is null");
			return null;
		}
		try
		{
			site = SiteService.getSite(course_id);
			ResourcePropertiesEdit siteProperties = site.getPropertiesEdit();
			datesSet = checkDatesSet(siteProperties);
			if (datesSet)
			{
				if (siteProperties.getProperty(PROP_SITE_PUB_DATE) != null)
				{
					// These properties should be Time properties, but they were initially stored in the data entry format, default time zone -ggolden
					String pubDateValue = siteProperties.getProperty(PROP_SITE_PUB_DATE);
					pubDate = DateHelper.parseDateFromDefault(pubDateValue);
				}
				if (siteProperties.getProperty(PROP_SITE_UNPUB_DATE) != null)
				{
					// These properties should be Time properties, but they were initially stored in the data entry format, default time zone -ggolden
					String unpubDateValue = siteProperties.getProperty(PROP_SITE_UNPUB_DATE);
					unpubDate = DateHelper.parseDateFromDefault(unpubDateValue);
				}
				if (pubDate != null && unpubDate != null)
				{
					if (pubDate.before(unpubDate))
						return pubDate;
					else
						return unpubDate;
				}
				if (pubDate != null) return pubDate;
				if (unpubDate != null) return unpubDate;
			}
		}
		catch (IdUnusedException e)
		{
		}
		catch (ParseException e)
		{
		}
		return null;
	}

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{

			M_log.info("init()");
		}
		catch (Throwable t)
		{
			M_log.warn(".init(): ", t);
		}
	}

	/**
	 * @inheritDoc
	 */
	public String processPublishOptions(String publishUnpublish, Time publishTime, Time unpublishTime, Site site, SiteInfo siteInfo)
	{
		ResourceLoader rb = new ResourceLoader("PublishUnpublish");

		boolean cleared = false;
		// User did not specify any publishing choice
		if (publishUnpublish == null)
		{
			return rb.getString("java.optionsemptyerror");
		}
		if (publishUnpublish != null)
		{
			// User choose "publish" or "unpublish"
			if ((publishUnpublish.equalsIgnoreCase("publish")) || (publishUnpublish.equalsIgnoreCase("unpublish")))
			{
				// Clear previous settings
				cleared = clearPreviousSettings(site);
				if (cleared)
				{
					if (publishUnpublish.equalsIgnoreCase("publish"))
					{
						setPublished(site, siteInfo, true);
					}
					if (publishUnpublish.equalsIgnoreCase("unpublish"))
					{
						setPublished(site, siteInfo, false);
					}
				}
			}

			// User chose to set dates
			if (publishUnpublish.equalsIgnoreCase("setdates"))
			{
				// Both dates are empty
				if ((publishTime == null) && (unpublishTime == null))
				{
					return rb.getString("java.emptydateserror");
				}

				// Clear previous settings
				cleared = clearPreviousSettings(site);
				if (cleared)
				{
					// Process dates
					processPublishLogic(publishTime, unpublishTime, site, siteInfo);
				}
			}
		}
		if (site != null)
		{
			try
			{
				SiteService.save(site);
			}
			catch (IdUnusedException e)
			{
				// TODO:
			}
			catch (PermissionException pe)
			{

			}
		}
		// No alert messages, so return null
		return null;
	}

	/**
	 * Dependency: SecurityService.
	 *
	 * @param service
	 *        The SecurityService.
	 */
	public void setSecurityService(SecurityService service)
	{
		this.securityService = service;
	}

	/**
	 * Dependency: SessionManager.
	 *
	 * @param service
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager service)
	{
		this.sessionManager = service;
	}

	/**
	 * @inheritDoc
	 */
	public void transferDates(String fromContext, Site toSite)
	{
		String publishTimeStr = null, unpublishTimeStr = null;
		Time publishTime = null, unpublishTime = null;
		boolean datesSet = false;
		boolean sourceDatesExist = false;
		boolean parseException = false;
		if ((fromContext == null) || (toSite == null)) return;
		ResourcePropertiesEdit toSiteProperties = toSite.getPropertiesEdit();
		datesSet = checkDatesSet(toSiteProperties);

		if (!datesSet)
		{
			try
			{
				Site fromSite = SiteService.getSite(fromContext);
				boolean cleared = clearPreviousSettings(toSite);
				ResourceProperties fromSiteProperties = fromSite.getProperties();
				if (cleared)
				{
					if (fromSiteProperties.getProperty(PROP_SITE_PUB_DATE) != null)
					{
						publishTimeStr = fromSiteProperties.getProperty(PROP_SITE_PUB_DATE);
						toSiteProperties.addProperty(PROP_SITE_PUB_DATE, publishTimeStr);
						if ((publishTimeStr != null) && (publishTimeStr.trim().length() > 0))
						{
							try
							{
								// These properties should be Time properties, but they were initially stored in the data entry format, default time zone -ggolden
								Date pubDate = DateHelper.parseDateFromDefault(publishTimeStr);
								if (pubDate != null) publishTime = TimeService.newTime(pubDate.getTime());
								sourceDatesExist = true;
							}
							catch (ParseException e)
							{
								parseException = true;
							}
						}
					}
					if (fromSiteProperties.getProperty(PROP_SITE_UNPUB_DATE) != null)
					{
						unpublishTimeStr = fromSiteProperties.getProperty(PROP_SITE_UNPUB_DATE);
						toSiteProperties.addProperty(PROP_SITE_UNPUB_DATE, unpublishTimeStr);
						if ((unpublishTimeStr != null) && (unpublishTimeStr.trim().length() > 0))
						{
							try
							{
								// These properties should be Time properties, but they were initially stored in the data entry format, default time zone -ggolden
								Date unpubDate = DateHelper.parseDateFromDefault(unpublishTimeStr);
								if (unpubDate != null) unpublishTime = TimeService.newTime(unpubDate.getTime());
								sourceDatesExist = true;
							}
							catch (ParseException e)
							{
								parseException = true;
							}
						}
					}
					// Process publish logic only if there are no parse exceptions
					// and if the source site has dates
					if (!parseException && sourceDatesExist) processPublishLogic(publishTime, unpublishTime, toSite, null);
				}
			}
			catch (IdUnusedException e)
			{
			}
		}
	}

	/**
	 * This method clears any previous publish settings for this site
	 *
	 * @param site
	 *        the site object
	 * @returns true after all is cleared
	 */
	private boolean clearPreviousSettings(Site site)
	{
		if (site == null) return true;
		DbScheduleService dbScheduleService = (DbScheduleService) ComponentManager.get(org.sakaiproject.scheduler.api.DbScheduleService.class);
		ResourcePropertiesEdit siteProperties = site.getPropertiesEdit();

		if (siteProperties.getProperty(PROP_SITE_PUB_ID) != null)
		{
			dbScheduleService.deleteDelayedInvocation(siteProperties.getProperty(PROP_SITE_PUB_ID));
			siteProperties.removeProperty(PROP_SITE_PUB_ID);
		}
		if (siteProperties.getProperty(PROP_SITE_PUB_DATE) != null)
		{
			siteProperties.removeProperty(PROP_SITE_PUB_DATE);
		}
		if (siteProperties.getProperty(PROP_SITE_UNPUB_ID) != null)
		{
			dbScheduleService.deleteDelayedInvocation(siteProperties.getProperty(PROP_SITE_UNPUB_ID));
			siteProperties.removeProperty(PROP_SITE_UNPUB_ID);
		}
		if (siteProperties.getProperty(PROP_SITE_UNPUB_DATE) != null)
		{
			siteProperties.removeProperty(PROP_SITE_UNPUB_DATE);
		}
		return true;
	}

	/**
	 * Performs core publish logic for sites and new site that are being created
	 *
	 * @param publishTime
	 *        the publish time as a Time object (may be null)
	 * @param unpublishTime
	 *        the unpublish time as a Time object (may be null)
	 * @param site
	 *        the site object (may be null if site is being created)
	 * @param siteInfo
	 *        the siteInfo object (may be null if an existing site is being edited)
	 */
	private void processPublishLogic(Time publishTime, Time unpublishTime, Site site, SiteInfo siteInfo)
	{
		boolean publishBeforeNow = false;
		boolean unpublishBeforeNow = false;

		publishBeforeNow = processSetDates(publishTime, "org.sakaiproject.site.impl.SitePublishImpl", site, siteInfo);
		unpublishBeforeNow = processSetDates(unpublishTime, "org.sakaiproject.site.impl.SiteUnpublishImpl", site, siteInfo);
		if ((publishBeforeNow == true) && (unpublishBeforeNow == false))
		{
			setPublished(site, siteInfo, true);
		}
		if ((publishBeforeNow == false) && (unpublishBeforeNow == true)) setPublished(site, siteInfo, false);
		if ((publishBeforeNow == true) && (unpublishBeforeNow == true))
		{
			if (publishTime == null) setPublished(site, siteInfo, false);
			if (unpublishTime == null) setPublished(site, siteInfo, true);
			if ((publishTime != null) && (unpublishTime != null))
			{
				if (publishTime.before(unpublishTime)) setPublished(site, siteInfo, false);
				if (unpublishTime.before(publishTime)) setPublished(site, siteInfo, true);
			}
		}

		// The condition below is added for new sites. If publish and unpublish dates
		// are later, set publish status of siteInfo to false, but keep status of existing site unchanged.
		if ((publishBeforeNow == false) && (unpublishBeforeNow == false))
		{
			setPublished(site, siteInfo, false);
		}
	}

	/**
	 * If time warrants, creates entries in scheduling table
	 *
	 * @param actionTime
	 *        the action time as a Time object (may be null)
	 * @param implName
	 *        string value refers to either SitePublish or Unpublish class
	 * @param site
	 *        the site object (may be null if site is being created)
	 * @param siteInfo
	 *        the siteInfo object (may be null if an existing site is being edited)
	 * @return boolean true if actionTime is before now or null, false otherwise
	 */
	private boolean processSetDates(Time actionTime, String implName, Site site, SiteInfo siteInfo)
	{
		boolean actionBefore = true;
		String uuid;
		if (implName.equals("org.sakaiproject.site.impl.SitePublishImpl"))
		{
			if (site != null)
			{
				if (actionTime != null)
				{
					ResourcePropertiesEdit siteProperties = site.getPropertiesEdit();

					// These properties should be Time properties, but they were initially stored in the data entry format, default time zone -ggolden
					siteProperties.addProperty(PROP_SITE_PUB_DATE, DateHelper.formatDateToDefault(new Date(actionTime.getTime())));
					if (actionTime.after(TimeService.newTime()))
					{
						actionBefore = false;
						DbScheduleService dbScheduleService = (DbScheduleService) ComponentManager
								.get(org.sakaiproject.scheduler.api.DbScheduleService.class);

						uuid = dbScheduleService.createDelayedInvocation(actionTime, implName, site.getId());
						siteProperties.addProperty(PROP_SITE_PUB_ID, uuid);
					}
				}
			}
			else
			{
				siteInfo.publishTime = actionTime;
				if (actionTime != null)
				{
					if (actionTime.after(TimeService.newTime()))
					{
						actionBefore = false;
						siteInfo.publishLater = true;
					}
				}
			}
		}
		if (implName.equals("org.sakaiproject.site.impl.SiteUnpublishImpl"))
		{
			if (site != null)
			{
				if (actionTime != null)
				{
					ResourcePropertiesEdit siteProperties = site.getPropertiesEdit();

					// These properties should be Time properties, but they were initially stored in the data entry format, default time zone -ggolden
					siteProperties.addProperty(PROP_SITE_UNPUB_DATE, DateHelper.formatDateToDefault(new Date(actionTime.getTime())));
					if (actionTime.after(TimeService.newTime()))
					{
						actionBefore = false;
						DbScheduleService dbScheduleService = (DbScheduleService) ComponentManager
								.get(org.sakaiproject.scheduler.api.DbScheduleService.class);

						uuid = dbScheduleService.createDelayedInvocation(actionTime, implName, site.getId());
						siteProperties.addProperty(PROP_SITE_UNPUB_ID, uuid);
					}
				}
			}
			else
			{
				siteInfo.unpublishTime = actionTime;
				if (actionTime != null)
				{
					if (actionTime.after(TimeService.newTime()))
					{
						actionBefore = false;
						siteInfo.unpublishLater = true;
					}
				}
			}
		}
		return actionBefore;
	}

	/**
	 * Sets a site or siteInfo object to published or unpublished
	 *
	 * @param site
	 *        the site object (may be null if site is being created)
	 * @param siteInfo
	 *        the siteInfo object (may be null if an existing site is being edited)
	 * @param value
	 *        boolean value that determines site's publishing status
	 */
	private void setPublished(Site site, SiteInfo siteInfo, boolean value)
	{
		if (site != null)
			site.setPublished(value);
		else
			siteInfo.published = value;
	}

	/**
	 * Remove our security advisor.
	 */
	protected void popAdvisor()
	{
		this.securityService.popAdvisor();
	}

	/**
	 * Setup a security advisor.
	 */
	protected void pushAdvisor()
	{
		// setup a security advisor
		this.securityService.pushAdvisor(new SecurityAdvisor()
		{
			public SecurityAdvice isAllowed(String userId, String function, String reference)
			{
				return SecurityAdvice.ALLOWED;
			}
		});
	}
}
