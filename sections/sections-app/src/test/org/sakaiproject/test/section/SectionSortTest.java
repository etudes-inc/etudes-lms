/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-app/src/test/org/sakaiproject/test/section/SectionSortTest.java $
 * $Id: SectionSortTest.java 3 2008-10-20 18:44:42Z ggolden $
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

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.component.section.CourseImpl;
import org.sakaiproject.component.section.CourseSectionImpl;
import org.sakaiproject.tool.section.decorator.InstructorSectionDecorator;

public class SectionSortTest extends TestCase {
	private CourseSection sectionA;
	private CourseSection sectionB;
	
	private CourseSection sectionC;
	private CourseSection sectionD;
	
	private List instructorsA;
	private List instructorsB;
	
	private List categoryNames;
	private List categoryIds;
	
	protected void setUp() throws Exception {
		categoryNames = new ArrayList();
		categoryNames.add("Category A");
		categoryNames.add("Category B");
		
		categoryIds = new ArrayList();
		categoryIds.add("a category");
		categoryIds.add("b category");

		CourseImpl course = new CourseImpl();
		course.setUuid("course 1 uuid");
		course.setTitle("course 1 title");
		
		Calendar startCal = new GregorianCalendar();
		startCal.set(Calendar.HOUR_OF_DAY, 8);

		Calendar endCal = new GregorianCalendar();
		endCal.set(Calendar.HOUR_OF_DAY, 9);
		
		sectionA = new CourseSectionImpl(course, "a section",
				"a section uuid", "a category", new Integer(10), "a section location",
				new Time(startCal.getTimeInMillis()), new Time(endCal.getTimeInMillis()),
				false, false, false, false, false, false, false);

		sectionB = new CourseSectionImpl(course, "B section",
				"b section uuid", "a category", new Integer(20), "b section location",
				new Time(startCal.getTimeInMillis()), new Time(endCal.getTimeInMillis()),
				false, false, false, false, false, false, false);

		sectionC = new CourseSectionImpl(course, "c section",
				"c section uuid", "b category", new Integer(5), "c section location",
				new Time(startCal.getTimeInMillis()), new Time(endCal.getTimeInMillis()),
				false, false, false, false, false, false, false);
		
		startCal.set(Calendar.HOUR_OF_DAY, 9);
		endCal.set(Calendar.HOUR_OF_DAY, 10);

		sectionD = new CourseSectionImpl(course, "D section",
				"d section uuid", "b category", new Integer(15), "d section location",
				new Time(startCal.getTimeInMillis()), new Time(endCal.getTimeInMillis()),
				false, false, false, false, false, false, false);
		
		instructorsA = new ArrayList();
		instructorsA.add("Schmoe, Joe");
		instructorsA.add("Adams, Sally");
	
		instructorsB = new ArrayList();
		instructorsA.add("Schmoe, Joe");
	}

	
	public void testInstructorSectionDecoratorSorting() throws Exception {
		InstructorSectionDecorator secA = new InstructorSectionDecorator(sectionA, "Category A", instructorsA, 10);
		InstructorSectionDecorator secB = new InstructorSectionDecorator(sectionB, "Category A", instructorsB, 20);
		InstructorSectionDecorator secC = new InstructorSectionDecorator(sectionC, "Category B", new ArrayList(), 10);
		InstructorSectionDecorator secD = new InstructorSectionDecorator(sectionD, "Category B", new ArrayList(), 20);
		
		Comparator comp = InstructorSectionDecorator.getManagersComparator(true);

		// Compare managers in sections of the same category
		Assert.assertTrue(comp.compare(secA, secB) > 0);
		Assert.assertTrue(comp.compare(secC, secD) < 0); // Using the title, since managers are equal
		
		// Compare managers in sections in different categories.  The one with no managers sorts first
		Assert.assertTrue(comp.compare(secC, secA) > 0);
		
		comp = InstructorSectionDecorator.getEnrollmentsComparator(true, false);

		// Compare the max enrollments in sections of the same category
		Assert.assertTrue(comp.compare(secB, secA) > 0);

		// Compare the max enrollments in different categories
		Assert.assertTrue(comp.compare(secB, secC) < 0);
		
		comp = InstructorSectionDecorator.getFieldComparator("title", true);
		Assert.assertTrue(comp.compare(secA, secB) < 0);
		
	}
}
