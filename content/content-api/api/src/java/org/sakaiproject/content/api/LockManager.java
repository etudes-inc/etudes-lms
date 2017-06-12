/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/content/content-api/api/src/java/org/sakaiproject/content/api/LockManager.java $
 * $Id: LockManager.java 3 2008-10-20 18:44:42Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.content.api;

import java.util.Collection;

/**
 * Manager long-term locks on Content objects.
 */
public interface LockManager
{
	void lockObject(String assetId, String qualifierId, String reason, boolean system);

	void removeLock(String assetId, String qualifierId);

	/**
	 * @param node -
	 *        the asset to check
	 * @return - a non-empty Collection of active Locks, or null
	 */
	Collection getLocks(String assetId);

	boolean isLocked(String assetId);

	void removeAllLocks(String qualifier);
}
