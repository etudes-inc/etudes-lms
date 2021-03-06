/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/message/message-api/api/src/java/org/sakaiproject/message/api/MessageEdit.java $
 * $Id: MessageEdit.java 3 2008-10-20 18:44:42Z ggolden $
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

package org.sakaiproject.message.api;

import org.sakaiproject.entity.api.Edit;

/**
 * <p>
 * MessageEdit is the base interface for all Sakai communications messages in r/w mode.
 * </p>
 */
public interface MessageEdit extends Message, Edit
{
	/**
	 * Replace the body, as a string.
	 * 
	 * @param body
	 *        The body, as a string.
	 */
	public void setBody(String body);

	/**
	 * Access the message header.
	 * 
	 * @return The message header.
	 */
	public MessageHeaderEdit getHeaderEdit();
}
