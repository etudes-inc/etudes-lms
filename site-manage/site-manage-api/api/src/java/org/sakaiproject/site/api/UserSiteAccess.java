/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/site-manage/site-manage-api/api/src/java/org/sakaiproject/site/api/UserSiteAccess.java $
 * $Id: UserSiteAccess.java 11615 2015-09-15 19:23:10Z mallikamt $
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
package org.sakaiproject.site.api;

public class UserSiteAccess
{
	public String userId;
	public String siteId;
	public int days;
	public boolean untimed;
	public long timelimit;
	public float timemult;

	public UserSiteAccess()
	{
	}

	public UserSiteAccess(String userId, String siteId, int days, boolean untimed, long timelimit, float timemult)
	{
		this.userId = userId;
		this.siteId = siteId;
		this.days = days;
		this.untimed = untimed;
		this.timelimit = timelimit;
		this.timemult = timemult;
	}

}
