/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/branches/110210trunk-mallika/site-manage/site-manage-api/api/src/java/org/sakaiproject/site/api/SiteInfo.java $
 * $Id: SiteInfo.java 3 2008-10-20 18:44:42Z ggolden $
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

package org.sakaiproject.site.api;
import org.sakaiproject.time.api.Time;

/**
 * <p>
 * SiteInfo is a class used to store a site's properties when it is being created. This was originally an inner class in SiteAction.java
 * </p>
 */
public class SiteInfo
{
	/** The null/empty string */
	private static final String NULL_STRING = "";
	public String site_id = NULL_STRING; // getId of Resource
	public String external_id = NULL_STRING; // if matches site_id connects site with U-M course information
	public String site_type = "";
	public String iconUrl = NULL_STRING;
	public String infoUrl = NULL_STRING;
	public boolean joinable = false;
	public String joinerRole = NULL_STRING;
	public String title = NULL_STRING; // the short name of the site
	public String short_description = NULL_STRING; // the short (20 char) description of the site
	public String description = NULL_STRING;  // the longer description of the site
	public String additional = NULL_STRING; // additional information on crosslists, etc.
	public boolean published = false;
	//Fields below added for SAK-9, publish unpublish dates
	public Time publishTime = null;
	public Time unpublishTime = null;
	public boolean publishLater = false;
	public boolean unpublishLater = false;
	//End
	
	public boolean include = true;	// include the site in the Sites index; default is true.
	public String site_contact_name = NULL_STRING;	// site contact name
	public String site_contact_email = NULL_STRING;	// site contact email

	public String getSiteId() {return site_id;}
	public String getSiteType() { return site_type; }
	public String getTitle() { return title; }
	public String getDescription() { return description; }
	public String getIconUrl() { return iconUrl; }
	public String getInfoUrll() { return infoUrl; }
	public boolean getJoinable() {return joinable; }
	public String getJoinerRole() {return joinerRole; }
	public String getAdditional() { return additional; }
	public boolean getPublished() { return published; }
	public Time getPublishTime() { return publishTime; }
	public Time getUnublishTime() { return unpublishTime; }
	public boolean getPublishLater() { return publishLater; }
	public boolean getUnpublishLater() { return unpublishLater; }
	public boolean getInclude() {return include;}
	public String getSiteContactName() {return site_contact_name; }
	public String getSiteContactEmail() {return site_contact_email; }

} // SiteInfo
