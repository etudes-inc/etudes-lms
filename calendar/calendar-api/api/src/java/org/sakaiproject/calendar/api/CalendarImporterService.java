/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/calendar/calendar-api/api/src/java/org/sakaiproject/calendar/api/CalendarImporterService.java $
 * $Id: CalendarImporterService.java 3 2008-10-20 18:44:42Z ggolden $
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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.sakaiproject.exception.ImportException;

/**
 * Interface for importing various calendar exports into Sakai.
 */
public interface CalendarImporterService
{
	/** Comma separated value import type */
	public static final String CSV_IMPORT = "CSV";
	
	/** MeetingMaker import type */
	public static final String MEETINGMAKER_IMPORT = "MeetingMaker";
	
	/** Outlook import type */
	public static final String OUTLOOK_IMPORT = "Outlook";
	
	/**
	 * Get the default column mapping (keys are column headers, values are property names).
	 * @param importType Type such as Outlook, MeetingMaker, etc. defined in the CalendarImporterService interface.
	 * @throws ImportException
	 */
	public Map getDefaultColumnMap(String importType)  throws ImportException;
	
	/**
	 * Perform an import given the import type.
	 * @param importType Type such as Outlook, MeetingMaker, etc. defined in the CalendarImporterService interface.
	 * @param importStream Stream of data to be imported
	 * @param columnMapping Map of column headers (keys) to property names (values)
	 * @param customFieldPropertyNames Array of custom properties that we want to import.  null if there are no custom properties.
	 * @return A list of CalendarEvent objects.  These objects are not "real", so their copies
	 * must be copied into CalendarEvents created by the Calendar service.
	 * @throws ImportException
	 */
	public List doImport(String importType, InputStream importStream, Map columnMapping, String[] customFieldPropertyNames)
		throws ImportException;

}
