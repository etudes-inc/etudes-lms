/**********************************************************************************
*
* $Id: BaseHibernateManager.java 5793 2013-09-02 18:59:50Z mallikamt $
*
***********************************************************************************
*
* Copyright (c) 2005, 2006 The Regents of the University of California, The MIT Corporation
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
package org.sakaiproject.component.gradebook;

import java.util.*;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.facade.Role;
import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradebookProperty;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Provides methods which are shared between service business logic and application business
 * logic, but not exposed to external callers.
 */
public abstract class BaseHibernateManager extends HibernateDaoSupport {
    private static final Log log = LogFactory.getLog(BaseHibernateManager.class);

    // Oracle will throw a SQLException if we put more than this into a
    // "WHERE tbl.col IN (:paramList)" query.
    public static int MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST = 1000;

    protected SectionAwareness sectionAwareness;
    protected Authn authn;

    // Local cache of static-between-deployment properties.
    protected Map propertiesMap = new HashMap();
    
    private class EarnedPointsValue implements Comparable<EarnedPointsValue>
	{
		Double pointsVal;
		Double earnedPoints;
		Double pointsPossible;

		EarnedPointsValue(Double pointsVal, Double earnedPoints, Double pointsPossible)
		{
			this.pointsVal = pointsVal;
			this.earnedPoints = earnedPoints;
			this.pointsPossible = pointsPossible;
		}

		public int compareTo(EarnedPointsValue n)
		{
			if (this.pointsVal > n.pointsVal) return 1;
			if (this.pointsVal < n.pointsVal) return -1;
			if (this.pointsVal == n.pointsVal) return 0;
			return 0;
		}

		public Double getPointsVal()
		{
			return this.pointsVal;
		}

		public Double getEarnedPoints()
		{
			return this.earnedPoints;
		}
		
		public Double getPointsPossible()
		{
			return this.pointsPossible;
		}
	}

    public Gradebook getGradebook(String uid) throws GradebookNotFoundException {
    	List list = getHibernateTemplate().find("from Gradebook as gb where gb.uid=?",
    		uid);
		if (list.size() == 1) {
			return (Gradebook)list.get(0);
		} else {
            throw new GradebookNotFoundException("Could not find gradebook uid=" + uid);
        }
    }

    protected List getAssignments(Long gradebookId, Session session) throws HibernateException {
        List assignments = session.createQuery(
        	"from Assignment as asn where asn.gradebook.id=? and asn.removed=false").
        	setLong(0, gradebookId.longValue()).
        	list();
        return assignments;
    }

    /**
     * Get assignments(gradable objects) for which this student has a submission
     *
     * @param gradebookId Gradebook id
     * @param studentId Student id
     * @param session Session object
     * @return list of assignments
     * @throws HibernateException
     */
    protected List getStudentAssignments(Long gradebookId, String studentId, Session session) throws HibernateException {
    	List assignments = session.createQuery(
        "from Assignment as asn,AssignmentGradeRecord as agr where asn.gradebook.id=? and asn.notCounted=false and asn.removed=false and asn.name != \'Course Grade\' and asn=agr.gradableObject and agr.studentId=? and agr.pointsEarned is not null").
    			 setLong(0, gradebookId.longValue()).setString(1, studentId).list();
    	return assignments;
    }

    /**
     * Get total points for this list of assignments(includes open and already due)
     *
     * @param assignments list of assignments
     * @return Double object with total or 0.0
     */
    protected Double getTotalPointsForStudent(List assignments) {
    	double studentPoints = 0.0;
    	if (assignments == null || assignments.size() == 0) return new Double(studentPoints);
    	for(Iterator iter = assignments.iterator(); iter.hasNext();) {
    		Object pair[] = (Object[]) iter.next();
			Assignment asn = (Assignment)pair[0];
            if(asn.getPointsPossible() != null){
            	studentPoints += asn.getPointsPossible().doubleValue();
            }
    	}
    	return new Double(studentPoints);
    }
    
