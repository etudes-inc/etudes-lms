/**********************************************************************************
*
* $Id: GradebookManagerHibernateImpl.java 8043 2014-05-29 22:06:12Z ggolden $
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

package org.sakaiproject.tool.gradebook.business.impl;

import java.sql.SQLException;
import java.util.*;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.ConflictingSpreadsheetNameException;

import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.component.gradebook.BaseHibernateManager;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.GradeRecordSet;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.GradingEvents;
import org.sakaiproject.tool.gradebook.Spreadsheet;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;

/**
 * Manages Gradebook persistence via hibernate.
 */
public class GradebookManagerHibernateImpl extends BaseHibernateManager
    implements GradebookManager {

    private static final Log log = LogFactory.getLog(GradebookManagerHibernateImpl.class);

	public void updateGradebook(final Gradebook gradebook) throws StaleObjectModificationException {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                // Get the gradebook and selected mapping from persistence
                Gradebook gradebookFromPersistence = (Gradebook)session.load(
                        gradebook.getClass(), gradebook.getId());
                GradeMapping mappingFromPersistence = gradebookFromPersistence.getSelectedGradeMapping();

                // If the mapping has changed, and there are explicitly entered
                // course grade records, disallow this update.
                if (!mappingFromPersistence.getId().equals(gradebook.getSelectedGradeMapping().getId())) {
                    if(isExplicitlyEnteredCourseGradeRecords(gradebook.getId())) {
                        throw new IllegalStateException("Selected grade mapping can not be changed, since explicit course grades exist.");
                    }
                }

                // Evict the persisted objects from the session and update the gradebook
                // so the new grade mapping is used in the sort column update
                //session.evict(mappingFromPersistence);
                for(Iterator iter = gradebookFromPersistence.getGradeMappings().iterator(); iter.hasNext();) {
                    session.evict(iter.next());
                }
                session.evict(gradebookFromPersistence);
                try {
                    session.update(gradebook);
                    session.flush();
                } catch (StaleObjectStateException e) {
                    throw new StaleObjectModificationException(e);
                }

                // If the same mapping is selected, but it has been modified, we need
                // to trigger a sort value update on the explicitly entered course grades
                if(!mappingFromPersistence.equals(gradebook.getSelectedGradeMapping())) {
					updateCourseGradeRecordSortValues(gradebook.getId());
                }

                return null;
            }
        };
        getHibernateTemplate().execute(hc);
	}

    public void removeAssignment(final Long assignmentId) throws StaleObjectModificationException {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Assignment asn = (Assignment)session.load(Assignment.class, assignmentId);
                Gradebook gradebook = asn.getGradebook();
                asn.setRemoved(true);
                session.update(asn);
               // if(logger.isInfoEnabled()) logger.info("Assignment " + asn.getName() + " has been removed from " + gradebook);

                // Update the course grade records
                recalculateCourseGradeRecords(gradebook, session);

                return null;
            }
        };
        getHibernateTemplate().execute(hc);
    }

    public Gradebook getGradebook(Long id) {
        return (Gradebook)getHibernateTemplate().load(Gradebook.class, id);
    }

	/**
	 */
	public List getPointsEarnedSortedGradeRecords(final GradableObject go, final Collection studentUids) {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                if(studentUids == null || studentUids.size() == 0) {
                    // if(logger.isInfoEnabled()) logger.info("Returning no grade records for an empty collection of student UIDs");
                    return new ArrayList();
                }

				Query q = session.createQuery("from AbstractGradeRecord as agr where agr.gradableObject.removed=false and agr.gradableObject.id=:gradableObjectId order by agr.pointsEarned");
				q.setLong("gradableObjectId", go.getId().longValue());
				List records = filterGradeRecordsByStudents(q.list(), studentUids);

				// If this is a course grade, calculate the point totals for the grade records
				if(go.isCourseGrade()) {
					Double totalPoints = ((CourseGrade)go).getTotalPoints();
					//Get list of assignments that have been due and for which grades have been released
					//Does this by checking due date and checking to see if atleast one submission
					//exists for this assignment
					//This method call is independent of student id
					List dueGradedAssignments = getDueGradedAssignments(go.getGradebook().getId(), session);
					
					if(logger.isDebugEnabled()) logger.debug("Total points = " + totalPoints);
					for(Iterator iter = records.iterator(); iter.hasNext();) {
						CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
						if(logger.isDebugEnabled()) logger.debug("Points earned = " + cgr.getPointsEarned());
                        if(cgr.getPointsEarned() != null) {
                        	cgr.setTotalPoints(totalPoints);
                        	cgr.setAutoCalculatedGrade(cgr.calculatePercent(totalPoints.doubleValue()));
                        	cgr = setGradesAndPoints(cgr, go.getGradebook().getId(), cgr.getStudentId(), dueGradedAssignments, session);
                        }
					}
				}

		        return records;
			}
		};
        return (List)getHibernateTemplate().execute(hc);
	}

    /**
     */
    public List getPointsEarnedSortedAllGradeRecords(final Long gradebookId, final Collection studentUids) {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                if(studentUids.size() == 0) {
                    // If there are no enrollments, no need to execute the query.
                    // if(logger.isInfoEnabled()) logger.info("No enrollments were specified.  Returning an empty List of grade records");
                    return new ArrayList();
                } else {
                    Query q = session.createQuery("from AbstractGradeRecord as agr where agr.gradableObject.removed=false and " +
                            "agr.gradableObject.gradebook.id=:gradebookId order by agr.pointsEarned");
                    q.setLong("gradebookId", gradebookId.longValue());
                    return filterGradeRecordsByStudents(q.list(), studentUids);
                }
            }
        };
        return (List)getHibernateTemplate().execute(hc);
    }

   /**
     * @return Returns set of student UIDs who were given scores higher than the assignment's value.
     */
    public Set updateAssignmentGradeRecords(final GradeRecordSet gradeRecordSet)
        throws StaleObjectModificationException {

        final Collection gradeRecordsFromCall = gradeRecordSet.getAllGradeRecords();
        final Set studentIds = gradeRecordSet.getAllStudentIds();

        // If no grade records are sent, don't bother doing anything with the db
        if(gradeRecordsFromCall.size() == 0) {
            log.debug("updateAssignmentGradeRecords called for zero grade records");
            return new HashSet();
        }

        HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
                // Evict the grade records from the session so we can check for point changes
                for(Iterator iter = gradeRecordsFromCall.iterator(); iter.hasNext();) {
                    session.evict(iter.next());
                }

                Assignment assignment = (Assignment)session.load(Assignment.class, gradeRecordSet.getGradableObject().getId());
				Date now = new Date();
				Gradebook gb = assignment.getGradebook();
				String graderId = authn.getUserUid();

                Set studentsWithUpdatedAssignmentGradeRecords = new HashSet();
				Set studentsWithExcessiveScores = new HashSet();

				// In the following queries, we retrieve column values instead of
				// mapped Java objects. This is to avoid a Hibernate NonUniqueObjectException
				// due to conflicts with the input grade records.
				List persistentGradeRecords;
				if (studentIds.size() <= MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST) {
					String hql = "select gr.studentId, gr.pointsEarned from AssignmentGradeRecord as gr where gr.gradableObject=:go and gr.studentId in (:studentIds)";
					Query q = session.createQuery(hql);
					q.setParameter("go", assignment);
					q.setParameterList("studentIds", studentIds);
					persistentGradeRecords = q.list();
				} else {
					String hql = "select gr.studentId, gr.pointsEarned from AssignmentGradeRecord as gr where gr.gradableObject=:go";
					Query q = session.createQuery(hql);
					q.setParameter("go", assignment);
					persistentGradeRecords = new ArrayList();
					for (Iterator iter = q.list().iterator(); iter.hasNext(); ) {
						Object[] oa = (Object[])iter.next();
						if (studentIds.contains(oa[0])) {
							persistentGradeRecords.add(oa);
						}
					}
				}

				// Construct a map of student id to persistent grade record scores
                Map scoreMap = new HashMap();
                for(Iterator iter = persistentGradeRecords.iterator(); iter.hasNext();) {
                    Object[] oa = (Object[])iter.next();
                    scoreMap.put(oa[0], oa[1]);
                }

                for(Iterator iter = gradeRecordsFromCall.iterator(); iter.hasNext();) {
                    // Keep track of whether this grade record needs to be updated
                    boolean performUpdate = false;

                    AssignmentGradeRecord gradeRecordFromCall = (AssignmentGradeRecord)iter.next();
                    if(scoreMap.containsKey(gradeRecordFromCall.getStudentId())) {
                        // The student already has a grade record, only perform an update if the grade has changed
                        Double pointsInDb = (Double)scoreMap.get(gradeRecordFromCall.getStudentId());

                        if( (pointsInDb != null && !pointsInDb.equals(gradeRecordFromCall.getPointsEarned())) ||
                                (pointsInDb == null && gradeRecordFromCall.getPointsEarned() != null)) {
                            // The grade record's value has changed
                            gradeRecordFromCall.setGraderId(graderId);
                            gradeRecordFromCall.setDateRecorded(now);
                            session.update(gradeRecordFromCall);
                            performUpdate = true;
                        }
                    } else {
                        // This is a new grade record
                        if(gradeRecordFromCall.getPointsEarned() != null) {
                            gradeRecordFromCall.setGraderId(graderId);
                            gradeRecordFromCall.setDateRecorded(now);
                            session.save(gradeRecordFromCall);
                            performUpdate = true;
                        }
                    }

                    // Check for excessive (AKA extra credit) scoring.
                    if (performUpdate &&
                            gradeRecordFromCall.getPointsEarned() != null &&
                            gradeRecordFromCall.getPointsEarned().compareTo(assignment.getPointsPossible()) > 0) {
                    	studentsWithExcessiveScores.add(gradeRecordFromCall.getStudentId());
                    }

                    // Log the grading event, and keep track of the students with saved/updated grades
                    if(performUpdate) {
                        session.save(new GradingEvent(assignment, graderId, gradeRecordFromCall.getStudentId(), gradeRecordFromCall.getPointsEarned()));
                        studentsWithUpdatedAssignmentGradeRecords.add(gradeRecordFromCall.getStudentId());
                    }
                }
                try {
					// Update the course grade records for students with assignment grade record changes
					recalculateCourseGradeRecords(gb, studentsWithUpdatedAssignmentGradeRecords, session);
                } catch (StaleObjectStateException sose) {
                    if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update assignment grade records");
                    throw new StaleObjectModificationException(sose);
                }

                return studentsWithExcessiveScores;
			}
		};

        return (Set)getHibernateTemplate().execute(hc);
    }

    /**
     */
    public void updateCourseGradeRecords(final GradeRecordSet gradeRecordSet)
        throws StaleObjectModificationException {

        if(gradeRecordSet.getAllGradeRecords().size() == 0) {
            log.debug("updateCourseGradeRecords called with zero grade records to update");
            return;
        }

        HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
                for(Iterator iter = gradeRecordSet.getAllGradeRecords().iterator(); iter.hasNext();) {
                    session.evict(iter.next());
                }

				CourseGrade courseGrade = (CourseGrade)gradeRecordSet.getGradableObject();
				Date now = new Date();
				Gradebook gb = courseGrade.getGradebook();
				String graderId = authn.getUserUid();

				// Find the number of points possible in this gradebook
				double totalPointsPossibleInGradebook = getTotalPoints(gb.getId());

                // Find the grade records for these students on this gradable object
				// In the following queries, we retrieve column values instead of
				// mapped Java objects. This is to avoid a Hibernate NonUniqueObjectException
				// due to conflicts with the input grade records.
                Set studentIds = gradeRecordSet.getAllStudentIds();
				List persistentGradeRecords;
				if (studentIds.size() <= MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST) {
					String hql = "select gr.studentId, gr.enteredGrade from CourseGradeRecord as gr where gr.gradableObject=:go and gr.studentId in (:studentIds)";
					Query q = session.createQuery(hql);
					q.setParameter("go", courseGrade);
					q.setParameterList("studentIds", studentIds);
					persistentGradeRecords = q.list();
				} else {
					String hql = "select gr.studentId, gr.enteredGrade from CourseGradeRecord as gr where gr.gradableObject=:go";
					Query q = session.createQuery(hql);
					q.setParameter("go", courseGrade);
					persistentGradeRecords = new ArrayList();
					for (Iterator iter = q.list().iterator(); iter.hasNext(); ) {
						Object[] oa = (Object[])iter.next();
						if (studentIds.contains(oa[0])) {
							persistentGradeRecords.add(oa);
						}
					}
				}

                // Construct a map of student id to persistent grade record scores
                Map scoreMap = new HashMap();
                for(Iterator iter = persistentGradeRecords.iterator(); iter.hasNext();) {
                    Object[] oa = (Object[])iter.next();
                    scoreMap.put(oa[0], oa[1]);
                }

                for(Iterator iter = gradeRecordSet.getAllGradeRecords().iterator(); iter.hasNext();) {
                    // The possibly modified course grade record
                    CourseGradeRecord gradeRecordFromCall = (CourseGradeRecord)iter.next();

                    // The entered grade in the db for this grade record
                    String grade = (String)scoreMap.get(gradeRecordFromCall.getStudentId());

                    // Update the existing record

					// If the entered grade hasn't changed, just move on
					if(gradeRecordFromCall.getEnteredGrade() == null && grade == null) {
						continue;
					}
					if(gradeRecordFromCall.getEnteredGrade() != null && grade != null && gradeRecordFromCall.getEnteredGrade().equals(grade)) {
						continue;
					}

					// Update the sort grade
					if(gradeRecordFromCall.getEnteredGrade() == null) {
						gradeRecordFromCall.setSortGrade(gradeRecordFromCall.calculatePercent(totalPointsPossibleInGradebook));
					} else {
						gradeRecordFromCall.setSortGrade(gb.getSelectedGradeMapping().getValue(gradeRecordFromCall.getEnteredGrade()));
					}

					gradeRecordFromCall.setGraderId(graderId);
					gradeRecordFromCall.setDateRecorded(now);
                    try {
                        session.saveOrUpdate(gradeRecordFromCall);
                        session.flush();
                    } catch (StaleObjectStateException sose) {
                        if(log.isInfoEnabled()) log.info("An optimistic locking failure occurred while attempting to update course grade records");
                        throw new StaleObjectModificationException(sose);
                    }

                    // Log the grading event
                    session.save(new GradingEvent(courseGrade, graderId, gradeRecordFromCall.getStudentId(), gradeRecordFromCall.getEnteredGrade()));
                }

                return null;
            }
        };
        getHibernateTemplate().execute(hc);
    }

    /**
     */
    public boolean isExplicitlyEnteredCourseGradeRecords(final Long gradebookId) {
    	final Set studentUids = getAllStudentUids(getGradebookUid(gradebookId));
    	if (studentUids.isEmpty()) {
    		return false;
    	}

        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
				Integer total;
				if (studentUids.size() <= MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST) {
					Query q = session.createQuery(
						"select count(cgr) from CourseGradeRecord as cgr where cgr.enteredGrade is not null and cgr.gradableObject.gradebook.id=:gradebookId and cgr.studentId in (:studentUids)");
					q.setLong("gradebookId", gradebookId.longValue());
					q.setParameterList("studentUids", studentUids);
					total = (Integer)q.list().get(0);
					// if (log.isInfoEnabled()) log.info("total number of explicitly entered course grade records = " + total);
				} else {
					total = new Integer(0);
					Query q = session.createQuery(
						"select cgr.studentId from CourseGradeRecord as cgr where cgr.enteredGrade is not null and cgr.gradableObject.gradebook.id=:gradebookId");
					q.setLong("gradebookId", gradebookId.longValue());
					for (Iterator iter = q.list().iterator(); iter.hasNext(); ) {
						String studentId = (String)iter.next();
						if (studentUids.contains(studentId)) {
							total = new Integer(1);
							break;
						}
					}
				}
                return total;
            }
        };
        return ((Integer)getHibernateTemplate().execute(hc)).intValue() > 0;
    }

    public boolean isEnteredAssignmentScores(final Long assignmentId) {
		HibernateCallback hc = new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Integer total = (Integer)session.createQuery(
					"select count(agr) from AssignmentGradeRecord as agr where agr.gradableObject.id=? and agr.pointsEarned is not null").
					setLong(0, assignmentId.longValue()).
					uniqueResult();
               // if (log.isInfoEnabled()) log.info("assignment " + assignmentId + " has " + total + " entered scores");
                return total;
            }
        };
        return ((Integer)getHibernateTemplate().execute(hc)).intValue() > 0;
    }

	/**
	 */
	public List getStudentGradeRecords(final Long gradebookId, final String studentId) {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
				return session.createQuery(
					"from AssignmentGradeRecord as agr where agr.studentId=? and agr.gradableObject.removed=false and agr.gradableObject.gradebook.id=?").
					setString(0, studentId).
					setLong(1, gradebookId.longValue()).
					list();
            }
        };
        return (List)getHibernateTemplate().execute(hc);
	}

	public CourseGradeRecord getStudentCourseGradeRecord(final Gradebook gradebook, final String studentId) {
		return (CourseGradeRecord)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				return getCourseGradeRecord(gradebook, studentId, session);
			}
		});
	}

    public GradingEvents getGradingEvents(final GradableObject gradableObject, final Collection enrollments) {

        // Don't attempt to run the query if there are no enrollments
        if(enrollments == null || enrollments.size() == 0) {
            log.debug("No enrollments were specified.  Returning an empty GradingEvents object");
            return new GradingEvents();
        }

        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                // Construct a set of student ids from the enrollments
                Set studentIds = new HashSet();
                for(Iterator iter = enrollments.iterator(); iter.hasNext();) {
                    studentIds.add(((EnrollmentRecord)iter.next()).getUser().getUserUid());
                }

				List eventsList;
				if (studentIds.size() <= MAX_NUMBER_OF_SQL_PARAMETERS_IN_LIST) {
					Query q = session.createQuery("from GradingEvent as ge where ge.gradableObject=:go and ge.studentId in (:students)");
					q.setParameter("go", gradableObject, Hibernate.entity(GradableObject.class));
					q.setParameterList("students", studentIds);
					eventsList = q.list();
				} else {
					Query q = session.createQuery("from GradingEvent as ge where ge.gradableObject=:go");
					q.setParameter("go", gradableObject, Hibernate.entity(GradableObject.class));
					eventsList = new ArrayList();
					for (Iterator iter = q.list().iterator(); iter.hasNext(); ) {
						GradingEvent event = (GradingEvent)iter.next();
						if (studentIds.contains(event.getStudentId())) {
							eventsList.add(event);
						}
					}
				}
				return eventsList;
            }
        };

        List list = (List)getHibernateTemplate().execute(hc);

        GradingEvents events = new GradingEvents();

        for(Iterator iter = list.iterator(); iter.hasNext();) {
            GradingEvent event = (GradingEvent)iter.next();
            events.addEvent(event);
        }
        return events;
    }

    /**
     */
    public List getAssignments(final Long gradebookId, final String sortBy, final boolean ascending) {
        return (List)getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                List assignments = getAssignments(gradebookId, session);
                sortAssignments(assignments, sortBy, ascending);
                return assignments;
            }
        });
    }
    

    /**
     */
    public List getAssignmentsWithStats(final Long gradebookId, final String sortBy, final boolean ascending) {

    	Set studentUids = getAllStudentUids(getGradebookUid(gradebookId));
		return getAssignmentsWithStatsInternal(gradebookId, sortBy, ascending, studentUids);
    }

    public List getAssignmentsAndCourseGradeWithStats(final Long gradebookId, final String sortBy, final boolean ascending) {
    	Set studentUids = getAllStudentUids(getGradebookUid(gradebookId));
    	List assignments = getAssignmentsWithStatsInternal(gradebookId, sortBy, ascending, studentUids);

    	// Always put the Course Grade at the end.
    	assignments.add(getGradableObjectStatsInternal(getCourseGrade(gradebookId), studentUids));

    	return assignments;
    }

    private List getAssignmentsWithStatsInternal(final Long gradebookId, final String sortBy, final boolean ascending, final Set studentUids) {
        if(logger.isDebugEnabled())logger.debug("sort by is "+sortBy);
        List assignments;
    	if (studentUids.isEmpty()) {
    		// Hibernate 2.1.8 generates invalid SQL if an empty collection is used
    		// as a parameter list.
    		assignments = getAssignments(gradebookId, sortBy, ascending);
    	} else {
    		assignments = (List)getHibernateTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session) throws HibernateException {
					// TODO Combine queries for efficiency.
					Query q = session.createQuery(
						"from AbstractGradeRecord as gr where gr.gradableObject.gradebook.id=:gradebookId and gr.gradableObject.removed=false");
					q.setLong("gradebookId", gradebookId.longValue());
					List gradeRecords = filterGradeRecordsByStudents(q.list(), studentUids);

					List assignments = getAssignments(gradebookId, sortBy, ascending);

					// Calculate and insert the statistics into the assignments
					for(Iterator asnIter = assignments.iterator(); asnIter.hasNext();) {
						Assignment asn = (Assignment)asnIter.next();
						asn.calculateStatistics(gradeRecords, studentUids.size());
					}
					sortAssignments(assignments, sortBy, ascending);
					return assignments;
				}
			});
		}
		return assignments;
    }

    /**
     * TODO Remove this method in favor of doing database sorting.
     *
     * @param assignments
     * @param sortBy
     * @param ascending
     */
    private void sortAssignments(List assignments, String sortBy, boolean ascending) {
        Comparator comp;
        if(Assignment.SORT_BY_NAME.equals(sortBy)) {
            comp = Assignment.nameComparator;
        } else if(Assignment.SORT_BY_MEAN.equals(sortBy)) {
            comp = Assignment.meanComparator;
        } else if(Assignment.SORT_BY_POINTS.equals(sortBy)) {
            comp = Assignment.pointsComparator;
        }else if(Assignment.releasedComparator.equals(sortBy)){
            comp = Assignment.releasedComparator;
        } else {
            comp = Assignment.dateComparator;
        }
        Collections.sort(assignments, comp);
        if(!ascending) {
            Collections.reverse(assignments);
        }
    }

    /**
     */
    public List getAssignments(Long gradebookId) {
        return getAssignments(gradebookId, Assignment.DEFAULT_SORT, true);
    }
    

    /**
     */
    public GradableObject getGradableObject(Long gradableObjectId) {
        return (GradableObject)getHibernateTemplate().load(GradableObject.class, gradableObjectId);
    }

    /**
     */
    public Assignment getAssignmentWithStats(Long assignmentId) {
    	return (Assignment)getGradableObjectWithStats(getGradableObject(assignmentId));
    }

    private GradableObject getGradableObjectWithStats(GradableObject gradableObject) {
    	Set studentUids = getAllStudentUids(gradableObject.getGradebook().getUid());
    	return getGradableObjectStatsInternal(gradableObject, studentUids);
    }

    private GradableObject getGradableObjectStatsInternal(final GradableObject gradableObject, final Set studentUids) {
		return (GradableObject)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				List gradeRecords;
				if (studentUids.isEmpty()) {
					// Hibernate 2.1.8 generates invalid SQL if an empty collection is used
					// as a parameter list.
					gradeRecords = new ArrayList();
				} else {
					Query q = session.createQuery(
						"from AbstractGradeRecord as gr where gr.gradableObject.id=:gradableObjectId and gr.gradableObject.removed=false");
					q.setLong("gradableObjectId", gradableObject.getId().longValue());
					gradeRecords = filterGradeRecordsByStudents(q.list(), studentUids);
				}

				// Calculate the total points possible, along with the auto-calculated grade percentage for each grade record
				if(gradableObject.isCourseGrade()) {
					CourseGrade cg = (CourseGrade)gradableObject;
					cg.calculateTotalPointsPossible(getAssignments(cg.getGradebook().getId()));
					//Get list of assignments that have been due and for which grades have been released
					//Does this by checking due date and checking to see if atleast one submission
					//exists for this assignment
					//This method call is independent of student id
					List dueGradedAssignments = getDueGradedAssignments(cg.getGradebook().getId(), session);
					for(Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
						CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
						if (cgr.getPointsEarned() != null)
						{
							Double totalPoints = cg.getTotalPoints();
							cgr.setTotalPoints(totalPoints);
							cgr.setAutoCalculatedGrade(cgr.calculatePercent(totalPoints.doubleValue()));
							cgr = setGradesAndPoints(cgr, cg.getGradebook().getId(), cgr.getStudentId(), dueGradedAssignments, session);
						}
					}
				}

				gradableObject.calculateStatistics(gradeRecords, studentUids.size());
				return gradableObject;
			}
		});
    }
    
   

    /**
     */

    public Long createAssignment(final Long gradebookId, final String name, final Double points, final Date dueDate, final Boolean isNotCounted, final Boolean isReleased) throws ConflictingAssignmentNameException, StaleObjectModificationException {

        HibernateCallback hc = new HibernateCallback() {
               public Object doInHibernate(Session session) throws HibernateException {
                   Gradebook gb = (Gradebook)session.load(Gradebook.class, gradebookId);
                   int numNameConflicts = ((Integer)session.createQuery(
                           "select count(go) from GradableObject as go where go.name = ? and go.gradebook = ? and go.removed=false").
                           setString(0, name).
                           setEntity(1, gb).
                           uniqueResult()).intValue();
                   if(numNameConflicts > 0) {
                       throw new ConflictingAssignmentNameException("You can not save multiple assignments in a gradebook with the same name");
                   }

                   Assignment asn = new Assignment();
                   asn.setGradebook(gb);
                   asn.setName(name);
                   asn.setPointsPossible(points);
                   asn.setDueDate(dueDate);
                   if (isNotCounted != null) {
                       asn.setNotCounted(isNotCounted.booleanValue());
                   }

                   if(isReleased!=null){
                       asn.setReleased(isReleased.booleanValue());
                   }

                   // Save the new assignment
                   Long id = (Long)session.save(asn);

                   // Recalculate the course grades
                   recalculateCourseGradeRecords(asn.getGradebook(), session);

                   return id;
               }
           };

           return (Long)getHibernateTemplate().execute(hc);


    }

    /**
     */
    public void updateAssignment(final Assignment assignment)
        throws ConflictingAssignmentNameException, StaleObjectModificationException {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                // Ensure that we don't have the assignment in the session, since
                // we need to compare the existing one in the db to our edited assignment
                session.evict(assignment);

                boolean pointsChanged = false;

                Assignment asnFromDb = (Assignment)session.load(Assignment.class, assignment.getId());
                int numNameConflicts = ((Integer)session.createQuery(
                        "select count(go) from GradableObject as go where go.name = ? and go.gradebook = ? and go.removed=false and go.id != ?").
                        setString(0, assignment.getName()).
                        setEntity(1, assignment.getGradebook()).
                        setLong(2, assignment.getId().longValue()).
                        uniqueResult()).intValue();
                if(numNameConflicts > 0) {
                    throw new ConflictingAssignmentNameException("You can not save multiple assignments in a gradebook with the same name");
                }

                if (!asnFromDb.getPointsPossible().equals(assignment.getPointsPossible()) ||
                	(asnFromDb.isNotCounted() != assignment.isNotCounted())) {
                    pointsChanged = true;
                }

                session.evict(asnFromDb);
                session.update(assignment);

                if(pointsChanged) {
                	recalculateCourseGradeRecords(assignment.getGradebook(), session);
                }

                return null;
            }
        };
        try {
            getHibernateTemplate().execute(hc);
        } catch (HibernateOptimisticLockingFailureException holfe) {
            if(logger.isInfoEnabled()) logger.info("An optimistic locking failure occurred while attempting to update an assignment");
            throw new StaleObjectModificationException(holfe);
        }
    }

    /**
     */
    public CourseGrade getCourseGradeWithStats(Long gradebookId) {
        return (CourseGrade)getGradableObjectWithStats(getCourseGrade(gradebookId));
    }

    /**
     * Updates the values used for sorting on any course grade record where a letter
     * grade has been explicitly set.  This should happen anytime a gradebook's
     * grade mapping has been modified.
     *
     * @param gradebookId The gradebook id
     */
    private void updateCourseGradeRecordSortValues(final Long gradebookId) {
        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                if(logger.isDebugEnabled()) logger.debug("Updating sort values on manually entered course grades");

                Gradebook gb = (Gradebook)session.load(Gradebook.class, gradebookId);
                GradeMapping mapping = gb.getSelectedGradeMapping();

                StringBuffer hql = new StringBuffer(
					"from CourseGradeRecord as cgr where cgr.enteredGrade is not null and cgr.gradableObject.gradebook.id=?");
                List gradeRecords = session.createQuery(hql.toString()).
                	setLong(0, gradebookId.longValue()).
                	list();

                for(Iterator gradeRecordIterator = gradeRecords.iterator(); gradeRecordIterator.hasNext();) {
                    CourseGradeRecord cgr = (CourseGradeRecord)gradeRecordIterator.next();
                    cgr.setSortGrade(mapping.getValue(cgr.getEnteredGrade()));
                    session.update(cgr);
                }
                return null;
            }
        };
        getHibernateTemplate().execute(hc);
    }

    /**
     * Gets the total number of points possible in a gradebook.
     */
     public double getTotalPoints(Long gradebookId) {
        List assignmentPoints = getHibernateTemplate().find("select asn.pointsPossible from Assignment as asn where asn.removed=false and asn.notCounted=false and asn.gradebook.id=?", gradebookId);
        double totalPoints = 0;
        for(Iterator iter = assignmentPoints.iterator(); iter.hasNext();) {
            Double points = (Double)iter.next();
           totalPoints += points.doubleValue();
        }
        return totalPoints;
    }

    public Gradebook getGradebookWithGradeMappings(final Long id) {
		return (Gradebook)getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException {
				Gradebook gradebook = (Gradebook)session.load(Gradebook.class, id);
				Hibernate.initialize(gradebook.getGradeMappings());
				return gradebook;
			}
		});
	}


    /**
     *
     * @param spreadsheetId
     * @return
     */
    public Spreadsheet getSpreadsheet(final Long spreadsheetId) {
        return (Spreadsheet)getHibernateTemplate().load(Spreadsheet.class, spreadsheetId);
    }

    /**
     *
     * @param gradebookId
     * @return
     */
    public List getSpreadsheets(final Long gradebookId) {
        return (List)getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                List spreadsheets = getSpreadsheets(gradebookId, session);
                return spreadsheets;
            }
        });
    }

    /**
     *
     * @param spreadsheetid
     */
    public void removeSpreadsheet(final Long spreadsheetId)throws StaleObjectModificationException {

        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Spreadsheet spt = (Spreadsheet)session.load(Spreadsheet.class, spreadsheetId);
                session.delete(spt);
               // if(logger.isInfoEnabled()) logger.info("Spreadsheet " + spt.getName() + " has been removed from gradebook" );

                return null;
            }
        };
        getHibernateTemplate().execute(hc);

    }

    /**
     *
     * @param spreadsheet
     */
    public void updateSpreadsheet(final Spreadsheet spreadsheet)throws ConflictingAssignmentNameException, StaleObjectModificationException  {
            HibernateCallback hc = new HibernateCallback() {
                public Object doInHibernate(Session session) throws HibernateException {
                    // Ensure that we don't have the assignment in the session, since
                    // we need to compare the existing one in the db to our edited assignment
                    session.evict(spreadsheet);

                    Spreadsheet sptFromDb = (Spreadsheet)session.load(Spreadsheet.class, spreadsheet.getId());
                    int numNameConflicts = ((Integer)session.createQuery(
                            "select count(spt) from Spreadsheet as spt where spt.name = ? and spt.gradebook = ? and spt.id != ?").
                            setString(0, spreadsheet.getName()).
                            setEntity(1, spreadsheet.getGradebook()).
                            setLong(2, spreadsheet.getId().longValue()).
                            uniqueResult()).intValue();
                    if(numNameConflicts > 0) {
                        throw new ConflictingAssignmentNameException("You can not save multiple spreadsheets in a gradebook with the same name");
                    }

                    session.evict(sptFromDb);
                    session.update(spreadsheet);

                    return null;
                }
            };
            try {
                getHibernateTemplate().execute(hc);
            } catch (HibernateOptimisticLockingFailureException holfe) {
                if(logger.isInfoEnabled()) logger.info("An optimistic locking failure occurred while attempting to update a spreadsheet");
                throw new StaleObjectModificationException(holfe);
            }
    }


    public Long createSpreadsheet(final Long gradebookId, final String name, final String creator, Date dateCreated, final String content) throws ConflictingSpreadsheetNameException,StaleObjectModificationException {

        HibernateCallback hc = new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Gradebook gb = (Gradebook)session.load(Gradebook.class, gradebookId);
                int numNameConflicts = ((Integer)session.createQuery(
                        "select count(spt) from Spreadsheet as spt where spt.name = ? and spt.gradebook = ? ").
                        setString(0, name).
                        setEntity(1, gb).
                        uniqueResult()).intValue();
                if(numNameConflicts > 0) {
                    throw new ConflictingAssignmentNameException("You can not save multiple spreadsheets in a gradebook with the same name");
                }

                Spreadsheet spt = new Spreadsheet();
                spt.setGradebook(gb);
                spt.setName(name);
                spt.setCreator(creator);
                spt.setDateCreated(new Date());
                spt.setContent(content);

                // Save the new assignment
                Long id = (Long)session.save(spt);
                return id;
            }
        };

        return (Long)getHibernateTemplate().execute(hc);

    }

    protected List getSpreadsheets(Long gradebookId, Session session) throws HibernateException {
        List spreadsheets = session.createQuery(
                "from Spreadsheet as spt where spt.gradebook.id=? ").
                setLong(0, gradebookId.longValue()).
                list();
        return spreadsheets;
    }

}
