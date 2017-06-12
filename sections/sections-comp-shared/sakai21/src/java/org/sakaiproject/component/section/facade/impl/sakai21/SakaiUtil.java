/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-comp-shared/sakai21/src/java/org/sakaiproject/component/section/facade/impl/sakai21/SakaiUtil.java $
 * $Id: SakaiUtil.java 5866 2013-09-05 17:11:13Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California and The Regents of the University of Michigan
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
package org.sakaiproject.component.section.facade.impl.sakai21;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.component.section.sakai21.UserImpl;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;

public class SakaiUtil {
	private static final Log log = LogFactory.getLog(SakaiUtil.class);

	/**
	 * Gets a User from Sakai's UserDirectory (legacy) service.
	 * 
	 * @param userUid The user uuid
	 * @return
	 */
	public static final User getUserFromSakai(String userUid) {
		final org.sakaiproject.user.api.User sakaiUser;
		try {
			sakaiUser = UserDirectoryService.getUser(userUid);
		} catch (UserNotDefinedException e) {
			log.error("User not found: " + userUid);
			e.printStackTrace();
			return null;
		}
		return convertUser(sakaiUser);
	}

	/**
	 * Converts a sakai user object into a user object suitable for use in the section manager tool and in section awareness.
	 * 
	 * @param sakaiUser
	 *        The sakai user, as returned by Sakai's legacy SecurityService.
	 * 
	 * @return
	 */
	public static final User convertUser(final org.sakaiproject.user.api.User sakaiUser)
	{
		String userIdStr = null;
		Placement placement = ToolManager.getCurrentPlacement();
		if (placement != null)
		{
			String context = placement.getContext();
			userIdStr = sakaiUser.getIidInContext(context);
			if (userIdStr == null) userIdStr = sakaiUser.getEid();
		}
		else
		{
			// Note: this no-current-placement scenario is when the roster is processing and updating a site's sections based on the new student information.
			// in this case, we revert to the old behavior and use the display id -ggolden
			userIdStr = sakaiUser.getDisplayId();
		}

		UserImpl user = new UserImpl(userIdStr, sakaiUser.getDisplayName(), sakaiUser.getSortName(), sakaiUser.getId());
		return user;
	}
	
    /**
     * @return The current sakai authz reference
     */
    public static final String getSiteReference() {
        Placement placement = ToolManager.getCurrentPlacement();
        String context = placement.getContext();
        return SiteService.siteReference(context);
    }


}
