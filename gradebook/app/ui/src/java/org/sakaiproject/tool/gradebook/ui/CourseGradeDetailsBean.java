/**********************************************************************************
*
* $Id: CourseGradeDetailsBean.java 5793 2013-09-02 18:59:50Z mallikamt $
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.GradeRecordSet;
import org.sakaiproject.tool.gradebook.GradingEvent;
import org.sakaiproject.tool.gradebook.GradingEvents;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

public class CourseGradeDetailsBean extends EnrollmentTableBean {
	private static final Log logger = LogFactory.getLog(CourseGradeDetailsBean.class);

	// View maintenance fields - serializable.
	private List scoreRows;

	// Controller fields - transient.
	private CourseGrade courseGrade;
    private GradeRecordSet gradeRecordSet;
    private GradeMapping gradeMapping;
    private double totalPoints;
    private boolean todateGradeReleased;
    private boolean todatePointsReleased;
    private boolean dropGradeDisplayed; 
    
    protected Gradebook gradebook;


	public class ScoreRow implements Serializable {
        private EnrollSectionRecord enrollment;
        private CourseGradeRecord courseGradeRecord;
        private List eventRows;

		public ScoreRow() {
		}
		public ScoreRow(EnrollSectionRecord enrollment, List gradingEvents) {
            this.enrollment = enrollment;
			courseGradeRecord = (CourseGradeRecord)gradeRecordSet.getGradeRecord(enrollment.getEnrollRec().getUser().getUserUid());
			if (courseGradeRecord == null) {
				// Since we're using the domain object to transport UI input, we need
				// to create it, even if we don't end up storing it in the database.
				courseGradeRecord = new CourseGradeRecord(courseGrade, enrollment.getEnrollRec().getUser().getUserUid(), null);
				gradeRecordSet.addGradeRecord(courseGradeRecord);
			}

            eventRows = new ArrayList();
            for (Iterator iter = gradingEvents.iterator(); iter.hasNext();) {
            	GradingEvent gradingEvent = (GradingEvent)iter.next();
            	eventRows.add(new GradingEventRow(gradingEvent));
            }
		}

        public String getCalculatedLetterGrade() {
        	return gradeMapping.getGrade(courseGradeRecord.getNonNullAutoCalculatedGrade());
        }

        public Double getCalculatedPercentGrade() {
        	return new Double(courseGradeRecord.getNonNullAutoCalculatedGrade().doubleValue() / 100.);
        }

        public String getCalculatedDropLetterGrade() {
        	return gradeMapping.getGrade(courseGradeRecord.getNonNullAutoCalculatedDropGrade());
        }

        public Double getCalculatedDropPercentGrade() {
        	return new Double(courseGradeRecord.getNonNullAutoCalculatedDropGrade().doubleValue() / 100.);
        }

        public boolean isTotalPointsZero() {
        	if (courseGradeRecord.getTotalPoints() == null) return true;
        	return courseGradeRecord.getTotalPoints().doubleValue() == 0.0;
        }

        public String getCumulativeLetterGrade() {
        	return gradeMapping.getGrade(courseGradeRecord.getNonNullCumulativeGrade());
        }

        public Double getCumulativePercentGrade() {
        	return new Double(courseGradeRecord.getNonNullCumulativeGrade().doubleValue() / 100.);
        }

        public String getCumulativeDropLetterGrade() {
        	return gradeMapping.getGrade(courseGradeRecord.getNonNullCumulativeDropGrade());
        }

        public Double getCumulativeDropPercentGrade() {
        	return new Double(courseGradeRecord.getNonNullCumulativeDropGrade().doubleValue() / 100.);
        }

        public boolean isTotalGradedPointsZero() {
        	if (courseGradeRecord.getTotalGradedPoints() == null) return true;
        	return courseGradeRecord.getTotalGradedPoints().doubleValue() == 0.0;
        }

        public boolean isMissingSubmission() {
        	//The cumulative grade is null for students who have a missing submission or if they have not turned in anything at all
        	//Total graded points is zero if the student hasn't turned in anything
        	//This condition only returns true if the student is missing a submission
        	if (courseGradeRecord.getCumulativeGrade() == null && courseGradeRecord.getTotalGradedPoints() != null && courseGradeRecord.getTotalGradedPoints().doubleValue() > 0.0)
        	{
        		return true;
        	}
        	return false;
        }

        public CourseGradeRecord getCourseGradeRecord() {
            return courseGradeRecord;
        }
        public void setCourseGradeRecord(CourseGradeRecord courseGradeRecord) {
            this.courseGradeRecord = courseGradeRecord;
        }
        public EnrollSectionRecord getEnrollment() {
            return enrollment;
        }

        public List getEventRows() {
        	return eventRows;
        }
        public String getEventsLogTitle() {
        	return FacesUtil.getLocalizedString("course_grade_details_log_title", new String[] {enrollment.getEnrollRec().getUser().getDisplayName()});
        }
	}

	protected void init() {
		super.init();
		
		// Clear view state.
		scoreRows = new ArrayList();
		courseGrade = getGradebookManager().getCourseGradeWithStats(getGradebookId());
		gradebook = getGradebook();
        gradeMapping = gradebook.getSelectedGradeMapping();
        todateGradeReleased = gradebook.isTodateGradeDisplayed();
        todatePointsReleased = gradebook.isTodatePointsDisplayed();
        dropGradeDisplayed = gradebook.isDropGradeDisplayed();
        //if (dropGradeDisplayed) setSortColumn(PreferencesBean.SORT_BY_NAME);

        totalPoints = getGradebookManager().getTotalPoints(getGradebookId());

		// Set up score rows.
		Map enrollmentMap = getOrderedEnrollmentMap();
		List gradeRecords = getGradebookManager().getPointsEarnedSortedGradeRecords(courseGrade, enrollmentMap.keySet());
		List workingEnrollments = new ArrayList(enrollmentMap.values());

		if (!isEnrollmentSort()) {
			// Need to sort and page based on a scores column.
			String sortColumn = getSortColumn();
			Comparator comparator = null;
			if (sortColumn.equals(CourseGrade.SORT_BY_CALCULATED_GRADE) ||
                sortColumn.equals(CourseGrade.SORT_BY_POINTS_EARNED)) {
                comparator = CourseGradeRecord.calcComparator;
            } else if (sortColumn.equals(CourseGrade.SORT_BY_OVERRIDE_GRADE )) {
            	comparator = CourseGradeRecord.getOverrideComparator(courseGrade.getGradebook().getSelectedGradeMapping());
            }
            else if (sortColumn.equals(CourseGrade.SORT_BY_CUMULATIVE_GRADE)) {
            	comparator = CourseGradeRecord.getCumuComparator(courseGrade.getGradebook().getSelectedGradeMapping());
            }
            else if (sortColumn.equals(CourseGrade.SORT_BY_POINTS_DROPPED)) {
            	comparator = CourseGradeRecord.getDropComparator(courseGrade.getGradebook().getSelectedGradeMapping());
            }
            else if (sortColumn.equals(CourseGrade.SORT_BY_DROPCUMU_GRADE)) {
            	comparator = CourseGradeRecord.getCumuDropComparator(courseGrade.getGradebook().getSelectedGradeMapping());
            }
            else if (sortColumn.equals(CourseGrade.SORT_BY_DROPCALC_GRADE)) {
            	comparator = CourseGradeRecord.getAutoDropComparator(courseGrade.getGradebook().getSelectedGradeMapping());
            }

            if (comparator != null) {
	            Collections.sort(gradeRecords, comparator);
	        }

			List scoreSortedEnrollments = new ArrayList();
			for(Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
				CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
				scoreSortedEnrollments.add(enrollmentMap.get(cgr.getStudentId()));
			}

			// Put enrollments with no scores at the beginning of the final list.
			workingEnrollments.removeAll(scoreSortedEnrollments);

			// Add all sorted enrollments with scores into the final list
			workingEnrollments.addAll(scoreSortedEnrollments);

			workingEnrollments = finalizeSortingAndPaging(workingEnrollments);
		}

		List actualEnrollments = new ArrayList();
		for (Iterator iter = workingEnrollments.iterator(); iter.hasNext();) {
			actualEnrollments.add(((EnrollSectionRecord)iter.next()).getEnrollRec());
		}
		// Get all of the grading events for these enrollments on this assignment
		GradingEvents allEvents = getGradebookManager().getGradingEvents(courseGrade, actualEnrollments);

		gradeRecordSet = new GradeRecordSet(courseGrade);
		for (Iterator iter = gradeRecords.iterator(); iter.hasNext(); ) {
			CourseGradeRecord gradeRecord = (CourseGradeRecord)iter.next();
			gradeRecordSet.addGradeRecord(gradeRecord);
		}
		/*String groupTitle = null;
		try
		{
		Site site = SiteService.getSite(getGradebookUid());
		Collection groups = site.getGroups();
		List<org.sakaiproject.site.api.Group> siteGroups = new ArrayList();
		for (Object groupO : groups)
		{
			org.sakaiproject.site.api.Group g = (org.sakaiproject.site.api.Group) groupO;

			// skip non-section groups
			if (g.getProperties().getProperty("sections_category") != null) siteGroups.add(g);


		}
		for (Iterator iter = workingEnrollments.iterator(); iter.hasNext(); ) {
			EnrollmentRecord enrollRec = (EnrollmentRecord)iter.next();
			EnrollSectionRecord enrollment = new EnrollSectionRecord();
			enrollment.setSection(enrollRec, siteGroups);
            scoreRows.add(new ScoreRow(enrollment, allEvents.getEvents(enrollRec.getUser().getUserUid())));
		}
		}
		catch (IdUnusedException e)
		{
			logger.error(e);
		}*/
		for (Iterator iter = workingEnrollments.iterator(); iter.hasNext(); ) {
			EnrollSectionRecord enrollment = (EnrollSectionRecord)iter.next();
			scoreRows.add(new ScoreRow(enrollment, allEvents.getEvents(enrollment.getEnrollRec().getUser().getUserUid())));
		}
	}

    public Gradebook getGradebook()
	{
		return super.getGradebook();
	}

    public CourseGrade getCourseGrade() {
        return courseGrade;
    }
    public GradeRecordSet getGradeRecordSet() {
        return gradeRecordSet;
    }
    public double getTotalPoints() {
        return totalPoints;
    }
	/**
	 * Action listener to update grades.
	 * NOTE: No transient fields are available yet.
	 */
	public void processUpdateGrades(ActionEvent event) {
		try {
			saveGrades();
		} catch (StaleObjectModificationException e) {
            logger.error(e);
            FacesUtil.addErrorMessage(getLocalizedString("course_grade_details_locking_failure"));
		}
	}

	private void saveGrades() throws StaleObjectModificationException {
		getGradebookManager().updateCourseGradeRecords(gradeRecordSet);
		getGradebookManager().updateGradebook(gradebook);
		// Let the user know.
		FacesUtil.addMessage(getLocalizedString("course_grade_details_grades_saved"));
	}

	private ScoreRow getScoreRow(UIComponent component) {
		UIData gradingTable = (UIData)component.findComponent("gradingTable");
		return (ScoreRow)gradingTable.getRowData();
	}

	public List getScoreRows() {
		return scoreRows;
	}
	public void setScoreRows(List scoreRows) {
		this.scoreRows = scoreRows;
	}

	public String getEventsLogType() {
		return FacesUtil.getLocalizedString("course_grade_details_log_type");
	}

    // Sorting
    public boolean isSortAscending() {
        return getPreferencesBean().isCourseGradeDetailsTableSortAscending();
    }
    public void setSortAscending(boolean sortAscending) {
        getPreferencesBean().setCourseGradeDetailsTableSortAscending(sortAscending);
    }
    public String getSortColumn() {
        return getPreferencesBean().getCourseGradeDetailsTableSortColumn();
    }
    public void setSortColumn(String sortColumn) {
        getPreferencesBean().setCourseGradeDetailsTableSortColumn(sortColumn);
    }

    /**
	 * @return Returns the todateGradeReleased
	 */
	public boolean isTodateGradeReleased() {
		return todateGradeReleased;
	}

	/**
	 * @return Returns the todatePointsReleased
	 */
	public boolean isTodatePointsReleased()
	{
		return todatePointsReleased;
	}
	
	 public boolean isDropGradeDisplayed()
		{
			return dropGradeDisplayed;
		}

		public void setDropGradeDisplayed(boolean dropGradeDisplayed)
		{
			this.dropGradeDisplayed = dropGradeDisplayed;
		}


}



