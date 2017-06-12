/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/user/user-api/api/src/java/org/sakaiproject/user/api/IdPwInstEvidence.java $
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

package org.sakaiproject.user.api;

/**
 * IdPwInstEvidence is Authentication evidence made up of a user identifier and a password and an institution code.<br />
 * Note the "id" used here is something the user offers for authentication purposes, and is *not* the user's Sakai user object UUID.
 */
public interface IdPwInstEvidence extends Evidence
{
	/**
	 * Access the user identifier.
	 * 
	 * @return The user identifier.
	 */
	String getIdentifier();

	/**
	 * Access the password.
	 * 
	 * @return The password.
	 */
	String getPassword();

	/**
	 * Access the institution code.
	 * 
	 * @return The institution code.
	 */
	String getInstitutionCode();
}
