/**********************************************************************************
*
* $Id: CourseGrade.java 5793 2013-09-02 18:59:50Z mallikamt $
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

package org.sakaiproject.tool.gradebook;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A CourseGrade is a GradableObject that represents the overall course grade
 * in a gradebook.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 */
public class CourseGrade extends GradableObject {
    private static final Log log = LogFactory.getLog(CourseGrade.class);

    private Double totalPoints;  // Not persisted

    public static final String COURSE_GRADE_NAME = "Course Grade";

    public static String SORT_BY_OVERRIDE_GRADE = "override";
    public static String SORT_BY_CALCULATED_GRADE = "autoCalc";
    public static String SORT_BY_CUMULATIVE_GRADE = "cumuCalc";
    public static String SORT_BY_DROPCALC_GRADE = "autoDropCalc";
    public static String SORT_BY_DROPCUMU_GRADE = "dropCumuCalc";
    public static String SORT_BY_POINTS_EARNED = "pointsEarned";
    public static String SORT_BY_POINTS_DROPPED = "pointsDropped";

    public CourseGrade() {
    	setName(COURSE_GRADE_NAME);
    }

    /**
     * @see org.sakaiproject.tool.gradebook.GradableObject#isCourseGrade()
     */
    public boolean isCourseGrade() {
        return true;
    }

    /**
     * The course grade can not return the total number of points possible until
     * this value is set explicitly.  This should be done while gathering all of
     * the assignments from the database and calling calculateTotalPoints(Collection assignments).
     *
     * TODO Try to figure out a better approach
     *
     * @see org.sakaiproject.tool.gradebook.GradableObject#getPointsForDisplay()
     */
    public Double getPointsForDisplay() {
        if(totalPoints == null) {
			throw new IllegalStateException("Total points is null.  Make sure " +
                    "that total points has been calculated prior to display.");
        }
        return totalPoints;
    }

    /**
     * A CourseGrade is never "due", so it will always return null.
     *
	 * @see org.sakaiproject.tool.gradebook.GradableObject#getDateForDisplay()
	 */
	public Date getDateForDisplay() {
        return null;
	}

    /**
     * Calculates the total points possible based on a collection of assignments
     *
     * Note:  This calculation assumes that the collection of assignments passed
     * is an accurate collection of the assignments in this course grade's gradebook.
     *
     * @param assignments The assignments in this gradebook
     */
    public void calculateTotalPointsPossible(Collection assignments) {
        double totalPoints = 0;
        for(Iterator asnIter = assignments.iterator(); asnIter.hasNext();) {
            GradableObject go = (GradableObject)asnIter.next();
            if(go.isCourseGrade()) {
                continue;
            }
            Assignment asn = (Assignment)go;
            if (asn.isNotCounted()) {
            	continue;
            }
            totalPoints+=asn.getPointsPossible().doubleValue();
        }
        this.totalPoints = new Double(totalPoints);
    }

    //// Bean getters and setters ////

    /**
     * @return Returns the totalPoints.
     */
    public Double getTotalPoints() {
        return totalPoints;
    }
    /**
     * @param totalPoints The totalPoints to set.
     */
    public void setTotalPoints(Double totalPoints) {
        this.totalPoints = totalPoints;
    }

	/**
     * Calculate the mean for all enrollments, counting null grades as zero.
     *
	 * @see org.sakaiproject.tool.gradebook.GradableObject#calculateMean(java.util.Collection, int)
	 */
	protected Double calculateMean(Collection grades, int numEnrollments) {
		for (int i = 0; i < (numEnrollments - grades.size()); i++) {
			grades.add(new Double(0));
		}

        if (grades == null || grades.size() == 0) {
			return null;
		}

		double total = 0;
		for (Iterator iter = grades.iterator(); iter.hasNext();) {
			Double grade = (Double) iter.next();
			if (grade == null) {
                grade = new Double(0);
			}
			total += grade.doubleValue();
		}
		return new Double(total / grades.size());
	}
}



