/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/tool/tool-util/servlet/src/java/org/sakaiproject/util/ToolListener.java $
 * $Id: ToolListener.java 3 2008-10-20 18:44:42Z ggolden $
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

package org.sakaiproject.util;

import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.cover.ActiveToolManager;

/**
 * <p>
 * Webapp listener to detect webapp-housed tool registration.
 * </p>
 */
public class ToolListener implements ServletContextListener
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(ToolListener.class);

	/**
	 * Initialize.
	 */
	public void contextInitialized(ServletContextEvent event)
	{
		// find the resources in the webapp in the /tools/ area TODO: param this
		Set paths = event.getServletContext().getResourcePaths("/tools/");
		if (paths == null) return;

		for (Iterator i = paths.iterator(); i.hasNext();)
		{
			String path = (String) i.next();

			// skip directories
			if (path.endsWith("/")) continue;

			// load this
			M_log.info("registering tools from resource: " + path);
			ActiveToolManager.register(event.getServletContext().getResourceAsStream(path), event.getServletContext());
		}
	}

	/**
	 * Destroy.
	 */
	public void contextDestroyed(ServletContextEvent event)
	{
	}
}
