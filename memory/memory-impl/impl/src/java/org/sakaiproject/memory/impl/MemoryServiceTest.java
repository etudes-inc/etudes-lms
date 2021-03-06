/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/memory/memory-impl/impl/src/java/org/sakaiproject/memory/impl/MemoryServiceTest.java $
 * $Id: MemoryServiceTest.java 3 2008-10-20 18:44:42Z ggolden $
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

package org.sakaiproject.memory.impl;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.UsageSessionService;

/**
 * <p>
 * MemoryServiceTest extends the basic memory service providing the dependency injectors for testing.
 * </p>
 */
public class MemoryServiceTest extends BasicMemoryService
{
	/**
	 * @return the EventTrackingService collaborator.
	 */
	protected EventTrackingService eventTrackingService()
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
	 * @return the UsageSessionService collaborator.
	 */
	protected UsageSessionService usageSessionService()
	{
		return null;
	}
}
