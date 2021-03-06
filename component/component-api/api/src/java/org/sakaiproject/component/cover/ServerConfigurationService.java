/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/component/component-api/api/src/java/org/sakaiproject/component/cover/ServerConfigurationService.java $
 * $Id: ServerConfigurationService.java 3 2008-10-20 18:44:42Z ggolden $
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

package org.sakaiproject.component.cover;

import org.sakaiproject.component.cover.ComponentManager;

/**
 * <p>
 * ServerConfigurationService is a static Cover for the {@link org.sakaiproject.component.api.ServerConfigurationService ServerConfigurationService}; see that interface for usage details.
 * </p>
 * 
 * @author University of Michigan, Sakai Software Development Team
 * @version $Revision: 3 $
 */
public class ServerConfigurationService
{
	public final static String CURRENT_SERVER_URL = org.sakaiproject.component.api.ServerConfigurationService.CURRENT_SERVER_URL;

	/**
	 * Access the component instance: special cover only method.
	 * 
	 * @return the component instance.
	 */
	public static org.sakaiproject.component.api.ServerConfigurationService getInstance()
	{
		if (ComponentManager.CACHE_COMPONENTS)
		{
			if (m_instance == null)
				m_instance = (org.sakaiproject.component.api.ServerConfigurationService) ComponentManager
						.get(org.sakaiproject.component.api.ServerConfigurationService.class);
			return m_instance;
		}
		else
		{
			return (org.sakaiproject.component.api.ServerConfigurationService) ComponentManager
					.get(org.sakaiproject.component.api.ServerConfigurationService.class);
		}
	}

	private static org.sakaiproject.component.api.ServerConfigurationService m_instance = null;

	public static java.lang.String SERVICE_NAME = org.sakaiproject.component.api.ServerConfigurationService.SERVICE_NAME;

	public static java.lang.String getServerId()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getServerId();
	}

	public static java.lang.String getServerInstance()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getServerInstance();
	}

	public static java.lang.String getServerIdInstance()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getServerIdInstance();
	}

	public static java.lang.String getServerName()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getServerName();
	}

	public static java.lang.String getServerUrl()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getServerUrl();
	}

	public static java.lang.String getAccessUrl()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getAccessUrl();
	}

	public static java.lang.String getHelpUrl(java.lang.String param0)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getHelpUrl(param0);
	}

	public static java.lang.String getPortalUrl()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getPortalUrl();
	}

	public static java.lang.String getToolUrl()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getToolUrl();
	}

	public static java.lang.String getGatewaySiteId()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getGatewaySiteId();
	}

	public static java.lang.String getLoggedOutUrl()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getLoggedOutUrl();
	}

	public static java.lang.String getUserHomeUrl()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getUserHomeUrl();
	}

	public static java.lang.String getSakaiHomePath()
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getSakaiHomePath();
	}

	public static boolean getBoolean(java.lang.String param0, boolean param1)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return false;

		return service.getBoolean(param0, param1);
	}

	public static java.lang.String getString(java.lang.String param0, java.lang.String param1)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getString(param0, param1);
	}

	public static java.lang.String getString(java.lang.String param0)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getString(param0);
	}

	public static java.lang.String[] getStrings(java.lang.String param0)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getStrings(param0);
	}

	public static java.util.List getToolOrder(java.lang.String param0)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getToolOrder(param0);
	}

	public static java.util.List getToolsRequired(java.lang.String param0)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getToolsRequired(param0);
	}

	public static java.util.List getDefaultTools(java.lang.String param0)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return null;

		return service.getDefaultTools(param0);
	}

	public static int getInt(java.lang.String param0, int param1)
	{
		org.sakaiproject.component.api.ServerConfigurationService service = getInstance();
		if (service == null) return 0;

		return service.getInt(param0, param1);
	}
}
