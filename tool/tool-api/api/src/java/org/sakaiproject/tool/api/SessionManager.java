/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/tool/tool-api/api/src/java/org/sakaiproject/tool/api/SessionManager.java $
 * $Id: SessionManager.java 4894 2013-05-20 21:01:29Z ggolden $
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

package org.sakaiproject.tool.api;

/**
 * <p>
 * SessionManager keeps track of Sakai-wide and Tool placement specific user sessions, modeled on the HttpSession of the Servlet API.
 * </p>
 */
public interface SessionManager
{
	/** Key in the ThreadLocalManager for the case where a session requested was invalid, and we started a new one. */
	final static String CURRENT_INVALID_SESSION = "sakai:session.was.invalid";

	/**
	 * Access the known session with this id.
	 * 
	 * @return The Session object that has this id, or null if the id is not known.
	 */
	Session getSession(String sessionId);

	/**
	 * Start a new session.
	 * 
	 * @return The new UsageSession.
	 */
	Session startSession();

	/**
	 * Promote a temp. session into a full stored session.
	 * 
	 * @return The new Session.
	 */
	Session startSession(Session session);

	/**
	 * Start a new session, using this session id.
	 * 
	 * @param id
	 *        The session Id to use.
	 * @return The new UsageSession.
	 */
	Session startSession(String id);

	/**
	 * Access the session associated with the current request or processing thread.
	 * 
	 * @return The session associatd with the current request or processing thread.
	 */
	Session getCurrentSession();

	/**
	 * Access the current session's user id, or return null if there is no current session.
	 * 
	 * @return The current session's user id, or null if there is no current session or it has no user id.
	 */
	String getCurrentSessionUserId();

	/**
	 * Access the tool session associated with the current request or processing thread.
	 * 
	 * @return The tool session associatd with the current request or processing thread.
	 */
	ToolSession getCurrentToolSession();

	/**
	 * Set this session as the current one for this request processing or thread.
	 * 
	 * @param s
	 *        The session to set as the current session.
	 */
	void setCurrentSession(Session s);

	/**
	 * Set this session as the current tool session for this request processing or thread.
	 * 
	 * @param s
	 *        The session to set as the current tool session.
	 */
	void setCurrentToolSession(ToolSession s);
}
