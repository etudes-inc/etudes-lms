/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-app/src/test/org/sakaiproject/test/section/SectionsTestBase.java $
 * $Id: SectionsTestBase.java 3 2008-10-20 18:44:42Z ggolden $
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

import org.springframework.test.AbstractTransactionalSpringContextTests;

public class SectionsTestBase extends AbstractTransactionalSpringContextTests {
    protected String[] getConfigLocations() {
        String[] configLocations = {
			"org/sakaiproject/component/section/spring-beans.xml",
			"org/sakaiproject/component/section/spring-db.xml",
			"org/sakaiproject/component/section/support/spring-hib-test.xml",
			"org/sakaiproject/component/section/support/spring-services-test.xml",
			"org/sakaiproject/component/section/support/spring-integrationSupport.xml"
        };
        return configLocations;
    }

}




