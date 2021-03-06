/**********************************************************************************
*
* $Id: AuthzSectionsImpl.java 9204 2014-11-14 17:39:22Z mallikamt $
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

package org.sakaiproject.tool.gradebook.facades.sections;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.authz.cover.SecurityService;

import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Authz;


/**
 * An implementation of Gradebook-specific authorization needs based
 * on the shared Section Awareness API.
 */
public class AuthzSectionsImpl implements Authz {
    private static final Log log = LogFactory.getLog(AuthzSectionsImpl.class);

    private Authn authn;
    private SectionAwareness sectionAwareness;

	public boolean isUserAbleToGrade(String gradebookUid) {
		String userUid = authn.getUserUid();
		return (getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.INSTRUCTOR) || getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.TA));
	}

	public boolean isUserAbleToGradeAll(String gradebookUid) {
		String userUid = authn.getUserUid();
		return getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.INSTRUCTOR);
	}

	public boolean isUserAbleToGradeSection(String sectionUid) {
		String userUid = authn.getUserUid();
		return getSectionAwareness().isSectionMemberInRole(sectionUid, userUid, Role.TA);
	}

	public boolean isUserAbleToEditAssessments(String gradebookUid) {
		String userUid = authn.getUserUid();
		return getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.INSTRUCTOR);
	}

	public boolean isUserAbleToViewOwnGrades(String gradebookUid) {
		String userUid = authn.getUserUid();
		return getSectionAwareness().isSiteMemberInRole(gradebookUid, userUid, Role.STUDENT);
	}

	/**
	 * Note that this is not a particularly efficient implementation.
	 * If the method becomes more heavily used, it should be optimized.
	 */
	public boolean isUserAbleToGradeStudent(String gradebookUid, String studentUid) {
		if (isUserAbleToGradeAll(gradebookUid)) {
			return true;
		}

		List sections = getAvailableSections(gradebookUid);
		for (Iterator iter = sections.iterator(); iter.hasNext(); ) {
			CourseSection section = (CourseSection)iter.next();
			if (getSectionAwareness().isSectionMemberInRole(section.getUuid(), studentUid, Role.STUDENT)) {
				return true;
			}
		}

		return false;
	}

	/**
	 */
	public List getAvailableEnrollments(String gradebookUid) {
		List enrollments;
		if (isUserAbleToGradeAll(gradebookUid)) {
			enrollments = getSectionAwareness().getSiteMembersInRole(gradebookUid, Role.STUDENT);
		} else {
			// We use a map because we may have duplicate students among the section
			// participation records.
			Map enrollmentMap = new HashMap();
			List sections = getAvailableSections(gradebookUid);
			for (Iterator iter = sections.iterator(); iter.hasNext(); ) {
				CourseSection section = (CourseSection)iter.next();
				List sectionEnrollments = getSectionEnrollmentsTrusted(section.getUuid());
				for (Iterator eIter = sectionEnrollments.iterator(); eIter.hasNext(); ) {
					EnrollmentRecord enr = (EnrollmentRecord)eIter.next();
					enrollmentMap.put(enr.getUser().getUserUid(), enr);
				}
			}
			enrollments = new ArrayList(enrollmentMap.values());
		}
		enrollments = filterOutNonStudents(enrollments, gradebookUid);
		return enrollments;
	}

	public List getAvailableSections(String gradebookUid) {
		SectionAwareness sectionAwareness = getSectionAwareness();
		List availableSections = new ArrayList();

		// Get the list of sections. For now, just use whatever default
		// sorting we get from the Section Awareness component.
		List sections = sectionAwareness.getSections(gradebookUid);
		for (Iterator iter = sections.iterator(); iter.hasNext(); ) {
			CourseSection section = (CourseSection)iter.next();
			if (isUserAbleToGradeAll(gradebookUid) || isUserAbleToGradeSection(section.getUuid())) {
				availableSections.add(section);
			}
		}

		return availableSections;
	}

	private List getSectionEnrollmentsTrusted(String sectionUid) {
		return getSectionAwareness().getSectionMembersInRole(sectionUid, Role.STUDENT);
	}

	public List getSectionEnrollments(String gradebookUid, String sectionUid) {
		String userUid = authn.getUserUid();
		List enrollments;
		if (isUserAbleToGradeAll(gradebookUid) || isUserAbleToGradeSection(sectionUid)) {
			enrollments = getSectionEnrollmentsTrusted(sectionUid);
		} else {
			enrollments = new ArrayList();
			log.warn("getSectionEnrollments for sectionUid=" + sectionUid + " called by unauthorized userUid=" + userUid);
		}
		enrollments = filterOutNonStudents(enrollments, gradebookUid);
		return enrollments;
	}

	public List findMatchingEnrollments(String gradebookUid, String searchString, String optionalSectionUid) {
		List enrollments;
        List allEnrollmentsFilteredBySearch = getSectionAwareness().findSiteMembersInRole(gradebookUid, Role.STUDENT, searchString);

		if (allEnrollmentsFilteredBySearch.isEmpty() ||
			((optionalSectionUid == null) && isUserAbleToGradeAll(gradebookUid))) {
			enrollments = allEnrollmentsFilteredBySearch;
		} else {
			if (optionalSectionUid == null) {
				enrollments = getAvailableEnrollments(gradebookUid);
			} else {
				// The user has selected a particular section.
				enrollments = getSectionEnrollments(gradebookUid, optionalSectionUid);
			}
			Set availableStudentUids = new HashSet();
			for(Iterator iter = enrollments.iterator(); iter.hasNext();) {
				EnrollmentRecord enr = (EnrollmentRecord)iter.next();
				availableStudentUids.add(enr.getUser().getUserUid());
			}

			enrollments = new ArrayList();
			for (Iterator iter = allEnrollmentsFilteredBySearch.iterator(); iter.hasNext(); ) {
				EnrollmentRecord enr = (EnrollmentRecord)iter.next();
				if (availableStudentUids.contains(enr.getUser().getUserUid())) {
					enrollments.add(enr);
				}
			}
		}
		enrollments = filterOutNonStudents(enrollments, gradebookUid);
		return enrollments;
	}
	
	/**
	 * This method filters out guests and observers
	 * returns only students
	 */
	public List filterOutNonStudents(List enrollments, String gradebookUid)
	{
		List onlyStudents = new ArrayList();
		if (enrollments == null || enrollments.size() == 0) return enrollments;
		for (Iterator iter = enrollments.iterator(); iter.hasNext(); ) {
        	EnrollmentRecord enr = (EnrollmentRecord)iter.next();
        	boolean ok = SecurityService.unlock(enr.getUser().getUserUid(), STUDENT_VIEW_GRADE, SiteService.siteReference(gradebookUid));
			if (ok) onlyStudents.add(enr);
		}
		return onlyStudents;
	}

	public Authn getAuthn() {
		return authn;
	}
	public void setAuthn(Authn authn) {
		this.authn = authn;
	}
	public SectionAwareness getSectionAwareness() {
		return sectionAwareness;
	}
	public void setSectionAwareness(SectionAwareness sectionAwareness) {
		this.sectionAwareness = sectionAwareness;
	}

}
