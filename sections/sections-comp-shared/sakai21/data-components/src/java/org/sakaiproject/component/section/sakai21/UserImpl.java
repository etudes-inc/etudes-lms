/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-comp-shared/sakai21/data-components/src/java/org/sakaiproject/component/section/sakai21/UserImpl.java $
 * $Id: UserImpl.java 3 2008-10-20 18:44:42Z ggolden $
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
package org.sakaiproject.component.section.sakai21;

import java.io.Serializable;

import org.sakaiproject.api.section.coursemanagement.User;

public class UserImpl implements User, Serializable {
	private static final long serialVersionUID = 1L;
	
	private String userUid;
	private String displayId;
	private String sortName;
	private String displayName;
	
	public UserImpl(String displayId, String displayName, String sortName, String uid) {
		this.displayId = displayId;
		this.displayName = displayName;
		this.sortName = sortName;
		this.userUid = uid;
	}

	public String getUserUid() {
		return userUid;
	}

	public String getSortName() {
		return sortName;
	}

	public String getDisplayId() {
		return displayId;
	}

	public String getDisplayName() {
		return displayName;
	}
}
