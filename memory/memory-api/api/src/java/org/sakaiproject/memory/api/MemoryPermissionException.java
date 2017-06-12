/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/memory/memory-api/api/src/java/org/sakaiproject/memory/api/MemoryPermissionException.java $
 * $Id: MemoryPermissionException.java 3 2008-10-20 18:44:42Z ggolden $
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

package org.sakaiproject.memory.api;

/**
 * <p>
 * MemoryPermissionException is thrown by the Memory system when an activity is attempted that the end-user does not have permission to do.
 * </p>
 */
public class MemoryPermissionException extends Exception
{
	/** The id of the user. */
	private String m_user = null;

	/**
	 * Access the id of the user.
	 * 
	 * @return The id of the user.
	 */
	public String getUser()
	{
		return m_user;
	}

	/** The function name. */
	private String m_function = null;

	/**
	 * Access the function name.
	 * 
	 * @return The function name.
	 */
	public String getFunction()
	{
		return m_function;
	}

	/** The resource id. */
	private String m_resource = null;

	/**
	 * Access the resource id.
	 * 
	 * @return The resource id.
	 */
	public String getResource()
	{
		return m_resource;
	}

	/**
	 * Construct.
	 * 
	 * @param user
	 *        The id of the user.
	 * @param function
	 *        The function name.
	 * @param resource
	 *        The resource id.
	 */
	public MemoryPermissionException(String user, String function, String resource)
	{
		m_user = user;
		m_function = function;
		m_resource = resource;
	}

	public String toString()
	{
		return super.toString() + " user=" + m_user + " function=" + m_function + " resource=" + m_resource;
	}
}