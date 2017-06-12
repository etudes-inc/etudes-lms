/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/user/user-util/util/src/java/org/sakaiproject/util/IdPwInstEvidence.java $
 * $Id: IdPwInstEvidence.java 720 2010-09-13 19:08:47Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2010 Etudes, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
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
 * IdPwInstEvidence is a utility class that implements the IdPwInstEvidence interface.
 * </p>
 */
public class IdPwInstEvidence implements org.sakaiproject.user.api.IdPwInstEvidence
{
	/** The user identifier string. */
	protected String m_identifier = null;

	/** The institution code string. */
	protected String m_instCode = null;

	/** The password string. */
	protected String m_password = null;

	/**
	 * Construct, with identifier and password.
	 * 
	 * @param identifier
	 *        The user identifier string.
	 * @param password
	 *        The password string. * @param instCode The institution code string.
	 */
	public IdPwInstEvidence(String identifier, String password, String instCode)
	{
		m_identifier = identifier;
		m_password = password;
		m_instCode = instCode;
	}

	/**
	 * @inheritDoc
	 */
	public String getIdentifier()
	{
		return m_identifier;
	}

	/**
	 * @inheritDoc
	 */
	public String getInstitutionCode()
	{
		return m_instCode;
	}

	/**
	 * @inheritDoc
	 */
	public String getPassword()
	{
		return m_password;
	}
}
