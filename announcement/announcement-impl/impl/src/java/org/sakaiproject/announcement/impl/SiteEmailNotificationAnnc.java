/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/announcement/announcement-impl/impl/src/java/org/sakaiproject/announcement/impl/SiteEmailNotificationAnnc.java $
 * $Id: SiteEmailNotificationAnnc.java 12190 2015-12-02 20:20:14Z mallikamt $
 ***********************************************************************************
 * Copyright (c) 2013 Etudes, Inc. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
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

package org.sakaiproject.announcement.impl;

import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import org.etudes.util.XrefHelper;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementService;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.cover.SecurityService;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.scheduler.api.ScheduledInvocationCommand;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.Notification;
import org.sakaiproject.event.api.NotificationAction;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;

import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.util.SiteEmailNotification;

/**
 * <p>
 * SiteEmailNotificationAnnc fills the notification message and headers with details from the announcement message that triggered the notification event.
 * </p>
 */
public class SiteEmailNotificationAnnc extends SiteEmailNotification implements ScheduledInvocationCommand
{
	private static ResourceBundle rb = ResourceBundle.getBundle("siteemaanc");

    private ComponentManager componentManager;
	/**
	 * Construct.
	 */
	public SiteEmailNotificationAnnc()
	{
	}

	/**
	 * Construct.
	 */
	public SiteEmailNotificationAnnc(String siteId)
	{
		super(siteId);
	}

	/**
	 * @inheritDoc
	 */
	protected String getResourceAbility()
	{
		return AnnouncementService.SECURE_ANNC_READ;
    }
    /**
	 * Inject ComponentManager
	 */
	public void setComponentManager(ComponentManager componentManager) {
		this.componentManager = componentManager;
	}

	/**
	 * @inheritDoc
	 */
	public NotificationAction getClone()
	{
		SiteEmailNotificationAnnc clone = new SiteEmailNotificationAnnc();
		clone.set(this);

		return clone;
	}