	protected void getDroppedTotalPointsForStudent(String studentId, CourseGradeRecord cgr, List assignments)
	{
		double droppedEarnedTotal = 0.0;
		double droppedPointsTotal = 0.0;
		ArrayList<EarnedPointsValue> pointsValueList = new ArrayList<EarnedPointsValue>();

		if (assignments == null || assignments.size() == 0) return;
		
		for (Iterator iter = assignments.iterator(); iter.hasNext();)
		{
			Object pair[] = (Object[]) iter.next();
			Assignment asn = (Assignment) pair[0];
			AssignmentGradeRecord agr = (AssignmentGradeRecord) pair[1];
			if (asn.getPointsPossible() != null)
			{
				pointsValueList.add(new EarnedPointsValue(agr.getPointsEarned() / asn.getPointsPossible(), agr.getPointsEarned(), asn
						.getPointsPossible()));
			}
		}
		if (pointsValueList != null && pointsValueList.size() > 0)
		{
			if (pointsValueList.size() > 1)
			{
				Collections.sort(pointsValueList);
				for (EarnedPointsValue epv : pointsValueList.subList(1, pointsValueList.size()))
				{
					droppedEarnedTotal = droppedEarnedTotal + epv.getEarnedPoints();
					droppedPointsTotal = droppedPointsTotal + epv.getPointsPossible();
				}
				EarnedPointsValue epv = (EarnedPointsValue) pointsValueList.get(0);
				cgr.setDroppedItemPoints(epv.getPointsPossible());
			}
			if (pointsValueList.size() == 1)
			{
				EarnedPointsValue epv = (EarnedPointsValue) pointsValueList.get(0);
				droppedEarnedTotal = droppedEarnedTotal + epv.getEarnedPoints();
				droppedPointsTotal = droppedPointsTotal + epv.getPointsPossible();
				cgr.setDroppedItemPoints(0.0);
			}
		}
		cgr.setDroppedEarnedTotal(droppedEarnedTotal);
		cgr.setDroppedPointsTotal(droppedPointsTotal);
	}

    /**
     * Get total points for this list of assignments
     * Limit list to those that are already due
     *
     * @param assignments list of assignments
     * @return Double object with total value or 0.0
     */
    protected Double getTotalDuePointsForStudent(List assignments) {
    	double studentDuePoints = 0.0;
    	Date currentDate = new Date(Calendar.getInstance().getTimeInMillis());

    	if (assignments == null || assignments.size() == 0) return new Double(studentDuePoints);
    	for(Iterator iter = assignments.iterator(); iter.hasNext();) {
    		Object pair[] = (Object[]) iter.next();
			Assignment asn = (Assignment)pair[0];
            if(asn.getPointsPossible() != null && asn.getDueDate() != null && asn.getDueDate().before(currentDate)){
            	studentDuePoints += asn.getPointsPossible().doubleValue();
            }
    	}
    	return new Double(studentDuePoints);
    }

    /**
     * Gets assignments that are due and for which a grade record exists for atleast one student
     * using an hql sum query
     *
     * @param gradebookId Gradebook id
     * @param session Session object
     * @return Double object with total points value or 0.0
     * @throws HibernateException
     */
    protected List getDueGradedAssignments(Long gradebookId, Session session) throws HibernateException {
    	java.sql.Date currentDate = new java.sql.Date(Calendar.getInstance().getTimeInMillis());
    	return session.createQuery(
        "select asn from Assignment as asn where asn.gradebook.id=? and asn.notCounted=false and asn.removed=false and asn.dueDate < ? and asn.name != \'Course Grade\' and asn.id in (select agr.gradableObject.id from AssignmentGradeRecord agr where agr.gradableObject.gradebook.id = ? and agr.pointsEarned is not null)").
    			 setLong(0, gradebookId.longValue()).setDate(1, currentDate).setLong(2, gradebookId.longValue()).list();
    }

