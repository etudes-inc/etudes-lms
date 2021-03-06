/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-app/src/test/org/sakaiproject/test/section/SectionsTestSuite.java $
 * $Id: SectionsTestSuite.java 3 2008-10-20 18:44:42Z ggolden $
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SectionsTestSuite extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(AuthzTest.class);
		suite.addTestSuite(CourseManagerTest.class);
		suite.addTestSuite(SectionAwarenessTest.class);
		suite.addTestSuite(SectionManagerTest.class);
		suite.addTestSuite(SectionSortTest.class);
		suite.addTestSuite(TimeConversionTest.class);
		return suite;
	}
}
