/**********************************************************************************
*
* $Id: TestGradebookTool.java 3 2008-10-20 18:44:42Z ggolden $
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;

/**
 * @author josh
 *
 */
public class TestGradebookTool {
    // UI State
    Gradebook selectedGradebook;

    // Services
    private GradebookManager gradebookManager;
	private SectionAwareness sectionAwareness;

    /**
     * @return A List of all assignments in the currently selected gradebook
     */
    public List getAssignments() {
        List gradableObjects = gradebookManager.getAssignmentsAndCourseGradeWithStats(selectedGradebook.getId(), Assignment.DEFAULT_SORT, true);
        return gradableObjects;
    }

    /**
     * @return A Set of all Users enrolled in the currently selected gradebook
     */
    public Set getStudents() {
        Set students = new HashSet();
        List enrollments = sectionAwareness.getSiteMembersInRole(selectedGradebook.getUid(), Role.STUDENT);
        for(Iterator enrIter = enrollments.iterator(); enrIter.hasNext();) {
            students.add(((EnrollmentRecord)enrIter.next()).getUser());
        }
        return students;
    }

    //// JSF Action Events ////
    public void selectGradebook(ActionEvent ae) {
        selectedGradebook = (Gradebook)FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("gb");
    }

    //// Tool state getters and setters

    /**
     * @return Returns the selectedGradebook.
     */
    public Gradebook getSelectedGradebook() {
        return selectedGradebook;
    }
    /**
     * @param selectedGradebook The selectedGradebook to set.
     */
    public void setSelectedGradebook(Gradebook selectedGradebook) {
        this.selectedGradebook = selectedGradebook;
    }

    //// Service Dependencies ////

	public SectionAwareness getSectionAwareness() {
		return sectionAwareness;
	}
	public void setSectionAwareness(SectionAwareness sectionAwareness) {
		this.sectionAwareness = sectionAwareness;
	}

	/**
	 * @return Returns the gradebookManager.
	 */
	public GradebookManager getGradebookManager() {
		return gradebookManager;
	}
	/**
	 * @param gradebookManager The gradebookManager to set.
	 */
	public void setGradebookManager(GradebookManager gradebookManager) {
		this.gradebookManager = gradebookManager;
	}
}



