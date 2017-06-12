/**********************************************************************************
*
* $Id: CourseGradeRecord.java 10039 2015-02-06 01:29:15Z mallikamt $
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
import java.util.Comparator;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A CourseGradeRecord is a grade record that can be associated with a CourseGrade.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class CourseGradeRecord extends AbstractGradeRecord {
    private String enteredGrade;
    private Double sortGrade; // Persisted for sorting purposes
    private Double autoCalculatedGrade;  // Not persisted
    private Double autoGradeWithDrop; //Not persisted
   
    private Double cumulativeGrade;  // Not persisted
    private Double droppedCmlGrade; //Not persisted
   
    private Double totalPoints; //Not persisted
    private Double totalGradedPoints; //Not persisted
    private Double droppedEarnedTotal; //Not persisted
	private Double droppedPointsTotal; //Not persisted
	private Double droppedItemPoints; //Denominator points of item that was dropped
   
	
	private static final Log log = LogFactory.getLog(CourseGradeRecord.class);
    public static Comparator calcComparator;

    static {
        calcComparator = new Comparator() {
            public int compare(Object o1, Object o2) {
                CourseGradeRecord cgr1 = (CourseGradeRecord)o1;
                CourseGradeRecord cgr2 = (CourseGradeRecord)o2;

                if(cgr1 == null && cgr2 == null) {
                    return 0;
                }
                if(cgr1 == null) {
                    return -1;
                }
                if(cgr2 == null) {
                    return 1;
                }
                return cgr1.getPointsEarned().compareTo(cgr2.getPointsEarned());
            }
        };
    }

    public static Comparator getOverrideComparator(final GradeMapping mapping) {
        return new Comparator() {
            public int compare(Object o1, Object o2) {
                CourseGradeRecord cgr1 = (CourseGradeRecord)o1;
                CourseGradeRecord cgr2 = (CourseGradeRecord)o2;

                if(cgr1 == null && cgr2 == null) {
                    return 0;
                }
                if(cgr1 == null) {
                    return -1;
                }
                if(cgr2 == null) {
                    return 1;
                }

                String enteredGrade1 = StringUtils.trimToEmpty(cgr1.getEnteredGrade());
                String enteredGrade2 = StringUtils.trimToEmpty(cgr2.getEnteredGrade());

                if(!mapping.getGrades().contains(enteredGrade1) && !mapping.getGrades().contains(enteredGrade2)) {
                    return 0; // neither of these are valid grades (they are probably empty strings)
                }
                if(!mapping.getGrades().contains(enteredGrade1)) {
                    return -1;
                }
                if(!mapping.getGrades().contains(enteredGrade2)) {
                    return 1;
                }
                return mapping.getValue(enteredGrade1).compareTo(mapping.getValue(enteredGrade2));
            }
        };

    }
    
    public static Comparator getCumuComparator(final GradeMapping mapping) {
        return new Comparator() {
            public int compare(Object o1, Object o2) {
                CourseGradeRecord cgr1 = (CourseGradeRecord)o1;
                CourseGradeRecord cgr2 = (CourseGradeRecord)o2;

                if(cgr1 == null && cgr2 == null) {
                    return 0;
                }
                if(cgr1 == null) {
                    return -1;
                }
                if(cgr2 == null) {
                    return 1;
                }

                return cgr1.getNonNullCumulativeGrade().compareTo(cgr2.getNonNullCumulativeGrade());
            }
        };

    }
    
    public static Comparator getDropComparator(final GradeMapping mapping) {
        return new Comparator() {
            public int compare(Object o1, Object o2) {
                CourseGradeRecord cgr1 = (CourseGradeRecord)o1;
                CourseGradeRecord cgr2 = (CourseGradeRecord)o2;

                if(cgr1 == null && cgr2 == null) {
                    return 0;
                }
                if(cgr1 == null) {
                    return -1;
                }
                if(cgr2 == null) {
                    return 1;
                }

                return cgr1.getNonNullDroppedEarnedTotal().compareTo(cgr2.getNonNullDroppedEarnedTotal());
            }
        };

    }
    
    public static Comparator getCumuDropComparator(final GradeMapping mapping) {
        return new Comparator() {
            public int compare(Object o1, Object o2) {
                CourseGradeRecord cgr1 = (CourseGradeRecord)o1;
                CourseGradeRecord cgr2 = (CourseGradeRecord)o2;

                if(cgr1 == null && cgr2 == null) {
                    return 0;
                }
                if(cgr1 == null) {
                    return -1;
                }
                if(cgr2 == null) {
                    return 1;
                }

                return cgr1.getNonNullCumulativeDropGrade().compareTo(cgr2.getNonNullCumulativeDropGrade());
            }
        };

    }
    
    public static Comparator getAutoDropComparator(final GradeMapping mapping) {
        return new Comparator() {
            public int compare(Object o1, Object o2) {
                CourseGradeRecord cgr1 = (CourseGradeRecord)o1;
                CourseGradeRecord cgr2 = (CourseGradeRecord)o2;

                if(cgr1 == null && cgr2 == null) {
                    return 0;
                }
                if(cgr1 == null) {
                    return -1;
                }
                if(cgr2 == null) {
                    return 1;
                }

                return cgr1.getNonNullAutoCalculatedDropGrade().compareTo(cgr2.getNonNullAutoCalculatedDropGrade());
            }
        };

    }
    
    /**
     * The graderId and dateRecorded properties will be set explicitly by the
     * grade manager before the database is updated.
	 * @param courseGrade
	 * @param studentId
	 * @param grade
	 */
	public CourseGradeRecord(CourseGrade courseGrade, String studentId, String grade)
	{
		this.gradableObject = courseGrade;
		this.studentId = studentId;
		this.enteredGrade = grade;
		this.sortGrade = courseGrade.getGradebook().getSelectedGradeMapping().getValue(grade);
	}

    /**
     * Default no-arg constructor
     */
    public CourseGradeRecord() {
        super();
    }

    /**
     * Calculates the total points earned for a set of grade records.
     *
     * @param gradeRecords The collection of all grade records for a student
     * that count toward the course grade
     */
    public void calculateTotalPointsEarned(Collection gradeRecords) {
        double total = 0;
        for(Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
            AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
            // Skip this if it doesn't have any points earned
            if(agr.getPointsEarned() == null) {
                continue;
            }

            total += agr.getPointsEarned().doubleValue();
        }
        pointsEarned = new Double(total);
    }


	/**
     * This method will fail unless this course grade was fetched "with statistics",
     * since it relies on having the total number of points possible available to
     * calculate the percentage.
     *
     * @see org.sakaiproject.tool.gradebook.AbstractGradeRecord#getGradeAsPercentage()
     */
    public Double getGradeAsPercentage() {
        if(enteredGrade == null) {
            return autoCalculatedGrade;
        } else {
            return getGradableObject().getGradebook().getSelectedGradeMapping().getValue(enteredGrade);
        }
    }

    /**
     * Convenience method to get the correctly cast CourseGrade that this
     * CourseGradeRecord references.
     *
     * @return CourseGrade referenced by this GradableObject
     */
    public CourseGrade getCourseGrade() {
    	return (CourseGrade)super.getGradableObject();
    }
    /**
	 * @return Returns the enteredGrade.
	 */
	public String getEnteredGrade() {
		return enteredGrade;
	}
	/**
	 * @param enteredGrade The enteredGrade to set.
	 */
	public void setEnteredGrade(String enteredGrade) {
		this.enteredGrade = enteredGrade;
	}
	/**
	 * @return Returns the autoCalculatedGrade.
	 */
	public Double getAutoCalculatedGrade() {
		return autoCalculatedGrade;
	}
	/**
	 * @param autoCalculatedGrade The autoCalculatedGrade to set.
	 */
	public void setAutoCalculatedGrade(Double autoCalculatedGrade) {
		this.autoCalculatedGrade = autoCalculatedGrade;
	}
	/**
	 * @return Returns the cumulativeGrade.
	 */
	public Double getCumulativeGrade() {
		return cumulativeGrade;
	}
	/**
	 * @param cumulativeGrade The cumulativeGrade to set.
	 */
	public void setCumulativeGrade(Double cumulativeGrade) {
		this.cumulativeGrade = cumulativeGrade;
	}
	/**
	 * @return Returns the sortGrade.
	 */
	public Double getSortGrade() {
		return sortGrade;
	}
	/**
	 * @param sortGrade The sortGrade to set.
	 */
	public void setSortGrade(Double sortGrade) {
		this.sortGrade = sortGrade;
	}
	
	/**
	 * @param totalPoints The totalPoints to set.
	 */
	public void setTotalPoints(Double totalPoints)
	{
		this.totalPoints = totalPoints;
	}
	
	/**
	 * @param totalGradedPoints The totalGradedPoints to set
	 */
	public void setTotalGradedPoints(Double totalGradedPoints)
	{
		this.totalGradedPoints = totalGradedPoints;
	}
	
    /**
	 * @return Returns the displayGrade.
	 */
	public String getDisplayGrade() {
        if(enteredGrade != null) {
            return enteredGrade;
        } else {
            CourseGrade cg = (CourseGrade)getGradableObject();
            return cg.getGradebook().getSelectedGradeMapping().getGrade(sortGrade);

        }
	}
	
	public String getDisplayDropGrade() {
        if(enteredGrade != null) {
            return enteredGrade;
        } else {
            CourseGrade cg = (CourseGrade)getGradableObject();
            autoGradeWithDrop = getAutoGradeWithDrop();
            if (autoGradeWithDrop == null) autoGradeWithDrop = new Double(0);
            return cg.getGradebook().getSelectedGradeMapping().getGrade(autoGradeWithDrop);

        }
	}
	
	public String getDisplayCmlGrade()
	{
		CourseGrade cg = (CourseGrade) getGradableObject();
		if (cumulativeGrade == null) return null;
		return cg.getGradebook().getSelectedGradeMapping().getGrade(cumulativeGrade);
	}
	
	public String getDisplayDropCmlGrade()
	{
		CourseGrade cg = (CourseGrade) getGradableObject();
		if (droppedCmlGrade == null) return null;
		return cg.getGradebook().getSelectedGradeMapping().getGrade(droppedCmlGrade);
	}
	
	 /**
	 * @return Returns the exportGrade.
	 */
	public String getExportGrade() {
        if(enteredGrade != null) {
            return enteredGrade;
        } else {
            CourseGrade cg = (CourseGrade)getGradableObject();
            return cg.getGradebook().getSelectedGradeMapping().getGrade(getNonNullAutoCalculatedGrade());

        }
	}


	/**
	 * @see org.sakaiproject.tool.gradebook.AbstractGradeRecord#isCourseGradeRecord()
	 */
	public boolean isCourseGradeRecord() {
		return true;
	}


    /**
     * Calculates the grade as a percentage for a course grade record.
     */
    public Double calculatePercent(double totalPointsPossible) {
        Double pointsEarned = getPointsEarned();
        if (log.isDebugEnabled()) log.debug("calculatePercent; totalPointsPossible=" + totalPointsPossible + ", pointsEarned=" + pointsEarned);
        if ((pointsEarned == null) || (totalPointsPossible == 0.0)) {
        	return null;
        } else {
        	return new Double(pointsEarned.doubleValue() / totalPointsPossible * 100);
        }
    }
    
    public Double calculateDropPercent() {
        Double droppedEarnedTotal = getDroppedEarnedTotal();
        Double droppedPointsTotal = getDroppedPointsTotal();
        if ((droppedEarnedTotal == null) || (droppedPointsTotal == 0.0)) {
        	return null;
        } else {
        	return new Double(droppedEarnedTotal.doubleValue() / droppedPointsTotal * 100);
        }
    }
    
    public Double calculateDropOverallPercent() {
        Double droppedEarnedTotal = getDroppedEarnedTotal();
        Double totalPoints = getTotalPoints();
        Double droppedItemPoints = getDroppedItemPoints();
        if ((droppedEarnedTotal == null) || (totalPoints == null) || (totalPoints == 0.0)) {
        	return null;
        } else {
        	if (droppedItemPoints == null) droppedItemPoints = new Double(0.0);
        	double denomValue = totalPoints.doubleValue() - droppedItemPoints.doubleValue();
        	return new Double(droppedEarnedTotal.doubleValue() / denomValue * 100);
        }
    }

	/**
	 * For use by the Course Grade UI.
	 */
	public Double getNonNullAutoCalculatedGrade() {
		Double percent = getAutoCalculatedGrade();
		if (percent == null) {
			percent = new Double(0);
		}
		return percent;
	}
	
	public Double getNonNullAutoCalculatedDropGrade() {
		Double percent = getAutoGradeWithDrop();
		if (percent == null) {
			percent = new Double(0);
		}
		return percent;
	}
	
	/**
	 * For use by the Course Grade UI.
	 */
	public Double getNonNullCumulativeGrade() {
		Double percent = getCumulativeGrade();
		if (percent == null) {
			percent = new Double(0);
		}
		return percent;
	}
	
	public Double getNonNullCumulativeDropGrade() {
		Double percent = getDroppedCmlGrade();
		if (percent == null) {
			percent = new Double(0);
		}
		return percent;
	}
	
	public Double getNonNullDroppedEarnedTotal() {
		Double percent = getDroppedEarnedTotal();
		if (percent == null) {
			percent = new Double(0);
		}
		return percent;
	}
	
	 /**
	 * @return Returns the total points
	 */
	public Double getTotalPoints()
	{
		return totalPoints;
	}
	
	/**
	 * @return Returns the total graded points
	 */
	public Double getTotalGradedPoints()
	{
		return totalGradedPoints;
	}
	
	public Double getDroppedEarnedTotal()
	{
		return droppedEarnedTotal;
	}

	public void setDroppedEarnedTotal(Double droppedEarnedTotal)
	{
		this.droppedEarnedTotal = droppedEarnedTotal;
	}

	public Double getDroppedPointsTotal()
	{
		return droppedPointsTotal;
	}

	public void setDroppedPointsTotal(Double droppedPointsTotal)
	{
		this.droppedPointsTotal = droppedPointsTotal;
	}
	
	public Double getDroppedItemPoints()
	{
		return droppedItemPoints;
	}

	public void setDroppedItemPoints(Double droppedItemPoints)
	{
		this.droppedItemPoints = droppedItemPoints;
	}
	
    public Double getDroppedCmlGrade()
	{
		return droppedCmlGrade;
	}

	public void setDroppedCmlGrade(Double droppedCmlGrade)
	{
		this.droppedCmlGrade = droppedCmlGrade;
	}

	public Double getAutoGradeWithDrop()
	{
		return calculateDropOverallPercent();
	}

}
