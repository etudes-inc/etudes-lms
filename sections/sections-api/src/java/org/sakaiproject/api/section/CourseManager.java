/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-api/src/java/org/sakaiproject/api/section/CourseManager.java $
 * $Id: CourseManager.java 3 2008-10-20 18:44:42Z ggolden $
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

import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.User;

/**
 * Provides methods for adding a top-level course object, to which CourseSections
 * can be associated, and for associating users with the course.
 * 
 * The membership-related methods are intended for use in testing the standalone
 * application only.  In sakai, we will can use this as an external interface
 * that allows for Course creation, but does not alter membership in the course.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public interface CourseManager {
	
	/**
	 * Creates a new Course object for this site.
	 * 
	 * @param siteContext The site context
	 * @param title The title of the course or site
	 * @param selfRegAllowed Whether to allow students to register for sections in this course.
	 * @param selfSwitchingAllowed Whether to allow students to switch sections in this course.
	 * @param externallyManaged Whether to flag this course as externally manager
	 * (read-only to the app).
	 * 
	 * @return The newly created Course object.
	 */
	public Course createCourse(String siteContext, String title,
			boolean selfRegAllowed, boolean selfSwitchingAllowed,
			boolean externallyManaged);
	
	/**
	 * Checks to see whether a course exists in this site.
	 * 
	 * @param siteContext The site context
	 * @return
	 */
	public boolean courseExists(String siteContext);
		
	/**
	 * Adds a student to a course.  Useful for dataloading in standalone mode.
	 * 
	 * @param user
	 * @param course
	 * @return
	 */
	public ParticipationRecord addEnrollment(User user, Course course);

	/**
	 * Adds a TA to a course.  Useful for dataloading in standalone mode.
	 * 
	 * @param user
	 * @param course
	 * @return
	 */
	public ParticipationRecord addTA(User user, Course course);

	/**
	 * Adds an instructor to a course.  Useful for dataloading in standalone mode.
	 * 
	 * @param user
	 * @param course
	 * @return
	 */
	public ParticipationRecord addInstructor(User user, Course course);
	
	/**
	 * Removes a user from the course.
	 * 
	 * @param userUid
	 * @param course
	 */
	public void removeCourseMembership(String userUid, Course course);
	
	/**
	 * Removes any section membership record from a site that belongs to a user
	 * who is no longer associated with the site.
	 * 
	 * @param siteContext The site context from which to remove the orphaned records
	 * @param userUids The current set of user ids that are a member of the site.
	 * Must not be null or empty.
	 */
	public void removeOrphans(String siteContext);
	
}
