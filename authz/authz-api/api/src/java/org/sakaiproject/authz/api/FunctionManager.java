/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/authz/authz-api/api/src/java/org/sakaiproject/authz/api/FunctionManager.java $
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

package org.sakaiproject.authz.api;

import java.util.List;

/**
 * <p>
 * FunctionManager is the API for the service that manages security function registrations from the various Sakai applications.
 * </p>
 */
public interface FunctionManager
{
	/**
	 * Register an authz function
	 * 
	 * param function The function name.
	 */
	void registerFunction(String function);

	/**
	 * Access all the registered functions.
	 * 
	 * @return A List (String) of registered functions.
	 */
	List getRegisteredFunctions();

	/**
	 * Access all the registered functions that begin with the string.
	 * 
	 * @param prefix
	 *        The prefix pattern to find.
	 * @return A List (String) of registered functions that begin with the string.
	 */
	List getRegisteredFunctions(String prefix);
}
