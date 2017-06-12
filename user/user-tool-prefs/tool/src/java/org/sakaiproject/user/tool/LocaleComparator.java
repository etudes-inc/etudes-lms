/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/user/user-tool-prefs/tool/src/java/org/sakaiproject/user/tool/LocaleComparator.java $
 * $Id: LocaleComparator.java 3 2008-10-20 18:44:42Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.user.tool;

import java.util.Comparator;
import java.util.Locale;

/**
 * Comparator for sorting locale by DisplayName
 */
public final class LocaleComparator implements Comparator
{
	/**
	 * * Compares Locale objects by comparing the DisplayName * *
	 * 
	 * @param obj1
	 *        1st Locale Object for comparison *
	 * @param obj2
	 *        2nd Locale Object for comparison *
	 * @return negative, zero, or positive integer * (obj1 charge is less than, equal to, or greater than the obj2 charge)
	 */
	public int compare(Object obj1, Object obj2)
	{
		if (obj1 instanceof Locale && obj2 instanceof Locale)
		{
			Locale localeOne = (Locale) obj1;
			Locale localeTwo = (Locale) obj2;

			String displayNameOne = localeOne.getDisplayName();
			String displayNameTwo = localeTwo.getDisplayName();

			return displayNameOne.compareTo(displayNameTwo);
		}
		else
		{
			throw new ClassCastException("Inappropriate object class for LocaleComparator");
		}
	}

	/**
	 * * Override of equals method * *
	 * 
	 * @param obj
	 *        LocaleComparator object *
	 * @return true if equal, false if not equal
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof LocaleComparator)
			return super.equals(obj);
		else
			return false;
	}
}
