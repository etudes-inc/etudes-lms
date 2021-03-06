/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-app/src/java/org/sakaiproject/tool/section/decorator/CourseSectionDecorator.java $
 * $Id: CourseSectionDecorator.java 3 2008-10-20 18:44:42Z ggolden $
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
package org.sakaiproject.tool.section.decorator;

import java.io.Serializable;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.sakaiproject.api.section.coursemanagement.Course;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Decorates CourseSections for display in the UI.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class CourseSectionDecorator implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected CourseSection section;
	protected String categoryForDisplay;

	/**
	 * Creates a decorator based on the course section alone.  Useful for only
	 * displaying meeting times.
	 * 
	 * @param section
	 */public CourseSectionDecorator(CourseSection section) {
		this.section = section;
	}

	 public CourseSectionDecorator(CourseSection section, String categoryForDisplay) {
		this.section = section;
		this.categoryForDisplay = categoryForDisplay;
	}

	public CourseSectionDecorator() {
		// Needed for serialization
	}

	// TODO Added for debugging. Should be more efficient to make section transient,

	// and store and retrieve a section UID to keep track of which section goes
	// with which row.
	public CourseSection getSection() {
		return section;
	}

	// Decorator methods
	public String getCategoryForDisplay() {
		return categoryForDisplay;
	}

	public String getMeetingDays() {
		String daySepChar = JsfUtil.getLocalizedMessage("day_of_week_sep_char");

		StringBuffer sb = new StringBuffer();
		for(Iterator iter = getDayList().iterator(); iter.hasNext();) {
			String day = (String)iter.next();
			sb.append(JsfUtil.getLocalizedMessage(day));
			if(iter.hasNext()) {
				sb.append(daySepChar);
			}
		}
		return sb.toString();
	}

	public String getMeetingTimes() {
		String daySepChar = JsfUtil.getLocalizedMessage("day_of_week_sep_char");
		String timeSepChar = JsfUtil.getLocalizedMessage("time_sep_char");

		StringBuffer sb = new StringBuffer();

		// Days of the week
		for(Iterator iter = getAbbreviatedDayList().iterator(); iter.hasNext();) {
			String day = (String)iter.next();
			sb.append(JsfUtil.getLocalizedMessage(day));
			if(iter.hasNext()) {
				sb.append(daySepChar);
			}
		}

		// Start time
		DateFormat df = new SimpleDateFormat("h:mm a");
		sb.append(" ");
		if(section.getStartTime() != null) {
			sb.append(df.format(new Date(section.getStartTime().getTime())).toLowerCase());
		}

		// End time
		if(section.getStartTime() != null &&
				section.getEndTime() != null) {
			sb.append(timeSepChar);
		}

		if(section.getEndTime() != null) {
			sb.append(df.format(new Date(section.getEndTime().getTime())).toLowerCase());
		}

		return sb.toString();
	}

	private List getAbbreviatedDayList() {
		List list = new ArrayList();
		if(section.isMonday())
			list.add("day_of_week_monday_abbrev");
		if(section.isTuesday())
			list.add("day_of_week_tuesday_abbrev");
		if(section.isWednesday())
			list.add("day_of_week_wednesday_abbrev");
		if(section.isThursday())
			list.add("day_of_week_thursday_abbrev");
		if(section.isFriday())
			list.add("day_of_week_friday_abbrev");
		if(section.isSaturday())
			list.add("day_of_week_saturday_abbrev");
		if(section.isSunday())
			list.add("day_of_week_sunday_abbrev");
		return list;
	}

	private List getDayList() {
		List list = new ArrayList();
		if(section.isMonday())
			list.add("day_of_week_monday");
		if(section.isTuesday())
			list.add("day_of_week_tuesday");
		if(section.isWednesday())
			list.add("day_of_week_wednesday");
		if(section.isThursday())
			list.add("day_of_week_thursday");
		if(section.isFriday())
			list.add("day_of_week_friday");
		if(section.isSaturday())
			list.add("day_of_week_saturday");
		if(section.isSunday())
			list.add("day_of_week_sunday");
		return list;
	}

	// Delegate methods

	public String getCategory() {
		return section.getCategory();
	}

	public Course getCourse() {
		return section.getCourse();
	}

	public Time getEndTime() {
		return section.getEndTime();
	}

	public String getLocation() {
		return section.getLocation();
	}

	public Integer getMaxEnrollments() {
		return section.getMaxEnrollments();
	}

	public Time getStartTime() {
		return section.getStartTime();
	}

	public String getTitle() {
		return section.getTitle();
	}

	public String getUuid() {
		return section.getUuid();
	}

	public boolean isFriday() {
		return section.isFriday();
	}

	public boolean isMonday() {
		return section.isMonday();
	}

	public boolean isSaturday() {
		return section.isSaturday();
	}

	public boolean isSunday() {
		return section.isSunday();
	}

	public boolean isThursday() {
		return section.isThursday();
	}

	public boolean isTuesday() {
		return section.isTuesday();
	}

	public boolean isWednesday() {
		return section.isWednesday();
	}
}
