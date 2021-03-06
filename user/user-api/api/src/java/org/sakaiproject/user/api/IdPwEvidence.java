/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/user/user-api/api/src/java/org/sakaiproject/user/api/IdPwEvidence.java $
 * $Id: IdPwEvidence.java 3 2008-10-20 18:44:42Z ggolden $
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

package org.sakaiproject.user.api;

/**
 * <p>
 * IdPwEvidence is Authetication evidence made up of a user identifier and a password. Note the "id" used here is something the user offers for authentication purposes, and is *not* the user's Sakai user object UUID.
 * </p>
 */
public interface IdPwEvidence extends Evidence
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
}
