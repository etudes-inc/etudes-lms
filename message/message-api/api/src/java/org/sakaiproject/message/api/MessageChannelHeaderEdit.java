/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/branches/ETU-190/message/message-api/api/src/java/org/sakaiproject/message/api/MessageChannelHeaderEdit.java $
 * $Id: MessageChannelHeaderEdit.java 3 2008-10-20 18:44:42Z ggolden $
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

import java.util.Collection;

import org.sakaiproject.entity.api.AttachmentContainerEdit;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.user.api.User;

/**
 * <p>
 * MessageChannelHeader is the base Interface for a Sakai Channel headers. Header fields common to all message service channel headers are defined here.
 * </p>
 * 
 * @author Sakai Software Development Team
 */
public interface MessageChannelHeaderEdit extends MessageChannelHeader
{
	/**
	 * Set these as the message's groups, replacing the access and groups already defined.
	 * 
	 * @param Collection
	 *        groups The colelction of Group objects to use for this message.
	 * @throws PermissionException
	 *         if the end user does not have permission to remove from the groups that would be removed or add to the groups that would be added.
	 */
	void setGroupAccess(Collection groups) throws PermissionException;

	/**
	 * Remove any grouping for this message; the access mode reverts to channel and any groups are removed.
	 * 
	 * @throws PermissionException
	 *         if the end user does not have permission to do this.
	 */
	void clearGroupAccess() throws PermissionException;
}
