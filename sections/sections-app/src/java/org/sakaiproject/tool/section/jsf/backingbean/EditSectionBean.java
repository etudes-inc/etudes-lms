/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-app/src/java/org/sakaiproject/tool/section/jsf/backingbean/EditSectionBean.java $
 * $Id: EditSectionBean.java 3 2008-10-20 18:44:42Z ggolden $
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
package org.sakaiproject.tool.section.jsf.backingbean;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.tool.section.decorator.CourseSectionDecorator;
import org.sakaiproject.tool.section.jsf.JsfUtil;

/**
 * Controls the edit and delete sections pages.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class EditSectionBean extends CourseDependentBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private static final Log log = LogFactory.getLog(EditSectionBean.class);
	
	private String sectionUuid;
	private String title;
	private String location;
	private Integer maxEnrollments;
	private boolean monday;
	private boolean tuesday;
	private boolean wednesday;
	private boolean thursday;
	private boolean friday;
	private boolean saturday;
	private boolean sunday;
	
	private String startTime;
	private String endTime;
	private boolean startTimeAm;
	private boolean endTimeAm;
	
	/**
	 * @inheritDoc
	 */
	public void init() {
		if(sectionUuid == null || isNotValidated()) {
			String sectionUuidFromParam = JsfUtil.getStringFromParam("sectionUuid");
			if(sectionUuidFromParam != null) {
				sectionUuid = sectionUuidFromParam;
			}
			CourseSection section = getSectionManager().getSection(sectionUuid);
			SimpleDateFormat sdf = new SimpleDateFormat(JsfUtil.TIME_PATTERN_DISPLAY);
			
			title = section.getTitle();
			location = section.getLocation();
			maxEnrollments = section.getMaxEnrollments();
			monday = section.isMonday();
			tuesday = section.isTuesday();
			wednesday = section.isWednesday();
			thursday = section.isThursday();
			friday = section.isFriday();
			saturday = section.isSaturday();
			sunday = section.isSunday();
			if(section.getStartTime() != null) {
				startTime = sdf.format(section.getStartTime());
				Calendar cal = new GregorianCalendar();
				cal.setTime(section.getStartTime());
				startTimeAm = cal.get(Calendar.HOUR_OF_DAY) <= 11;
			}
			if(section.getEndTime() != null) {
				endTime = sdf.format(section.getEndTime());
				Calendar cal = new GregorianCalendar();
				cal.setTime(section.getEndTime());
				endTimeAm = cal.get(Calendar.HOUR_OF_DAY) <= 11;
			}
		}
	}

	/**
	 * Since the validation and conversion rules rely on the *relative*
	 * values of one component to another, we can't use JSF validators and
	 * converters.  So we check everything here.
	 * 
	 * @return
	 */
	private boolean validationFails() {
		boolean validationFailure = false;
		
		// We also need to keep track of whether an invalid time was entered,
		// so we can skip the time comparisons
		boolean invalidTimeEntered = false;

		// Ensure that this title isn't being used by another section
		if(isDuplicateSectionTitle()) {
			if(log.isDebugEnabled()) log.debug("Failed to update section... duplicate title: " + title);
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
					"section_update_failure_duplicate_title", new String[] {title}), "editSectionForm:titleInput");
			validationFailure = true;
		}
		
		if(JsfUtil.isInvalidTime(startTime)) {
			if(log.isDebugEnabled()) log.debug("Failed to update section... start time is invalid");
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
					"javax.faces.convert.DateTimeConverter.CONVERSION"), "editSectionForm:startTime");
			validationFailure = true;
			invalidTimeEntered = true;
		}
		
		if(JsfUtil.isInvalidTime(endTime)) {
			if(log.isDebugEnabled()) log.debug("Failed to update section... end time is invalid");
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
					"javax.faces.convert.DateTimeConverter.CONVERSION"), "editSectionForm:endTime");
			validationFailure = true;
			invalidTimeEntered = true;
		}

		if(JsfUtil.isEndTimeWithoutStartTime(startTime, endTime)) {
			if(log.isDebugEnabled()) log.debug("Failed to update section... start time without end time");
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
					"section_update_failure_end_without_start"), "editSectionForm:startTime");
			validationFailure = true;
		}
		
		if(isInvalidMaxEnrollments()) {
			if(log.isDebugEnabled()) log.debug("Failed to update section... max enrollments is not valid");
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
					"javax.faces.validator.LongRangeValidator.MINIMUM", new String[] {"0"}),
					"editSectionForm:maxEnrollmentInput");
			validationFailure = true;
		}

		if(!invalidTimeEntered && JsfUtil.isEndTimeBeforeStartTime(startTime, startTimeAm, endTime, endTimeAm)) {
			if(log.isDebugEnabled()) log.debug("Failed to update section... end time is before start time");
			JsfUtil.addErrorMessage(JsfUtil.getLocalizedMessage(
					"section_update_failure_end_before_start"), "editSectionForm:endTime");
			validationFailure = true;
		}
		return validationFailure;
	}

	/**
	 * Updates the section in persistence.
	 * 
	 * @return
	 */
	public String update() {
		if(log.isInfoEnabled()) log.info("Updating section " + sectionUuid);
		
		if(validationFails()) {
			return null;
		}
		
		// Perform the update
		getSectionManager().updateSection(sectionUuid, title, maxEnrollments,
				location, JsfUtil.convertStringToTime(startTime, startTimeAm),
				JsfUtil.convertStringToTime(endTime, endTimeAm), monday, tuesday,
				wednesday, thursday, friday, saturday, sunday);
		
		// Add a success message
		JsfUtil.addRedirectSafeInfoMessage(JsfUtil.getLocalizedMessage(
				"section_update_successful", new String[] {title}));
		
		// Add a warning if max enrollments has been exceeded
		CourseSection section = getSectionManager().getSection(sectionUuid);
		Integer maxEnrollments = section.getMaxEnrollments();
		int totalEnrollments = getSectionManager().getTotalEnrollments(section.getUuid());
		if(maxEnrollments != null && totalEnrollments > maxEnrollments.intValue()) {
			JsfUtil.addRedirectSafeWarnMessage(JsfUtil.getLocalizedMessage(
					"edit_student_over_max_warning", new String[] {
							section.getTitle(),
							Integer.toString(totalEnrollments),
							Integer.toString(totalEnrollments - maxEnrollments.intValue()) }));
		}
		return "overview";
	}
		
	/**
	 * Returns true if the title is a duplicate of another section.
	 * 
	 * @return
	 */
	private boolean isDuplicateSectionTitle() {
		for(Iterator iter = getAllSiteSections().iterator(); iter.hasNext();) {
			CourseSection section = (CourseSection)iter.next();
			// Skip this section, since it is OK for it to keep the same title
			if(section.getUuid().equals(sectionUuid)) {
				continue;
			}
			if(section.getTitle().equals(title)) {
				if(log.isDebugEnabled()) log.debug("Conflicting section name found.");
				return true;
			}
		}
		return false;
	}

	private boolean isInvalidMaxEnrollments() {
		return maxEnrollments != null && maxEnrollments.intValue() < 0;
	}
	
	public String getDays() {
		CourseSection section = getSectionManager().getSection(sectionUuid);
		CourseSectionDecorator decorator = new CourseSectionDecorator(section, null);
		return decorator.getMeetingDays();
	}
	
	public String getSectionUuid() {
		return sectionUuid;
	}
	public void setSectionUuid(String sectionUuid) {
		this.sectionUuid = sectionUuid;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	// Must use a string due to http://issues.apache.org/jira/browse/MYFACES-570
	public String getEndTimeAm() {
		return Boolean.toString(endTimeAm);
	}

	// Must use a string due to http://issues.apache.org/jira/browse/MYFACES-570
	public void setEndTimeAm(String endTimeAm) {
		this.endTimeAm = Boolean.valueOf(endTimeAm).booleanValue();
	}

	public boolean isFriday() {
		return friday;
	}

	public void setFriday(boolean friday) {
		this.friday = friday;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Integer getMaxEnrollments() {
		return maxEnrollments;
	}

	public void setMaxEnrollments(Integer maxEnrollments) {
		this.maxEnrollments = maxEnrollments;
	}

	public boolean isMonday() {
		return monday;
	}

	public void setMonday(boolean monday) {
		this.monday = monday;
	}

	public boolean isSaturday() {
		return saturday;
	}

	public void setSaturday(boolean saturday) {
		this.saturday = saturday;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	// Must use a string due to http://issues.apache.org/jira/browse/MYFACES-570
	public String getStartTimeAm() {
		return Boolean.toString(startTimeAm);
	}

	// Must use a string due to http://issues.apache.org/jira/browse/MYFACES-570
	public void setStartTimeAm(String startTimeAm) {
		this.startTimeAm = Boolean.valueOf(startTimeAm).booleanValue();
	}

	public boolean isSunday() {
		return sunday;
	}

	public void setSunday(boolean sunday) {
		this.sunday = sunday;
	}

	public boolean isThursday() {
		return thursday;
	}

	public void setThursday(boolean thursday) {
		this.thursday = thursday;
	}

	public boolean isTuesday() {
		return tuesday;
	}

	public void setTuesday(boolean tuesday) {
		this.tuesday = tuesday;
	}

	public boolean isWednesday() {
		return wednesday;
	}

	public void setWednesday(boolean wednesday) {
		this.wednesday = wednesday;
	}
}