    protected List getCountedStudentGradeRecords(Long gradebookId, String studentId, Session session) throws HibernateException {
        return session.createQuery(
        	"select agr from AssignmentGradeRecord as agr, Assignment as asn where agr.studentId=? and agr.gradableObject=asn and asn.removed=false and asn.notCounted=false and asn.gradebook.id=?").
        	setString(0, studentId).
        	setLong(1, gradebookId.longValue()).
        	list();
    }

    /**
     */
    public CourseGrade getCourseGrade(Long gradebookId) {
        return (CourseGrade)getHibernateTemplate().find(
                "from CourseGrade as cg where cg.gradebook.id=?",
                gradebookId).get(0);
    }

    /**
     * This method gets the list of gradable objects(assignments) for which this student has submissions.
     * Sets the total graded(closed and open), total due(only closed) points for
     * a course grade record. Also sets the cumulative grade by performing the comparison between total due points
     * and total due graded points
     *
     * @param cgr CourseGradeRecord object
     * @param gradebookId Gradebook id
     * @param studentId Student id
     * @param session Session object
     * @return CourseGradeRecord object
     */
    protected CourseGradeRecord setGradesAndPoints(CourseGradeRecord cgr, long gradebookId, String studentId, List dueGradedAssignments, Session session)
    {
    	if (cgr == null) return null;
    	//Get the list of gradable objects(assignments) for which this student has submissions.
        //This list will include assignments that have already been due as well as open ones(the instructor may
        //have graded some assignments but they might still be open)
    	List studentAssignments = getStudentAssignments(gradebookId, studentId, session);
    	if (studentAssignments == null || studentAssignments.size() == 0) return cgr;
    	if (cgr.getGradableObject().getGradebook().isDropGradeDisplayed()) getDroppedTotalPointsForStudent(studentId, cgr, studentAssignments);
    	

    	//Get the total points possible for all the list of assignments
    	//Includes due and open assignments
    	Double totalGradedPoints = getTotalPointsForStudent(studentAssignments);
		cgr.setTotalGradedPoints(totalGradedPoints);

		//Get the list of due assignments for this student
		List studentDueAssignments = getStudentDueAssignments(gradebookId, studentId, session);

		//This condition implies student has missed a submission that is due
		if (isMissingSubmission(studentDueAssignments, dueGradedAssignments))
		{	
			cgr.setCumulativeGrade(null);
            cgr.setDroppedCmlGrade(null);
		}
		else
		{	
			cgr.setCumulativeGrade(cgr.calculatePercent(((Double) totalGradedPoints).doubleValue()));
			cgr.setDroppedCmlGrade(cgr.calculateDropPercent());
		}
		return cgr;
    }

    /**
     * Returns list of assignments for this student that have come due
     * @param gradebookId The gradebook id
     * @param studentId The student id
     * @param session Session object
     * @return List of assignments
     */
	private List getStudentDueAssignments(Long gradebookId, String studentId, Session session)
	{
		java.sql.Date currentDate = new java.sql.Date(Calendar.getInstance().getTimeInMillis());

		List assignments = session
				.createQuery(
						"from Assignment as asn,AssignmentGradeRecord as agr where asn.gradebook.id=? and asn.notCounted=false and asn.removed=false and asn.name != \'Course Grade\' and asn.dueDate < ? and asn=agr.gradableObject and agr.studentId=? and agr.pointsEarned is not null")
				.setLong(0, gradebookId.longValue()).setDate(1, currentDate).setString(2, studentId).list();
		return assignments;
	}
	
    /**
     * Check is a student is missing a submission
     *
     * @param studentDueAssignments List of due assignments that the student has turned in and have been graded
     * @param dueGradedAssignments List of due and graded assignments for the class
     * @return true if student has a missing submission, false otherwise
     */
    private boolean isMissingSubmission(List studentDueAssignments, List dueGradedAssignments)
    {
    	if ((studentDueAssignments == null || studentDueAssignments.size() == 0)&& (dueGradedAssignments == null || dueGradedAssignments.size() == 0))return false;
    	if (studentDueAssignments == null || studentDueAssignments.size() == 0) return true;
    	if (dueGradedAssignments == null || dueGradedAssignments.size() == 0) return true;
    	if (dueGradedAssignments.size() > studentDueAssignments.size())return true;
    	return false;
  }

