/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/user/user-impl/impl/src/java/org/sakaiproject/user/impl/PreferencesServiceTest.java $
 * $Id: PreferencesServiceTest.java 3 2008-10-20 18:44:42Z ggolden $
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

package org.sakaiproject.user.impl;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.EntityManager;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.tool.api.SessionManager;

/**
 * <p>
 * PreferencesServiceTest extends the db preferences service providing the dependency injectors for testing.
 * </p>
 */
public class PreferencesServiceTest extends DbPreferencesService
{
	/**
	 * @return the MemoryService collaborator.
	 */
	protected SqlService sqlService()
	{
		return null;
	}

	/**
	 * @return the MemoryService collaborator.
	 */
	protected MemoryService memoryService()
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
	 * @return the EntityManager collaborator.
	 */
	protected EntityManager entityManager()
	{
		return null;
	}

	/**
	 * @return the SecurityService collaborator.
	 */
	protected SecurityService securityService()
	{
		return null;
	}

	/**
	 * @return the FunctionManager collaborator.
	 */
	protected FunctionManager functionManager()
	{
		return null;
	}

	/**
	 * @return the SessionManager collaborator.
	 */
	protected SessionManager sessionManager()
	{
		return null;
	}

	/**
	 * @return the EventTrackingService collaborator.
	 */
	protected EventTrackingService eventTrackingService()
	{
		return null;
	}

	/**
	 * @return the UserDirectoryService collaborator.
	 */
	protected UserDirectoryService userDirectoryService()
	{
		return null;
	}
}
