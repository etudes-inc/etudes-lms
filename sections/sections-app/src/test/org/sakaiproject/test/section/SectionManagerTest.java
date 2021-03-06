/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-app/src/test/org/sakaiproject/test/section/SectionManagerTest.java $
 * $Id: SectionManagerTest.java 3 2008-10-20 18:44:42Z ggolden $
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
package org.sakaiproject.test.section;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.CourseManager;
import org.sakaiproject.api.section.SectionManager;
import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.coursemanagement.ParticipationRecord;
import org.sakaiproject.api.section.coursemanagement.SectionEnrollments;
import org.sakaiproject.api.section.coursemanagement.User;
import org.sakaiproject.api.section.exception.MembershipException;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.api.section.facade.manager.Context;
import org.sakaiproject.component.section.facade.impl.standalone.AuthnTestImpl;
import org.sakaiproject.component.section.support.UserManager;

/**
 * Each test method is isolated in its own transaction, which is rolled back when
 * the method exits.  Since we can not assume that data will exist, we need to use
 * the SectionManager api to insert data before retrieving it with SectionAwareness.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class SectionManagerTest extends SectionsTestBase{
	private static final Log log = LogFactory.getLog(SectionManagerTest.class);
	
	private AuthnTestImpl authn;
	private Context context;
	private SectionManager secMgr;
	private CourseManager courseMgr;
	private UserManager userMgr;

    protected void onSetUpInTransaction() throws Exception {
    	authn = (AuthnTestImpl)applicationContext.getBean("org.sakaiproject.api.section.facade.manager.Authn");
    	context = (Context)applicationContext.getBean("org.sakaiproject.api.section.facade.manager.Context");
        secMgr = (SectionManager)applicationContext.getBean("org.sakaiproject.api.section.SectionManager");
        courseMgr = (CourseManager)applicationContext.getBean("org.sakaiproject.api.section.CourseManager");
        userMgr = (UserManager)applicationContext.getBean("org.sakaiproject.component.section.support.UserManager");
    }

    public void testChangeMembershipOnDeletedSection() throws Exception {
    	// These methods should gracefully handle operations on missing (possibly deleted) sections
    	
    	// Test joining a non-existent section
    	Assert.assertNull(secMgr.joinSection("foo"));
    	
    	// Test switching into a non-existent section
    	secMgr.switchSection("foo");

    	// Test setSectionMemberships on a non-existent section\
    	Set userSet = new HashSet();
    	userSet.add("user1");
    	secMgr.setSectionMemberships(userSet, Role.STUDENT, "foo");
    }
    
    public void testSectionMembership() throws Exception {

    	// FIXME This test has become totally unruly.  Split it up, even though it will lead to a lot of duplication
    	
    	String siteContext = context.getContext(null);
    	List categories = secMgr.getSectionCategories(siteContext);
    	
    	// Add a course and a section to work from
    	Course newCourse = courseMgr.createCourse(siteContext, "A course", false, false, false);
    	Course course = secMgr.getCourse(siteContext);
    	
    	// Assert that the correct course was retrieved
    	Assert.assertTrue(newCourse.equals(course));
    	
    	String firstCategory = (String)categories.get(0);
    	String secondCategory = (String)categories.get(1);
    	String thirdCategory = (String)categories.get(2);
    	CourseSection sec1 = secMgr.addSection(course.getUuid(), "A section", firstCategory, new Integer(10), null, null, null, false,  false, false,  false, false, false, false);
    	CourseSection sec2 = secMgr.addSection(course.getUuid(), "Another section", firstCategory, new Integer(10), null, null, null, false,  false, false,  false, false, false, false);
    	CourseSection sec3 = secMgr.addSection(course.getUuid(), "A different kind of section", secondCategory, new Integer(10), null, null, null, false,  false, false,  false, false, false, false);
    	CourseSection sec4 = secMgr.addSection(course.getUuid(), "Barely even a section", thirdCategory, new Integer(10), null, null, null, false, false, false,  false, false, false, false);
    	
		// Load students
		User student1 = userMgr.createUser("student1", "Joe Student", "Student, Joe", "jstudent");
		User student2 = userMgr.createUser("student2", "Jane Undergrad", "Undergrad, Jane", "jundergrad");

		// Load TAs
		User ta1 = userMgr.createUser("ta1", "Mike Grad", "Grad, Mike", "mgrad");
		User ta2 = userMgr.createUser("ta2", "Sara Postdoc", "Postdoc, Sara", "spostdoc");
		
		// Load instructors
		User instructor1 = userMgr.createUser("instructor1", "Bill Economist", "Economist, Bill", "beconomist");
		User instructor2 = userMgr.createUser("instructor2", "Amber Philosopher", "Philosopher, Amber", "aphilosopher");

		// Load other people
		User otherPerson = userMgr.createUser("other1", "Other Person", "Person, Other", "operson");

		// Load enrollments into the course
		ParticipationRecord siteEnrollment1 = courseMgr.addEnrollment(student1, course);
		ParticipationRecord siteEnrollment2 = courseMgr.addEnrollment(student2, course);
		ParticipationRecord siteEnrollment3 = courseMgr.addEnrollment(otherPerson, course);
		
		// Load enrollments into sections
		ParticipationRecord sectionEnrollment1 = secMgr.addSectionMembership("student1", Role.STUDENT, sec1.getUuid());
		ParticipationRecord sectionEnrollment2 = secMgr.addSectionMembership("student2", Role.STUDENT, sec1.getUuid());
		
		// Load TAs into the course
		ParticipationRecord siteTaRecord1 = courseMgr.addTA(ta1, course);
		ParticipationRecord siteTaRecord2 = courseMgr.addTA(ta2, course);
		
		// Load TAs into the sections
		ParticipationRecord sectionTaRecord1 = secMgr.addSectionMembership("ta1", Role.TA, sec1.getUuid());
		ParticipationRecord sectionTaRecord2 = secMgr.addSectionMembership("ta2", Role.TA, sec1.getUuid());
		
		// Load instructors into the courses
		ParticipationRecord siteInstructorRecord1 = courseMgr.addInstructor(instructor1, course);
				
		// Assert that an student who joins a section is returned as a member of that section
		authn.setUserUuid("other1");
		EnrollmentRecord sectionEnrollment3 = secMgr.joinSection(sec1.getUuid());

		List enrollments = secMgr.getSectionEnrollments(sec1.getUuid());
		Assert.assertTrue(enrollments.contains(sectionEnrollment3));
		
		// Assert that an enrolled student can not add themselves again
		authn.setUserUuid("student1");
		boolean joinSectionErrorThrown = false;
		try {
			secMgr.joinSection(sec1.getUuid());
		} catch (MembershipException me) {
			joinSectionErrorThrown = true;
		}
		Assert.assertTrue(joinSectionErrorThrown);
		
		// Assert that a student can switch between sections only within the same category
		secMgr.switchSection(sec2.getUuid());
		boolean switchingErrorThrown = false;
		try {
			secMgr.switchSection(sec3.getUuid());
		} catch(MembershipException me) {
			switchingErrorThrown = true;
		}
		Assert.assertTrue(switchingErrorThrown);
		
		// Add otherPerson to the section in the third category.  This is the only enrollment in this section or category.
		secMgr.addSectionMembership(otherPerson.getUserUid(), Role.STUDENT, sec4.getUuid());
		
		// Assert that the third category's unsectioned students returns the two students
		List unsectionedEnrollments = secMgr.getUnsectionedEnrollments(course.getUuid(), thirdCategory);
		List unsectionedStudents = new ArrayList();
		for(Iterator iter = unsectionedEnrollments.iterator(); iter.hasNext();) {
			unsectionedStudents.add(((ParticipationRecord)iter.next()).getUser());
		}
		
		Assert.assertTrue(unsectionedStudents.contains(student1));
		Assert.assertTrue(unsectionedStudents.contains(student2));
		Assert.assertTrue(! unsectionedStudents.contains(otherPerson));
		
		// Assert that an instructor can not be added to a section
		boolean instructorErrorThrown = false;
		try {
			secMgr.addSectionMembership(instructor1.getUserUid(), Role.INSTRUCTOR, sec1.getUuid());
		} catch (MembershipException me) {
			instructorErrorThrown = true;
		}
		Assert.assertTrue(instructorErrorThrown);

		// Assert that setting the entire membership of a section is successful
		Set set = new HashSet();
		set.add(student1.getUserUid());
		set.add(student2.getUserUid());
		secMgr.setSectionMemberships(set, Role.STUDENT, sec4.getUuid());
		List sectionMemberships = secMgr.getSectionEnrollments(sec4.getUuid());
		Set sectionMembers = new HashSet();
		for(Iterator iter = sectionMemberships.iterator(); iter.hasNext();) {
			sectionMembers.add(((ParticipationRecord)iter.next()).getUser());
		}
		Assert.assertTrue(sectionMembers.contains(student1));
		Assert.assertTrue(sectionMembers.contains(student2));
		// otherPerson was originally in the section, but wasn't included in the set operation
		Assert.assertTrue( ! sectionMembers.contains(otherPerson));
		
		// Drop a student from a section and ensure the enrollments reflect the drop
		secMgr.dropSectionMembership(student1.getUserUid(), sec2.getUuid());
		List sec2Members = secMgr.getSectionEnrollments(sec2.getUuid());
		Assert.assertTrue( ! sec2Members.contains(sectionEnrollment1));

		// Check whether the total enrollments in the course and in the sections is accurate
		Assert.assertTrue(secMgr.getTotalEnrollments(course.getUuid()) == 3);
		Assert.assertTrue(secMgr.getTotalEnrollments(sec1.getUuid()) == 2);
		
		// Ensure that a section can be updated
		secMgr.updateSection(sec1.getUuid(), "New title", new Integer(10), null, null, null, false, false, false, false, false, false, false);
		CourseSection updatedSec = secMgr.getSection(sec1.getUuid());
		Assert.assertTrue(updatedSec.getTitle().equals("New title"));
		sec1 = updatedSec;
		
		// Ensure that disbanding a section actually removes it from the course
		secMgr.disbandSection(sec4.getUuid());
		Assert.assertTrue( ! secMgr.getSections(siteContext).contains(sec4));

		// Assert that the correct enrollment records are returned for a student in a course
		ParticipationRecord enrollment1 = secMgr.addSectionMembership(student1.getUserUid(), Role.STUDENT, sec1.getUuid());
		ParticipationRecord enrollment2 = secMgr.addSectionMembership(student1.getUserUid(), Role.STUDENT, sec3.getUuid());
		Set myEnrollments = secMgr.getSectionEnrollments(student1.getUserUid(), course.getUuid());
		
		Assert.assertTrue(myEnrollments.contains(enrollment1));
		Assert.assertTrue(myEnrollments.contains(enrollment2));
		Assert.assertTrue(myEnrollments.size() == 2);

		// Assert that a valid SectionEnrollments object (just a convenient data structure) can be obtained
		Set studentUids = new HashSet();
		studentUids.add("student1");
		studentUids.add("student2");
		studentUids.add("other1");
		SectionEnrollments secEnrollments = secMgr.getSectionEnrollmentsForStudents(siteContext, studentUids);

		// Student 1 is enrolled in section 1 and 3
		Assert.assertTrue(secEnrollments.getSection("student1", firstCategory).equals(sec1));
		Assert.assertTrue(secEnrollments.getSection("student1", secondCategory).equals(sec3));
		
		// Student 2 is enrolled in section 1
		Assert.assertTrue(secEnrollments.getSection("student2", firstCategory).equals(sec1));
		Assert.assertTrue(secEnrollments.getSection("student2", secondCategory) == null);

		// Other person is enrolled in section 1
		Assert.assertTrue(secEnrollments.getSection("other1", firstCategory).equals(sec1));
		Assert.assertTrue(secEnrollments.getSection("other1", secondCategory) == null);
		
		// Remove an enrollment from a category
		secMgr.dropEnrollmentFromCategory("student1", siteContext, firstCategory);
		
		// Assert that the student is no longer in sec1
		Assert.assertTrue( ! secMgr.getSectionEnrollments("student1", course.getUuid()).contains(sec1));

		// Remove the enrollment from the same category, which should do nothing,
		// just to ensure that no error is thrown
		secMgr.dropEnrollmentFromCategory("student1", siteContext, firstCategory);
		
		// Change the self reg and self switching flags
		secMgr.setSelfRegistrationAllowed(course.getUuid(), true);
		Assert.assertTrue(secMgr.isSelfRegistrationAllowed(course.getUuid()));

		secMgr.setSelfRegistrationAllowed(course.getUuid(), false);
		Assert.assertTrue( ! secMgr.isSelfRegistrationAllowed(course.getUuid()));

		secMgr.setSelfSwitchingAllowed(course.getUuid(), true);
		Assert.assertTrue(secMgr.isSelfSwitchingAllowed(course.getUuid()));

		secMgr.setSelfSwitchingAllowed(course.getUuid(), false);
		Assert.assertTrue( ! secMgr.isSelfSwitchingAllowed(course.getUuid()));

    }
}
