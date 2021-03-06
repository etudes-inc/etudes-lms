/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/authz/authz-api/api/src/java/org/sakaiproject/authz/cover/FunctionManager.java $
 * $Id: FunctionManager.java 3 2008-10-20 18:44:42Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.authz.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * FunctionManager is a static Cover for the {@link org.sakaiproject.authz.api.FunctionManager FunctionManager}; see that interface for usage details.
 * </p>
 */
public class FunctionManager
{
	/** Possibly cached component instance. */
	private static org.sakaiproject.authz.api.FunctionManager m_instance = null;

	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.authz.api.FunctionManager getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.authz.api.FunctionManager) ComponentManager
						.get(org.sakaiproject.authz.api.FunctionManager.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.authz.api.FunctionManager) ComponentManager
					.get(org.sakaiproject.authz.api.FunctionManager.class);
		}
	}

	public static void registerFunction(java.lang.String param0)
	{
		org.sakaiproject.authz.api.FunctionManager manager = getInstance();
		if (manager == null) return;

		manager.registerFunction(param0);
	}

	public static java.util.List getRegisteredFunctions()
	{
		org.sakaiproject.authz.api.FunctionManager manager = getInstance();
		if (manager == null) return null;

		return manager.getRegisteredFunctions();
	}

	public static java.util.List getRegisteredFunctions(java.lang.String param0)
	{
		org.sakaiproject.authz.api.FunctionManager manager = getInstance();
		if (manager == null) return null;

		return manager.getRegisteredFunctions(param0);
	}
}
