/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/user/user-api/api/src/java/org/sakaiproject/user/api/User.java $
 * $Id: User.java 5772 2013-08-30 16:50:23Z mallikamt $
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

package org.sakaiproject.user.api;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.time.api.Time;

/**
 * <p>
 * User models a Sakai end-user.
 * </p>
 */
public interface User extends Entity, Comparable
{
	/**
	 * @return the user who created this.
	 */
	User getCreatedBy();

	/**
	 * @return the user who last modified this.
	 */
	User getModifiedBy();

	/**
	 * @return the time created.
	 */
	Time getCreatedTime();

	/**
	 * @return the time last modified.
	 */
	Time getModifiedTime();

	/**
	 * Access the email address.
	 * 
	 * @return The email address string.
	 */
	String getEmail();
	
	/**
	 * Access the user's name for display purposes.
	 * 
	 * @return The user's name for display purposes.
	 */
	String getDisplayName();

	/**
	 * Access the user's name for sorting purposes.
	 * 
	 * @return The user's name for sorting purposes.
	 */
	String getSortName();

	/**
	 * Access the user's first name.
	 * 
	 * @return The user's first name.
	 */
	String getFirstName();

	/**
	 * Access the user's last name.
	 * 
	 * @return The user's last name.
	 */
	String getLastName();

	/**
	 * Check if this is the user's password.
	 * 
	 * @param pw
	 *        The clear text password to check.
	 * @return true if the password matches, false if not.
	 */
	boolean checkPassword(String pw);

	/**
	 * Access the user type.
	 * 
	 * @return The user type.
	 */
	String getType();

	/**
	 * Access the user's enterprise id; the id they and the enterprise know as belonging to them.<br />
	 * The Enterprise id, like the User id, is unique among all defined users.<br />
	 * The EID may be used by the user to login, and will be used when communicating with the user directory provider.
	 * 
	 * @return The user's enterprise id.
	 */
	String getEid();

	/**
	 * Access a string portraying the user's enterprise identity, for display purposes.<br />
	 * Use this, not getEid(), when displaying the user's id, probably along with the user's sort or display name, for disambiguating purposes.
	 * 
	 * @return The user's display id string.
	 */
	String getDisplayId();

	/**
	 * @return the user's IID information in a single display string.
	 */
	String getIidDisplay();

	/**
	 * Access the user's iid in a given context (site).
	 * 
	 * @param context
	 *        The context - select the user's iid for the institution code that matches this context.
	 * @return The user's iid for the given context, or null if not defined.
	 */
	String getIidInContext(String context);
}
