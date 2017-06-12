/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/user/user-api/api/src/java/org/sakaiproject/user/api/AuthenticationMultipleException.java $
 * $Id: AuthenticationMultipleException.java 720 2010-09-13 19:08:47Z ggolden $
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
 * AuthenticationMultipleException is used when authentication fails because the given eid is not unique.
 */
public class AuthenticationMultipleException extends AuthenticationFailedException
{
	private static final long serialVersionUID = 3256445802430936627L;
	
	public AuthenticationMultipleException()
	{
		super();
	}

	public AuthenticationMultipleException(String msg)
	{
		super(msg);
	}
}
