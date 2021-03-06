/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-app/src/java/org/sakaiproject/tool/section/filter/AuthorizationFilterConfigurationBean.java $
 * $Id: AuthorizationFilterConfigurationBean.java 3 2008-10-20 18:44:42Z ggolden $
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
package org.sakaiproject.tool.section.filter;

import java.util.*;

/**
 * Singleton bean to set up URL filtering by current user's role.
 */
public class AuthorizationFilterConfigurationBean {
	private List manageEnrollments;
	private List manageTeachingAssistants;
	private List manageAllSections;
	private List viewAllSections;
	private List viewOwnSections;
	
	public List getManageEnrollments() {
		return manageEnrollments;
	}
	public void setManageEnrollments(List manageEnrollments) {
		this.manageEnrollments = manageEnrollments;
	}
	public List getManageAllSections() {
		return manageAllSections;
	}
	public void setManageAllSections(List manageAllSections) {
		this.manageAllSections = manageAllSections;
	}
	public List getManageTeachingAssistants() {
		return manageTeachingAssistants;
	}
	public void setManageTeachingAssistants(List manageTeachingAssistants) {
		this.manageTeachingAssistants = manageTeachingAssistants;
	}
	public List getViewOwnSections() {
		return viewOwnSections;
	}
	public void setViewOwnSections(List viewOwnSections) {
		this.viewOwnSections = viewOwnSections;
	}
	public List getViewAllSections() {
		return viewAllSections;
	}
	public void setViewAllSections(List viewAllSections) {
		this.viewAllSections = viewAllSections;
	}

}
