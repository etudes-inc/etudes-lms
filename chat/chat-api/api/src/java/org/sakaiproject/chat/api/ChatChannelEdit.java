/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/chat/chat-api/api/src/java/org/sakaiproject/chat/api/ChatChannelEdit.java $
 * $Id: ChatChannelEdit.java 8048 2014-05-30 00:01:56Z mallikamt $
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

import org.sakaiproject.message.api.MessageChannelEdit;

/**
 * <p>
 * ChatChannelEdit is an editable ChatChannel.
 * </p>
 */
public interface ChatChannelEdit extends ChatChannel, MessageChannelEdit
{
	/**
	 * A (ChatChannelHeaderEdit) cover for getHeaderEdit to access the chat channel header.
	 * 
	 * @return The chat channel header.
	 */
	public ChatChannelHeaderEdit getChatChannelHeaderEdit();
}
