/**********************************************************************************
*
* $Id: GradeManagerTest.java 3 2008-10-20 18:44:42Z ggolden $
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
package org.sakaiproject.tool.gradebook.test;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.GradeRecordSet;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingEvents;

/**
 * Tests the grade manager.
 *
 */
public class GradeManagerTest extends GradebookTestBase {
	private static Log log = LogFactory.getLog(GradeManagerTest.class);

    protected Gradebook gradebook;

    protected void onSetUpInTransaction() throws Exception {
        super.onSetUpInTransaction();

        String gradebookName = this.getClass().getName();
        gradebookService.addGradebook(gradebookName, gradebookName);

        // Set up a holder for enrollments, teaching assignments, and sections.
        integrationSupport.createCourse(gradebookName, gradebookName, false, false, false);

        // Grab the gradebook for use in the tests
        gradebook = gradebookManager.getGradebook(gradebookName);
    }

    public void testGradeManager() throws Exception {
		List studentUidsList = Arrays.asList(new String[] {
			"testStudentUserUid1",
			"testStudentUserUid2",
			"testStudentUserUid3",
		});
		addUsersEnrollments(gradebook, studentUidsList);

        gradebookManager.createAssignment(gradebook.getId(), "Assignment #1", new Double(20), new Date(), Boolean.FALSE,Boolean.FALSE);
        Assignment persistentAssignment = (Assignment)gradebookManager.
            getAssignmentsWithStats(gradebook.getId(), Assignment.DEFAULT_SORT, true).get(0);

        GradeRecordSet gradeRecordSet = new GradeRecordSet(persistentAssignment);
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(persistentAssignment, (String)studentUidsList.get(0), new Double(18)));
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(persistentAssignment, (String)studentUidsList.get(1), new Double(19)));
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(persistentAssignment, (String)studentUidsList.get(2), new Double(20)));

        gradebookManager.updateAssignmentGradeRecords(gradeRecordSet);

        // Fetch the grade records
        List records = gradebookManager.getPointsEarnedSortedGradeRecords(persistentAssignment, studentUidsList);

        // Ensure that each of the students in the map have a grade record, and
        // that their grade is correct
        Set students = gradeRecordSet.getAllStudentIds();
        for(Iterator iter = records.iterator(); iter.hasNext();) {
            AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
            double tmpScore = gradeRecordSet.getGradeRecord(agr.getStudentId()).getPointsEarned().doubleValue();
            double persistentScore = agr.getPointsEarned().doubleValue();
            Assert.assertTrue(tmpScore == persistentScore);
        }

        // Add overrides to the course grades
        CourseGrade courseGrade = gradebookManager.getCourseGradeWithStats(gradebook.getId());
        records = gradebookManager.getPointsEarnedSortedGradeRecords(courseGrade, students);

        gradeRecordSet = new GradeRecordSet(courseGrade);
        for(Iterator iter = records.iterator(); iter.hasNext();) {
            CourseGradeRecord record = (CourseGradeRecord)iter.next();
            if(record.getStudentId().equals(studentUidsList.get(0))) {
                record.setEnteredGrade("C-");
            } else if(record.getStudentId().equals(studentUidsList.get(1))) {
                record.setEnteredGrade("D+");
            } else if(record.getStudentId().equals(studentUidsList.get(2))) {
                record.setEnteredGrade("F");
            }
            gradeRecordSet.addGradeRecord(record);
        }

        gradebookManager.updateCourseGradeRecords(gradeRecordSet);

        GradeMapping gradeMapping = gradebook.getSelectedGradeMapping();

        // Ensure that the sort grades have been updated to reflect the overridden grades
        List courseGradeRecords = gradebookManager.getPointsEarnedSortedGradeRecords(courseGrade, studentUidsList);
        for(Iterator iter = courseGradeRecords.iterator(); iter.hasNext();) {
            CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
            Double sortGrade = cgr.getSortGrade();
            String studentId = cgr.getStudentId();
            String tmpGrade = ((CourseGradeRecord)gradeRecordSet.getGradeRecord(studentId)).getEnteredGrade();
            Assert.assertTrue(sortGrade.equals(gradeMapping.getValue(tmpGrade)));
            Assert.assertTrue(gradeMapping.getGrade(cgr.getSortGrade()).equals(tmpGrade));
        }

        // Remove the overrides
        gradeRecordSet = new GradeRecordSet(courseGrade);
        for(Iterator iter = records.iterator(); iter.hasNext();) {
            CourseGradeRecord record = (CourseGradeRecord)iter.next();
            record.setEnteredGrade(null);
            gradeRecordSet.addGradeRecord(record);
        }

        gradebookManager.updateCourseGradeRecords(gradeRecordSet);

        // Ensure that the sort grades have been updated
        courseGradeRecords = gradebookManager.getPointsEarnedSortedGradeRecords(courseGrade, studentUidsList);
        double totalPoints = gradebookManager.getTotalPoints(gradebook.getId());

        for(Iterator iter = courseGradeRecords.iterator(); iter.hasNext();) {
            CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
            Double percent = cgr.calculatePercent(totalPoints);
            Double sortGrade = cgr.getSortGrade();
            Assert.assertTrue(sortGrade.doubleValue() - percent.doubleValue() < .001);
        }

        List allGradeRecords = gradebookManager.getPointsEarnedSortedAllGradeRecords(gradebook.getId(), studentUidsList);
        // There should be six grade records for these students
        Assert.assertTrue(allGradeRecords.size() == 6);

        // Create a new, smaller set of enrollments and ensure the smaller set of grade records are selected correctly
        Set filteredStudentUids = new HashSet();
        filteredStudentUids.add(studentUidsList.get(0));
        filteredStudentUids.add(studentUidsList.get(1));
        List filteredGradeRecords = gradebookManager.getPointsEarnedSortedAllGradeRecords(gradebook.getId(), filteredStudentUids);

        // There should be four grade records for these students
        Assert.assertTrue(filteredGradeRecords.size() == 4);

        // There should be two grade records for these students and for this assignment
        filteredGradeRecords = gradebookManager.getPointsEarnedSortedGradeRecords(persistentAssignment, filteredStudentUids);
        Assert.assertTrue(filteredGradeRecords.size() == 2);
    }

    public void testNewExcessiveScores() throws Exception {
		List studentUidsList = Arrays.asList(new String[] {
			"normalStudent",
			"goodStudent",
			"excessiveStudent",
		});
		addUsersEnrollments(gradebook, studentUidsList);
        Set studentIds = new HashSet(studentUidsList);

        gradebookManager.createAssignment(gradebook.getId(), "Excessive Test", new Double(10), new Date(), Boolean.FALSE,Boolean.FALSE);
        Assignment asn = (Assignment)gradebookManager.getAssignmentsWithStats(gradebook.getId(), Assignment.DEFAULT_SORT, true).get(0);

        // Create a grade record set
        GradeRecordSet gradeRecordSet = new GradeRecordSet(asn);
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(asn, "normalStudent", new Double(9)));
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(asn, "goodStudent", new Double(10)));
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(asn, "excessiveStudent", new Double(11)));

        Set excessives = gradebookManager.updateAssignmentGradeRecords(gradeRecordSet);
        Assert.assertTrue(excessives.size() == 1);
        Assert.assertTrue(excessives.contains("excessiveStudent"));

        List persistentGradeRecords = gradebookManager.getPointsEarnedSortedGradeRecords(asn, studentIds);
        gradeRecordSet = new GradeRecordSet(asn);

        for(Iterator iter = persistentGradeRecords.iterator(); iter.hasNext();) {
            AssignmentGradeRecord gradeRecord = (AssignmentGradeRecord)iter.next();
            if(gradeRecord.getStudentId().equals("goodStudent")) {
                gradeRecord.setPointsEarned(new Double(12));
            }
            // Always add the grade record to ensure that records that have not changed are not updated in the db
            gradeRecordSet.addGradeRecord(gradeRecord);
        }

        // Only updates should be reported.
        excessives = gradebookManager.updateAssignmentGradeRecords(gradeRecordSet);

        Assert.assertTrue(excessives.contains("goodStudent"));
        Assert.assertEquals(1, excessives.size());
    }

    public void testAssignmentScoresEntered() throws Exception {
        Set students = new HashSet();
        students.add("entered1");

        Long asgId = gradebookManager.createAssignment(gradebook.getId(), "Scores Entered Test", new Double(10), new Date(), Boolean.FALSE,Boolean.FALSE
        );
        Assignment asn = (Assignment)gradebookManager.getAssignmentsWithStats(gradebook.getId(), Assignment.DEFAULT_SORT, true).get(0);

        Assert.assertTrue(!gradebookManager.isEnteredAssignmentScores(asgId));

        GradeRecordSet gradeRecordSet = new GradeRecordSet(asn);
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(asn, "entered1", new Double(9)));

        gradebookManager.updateAssignmentGradeRecords(gradeRecordSet);
        Assert.assertTrue(gradebookManager.isEnteredAssignmentScores(asgId));

        List persistentGradeRecords = gradebookManager.getPointsEarnedSortedGradeRecords(asn, students);

        gradeRecordSet = new GradeRecordSet(asn);
        AssignmentGradeRecord gradeRecord = (AssignmentGradeRecord)persistentGradeRecords.get(0);
        gradeRecord.setPointsEarned(null);
        gradeRecordSet.addGradeRecord(gradeRecord);

        gradebookManager.updateAssignmentGradeRecords(gradeRecordSet);
        Assert.assertTrue(!gradebookManager.isEnteredAssignmentScores(asgId));
    }

    public void testGradeEvents() throws Exception {
        String studentId = "student1";
		List studentUidsList = Arrays.asList(new String[] {
			studentId,
		});
		List enrollments = addUsersEnrollments(gradebook, studentUidsList);
        gradebookManager.createAssignment(gradebook.getId(), "GradingEvent Test", new Double(10), new Date(), Boolean.FALSE,Boolean.FALSE);
        Assignment assignment = (Assignment)gradebookManager.getAssignments(gradebook.getId()).get(0);

        // Create a map of studentUserUids to grades
        GradeRecordSet gradeRecordSet = new GradeRecordSet(assignment);
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(assignment, studentId, new Double(9)));

        // Save the grades
        gradebookManager.updateAssignmentGradeRecords(gradeRecordSet);

        // Update the grades (make sure we grab them from the db, first)
        Set students = new HashSet();
        students.add(studentId);
        List persistentGradeRecords = gradebookManager.getPointsEarnedSortedGradeRecords(assignment, students);

        gradeRecordSet = new GradeRecordSet(assignment);
        AssignmentGradeRecord gradeRecord = (AssignmentGradeRecord)persistentGradeRecords.get(0);
        gradeRecord.setPointsEarned(new Double(10));
        gradeRecordSet.addGradeRecord(gradeRecord);

        gradebookManager.updateAssignmentGradeRecords(gradeRecordSet);

        // Ensure that there are two grading events for this student
        GradingEvents events = gradebookManager.getGradingEvents(assignment, enrollments);
        Assert.assertEquals(events.getEvents(studentId).size(), 2);
    }

    public void testDroppedStudents() throws Exception {
        Gradebook gradebook = gradebookManager.getGradebook(this.getClass().getName());
        Long asgId = gradebookManager.createAssignment(gradebook.getId(), "Dropped Students Test", new Double(10), new Date(), Boolean.FALSE,Boolean.FALSE);
        Assignment asn = (Assignment)gradebookManager.getGradableObject(asgId);

        // We need to operate on whatever grade records already exist in the db
		List studentUidsList = Arrays.asList(new String[] {
			"realStudent1",
			"realStudent2",
		});
		addUsersEnrollments(gradebook, studentUidsList);

        // Create a grade record set
        GradeRecordSet gradeRecordSet = new GradeRecordSet(asn);
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(asn, "realStudent1", new Double(10)));
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(asn, "realStudent2", new Double(10)));
        gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(asn, "droppedStudent", new Double(1)));

        gradebookManager.updateAssignmentGradeRecords(gradeRecordSet);

        asn = gradebookManager.getAssignmentWithStats(asgId);

        // Make sure the dropped student wasn't included in the average.
        Assert.assertTrue(asn.getMean().doubleValue() == 100.0);

		// Now add the dropped student.
		studentUidsList = Arrays.asList(new String[] {
			"droppedStudent",
		});
		addUsersEnrollments(gradebook, studentUidsList);

        asn = gradebookManager.getAssignmentWithStats(asgId);
        Assert.assertTrue(asn.getMean().doubleValue() < 100.0);

		// Make sure that dropped students can't prevent changing final grade types.
        CourseGrade courseGrade = gradebookManager.getCourseGradeWithStats(gradebook.getId());
        gradeRecordSet = new GradeRecordSet(courseGrade);
        CourseGradeRecord courseGradeRecord = new CourseGradeRecord(courseGrade, "noSuchStudent", null);
        courseGradeRecord.setEnteredGrade("C-");
        gradeRecordSet.addGradeRecord(courseGradeRecord);
		gradebookManager.updateCourseGradeRecords(gradeRecordSet);
		Assert.assertFalse(gradebookManager.isExplicitlyEnteredCourseGradeRecords(gradebook.getId()));
    }

    public void testNotCountedAssignments() throws Exception {
		List studentUidsList = Arrays.asList(new String[] {
			"testStudentUserUid1",
		});
		addUsersEnrollments(gradebook, studentUidsList);

        Long id1 = gradebookManager.createAssignment(gradebook.getId(), "asn1", new Double(10), null, Boolean.FALSE,Boolean.TRUE);
        Long id2 = gradebookManager.createAssignment(gradebook.getId(), "asn2", new Double(20), new Date(10), Boolean.FALSE,Boolean.TRUE);

        Assignment asn1 = gradebookManager.getAssignmentWithStats(id1);
        Assignment asn2 = gradebookManager.getAssignmentWithStats(id2);

		// Add some scores to the assignments.
		GradeRecordSet gradeRecordSet = new GradeRecordSet(asn1);
		gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(asn1, (String)studentUidsList.get(0), new Double(8)));
		gradebookManager.updateAssignmentGradeRecords(gradeRecordSet);
		gradeRecordSet = new GradeRecordSet(asn2);
		gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(asn2, (String)studentUidsList.get(0), new Double(18)));
		gradebookManager.updateAssignmentGradeRecords(gradeRecordSet);

		// Make sure that the Course Grade total points includes both.
		CourseGrade courseGrade = gradebookManager.getCourseGradeWithStats(gradebook.getId());
		Assert.assertTrue(courseGrade.getPointsForDisplay().doubleValue() == 30.0);
        List courseGradeRecords = gradebookManager.getPointsEarnedSortedGradeRecords(courseGrade, studentUidsList);
        CourseGradeRecord cgr = (CourseGradeRecord)courseGradeRecords.get(0);
        Assert.assertTrue(cgr.getPointsEarned().doubleValue() == 26.0);

        // Don't count one assignment.
        asn2.setNotCounted(true);
        gradebookManager.updateAssignment(asn2);

		// Get what the student will see.
		CourseGradeRecord scgr = gradebookManager.getStudentCourseGradeRecord(gradebook, (String)studentUidsList.get(0));

        // Make sure it's not counted.
        courseGrade = gradebookManager.getCourseGradeWithStats(gradebook.getId());
		Assert.assertTrue(courseGrade.getPointsForDisplay().doubleValue() == 10.0);
		courseGradeRecords = gradebookManager.getPointsEarnedSortedGradeRecords(courseGrade, studentUidsList);
		cgr = (CourseGradeRecord)courseGradeRecords.get(0);
		Assert.assertTrue(cgr.getPointsEarned().doubleValue() == 8.0);

		// Make sure there's no disconnect between what the instructor
		// will see and what the student will see.
		Assert.assertTrue(cgr.getNonNullAutoCalculatedGrade().equals(scgr.getSortGrade()));

		// Test what is now (unfortunately) a different code path.
        List persistentGradeRecords = gradebookManager.getPointsEarnedSortedGradeRecords(asn1, studentUidsList);
        AssignmentGradeRecord gradeRecord = (AssignmentGradeRecord)persistentGradeRecords.get(0);
        gradeRecord.setPointsEarned(new Double(7));
		gradeRecordSet = new GradeRecordSet(asn1);
		gradeRecordSet.addGradeRecord(gradeRecord);
		gradebookManager.updateAssignmentGradeRecords(gradeRecordSet);
		courseGradeRecords = gradebookManager.getPointsEarnedSortedGradeRecords(courseGrade, studentUidsList);
		cgr = (CourseGradeRecord)courseGradeRecords.get(0);
		Assert.assertTrue(cgr.getPointsEarned().doubleValue() == 7.0);
    }

    /**
     * This tests an edge case responsible for an earlier bug: If an unscored assignment
     * changes whether it's counted towards the course grade, all course grades need
     * to be recalculated because the total points possible have changed.
     */
    public void testNotCountedUnscoredAssignments() throws Exception {
		List studentUidsList = Arrays.asList(new String[] {
			"testStudentUserUid1",
		});
		addUsersEnrollments(gradebook, studentUidsList);

        Long id1 = gradebookManager.createAssignment(gradebook.getId(), "asn1", new Double(10), null, Boolean.FALSE,Boolean.FALSE);
        Long id2 = gradebookManager.createAssignment(gradebook.getId(), "asn2", new Double(20), new Date(10), Boolean.FALSE,Boolean.FALSE);

        Assignment asn1 = gradebookManager.getAssignmentWithStats(id1);
        Assignment asn2 = gradebookManager.getAssignmentWithStats(id2);

		// Only score the first assignment.
		GradeRecordSet gradeRecordSet = new GradeRecordSet(asn1);
		gradeRecordSet.addGradeRecord(new AssignmentGradeRecord(asn1, (String)studentUidsList.get(0), new Double(8)));
		gradebookManager.updateAssignmentGradeRecords(gradeRecordSet);

        // Don't count the unscored assignment.
        asn2.setNotCounted(true);
        gradebookManager.updateAssignment(asn2);

		// Get what the student will see.
		CourseGradeRecord scgr = gradebookManager.getStudentCourseGradeRecord(gradebook, (String)studentUidsList.get(0));

        // Make sure it's not counted.
        CourseGrade courseGrade = gradebookManager.getCourseGradeWithStats(gradebook.getId());
		Assert.assertTrue(courseGrade.getPointsForDisplay().doubleValue() == 10.0);
		List courseGradeRecords = gradebookManager.getPointsEarnedSortedGradeRecords(courseGrade, studentUidsList);
		CourseGradeRecord cgr = (CourseGradeRecord)courseGradeRecords.get(0);
		Assert.assertTrue(cgr.getPointsEarned().doubleValue() == 8.0);

		// Make sure there's no disconnect between what the instructor
		// will see and what the student will see.
		Assert.assertTrue(cgr.getNonNullAutoCalculatedGrade().equals(scgr.getSortGrade()));
	}

}