	/**
	 * @inheritDoc
	 */
	public void notify(Notification notification, Event event)
	{
		// get the message
		Reference ref = EntityManager.newReference(event.getResource());
		AnnouncementMessage msg = (AnnouncementMessage) ref.getEntity();
		AnnouncementMessageHeader hdr = (AnnouncementMessageHeader) msg.getAnnouncementHeader();

		// do not do notification for draft messages
		if (hdr.getDraft()) return;
		if (msg.getProperties().getProperty("archived") != null) return;
		
		// read the notification options
		final String notif = msg.getProperties().getProperty("notificationLevel");

		int noti = NotificationService.NOTI_OPTIONAL;
		if ("r".equals(notif))
		{
			noti = NotificationService.NOTI_REQUIRED;
		}
		else if ("n".equals(notif))
		{
			noti = NotificationService.NOTI_NONE;
		}

		// use either the configured site, or if not configured, the site (context) of the resource
		String siteId = (getSite() != null) ? getSite() : ref.getContext();
		try
		{
			Site site = SiteService.getSite(siteId);
			if ((site.isPublished() == false) && (noti != NotificationService.NOTI_REQUIRED))
			{
				return;
			}
		}
		catch (Exception ignore)
		{
		}

		// Check release date. 
		// This will only activate for immediate notifications
		// Future notifications will always be executed by execute method
		Time now = TimeService.newTime();
		Time releaseDate;
		try
		{
			releaseDate = msg.getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE);
			if (releaseDate != null)
            {	
              try
              {
					if (now.after(releaseDate))
					{
						super.notify(notification, event);
					}
              }
              catch (Exception e1)
              {

              }
            }  
		}
		catch (Exception e2)
		{
		}
	}

	/**
	 * @inheritDoc
	 */
	protected String getMessage(Event event)
	{
		StringBuffer buf = new StringBuffer();
		String newline = "<br />\n";

		// get the message
		Reference ref = EntityManager.newReference(event.getResource());
		AnnouncementMessage msg = (AnnouncementMessage) ref.getEntity();
		AnnouncementMessageHeader hdr = (AnnouncementMessageHeader) msg.getAnnouncementHeader();

		// use either the configured site, or if not configured, the site (context) of the resource
		String siteId = (getSite() != null) ? getSite() : ref.getContext();

		// get a site title
		String title = siteId;
		try
		{
			Site site = SiteService.getSite(siteId);
			title = site.getTitle();
		}
		catch (Exception ignore)
		{
		}

		Time releaseDate;
		String dateDisplay;
		try
		{
			releaseDate = msg.getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE);
            if (releaseDate == null)
            {	
            	dateDisplay = hdr.getDate().toStringLocalFull();
            } 
            else
            {
            	dateDisplay = releaseDate.toStringLocalFull();
            }
		}
		catch (Exception e2)
		{
			dateDisplay = hdr.getDate().toStringLocalFull();
		}

		
		buf.append("<div style=\"display:inline; float:left;\">From: " + hdr.getFrom().getDisplayName() + "</div>");
		buf.append("<div style=\"display:inline; float:right;\">" + dateDisplay + "</div>");
		buf.append("<div style=\"clear:both\"></div>");
		buf.append("<div style=\"font-weight:bold;margin-top:10px;\">" + hdr.getSubject() + "</div>");
		buf.append("<hr />");
		
		// write with full urls for embedded data
		try
		{
			buf.append(XrefHelper.fullUrls(msg.getBody()));
		}
		catch (Exception e)
		{
			// in case of error write original data
			buf.append(msg.getBody());
		}
		buf.append(newline);

		// add any attachments
		List attachments = hdr.getAttachments();
		if (attachments.size() > 0)
		{
			buf.append(newline + rb.getString("Attachments") + newline);
			for (Iterator iAttachments = attachments.iterator(); iAttachments.hasNext();)
			{
				Reference attachment = (Reference) iAttachments.next();
				String attachmentTitle = attachment.getProperties().getPropertyFormatted(ResourceProperties.PROP_DISPLAY_NAME);
				buf.append("<a href=\"" + attachment.getUrl() + "\">" + attachmentTitle + "</a>" + newline);
			}
		}

		return buf.toString();
	}

	/**
	 * @inheritDoc
	 */
	protected List getHeaders(Event event)
	{
		List rv = new Vector();

		// Set the content type of the message body to HTML
		rv.add("Content-Type: text/html");

		// set the subject
		rv.add("Subject: " + getSubject(event));

		// from
		rv.add(getFrom(event));

		// to
		rv.add(getTo(event));

		return rv;
	}

	/**
	 * @inheritDoc
	 */
	protected String getTag(String newline, String title, String siteId)
	{
		// tag the message - HTML version
		String rv = newline + "<hr /><div style=\"text-align:center\">This automatic notification was sent by <a href=\"" + ServerConfigurationService.getPortalUrl() + "\">"
				+ ServerConfigurationService.getString("ui.service", "Sakai") + "</a> from the <a href=\""
				+ ServerConfigurationService.getPortalUrl() + "/site/" + siteId + "\">" + title + "</a> site.<br />"
				+ "You can modify how you receive notifications under Preferences.</div>";
		return rv;
	}

	/**
	 * @inheritDoc
	 */
	protected boolean isBodyHTML(Event e)
	{
		return true;
	}

	/**
	 * Format the announcement notification subject line.
	 * 
	 * @param event
	 *        The event that matched criteria to cause the notification.
	 * @return the announcement notification subject line.
	 */
	protected String getSubject(Event event)
	{
		// get the message
		Reference ref = EntityManager.newReference(event.getResource());
		AnnouncementMessage msg = (AnnouncementMessage) ref.getEntity();
		AnnouncementMessageHeader hdr = (AnnouncementMessageHeader) msg.getAnnouncementHeader();

		// use either the configured site, or if not configured, the site (context) of the resource
		String siteId = (getSite() != null) ? getSite() : ref.getContext();

		// get a site title
		String title = siteId;
		try
		{
			Site site = SiteService.getSite(siteId);
			title = site.getTitle();
		}
		catch (Exception ignore)
		{
		}

		// use the message's subject
		//return "[ " + title + " - " + rb.getString("Announcement") + " ]   " + hdr.getSubject();
		return hdr.getSubject() + " ["+ rb.getString("Announcement") +" from " + title +"]";
	}

	/**
	 * Add to the user list any other users who should be notified about this ref's change.
	 * 
	 * @param users
	 *        The user list, already populated based on site visit and resource ability.
	 * @param ref
	 *        The entity reference.
	 */
	protected void addSpecialRecipients(List users, Reference ref)
	{
		// include any users who have AnnouncementService.SECURE_ALL_GROUPS and getResourceAbility() in the context
		String contextRef = SiteService.siteReference(ref.getContext());

		// get the list of users who have SECURE_ALL_GROUPS
		List allGroupUsers = SecurityService.unlockUsers(AnnouncementService.SECURE_ANNC_ALL_GROUPS, contextRef);

		// filter down by the permission
		if (getResourceAbility() != null)
		{
			List allGroupUsers2 = SecurityService.unlockUsers(getResourceAbility(), contextRef);
			allGroupUsers.retainAll(allGroupUsers2);
		}

		// remove any in the list already
		allGroupUsers.removeAll(users);

		// combine
		users.addAll(allGroupUsers);
	}
	
	/**
	 * For ScheduledInvocationCommand, use a medium (0 .. 1000) priority
	 */
	public Integer getPriority()
	{
		return Integer.valueOf(500);
	}

	/**
	 * Implementation of command pattern. Will be called by ScheduledInvocationManager
	 * for delayed announcement notifications
	 *
	 * @param opaqueContext
	 * 			reference (context) for message
	 */
	public void execute(String opaqueContext)
	{
		// get the message
		final Reference ref = EntityManager.newReference(opaqueContext);

		// needed to access the message
		enableSecurityAdvisor();

		final AnnouncementMessage msg = (AnnouncementMessage) ref.getEntity();
		final AnnouncementMessageHeader hdr = (AnnouncementMessageHeader) msg.getAnnouncementHeader();
		// do not do notification for draft messages
		if (hdr.getDraft()) return;

		// read the notification options
		final String notif = msg.getProperties().getProperty("notificationLevel");

		int noti = NotificationService.NOTI_OPTIONAL;
		if ("r".equals(notif))
		{
			noti = NotificationService.NOTI_REQUIRED;
		}
		else if ("n".equals(notif))
		{
			noti = NotificationService.NOTI_NONE;
		}
		
		// use either the configured site, or if not configured, the site (context) of the resource
		String siteId = (getSite() != null) ? getSite() : ref.getContext();
		try
		{
			Site site = SiteService.getSite(siteId);
			if ((site.isPublished() == false) && (noti != NotificationService.NOTI_REQUIRED))
			{
				return;
			}
		}
		catch (Exception ignore)
		{
		}

		final Event delayedNotificationEvent = EventTrackingService.newEvent("annc.schInv.notify", msg.getReference(), true, noti);
//		EventTrackingService.post(event);

		final NotificationService notificationService = (NotificationService) ComponentManager.get(org.sakaiproject.event.api.NotificationService.class);
		NotificationEdit notify = notificationService.addTransientNotification();

		super.notify(notify, delayedNotificationEvent);
 		
		// since we build the notification by accessing the
		// message within the super class, can't remove the
		// SecurityAdvisor until this point
		// done with access, need to remove from stack
		SecurityService.clearAdvisors();

	}

	/**
	 * Establish a security advisor to allow the "embedded" azg work to occur
	 * with no need for additional security permissions.
	 */
	protected void enableSecurityAdvisor() {
		// put in a security advisor so we can do our podcast work without need
		// of further permissions
		SecurityService.pushAdvisor(new SecurityAdvisor() {
			public SecurityAdvice isAllowed(String userId, String function,
					String reference) {
				return SecurityAdvice.ALLOWED;
			}
		});
	}
}