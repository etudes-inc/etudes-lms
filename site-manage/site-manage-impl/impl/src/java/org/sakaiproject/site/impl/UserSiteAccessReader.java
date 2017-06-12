/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/site-manage/site-manage-impl/impl/src/java/org/sakaiproject/site/impl/UserSiteAccessReader.java $
 * $Id: UserSiteAccessReader.java 11615 2015-09-15 19:23:10Z mallikamt $
 ***********************************************************************************
 *
 * Copyright (c) 2013, 2015 Etudes, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.site.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.site.api.UserSiteAccess;


public class UserSiteAccessReader implements SqlReader
{
	private static final Log LOG = LogFactory.getLog(UserSiteAccessReader.class);

	public Object readSqlResultRecord(ResultSet result)
	{

		UserSiteAccess userSiteAccess = new UserSiteAccess();

		try
		{
			userSiteAccess.siteId = result.getString("SITE_ID");
			userSiteAccess.userId = result.getString("USER_ID");
			userSiteAccess.days = result.getInt("DAYS");
			if (result.getInt("UNTIMED") == 1)userSiteAccess.untimed = Boolean.TRUE;
			else userSiteAccess.untimed = Boolean.FALSE;
			userSiteAccess.timelimit = result.getLong("TIME_LIMIT");
			userSiteAccess.timemult = result.getFloat("TIME_MULT");
		}
		catch (SQLException e)
		{
			LOG.error("SqlException: " + e);
			return null;
		}

		return userSiteAccess;
	}

}
