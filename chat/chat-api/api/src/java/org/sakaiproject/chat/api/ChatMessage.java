/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/chat/chat-api/api/src/java/org/sakaiproject/chat/api/ChatMessage.java $
 * $Id: ChatMessage.java 3 2008-10-20 18:44:42Z ggolden $
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

import org.sakaiproject.message.api.Message;

/**
 * <p>
 * ChatMessage is the Interface for a Sakai Chat message.
 * </p>
 * <p>
 * The chat message has header fields (from, date) and a body (text). Each message also has an id, unique within the channel. All fields are read only.
 * </p>
 */
public interface ChatMessage extends Message
{
	/**
	 * A (ChatMessageHeader) cover for getHeader to access the chat message header.
	 * 
	 * @return The chat message header.
	 */
	public ChatMessageHeader getChatHeader();
}
