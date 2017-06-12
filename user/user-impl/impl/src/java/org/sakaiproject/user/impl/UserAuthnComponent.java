/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/user/user-impl/impl/src/java/org/sakaiproject/user/impl/UserAuthnComponent.java $
 * $Id: UserAuthnComponent.java 5616 2013-08-14 02:00:20Z ggolden $
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

package org.sakaiproject.user.impl;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.user.api.Authentication;
import org.sakaiproject.user.api.AuthenticationException;
import org.sakaiproject.user.api.AuthenticationManager;
import org.sakaiproject.user.api.AuthenticationMultipleException;
import org.sakaiproject.user.api.AuthenticationUnknownException;
import org.sakaiproject.user.api.Evidence;
import org.sakaiproject.user.api.ExternalTrustedEvidence;
import org.sakaiproject.user.api.IdPwEvidence;
import org.sakaiproject.user.api.IdPwInstEvidence;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * <p>
 * An Authentication component working with the UserDirectoryService.
 * </p>
 */
public abstract class UserAuthnComponent implements AuthenticationManager
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(UserAuthnComponent.class);

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @return the UserDirectoryService collaborator.
	 */
	protected abstract UserDirectoryService userDirectoryService();


	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		M_log.info("init()");
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Work interface methods: AuthenticationManager
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public Authentication authenticate(Evidence e) throws AuthenticationException
	{
		if (e instanceof IdPwEvidence)
		{
			IdPwEvidence evidence = (IdPwEvidence) e;

			// reject null or blank
			if ((evidence.getPassword() == null) || (evidence.getPassword().trim().length() == 0)
					|| (evidence.getIdentifier() == null) || (evidence.getIdentifier().trim().length() == 0))
			{
				throw new AuthenticationException("invalid login");
			}

			// reject if the eid / password are not unique
			if (userDirectoryService().nonUniqueEid(evidence.getIdentifier(), evidence.getPassword()))
			{
				throw new AuthenticationMultipleException("invalid login");
			}

			// the evidence id must match a defined User
			User user = userDirectoryService().authenticate(evidence.getIdentifier(), evidence.getPassword());
			if (user == null)
			{
				throw new AuthenticationException("invalid login");
			}

			Authentication rv = new org.sakaiproject.util.Authentication(user.getId(), user.getEid(), checkPasswordStrength(evidence.getPassword()));
			return rv;
		}

		else if (e instanceof IdPwInstEvidence)
		{
			IdPwInstEvidence evidence = (IdPwInstEvidence) e;

			// reject null or blank
			if ((evidence.getPassword() == null) || (evidence.getPassword().trim().length() == 0)
					|| (evidence.getIdentifier() == null) || (evidence.getIdentifier().trim().length() == 0)
					|| (evidence.getInstitutionCode() == null) || (evidence.getInstitutionCode().trim().length() == 0))
			{
				throw new AuthenticationException("invalid login");
			}

			// the evidence id must match a defined User
			User user = userDirectoryService().authenticateIid(evidence.getIdentifier(), evidence.getPassword(), evidence.getInstitutionCode());
			if (user == null)
			{
				throw new AuthenticationException("invalid login");
			}

			Authentication rv = new org.sakaiproject.util.Authentication(user.getId(), user.getEid(), checkPasswordStrength(evidence.getPassword()));
			return rv;
		}

		else if (e instanceof ExternalTrustedEvidence)
		{
			ExternalTrustedEvidence evidence = (ExternalTrustedEvidence) e;

			// reject null or blank
			if ((evidence.getIdentifier() == null) || (evidence.getIdentifier().trim().length() == 0))
			{
				throw new AuthenticationException("invalid login");
			}

			// accept, so now lookup the user in our database.
			try
			{
				User user = userDirectoryService().getUserByEid(evidence.getIdentifier());

				Authentication rv = new org.sakaiproject.util.Authentication(user.getId(), user.getEid(), Boolean.FALSE);
				return rv;
			}
			catch (UserNotDefinedException ex)
			{
				// reject if the user is not defined
				// TODO: create the user record here?
				throw new AuthenticationException("invalid login");
			}
		}

		else
		{
			throw new AuthenticationUnknownException(e.toString());
		}
	}

	/**
	 * @inheritDoc
	 */
	public Boolean checkPasswordStrength(String pw)
	{
		String trim = pw.trim();
		if (trim.length() < 8) return Boolean.FALSE;
		if (!Pattern.matches(".*?[A-Z].*?", trim)) return Boolean.FALSE;
		if (!Pattern.matches(".*?[a-z].*?", trim)) return Boolean.FALSE;
		if (!Pattern.matches(".*?[0-9].*?", trim)) return Boolean.FALSE;

		return Boolean.TRUE;
	}
}
