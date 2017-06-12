/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/site-manage/site-manage-api/api/src/java/org/sakaiproject/site/api/ToolDateRange.java $
 * $Id: ToolDateRange.java 4863 2013-05-16 18:03:43Z mallikamt $
 ***********************************************************************************
 *
 * Copyright (c) 2011, 2012 Etudes, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.site.api;

import java.util.Date;

/**
 * <p>
 * ToolDateRange stores tool name, current date range(<minimum start date>-<maximum start date>), adjusted date range(date range when base date changes are applied), hightlight status
 * </p>
 */
public class ToolDateRange
{
	/** The null/empty string */
	private static final String NULL_STRING = "";
	public String adjDateRange = NULL_STRING;
	public String currentDateRange = NULL_STRING;
	public Date maxDate;
	public Date minDate;
	public boolean outsideRangeFlag = false;
	public String toolName = NULL_STRING;

	public String getAdjDateRange()
	{
		return adjDateRange;
	}

	public String getCurrentDateRange()
	{
		return currentDateRange;
	}

	public Date getMaxDate()
	{
		return maxDate;
	}

	public Date getMinDate()
	{
		return minDate;
	}

	public boolean getOutsideRangeFlag()
	{
		return outsideRangeFlag;
	}

	public String getToolName()
	{
		return toolName;
	}
}
