/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/announcement/announcement-api/api/src/java/org/sakaiproject/announcement/api/AnnouncementChannel.java $
 * $Id: AnnouncementChannel.java 12190 2015-12-02 20:20:14Z mallikamt $
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

import java.util.List;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.message.api.MessageChannel;

/**
 * <p>
 * AnnouncementChannel is the extension to the MessageChanel interface for a Sakai Announcement service announcement channel.
 * </p>
 */
public interface AnnouncementChannel extends MessageChannel
{
	/**
	 * A (AnnouncementMessage) cover for getMessage to return a specific announcement channel message, as specified by message id.
	 * 
	 * @param messageId
	 *        The id of the message to get.
	 * @return the AnnouncementMessage that has the specified id.
	 * @exception IdUnusedException
	 *            If this name is not a defined message in this announcement channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to read the message.
	 */
	public AnnouncementMessage getAnnouncementMessage(String messageId) throws IdUnusedException, PermissionException;

	/**
	 * A (AnnouncementMessageEdit) cover for editMessage. Return a specific channel message, as specified by message name, locked for update. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @param messageId
	 *        The id of the message to get.
	 * @return the Message that has the specified id.
	 * @exception IdUnusedException
	 *            If this name is not a defined message in this channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to read the message.
	 * @exception InUseException
	 *            if the current user does not have permission to mess with this user.
	 */
	public AnnouncementMessageEdit editAnnouncementMessage(String messageId) throws IdUnusedException, PermissionException,
			InUseException;

	/**
	 * a (AnnouncementMessage) cover for addMessage to add a new message to this channel. Must commitEdit() to make official, or cancelEdit() when done!
	 * 
	 * @return The newly added message, locked for update.
	 * @exception PermissionException
	 *            If the user does not have write permission to the channel.
	 */
	public AnnouncementMessageEdit addAnnouncementMessage() throws PermissionException;

	/**
	 * a (AnnouncementMessage) cover for addMessage to add a new message to this channel.
	 * 
	 * @param subject
	 *        The message header subject.
	 * @param draft
	 *        The message header draft indication.
	 * @param attachments
	 *        The message header attachments, a vector of Reference objects.
	 * @param body
	 *        The message body.
	 * @return The newly added message.
	 * @exception PermissionException
	 *            If the user does not have write permission to the channel.
	 */
	public AnnouncementMessage addAnnouncementMessage(String subject, boolean draft, List attachments, String body)
			throws PermissionException;

	/**
	 * Check for a message in the channel that matches the key fields of this message
	 * 
	 * @param message
	 *        The message to check against.
	 * @return true if we find a close matching message, false if not.
	 */
	boolean containsMatchingMessage(AnnouncementMessage message);
	
	/**
	 * Return a list of all or filtered messages in the channel. The order in which the messages will be found in the iteration is by date, oldest first if ascending is true, newest first if ascending is false.
	 * Include archived messages.
	 * 
	 * @param filter
	 *        A filtering object to accept messages, or null if no filtering is desired.
	 * @param ascending
	 *        Order of messages, ascending if true, descending if false
	 * @return a list of channel Message objects or specializations of Message objects (may be empty).
	 * @exception PermissionException
	 *            if the user does not have read permission to the channel.
	 */
	List getAllMessages(Filter filter, boolean ascending) throws PermissionException;
	
	/**
	 * Return a list of all or filtered archived messages in the channel. The order in which the messages will be found in the iteration is by date, oldest first if ascending is true, newest first if ascending is false.
	 * 
	 * @param filter
	 *        A filtering object to accept messages, or null if no filtering is desired.
	 * @param ascending
	 *        Order of messages, ascending if true, descending if false
	 * @return a list of channel Message objects or specializations of Message objects (may be empty).
	 * @exception PermissionException
	 *            if the user does not have read permission to the channel.
	 */
	List getArchivedMessages(Filter filter, boolean ascending) throws PermissionException;	
	
}
