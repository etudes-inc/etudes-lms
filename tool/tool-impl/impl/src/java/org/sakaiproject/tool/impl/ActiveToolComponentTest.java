/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/tool/tool-impl/impl/src/java/org/sakaiproject/tool/impl/ActiveToolComponentTest.java $
 * $Id: ActiveToolComponentTest.java 3 2008-10-20 18:44:42Z ggolden $
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

package org.sakaiproject.tool.impl;

import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.thread_local.api.ThreadLocalManager;
import org.sakaiproject.tool.api.SessionManager;

/**
 * <p>
 * ActiveToolComponentTest extends the active tool component providing the dependency injectors for testing.
 * </p>
 */
public class ActiveToolComponentTest extends ActiveToolComponent
{
	/**
	 * @return the ThreadLocalManager collaborator.
	 */
	protected ThreadLocalManager threadLocalManager()
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
	 * @return the FunctionManager collaborator.
	 */
	protected FunctionManager functionManager()
	{
		return null;
	}
}
