/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/announcement/announcement-api/api/src/java/org/sakaiproject/announcement/api/AnnouncementService.java $
 * $Id: AnnouncementService.java 6140 2013-10-11 18:18:03Z ggolden $
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

package org.sakaiproject.announcement.api;

import java.util.Date;
import java.util.List;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.message.api.MessageService;

/**
 * <p>
 * AnnouncementService is the extension to MessageService configured for Announcements.
 * </p>
 * <p>
 * MessageChannels are AnnouncementMessageChannels, and Messages are AnnouncementMessages with AnnouncementMessageHeaders.
 * </p>
 * <p>
 * Security in the announcement service, in addition to that defined in the channels, include:
 * <ul>
 * <li>announcement.channel.add</li>
 * </ul>
 * </p>
 * <li>announcement.channel.remove</li>
 * </ul>
 * </p>
 * <p>
 * Usage Events are generated:
 * <ul>
 * <li>announcement.channel.add - announcement channel resource id</li>
 * <li>announcement.channel.remove - announcement channel resource id</li>
 * </ul>
 * </p>
 */
public interface AnnouncementService extends MessageService
{
	/** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
	static final String APPLICATION_ID = "sakai:announcement";

	/** This string starts the references to resources in this service. */
	public static final String REFERENCE_ROOT = Entity.SEPARATOR + "announcement";

	/** Security lock / event root for generic message events to make it a mail event. */
	public static final String SECURE_ANNC_ROOT = "annc.";

	/** Security lock / event for reading channel / message. */
	public static final String SECURE_ANNC_READ = SECURE_ANNC_ROOT + SECURE_READ;

	/** Security lock / event for adding channel / message. */
	public static final String SECURE_ANNC_ADD = SECURE_ANNC_ROOT + SECURE_ADD;

	/** Security lock / event for removing one's own message. */
	public static final String SECURE_ANNC_REMOVE_OWN = SECURE_ANNC_ROOT + SECURE_REMOVE_OWN;

	/** Security lock / event for removing anyone's message or channel. */
	public static final String SECURE_ANNC_REMOVE_ANY = SECURE_ANNC_ROOT + SECURE_REMOVE_ANY;

	/** Security lock / event for updating one's own message or the channel. */
	public static final String SECURE_ANNC_UPDATE_OWN = SECURE_ANNC_ROOT + SECURE_UPDATE_OWN;

	/** Security lock / event for updating any message. */
	public static final String SECURE_ANNC_UPDATE_ANY = SECURE_ANNC_ROOT + SECURE_UPDATE_ANY;

	/** Security lock / event for accessing someone elses draft. */
	public static final String SECURE_ANNC_READ_DRAFT = SECURE_ANNC_ROOT + SECURE_READ_DRAFT;

	/** Security function giving the user permission to all groups, if granted to at the channel or site level. */
	public static final String SECURE_ANNC_ALL_GROUPS = SECURE_ANNC_ROOT + SECURE_ALL_GROUPS;

	/** release date property names for announcements        */
	public static final String RELEASE_DATE = "releaseDate";

	/** property that stores the notification level */
	public static final String NOTIFICATION_LEVEL = "notificationLevel";
	
	/** property that stores the schedule invocation id */
	public static final String SCHED_INV_UUID = "schInvUuid";


	/**
	 * A (AnnouncementChannel) cover for getChannel() to return a specific announcement channel.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return the AnnouncementChannel that has the specified name.
	 * @exception IdUnusedException
	 *            If this name is not defined for a announcement channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to the channel.
	 */
	public AnnouncementChannel getAnnouncementChannel(String ref) throws IdUnusedException, PermissionException;

	/**
	 * A (AnnouncementChannel) cover for addChannel() to add a new announcement channel.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return The newly created channel.
	 * @exception IdUsedException
	 *            if the id is not unique.
	 * @exception IdInvalidException
	 *            if the id is not made up of valid characters.
	 * @exception PermissionException
	 *            if the user does not have permission to add a channel.
	 */
	public AnnouncementChannelEdit addAnnouncementChannel(String ref) throws IdUsedException, IdInvalidException,
			PermissionException;
	
	/**
	 * Gets the earliest release date(if defined) of all the announcements 
	 * @param course_id		Course id
	 * @return If release date exists for the announcements gets the earliest release date of all announcements else returns null
	 */
	public Date getMinStartDate(String course_id);
	
	/**
	 * Gets the latest release date(if defined) of all the announcements 
	 * @param course_id		Course id
	 * @return If release date exists for the announcements gets the latest release date of all announcements else returns null
	 */
	public Date getMaxStartDate(String course_id);
	
	/**
	 * Apply base date to all announcement release dates
	 * If release date doesn't exist, apply base date to header dates and save the value in release date
	 * @param course_id		Course id
	 * @param days_diff		Time difference in days
	 */
	public void applyBaseDateTx(String course_id, int days_diff);
	
	/**
	 * Get the messages in the channel that are visible to "students", ordered descending by release date + message order, limited to the number of messages.
	 * 
	 * @param channelRef
	 *        The announcement channel reference string
	 * @param numMessages
	 *        The number of messages to include; includes all if =0
	 * @return A List of announcement messages.
	 */
	List<AnnouncementMessage> getRecentMessages(String channelRef, int numMessages) throws PermissionException;
}
