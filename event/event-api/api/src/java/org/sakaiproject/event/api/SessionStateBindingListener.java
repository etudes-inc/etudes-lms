/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/event/event-api/api/src/java/org/sakaiproject/event/api/SessionStateBindingListener.java $
 * $Id: SessionStateBindingListener.java 3 2008-10-20 18:44:42Z ggolden $
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

package org.sakaiproject.event.api;

/**
 * <p>
 * SessionStateBindingListener is an interface for objects that wish to be notified when they are bound to and unbound from a SessionState.
 * </p>
 * <p>
 * This is loosely modeled on the HttpSessionBindingListener.
 * </p>
 */
public interface SessionStateBindingListener
{
	/**
	 * Accept notification that this object has been bound as a SessionState attribute.
	 * 
	 * @param sessionStateKey
	 *        The id of the session state which holds the attribute.
	 * @param attributeName
	 *        The id of the attribute to which this object is now the value.
	 */
	void valueBound(String sessionStateKey, String attributeName);

	/**
	 * Accept notification that this object has been removed from a SessionState attribute.
	 * 
	 * @param sessionStateKey
	 *        The id of the session state which held the attribute.
	 * @param attributeName
	 *        The id of the attribute to which this object was the value.
	 */
	void valueUnbound(String sessionStateKey, String attributeName);
}
