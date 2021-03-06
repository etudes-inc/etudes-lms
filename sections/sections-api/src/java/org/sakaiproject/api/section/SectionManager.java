/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-api/src/java/org/sakaiproject/api/section/SectionManager.java $
 * $Id: SectionManager.java 3 2008-10-20 18:44:42Z ggolden $
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
package org.sakaiproject.api.section;

import java.sql.Time;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.SectionEnrollments;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.api.section.exception.MembershipException;
import org.sakaiproject.api.section.exception.RoleConfigurationException;
import org.sakaiproject.api.section.facade.Role;

/**
 * An internal service interface for the Section Manager Tool (AKA "Section Info")
 * to provide for the creation, modification, and removal of CourseSections, along
 * with the membership of of these sections.
 * 
 * This service is not to be used outside of the Section Manager Tool.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public interface SectionManager {

	/**
	 * Gets the course (whatever that means) associated with this site context.
	 * 
	 * @param siteContext The site context
	 * @return The course (whatever that means)
	 */
	public Course getCourse(String siteContext);
	
    /**
     * Gets the sections associated with this site context.
     * 
	 * @param siteContext The site context
	 * 
     * @return The List of
     * {@link org.sakaiproject.api.section.coursemanagement.CourseSection CourseSections}
     * associated with this site context.
     */
    public List getSections(String siteContext);
	
    /**
     * Lists the sections in this context that are a member of the given category.
     * 
	 * @param siteContext The site context
     * @param categoryId
     * 
     * @return A List of {@link org.sakaiproject.api.section.coursemanagement.CourseSection CourseSections}
     */
    public List getSectionsInCategory(String siteContext, String categoryId);

    /**
     * Gets a {@link org.sakaiproject.api.section.coursemanagement.CourseSection CourseSection}
     * by its uuid.
     * 
     * @param sectionUuid The uuid of a section
     * 
     * @return A section
     */
    public CourseSection getSection(String sectionUuid);

    /**
     * Gets a list of {@link org.sakaiproject.api.section.coursemanagement.ParticipationRecord
     * ParticipationRecord}s for all instructors in the current site.
     * 
     * @param siteContext The current site context
     * @return The instructors
     */
    public List getSiteInstructors(String siteContext);

    /**
     * Gets a list of {@link org.sakaiproject.api.section.coursemanagement.ParticipationRecord
     * ParticipationRecord}s for all TAs in the current site.
     * 
     * @param siteContext The current site context
     * @return The TAs
     */
    public List getSiteTeachingAssistants(String siteContext);

    /**
     * Gets a list of {@link org.sakaiproject.api.section.coursemanagement.ParticipationRecord
     * ParticipationRecord}s for all TAs in a section.
     * 
     * @param sectionUuid The section uuid
     * @return The TAs
     */
    public List getSectionTeachingAssistants(String sectionUuid);

    /**
     * Gets a list of {@link org.sakaiproject.api.section.coursemanagement.EnrollmentRecord
     * EnrollmentRecord}s belonging to the current site.
     * 
     * @param siteContext The current site context
     * @return The enrollments
     */
    public List getSiteEnrollments(String siteContext);

    /**
     * Gets a list of {@link org.sakaiproject.api.section.coursemanagement.EnrollmentRecord
     * EnrollmentRecord}s belonging to a section.
     * 
     * @param sectionUuid The section uuid
     * @return The enrollments
     */
    public List getSectionEnrollments(String sectionUuid);

    /**
     * Finds a list of {@link org.sakaiproject.api.section.coursemanagement.EnrollmentRecord
     * EnrollmentRecord}s belonging to the current site and whose sort name, display name,
     * or display id start with the given string pattern.
     * 
     * @param siteContext The current site context
     * @param pattern The pattern to match students names or ids
     * 
     * @return The enrollments
     */
    public List findSiteEnrollments(String siteContext, String pattern);

    /**
	 * Gets a SectionEnrollments data structure for the given students.
	 * 
	 * @param siteContext The site context
	 * @param studentUids The Set of userUids to include in the SectionEnrollments
	 * 
	 * @return
	 */
	public SectionEnrollments getSectionEnrollmentsForStudents(String siteContext, Set studentUids);
	
    /**
     * Adds the current user to a section as a student.  This is a convenience
     * method for addSectionMembership(currentUserId, Role.STUDENT, sectionId).
     * @param sectionUuid
     * @throws RoleConfigurationException If there is no valid student role, or
     * if there is more than one group-scoped role flagged as a student role.
     */
    public EnrollmentRecord joinSection(String sectionUuid) throws RoleConfigurationException;
        
    /**
     * Switches a student's currently assigned section.  If the student is enrolled
     * in another section of the same type, that enrollment will be dropped.
     * 
     * This is a convenience method to allow a drop/add (a switch) in a single transaction.
     * 
     * @param newSectionUuid The new section uuid to which the student should be assigned
     * @throws RoleConfigurationException If there is no valid student role, or
     * if there is more than one group-scoped role flagged as a student role.
     */
    public void switchSection(String newSectionUuid) throws RoleConfigurationException;
    
    /**
     * Returns the total number of students enrolled in a learning context.  Useful for
     * comparing to the max number of enrollments allowed in a section.
     * 
     * @param sectionUuid
     * @return
     */
    public int getTotalEnrollments(String learningContextUuid);
    
    /**
     * Adds a user to a section under the specified role.  If a student is added
     * to a section, s/he will be automatically removed from any other section
     * of the same category in this site.  So adding 'student1' to 'Lab1', for
     * example, will automatically remove 'student1' from 'Lab2'.  TAs may be
     * added to multiple sections in a site regardless of category.
     * 
     * @param userUid
     * @param role
     * @param sectionUuid
     * @throws MembershipException Only students and TAs can be members of a
     * section.  Instructor roles are assigned only at the course level.
     * @throws RoleConfigurationException Thrown when no sakai role can be
     * identified to represent the given role.
     */
    public ParticipationRecord addSectionMembership(String userUid, Role role, String sectionUuid)
        throws MembershipException, RoleConfigurationException;
    
    /**
     * Defines the complete set of users that make up the members of a section in
     * a given role.  This is useful when doing bulk modifications of section
     * membership.
     * 
     * @param userUids The set of userUids as strings
     * @param sectionId The sectionId
     * 
     * @throws RoleConfigurationException If there is no properly configured role
     * in the site matching the role specified.
     */
    public void setSectionMemberships(Set userUids, Role role, String sectionId)
    	throws RoleConfigurationException;
    
    /**
     * Removes a user from a section.
     * 
     * @param userUid
     * @param sectionUuid
     */
    public void dropSectionMembership(String userUid, String sectionUuid);
    
	/**
	 * Removes the user from any enrollment to a section of the given category.
	 * 
	 * @param studentUid
	 * @param siteContext
	 * @param category
	 */
    public void dropEnrollmentFromCategory(String studentUid, String siteContext, String category);

    /**
     * Adds a CourseSection to a parent CourseSection.  This assumes that meeting times
     * will not be handled by an external service.  The added functionality of
     * linking course sections to repeating events (meet every 2nd Tuesday of the
     * month at 3pm) is currently out of scope, so meetingTimes is represented
     * as a start time, end time, and seven booleans representing the days that
     * the section meets.
     * 
     * @param courseUuid
     * @param title
     * @param category
     * @param maxEnrollments
     * @param location
     * @param startTime
     * @param startTimeAm
     * @param endTime
     * @param endTimeAm
     * @param monday
     * @param tuesday
     * @param wednesday
     * @param thursday
     * @param friday
     * @param saturday
     * @param sunday
     * @return
     */
    public CourseSection addSection(String courseUuid, String title,
    		String category, Integer maxEnrollments, String location, 
    		Time startTime, Time endTime,
    		boolean monday, boolean tuesday, boolean wednesday, boolean thursday,
    		boolean friday, boolean saturday, boolean sunday);
	
    /**
     * Updates the persistent representation of the given CourseSection.  Once
     * a section is created, its category is immutable.
     * 
     * @param sectionUuid
     * @param title
     * @param maxEnrollments
     * @param location
     * @param startTime
     * @param startTimeAm
     * @param endTime
     * @param endTimeAm
     * @param monday
     * @param tuesday
     * @param wednesday
     * @param thursday
     * @param friday
     * @param saturday
     * @param sunday
     */
    public void updateSection(String sectionUuid, String title, Integer maxEnrollments,
    		String location, Time startTime, Time endTime,
    		boolean monday, boolean tuesday, boolean wednesday,
    		boolean thursday, boolean friday, boolean saturday, boolean sunday);
    
    /**
     * Disbands a course section.  This does not affect enrollment records for
     * the course.
     * 
     * @param sectionUuid
     */
    public void disbandSection(String sectionUuid);


    /**
     * Determines whether students can enroll themselves in a section.
     * 
     * @param courseUuid
     * @return
     */
    public boolean isSelfRegistrationAllowed(String courseUuid);
    
    /**
     * Sets the "self registration" status of a section.
     * 
     * @param courseUuid
     * @param allowed
     */
    public void setSelfRegistrationAllowed(String courseUuid, boolean allowed);
    
    /**
     * Determines whether students can switch sections once they are enrolled in
     * a section of a given category (for instance, swapping one lab for another).
     * 
     * @param courseUuid
     * @return
     */
    public boolean isSelfSwitchingAllowed(String courseUuid);
    
    /**
     * Sets the "student switching" status of a primary section.
     * 
     * @param courseId
     * @param allowed
     */
    public void setSelfSwitchingAllowed(String courseUuid, boolean allowed);
    
    /**
     * The Section Manager tool could use more specific queries on membership,
     * such as this:  getting all students in a primary section that are not
     * enrolled in any secondary sections of a given type.  For instance, 'Who
     * are the students who are not enrolled in any lab?'
     * 
     * @return A List of {@link
     * org.sakaiproject.api.section.coursemanagement.EnrollmentRecord
     * EnrollmentRecords} of students who are enrolled in the course but are
     * not enrolled in a section of the given section category.
     */
    public List getUnsectionedEnrollments(String courseUuid, String category);

    /**
     * Gets all of the section enrollments for a user in a course.  Useful for
     * listing all of the sections in which a student is enrolled.
     * 
     * @param userUid
     * @param courseUuid
     * @return A Set of EnrollmentRecords
     */
    public Set getSectionEnrollments(String userUid, String courseUuid);


    /**
     * Gets the localized name of a given category.
     * 
     * @param categoryId A string identifying the category
     * @param locale The locale of the client
     * 
     * @return An internationalized string to display for this category.
     * 
     */
    public String getCategoryName(String categoryId, Locale locale);
   
    /**
     * Gets the list of section categories.  In sakai 2.1, there will be only a
     * single set of categories.  They will not be configurable on a per-course
     * or per-context bases.
     * 
     * @param siteContext The site context (which is not used in the
     * current implementation)
     * 
     * @return A List of unique Strings that identify the available section
     * categories.  These should be internationalized for display using
     * {@link SectionAwareness#getCategoryName(String, Locale) getCategoryName}.
     */
    public List getSectionCategories(String siteContext);

	/**
	 * Gets a single User object for a student in a site.
	 * 
	 * @param siteContext Needed by the standalone implementation to find the user
	 * @param studentUid
	 * @return The User representing this student
	 */
    public User getSiteEnrollment(String siteContext, String studentUid);

}