/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/event/event-api/api/src/java/org/sakaiproject/event/api/NotificationNotDefinedException.java $
 * $Id: NotificationNotDefinedException.java 3 2008-10-20 18:44:42Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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

package org.sakaiproject.event.api;

/**
 * <p>
 * NotificationNotDefinedException is thrown whenever no Notification is associated with the requested id.
 * </p>
 */
public class NotificationNotDefinedException extends Exception
{
	private String m_id = null;

	public NotificationNotDefinedException(String id)
	{
		m_id = id;

	}

	public String getId()
	{
		return m_id;
	}

	public String toString()
	{
		return super.toString() + " id=" + m_id;
	}
}
