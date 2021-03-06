/**********************************************************************************
*
* $Id: ContextManagement.java 3 2008-10-20 18:44:42Z ggolden $
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.facades;

/**
 * Facade to context management service, to give control of application entry
 * and gradebook selection to the framework.
 */
public interface ContextManagement {
	/**
	 * @param request
	 *	the javax.servlet.http.HttpServletRequest or javax.portlet.PortletRequest from
	 *	which to determine the current gradebook. Since they don't share an interface,
	 *	a generic object is passed.
	 *
	 * @return
	 *	the UID of the currently selected gradebook, or null if the context manager
	 *	cannot determine a selected gradebook
	 */
	public String getGradebookUid(Object request);

}


