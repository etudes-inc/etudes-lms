/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/chat/chat-api/api/src/java/org/sakaiproject/chat/api/ChatService.java $
 * $Id: ChatService.java 8282 2014-06-18 18:32:02Z mallikamt $
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

package org.sakaiproject.chat.api;

import java.util.List;

import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.message.api.MessageService;

/**
 * <p>
 * ChatService is the extension to MessageService configured for Chat.
 * </p>
 * <p>
 * MessageChannels are ChatMessageChannels, and Messages are ChatMessages with ChatMessageHeaders.
 * </p>
 * <p>
 * Security is defined, see MessageService.
 * </p>
 * <p>
 * Usage Events are generated:
 * <ul>
 * <li>chat.message.channel.add - chat channel resource id</li>
 * <li>chat.message.channel.remove - chat channel resource id</li>
 * </ul>
 * </p>
 */
public interface ChatService extends MessageService
{
	/** The type string for this application: should not change over time as it may be stored in various parts of persistent entities. */
	static final String APPLICATION_ID = "sakai:chat";

	/** This string starts the references to resources in this service. */
	public static final String REFERENCE_ROOT = "/chat";

	/** Security function / event for updating channel / message. */
	public static final String SECURE_UPDATE = "site.upd";


	/**
	 * A (ChatChannel) cover for getChannel() to return a specific chat channel.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return the ChatChannel that has the specified name.
	 * @exception IdUnusedException
	 *            If this name is not defined for a chat channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to the channel.
	 */
	public ChatChannel getChatChannel(String ref) throws IdUnusedException, PermissionException;

	/**
	 * A (ChatChannel) cover for addChannel() to add a new chat channel.
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
	public ChatChannelEdit addChatChannel(String ref) throws IdUsedException, IdInvalidException, PermissionException;
	
	/**
	 * Used to update channel title, also updates the channel of all its messages (if any)
	 * 
	 * @param ref Channel reference
	 * @param newRef new reference
	 * @return The channel with the new reference
	 * @throws IdUsedException if the id is not unique.
	 * @throws IdInvalidException if the id is not made up of valid characters.
	 * @throws PermissionException if the user does not have permission to add a channel.
	 */
	public ChatChannelEdit updateChatChannel(String ref, String newRef) throws IdUsedException, IdInvalidException, PermissionException;

	/**
	 * Check to see if the list of chat rooms has positions.
	 * 
	 * @param chatList List of chat rooms
	 * @return false if no positions, true otherwise
	 */
	public boolean checkPositions(List chatList);
	
	/**
	 * Populates the positions of a site and puts the default channel in position 1
	 * 
	 * @param siteId The site id
	 * @param defaultChannel The default channel
	 */
	public void populatePositions(String siteId, String defaultChannel);
	
	/**
	 * Get the max position in the chat rooms of this list
	 * 
	 * @param chatList The chat list
	 * @return max position
	 */
	public int getMaxPosition(List chatList);
	
	/**
	 * Take in a list of chat rooms and return them sorted by position
	 * 
	 * @param from_channel_list Chat room list
	 * @return Chat room list sorted by position
	 */
	public List sortByPosition(List from_channel_list);
	
	
}