    /**
     * Gets the course grade record for a student, or null if it does not yet exist.
     *
     * @param studentId The student ID
     * @param session The hibernate session
     * @return A List of grade records
     *
     * @throws HibernateException
     */
	protected CourseGradeRecord getCourseGradeRecord(Gradebook gradebook, String studentId, Session session) throws HibernateException
	{
		CourseGradeRecord cgr = (CourseGradeRecord) session
				.createQuery("from CourseGradeRecord as cgr where cgr.studentId=? and cgr.gradableObject.gradebook=?").setString(0, studentId)
				.setEntity(1, gradebook).uniqueResult();
		if (cgr != null)
		{
			//Get list of assignments that have been due and for which grades have been released
			//Does this by checking due date and checking to see if atleast one submission
			//exists for this assignment
			//This method call is independent of student id
			List dueGradedAssignments = getDueGradedAssignments(gradebook.getId(), session);
			cgr = setGradesAndPoints(cgr, gradebook.getId(), studentId, dueGradedAssignments, session);
		}
		return cgr;
	}

    /**
     * Recalculates the course grade records for the specified set of students.
     * This should be called any time the total number of points possible in a
     * gradebook is modified, either by editing, adding, or removing assignments
     * or external assessments.
     *
     * You must flush and clear the hibernate session prior to calling this method,
     * or you risk causing data contention here.  If data contention does occur
     * here, you will be unable to catch the exception (due to the spring proxy
     * mechanism).
     *
     * TODO Clean up optimistic locking difficulties in recalculate grades.
     * We currently have a "update calculation on write" model. Inside the Gradebook
     * application, reading the calculated grade happens much more often than updating
     * it, and so that design made sense at first. But nowadays, many sites will be
     * updating the Gradebook through services more often than the instructor looks
     * at the calculated course grade. And so we should probably move to a "mark
     * calculation as dirty and update calculation on read" design instead.
     *
     * @param gradebook The gradebook containing the course grade records to update
     * @param studentIds The collection of student IDs
     * @param session The hibernate session
     */
    protected void recalculateCourseGradeRecords(final Gradebook gradebook,
            final Collection studentIds, Session session) throws HibernateException {
        if(logger.isDebugEnabled()) logger.debug("Recalculating " + studentIds.size() + " course grade records");
        int changedRecordCount = 0;	// For debugging
        List assignments = getAssignments(gradebook.getId(), session);
        String graderId = getUserUid();
        Date now = new Date();
        for(Iterator studentIter = studentIds.iterator(); studentIter.hasNext();) {
            String studentId = (String)studentIter.next();

            List gradeRecords = getCountedStudentGradeRecords(gradebook.getId(), studentId, session);
            CourseGrade cg = getCourseGrade(gradebook.getId());
            cg.calculateTotalPointsPossible(assignments);

            // Find the course grade record, if it exists
            CourseGradeRecord cgr = getCourseGradeRecord(gradebook, studentId, session);
            if(cgr == null) {
            	cgr = new CourseGradeRecord(cg, studentId, null);
                cgr.setGraderId(graderId);
                cgr.setDateRecorded(now);
            }

            // Store old value to see whether an update is really needed.
            Double oldPointsEarned = cgr.getPointsEarned();
			Double oldSortGrade = cgr.getSortGrade();

            // Calculate and update the total points and sort grade fields
            cgr.calculateTotalPointsEarned(gradeRecords);
            if (cgr.getEnteredGrade() == null) {
				cgr.setSortGrade(cgr.calculatePercent(cg.getTotalPoints().doubleValue()));
			}

            // Only update if there's been a change.
            Double newPointsEarned = cgr.getPointsEarned();
            Double newSortGrade = cgr.getSortGrade();

            if (
				((newPointsEarned != null) && (!newPointsEarned.equals(oldPointsEarned))) ||
            	((newPointsEarned == null) && (oldPointsEarned != null)) ||
            	((newSortGrade != null) && (!newSortGrade.equals(oldSortGrade))) ||
            	((newSortGrade == null) && (oldSortGrade != null))
            ) {
				session.saveOrUpdate(cgr);
				changedRecordCount++;
			}
        }
        if(logger.isDebugEnabled()) logger.debug("Stored " + changedRecordCount + " changed course grade records");
    }

