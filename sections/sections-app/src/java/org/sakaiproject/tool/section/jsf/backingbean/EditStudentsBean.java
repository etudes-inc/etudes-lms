/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-app/src/java/org/sakaiproject/tool/section/jsf/backingbean/EditStudentsBean.java $
 * $Id: EditStudentsBean.java 3 2008-10-20 18:44:42Z ggolden $
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
package org.sakaiproject.tool.section.jsf.backingbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.api.section.exception.RoleConfigurationException;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.tool.section.decorator.CourseSectionDecorator;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Controls the edit students page (where students are assigned to sections).
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class EditStudentsBean extends EditManagersBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(EditStudentsBean.class);
	
	
	// For the "View" selectbox
	private String availableSectionUuid;
	private String availableSectionTitle;
	private Integer availableSectionMax;
	private List availableSectionItems;
	
	private Integer sectionMax;

	public void init() {
		CourseSectionDecorator currentSection = initializeFields();
		sectionMax = currentSection.getMaxEnrollments();
		
		// Get the current users
		List enrollments = getSectionManager().getSectionEnrollments(currentSection.getUuid());
		Collections.sort(enrollments, EditManagersBean.sortNameComparator);

		populateSelectedUsers(enrollments);
		
		// Build the list of items for the left-side box
		List available;
		if(StringUtils.trimToNull(availableSectionUuid) == null) {
			available = getSectionManager().getUnsectionedEnrollments(currentSection.getCourse().getUuid(), currentSection.getCategory());
		} else {
			available = getSectionManager().getSectionEnrollments(availableSectionUuid);
		}
		Collections.sort(available, EditManagersBean.sortNameComparator);

		availableUsers = new ArrayList();
		for(Iterator iter = available.iterator(); iter.hasNext();) {
			User student = ((ParticipationRecord)iter.next()).getUser();
			availableUsers.add(new SelectItem(student.getUserUid(), student.getSortName()));
		}
		
		// Build the list of available sections
		List sectionsInCategory = getSectionManager().getSectionsInCategory(getSiteContext(), currentSection.getCategory());
		Collections.sort(sectionsInCategory);
		availableSectionItems = new ArrayList();
		availableSectionItems.add(new SelectItem("", JsfUtil.getLocalizedMessage("edit_student_unassigned")));
		for(Iterator iter = sectionsInCategory.iterator(); iter.hasNext();) {
			CourseSection section = (CourseSection)iter.next();
			// Don't include the current section
			if(section.getUuid().equals(currentSection.getUuid())) {
				continue;
			}
			if(section.getUuid().equals(availableSectionUuid)) {
				availableSectionTitle = section.getTitle();
				availableSectionMax = section.getMaxEnrollments();
			}
			availableSectionItems.add(new SelectItem(section.getUuid(), section.getTitle()));
		}
	}

	public void processChangeSection(ValueChangeEvent event) {
		// Reset all lists
		init();
	}
	
	public String update() {
		Set selectedUserUuids = getHighlightedUsers("memberForm:selectedUsers");
		try {
			getSectionManager().setSectionMemberships(selectedUserUuids, Role.STUDENT, sectionUuid);
		} catch (RoleConfigurationException rce) {
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("role_config_error"));
			return null;
		}
		
		// If the "available" box is a section, update that section's members as well
		Set availableUserUuids = getHighlightedUsers("memberForm:availableUsers");
		if(StringUtils.trimToNull(availableSectionUuid) != null) {
			availableUserUuids = getHighlightedUsers("memberForm:availableUsers");
			try {
				getSectionManager().setSectionMemberships(availableUserUuids, Role.STUDENT, availableSectionUuid);
			} catch (RoleConfigurationException rce) {
				JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("role_config_error"));
				return null;
			}
		}
		StringBuffer titles = new StringBuffer();
		titles.append(sectionTitle);
		if(StringUtils.trimToNull(availableSectionUuid) != null) {
			titles.append(" ");
			titles.append(JsfUtil.getLocalizedMessage("and"));
			titles.append(" ");
			titles.append(availableSectionTitle);
		}
		
		// Add the success message first, before any caveats (see below)
		JsfUtil.addRedirectSafeInfoMessage(JsfUtil.getLocalizedMessage(
				"edit_student_successful", new String[] {titles.toString()}));

		// If the selected section is now overenrolled, let the user know
		if(sectionMax != null && selectedUserUuids.size() > sectionMax.intValue()) {
			JsfUtil.addRedirectSafeWarnMessage(JsfUtil.getLocalizedMessage(
					"edit_student_over_max_warning", new String[] {
							sectionTitle,
							Integer.toString(selectedUserUuids.size()),
							Integer.toString(selectedUserUuids.size() - sectionMax.intValue()) }));
		}

		// If the available section is now overenrolled, let the user know
		if(availableSectionMax != null && availableUserUuids.size() > availableSectionMax.intValue()) {
			JsfUtil.addRedirectSafeWarnMessage(JsfUtil.getLocalizedMessage(
					"edit_student_over_max_warning", new String[] {
							availableSectionTitle,
							Integer.toString(availableUserUuids.size()),
							Integer.toString(availableUserUuids.size() - availableSectionMax.intValue()) }));
		}
		
		return "overview";
	}

	public String getAvailableSectionUuid() {
		return availableSectionUuid;
	}

	public void setAvailableSectionUuid(String availableSectionUuid) {
		this.availableSectionUuid = availableSectionUuid;
	}

	public List getAvailableSectionItems() {
		return availableSectionItems;
	}

	public Integer getSectionMax() {
		return sectionMax;
	}
}
