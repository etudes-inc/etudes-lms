/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-app/src/java/org/sakaiproject/tool/section/jsf/backingbean/EditManagersBean.java $
 * $Id: EditManagersBean.java 3 2008-10-20 18:44:42Z ggolden $
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.api.section.exception.RoleConfigurationException;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.tool.section.decorator.CourseSectionDecorator;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Controls the edit managers page (where TAs are assigned to sections).
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class EditManagersBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(EditManagersBean.class);
	
	// For the right-side list box
	protected List selectedUsers;
	
	// For the left-side list box
	protected List availableUsers;
	
	protected String sectionUuid;
	protected String sectionTitle;
	protected String sectionDescription;
	
	protected boolean externallyManaged;

	/**
	 * Compares ParticipationRecords by users' sortNames.
	 */
	static Comparator sortNameComparator = new Comparator() {
		public int compare(Object o1, Object o2) {
			ParticipationRecord manager1 = (ParticipationRecord)o1;
			ParticipationRecord manager2 = (ParticipationRecord)o2;
			return manager1.getUser().getSortName().compareTo(manager2.getUser().getSortName());
		}
	};

	protected CourseSectionDecorator initializeFields() {
		// Determine whether this course is externally managed
		externallyManaged = getCourse().isExternallyManaged();
		
		// Get the section to edit
		String sectionUuidFromParam = (String)FacesContext.getCurrentInstance()
			.getExternalContext().getRequestParameterMap().get("sectionUuid");
		if(sectionUuidFromParam != null) {
			sectionUuid = sectionUuidFromParam;
		}
		CourseSectionDecorator currentSection = new CourseSectionDecorator(getSectionManager().getSection(sectionUuid));

		sectionTitle = currentSection.getTitle();
		
		// Generate the description
		String sectionMeetingTimes = currentSection.getMeetingTimes();
		if(StringUtils.trimToNull(sectionMeetingTimes) == null) {
			sectionDescription = sectionTitle;
		} else {
			sectionDescription = JsfUtil.getLocalizedMessage("section_description",
				new String[] {sectionTitle, sectionMeetingTimes});
		}
		
		return currentSection;
	}

	protected void populateSelectedUsers(List participationRecords) {
		// Build the list of items for the right-side list box
		selectedUsers = new ArrayList();
		for(Iterator iter =participationRecords.iterator(); iter.hasNext();) {
			ParticipationRecord record = (ParticipationRecord)iter.next();
			SelectItem item = new SelectItem(record.getUser().getUserUid(),
					record.getUser().getSortName());
			selectedUsers.add(item);
		}
	}

	public void init() {
		initializeFields();

		// Get the current users in the manager role for this section
		List selectedManagers = getSectionManager().getSectionTeachingAssistants(sectionUuid);
		Collections.sort(selectedManagers, sortNameComparator);
		
		populateSelectedUsers(selectedManagers);

		// Build the list of items for the left-side box.  Since the selected (right-side)
		// participation records are linked to a section, while the available records
		// are linked to the course, we can not use collection manipulation on these
		// objects.  So, generate a set of user uuids to filter out the currently
		// selected users from the available (left side) list.
		Set selectedUserUuids = new HashSet();
		for(Iterator iter = selectedManagers.iterator(); iter.hasNext();) {
			ParticipationRecord manager = (ParticipationRecord)iter.next();
			selectedUserUuids.add(manager.getUser().getUserUid());
		}

		List availableManagers = getSectionManager().getSiteTeachingAssistants(getSiteContext());
		Collections.sort(availableManagers, sortNameComparator);
		
		availableUsers = new ArrayList();
		for(Iterator iter = availableManagers.iterator(); iter.hasNext();) {
			User manager = ((ParticipationRecord)iter.next()).getUser();
			if( ! selectedUserUuids.contains(manager.getUserUid())) {
				availableUsers.add(new SelectItem(manager.getUserUid(), manager.getSortName()));
			}
		}
	}
	
	public String update() {
		Set userUids = getHighlightedUsers("memberForm:selectedUsers");
		try {
			getSectionManager().setSectionMemberships(userUids, Role.TA, sectionUuid);
		} catch (RoleConfigurationException rce) {
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage("role_config_error"));
			return null;
		}
		
		JsfUtil.addRedirectSafeInfoMessage(JsfUtil.getLocalizedMessage(
				"edit_manager_successful", new String[] {sectionTitle}));
		
		return "overview";
	}

	public String cancel() {
		return "overview";
	}

	protected Set getHighlightedUsers(String componentId) {
		Set userUids = new HashSet();
		
		String[] highlighted = (String[])FacesContext.getCurrentInstance()
		.getExternalContext().getRequestParameterValuesMap().get(componentId);

		if(highlighted != null) {
			for(int i=0; i < highlighted.length; i++) {
				userUids.add(highlighted[i]);
			}
		}
		return userUids;
	}
	
	public List getAvailableUsers() {
		return availableUsers;
	}

	public void setAvailableUsers(List availableUsers) {
		this.availableUsers = availableUsers;
	}

	public List getSelectedUsers() {
		return selectedUsers;
	}

	public void setSelectedUsers(List selectedUsers) {
		this.selectedUsers = selectedUsers;
	}

	public String getSectionUuid() {
		return sectionUuid;
	}

	public void setSectionUuid(String sectionUuid) {
		this.sectionUuid = sectionUuid;
	}

	public String getSectionTitle() {
		return sectionTitle;
	}

	public boolean isExternallyManaged() {
		return externallyManaged;
	}

	public String getSectionDescription() {
		return sectionDescription;
	}

	public String getAbbreviatedSectionTitle() {
		return StringUtils.abbreviate(sectionTitle, 15);
	}
}
