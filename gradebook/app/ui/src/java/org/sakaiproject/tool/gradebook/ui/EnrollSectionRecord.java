/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/gradebook/app/ui/src/java/org/sakaiproject/tool/gradebook/ui/EnrollSectionRecord.java $
 * $Id: EnrollSectionRecord.java 7492 2014-02-24 21:55:22Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2013, 2014 Etudes, Inc.
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
package org.sakaiproject.tool.gradebook.ui;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.sakaiproject.authz.api.Member;
import org.sakaiproject.site.api.Group;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;

public class EnrollSectionRecord implements Serializable {
	private static final Log logger = LogFactory.getLog(EnrollSectionRecord.class);

	private EnrollmentRecord enrollRec;
	public EnrollmentRecord getEnrollRec()
	{
		return enrollRec;
	}

	public void setEnrollRec(EnrollmentRecord enrollRec)
	{
		this.enrollRec = enrollRec;
	}

	private String sectionTitle;

	public String getSectionTitle()
	{
		return sectionTitle;
	}

	public void setSectionTitle(String sectionTitle)
	{
		this.sectionTitle = sectionTitle;
	}

	void setSection(EnrollmentRecord enrollRec, List<Group> siteGroups)
	{
		this.enrollRec = enrollRec;
		for (Group g : siteGroups)
		{
		Set<Member> groupMembers = g.getMembers();
		for (Member gm : groupMembers)
		{
			if (gm.isActive() && gm.getUserId().equals(enrollRec.getUser().getUserUid()))
			{
				this.sectionTitle = g.getTitle();
				break;
			}
		}
		}
	}
}