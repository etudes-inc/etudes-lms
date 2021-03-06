/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/rwiki/rwiki-util/util/src/java/uk/ac/cam/caret/sakai/rwiki/utils/UserDisplayHelper.java $
 * $Id: UserDisplayHelper.java 68 2008-10-21 16:24:43Z ggolden $
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

package uk.ac.cam.caret.sakai.rwiki.utils;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;

public class UserDisplayHelper
{

	private static boolean displayID = ServerConfigurationService.getBoolean("wiki.display.user.id", false);
	
	public static String formatDisplayName(String name)
	{
		return formatDisplayName(name, "Unknown"); //$NON-NLS-1$
	}

	public static String formatDisplayName(String name, String defaultName)
	{
		User user;
		try
		{
			user = UserDirectoryService.getUser(name);
		}
		catch (UserNotDefinedException e)
		{
			if ( displayID )
				return defaultName + " (" + XmlEscaper.xmlEscape(name) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			else
				return XmlEscaper.xmlEscape(defaultName);
		}
		if ( displayID ) {
			return XmlEscaper.xmlEscape(user.getDisplayName() + " (" + user.getDisplayId() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return XmlEscaper.xmlEscape(user.getDisplayName()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
}
