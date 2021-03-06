/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/calendar/calendar-api/api/src/java/org/sakaiproject/calendar/api/CalendarEdit.java $
 * $Id: CalendarEdit.java 3 2008-10-20 18:44:42Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.calendar.api;

import org.sakaiproject.entity.api.Edit;

/**
* <p>CalendarEdit is an editable Calendar.</p>
*/
public interface CalendarEdit
	extends Calendar, Edit
{
	/**
	* Set the extra fields kept for each event in this calendar.
	* @param fields The extra fields kept for each event in this calendar, formatted into a single string. %%%
	*/
	public void setEventFields(String fields);

}	// CalendarEdit



