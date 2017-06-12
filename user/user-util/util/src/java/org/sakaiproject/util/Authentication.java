/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/user/user-util/util/src/java/org/sakaiproject/util/Authentication.java $
 * $Id: Authentication.java 5592 2013-08-13 02:58:43Z ggolden $
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

/**
 * <p>
 * Authentication is a utility class that implements the Authentication interface.
 * </p>
 */
public class Authentication implements org.sakaiproject.user.api.Authentication
{
	/** The UUID identifier string. */
	protected String m_uid = null;

	/** The enterprise identifier string. */
	protected String m_eid = null;

	/** User password is strong indicator. */
	protected Boolean m_passwordStrength = Boolean.FALSE;

	/**
	 * Construct, with uid and eid
	 * 
	 * @param uid
	 *        The UUID internal end user identifier string.
	 * @param eid
	 *        The enterprise end user identifier string.
	 * @param passwordStrength
	 *        TRUE for strong passwords, FALSE for week.
	 */
	public Authentication(String uid, String eid, Boolean passwordStrength)
	{
		m_uid = uid;
		m_eid = eid;
		m_passwordStrength = passwordStrength;
	}

	/**
	 * @inheritDoc
	 */
	public String getUid()
	{
		return m_uid;
	}

	/**
	 * @inheritDoc
	 */
	public String getEid()
	{
		return m_eid;
	}

	/**
	 * @inheritDoc
	 */
	public Boolean getPasswordStrength()
	{
		return m_passwordStrength;
	}
}
