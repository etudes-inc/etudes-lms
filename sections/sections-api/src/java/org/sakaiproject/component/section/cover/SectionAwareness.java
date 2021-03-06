/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-api/src/java/org/sakaiproject/component/section/cover/SectionAwareness.java $
 * $Id: SectionAwareness.java 3 2008-10-20 18:44:42Z ggolden $
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
package org.sakaiproject.component.section.cover;

import java.util.List;
import java.util.Locale;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.facade.Role;

public class SectionAwareness {
	private static org.sakaiproject.api.section.SectionAwareness instance;

	private static org.sakaiproject.api.section.SectionAwareness getInstance() {
		if(instance == null) {
			instance = (org.sakaiproject.api.section.SectionAwareness)ComponentManager.get(
					org.sakaiproject.api.section.SectionAwareness.class);
		}
		return instance;
	}
	
	public static final List findSiteMembersInRole(String siteContext, Role role, String pattern) {
		return getInstance().findSiteMembersInRole(siteContext, role, pattern);
	}

	public static final String getCategoryName(String categoryId, Locale locale) {
		return getInstance().getCategoryName(categoryId, locale);
	}

	public static final CourseSection getSection(String sectionUuid) {
		return getInstance().getSection(sectionUuid);
	}

	public static final List getSectionCategories(String siteContext) {
		return getInstance().getSectionCategories(siteContext);
	}

	public static final List getSectionMembers(String sectionUuid) {
		return getInstance().getSectionMembers(sectionUuid);
	}

	public static final List getSectionMembersInRole(String sectionUuid, Role role) {
		return getInstance().getSectionMembersInRole(sectionUuid, role);
	}

	public static final List getSections(String siteContext) {
		return getInstance().getSections(siteContext);
	}

	public static final List getSectionsInCategory(String siteContext, String categoryId) {
		return getInstance().getSectionsInCategory(siteContext, categoryId);
	}

	public static final List getSiteMembersInRole(String siteContext, Role role) {
		return getInstance().getSiteMembersInRole(siteContext, role);
	}

	public static final boolean isSectionMemberInRole(String sectionUuid, String userUid, Role role) {
		return getInstance().isSectionMemberInRole(sectionUuid, userUid, role);
	}

	public static final boolean isSiteMemberInRole(String siteContext, String userUid, Role role) {
		return getInstance().isSiteMemberInRole(siteContext, userUid, role);
	}
	
    public static final List getUnassignedMembersInRole(String siteContext, Role role) {
    	return getInstance().getUnassignedMembersInRole(siteContext, role);
    }

}
