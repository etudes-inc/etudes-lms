/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-api/src/java/org/sakaiproject/api/section/coursemanagement/ParticipationRecord.java $
 * $Id: ParticipationRecord.java 3 2008-10-20 18:44:42Z ggolden $
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
package org.sakaiproject.api.section.coursemanagement;

import org.sakaiproject.api.section.facade.Role;

/**
 * Associates a user with a Learning Context.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public interface ParticipationRecord {
	/**
	 * Gets the user.
	 * 
	 * @return
	 */
	public User getUser();
	
	/**
	 * Gets the Role for this user in this LearningContext.
	 * 
	 * @return
	 */
	public Role getRole();
	
	/**
	 * Gets the LearningContext.
	 * 
	 * @return
	 */
	public LearningContext getLearningContext();
}