    /**
     * Recalculates the course grade records for all students in a gradebook.
     * This should be called any time the total number of points possible in a
     * gradebook is modified, either by editing, adding, or removing assignments
     * or external assessments.
     *
     * @param gradebook
     * @param session
     * @throws HibernateException
     */
    protected void recalculateCourseGradeRecords(Gradebook gradebook, Session session) throws HibernateException {
		// Need to fix any data contention before calling the recalculation.
		session.flush();
		session.clear();
        recalculateCourseGradeRecords(gradebook, getAllStudentUids(gradebook.getUid()), session);
    }

    public String getGradebookUid(Long id) {
        return ((Gradebook)getHibernateTemplate().load(Gradebook.class, id)).getUid();
    }

	protected Set getAllStudentUids(String gradebookUid) {
		List enrollments = getSectionAwareness().getSiteMembersInRole(gradebookUid, Role.STUDENT);
        Set studentUids = new HashSet();
        for(Iterator iter = enrollments.iterator(); iter.hasNext();) {
            studentUids.add(((EnrollmentRecord)iter.next()).getUser().getUserUid());
        }
        return studentUids;
	}

	protected Map getPropertiesMap() {

		return propertiesMap;
	}

	public String getPropertyValue(final String name) {
		String value = (String)propertiesMap.get(name);
		if (value == null) {
			List list = getHibernateTemplate().find("from GradebookProperty as prop where prop.name=?",
				name);
			if (!list.isEmpty()) {
				GradebookProperty property = (GradebookProperty)list.get(0);
				value = property.getValue();
				propertiesMap.put(name, value);
			}
		}
		return value;
	}
	public void setPropertyValue(final String name, final String value) {
		GradebookProperty property;
		List list = getHibernateTemplate().find("from GradebookProperty as prop where prop.name=?",
			name);
		if (!list.isEmpty()) {
			property = (GradebookProperty)list.get(0);
		} else {
			property = new GradebookProperty(name);
		}
		property.setValue(value);
		getHibernateTemplate().saveOrUpdate(property);
		propertiesMap.put(name, value);
	}

	/**
	 * Oracle has a low limit on the maximum length of a parameter list
	 * in SQL queries of the form "WHERE tbl.col IN (:paramList)".
	 * Since enrollment lists can sometimes be very long, we've replaced
	 * such queries with full selects followed by filtering. This helper
	 * method filters out unwanted grade records. (Typically they're not
	 * wanted because they're either no longer officially enrolled in the
	 * course or they're not members of the selected section.)
	 */
	protected List filterGradeRecordsByStudents(Collection gradeRecords, Collection studentUids) {
		List filteredRecords = new ArrayList();
		for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
			AbstractGradeRecord agr = (AbstractGradeRecord)iter.next();
			if (studentUids.contains(agr.getStudentId())) {
				filteredRecords.add(agr);
			}
		}
		return filteredRecords;
	}

    public Authn getAuthn() {
        return authn;
    }
    public void setAuthn(Authn authn) {
        this.authn = authn;
    }

    protected String getUserUid() {
        return authn.getUserUid();
    }

    protected SectionAwareness getSectionAwareness() {
        return sectionAwareness;
    }
    public void setSectionAwareness(SectionAwareness sectionAwareness) {
        this.sectionAwareness = sectionAwareness;
    }

}
