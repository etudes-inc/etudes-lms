/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/event/event-impl/impl/src/java/org/sakaiproject/event/impl/NotificationServiceTest.java $
 * $Id: NotificationServiceTest.java 3 2008-10-20 18:44:42Z ggolden $
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

package org.sakaiproject.event.impl;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.id.api.IdManager;

/**
 * <p>
 * NotificationServiceTest extends the db alias service providing the dependency injectors for testing.
 * </p>
 */
public class NotificationServiceTest extends DbNotificationService
{
	/**
	 * @return the EventTrackingService collaborator.
	 */
	protected EventTrackingService eventTrackingService()
	{
		return null;
	}

	/**
	 * @return the ServerConfigurationService collaborator.
	 */
	protected ServerConfigurationService serverConfigurationService()
	{
		return null;
	}

	/**
	 * @return the IdManager collaborator.
	 */
	protected IdManager idManager()
	{
		return null;
	}

	/**
	 * @return the MemoryService collaborator.
	 */
	protected SqlService sqlService()
	{
		return null;
	}
}
