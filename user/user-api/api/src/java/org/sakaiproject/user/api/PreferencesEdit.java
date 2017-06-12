/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/user/user-api/api/src/java/org/sakaiproject/user/api/PreferencesEdit.java $
 * $Id: PreferencesEdit.java 3 2008-10-20 18:44:42Z ggolden $
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

package org.sakaiproject.user.api;

import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;

/**
 * <p>
 * PreferencesEdit is a mutable Preferences.
 * </p>
 */
public interface PreferencesEdit extends Preferences, Edit
{
	/**
	 * Access the properties keyed by the specified value. If the key does not yet exist, create it.
	 * 
	 * @param key
	 *        The key to the properties.
	 * @return The properties keyed by the specified value (possibly empty)
	 */
	ResourcePropertiesEdit getPropertiesEdit(String key);
}
