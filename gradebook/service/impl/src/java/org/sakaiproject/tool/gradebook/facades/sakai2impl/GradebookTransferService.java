/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/gradebook/service/impl/src/java/org/sakaiproject/tool/gradebook/facades/sakai2impl/GradebookTransferService.java $ 
 * $Id: GradebookTransferService.java 9580 2014-12-18 03:31:53Z ggolden $ 
 *********************************************************************************** 
 * 
 * Copyright (c) 2008, 2009, 2010 Etudes, Inc. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 ***********************************************************************************/

package org.sakaiproject.tool.gradebook.facades.sakai2impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.mneme.api.Assessment;
import org.etudes.mneme.api.AssessmentPermissionException;
import org.etudes.mneme.api.AssessmentPolicyException;
import org.etudes.mneme.api.AssessmentService;
import org.etudes.mneme.api.AssessmentType;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.service.gradebook.shared.Assignment;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.site.api.SiteService;

public class GradebookTransferService extends BaseEntityProducer implements EntityTransferrer
{
	/** Dependency: a logger component. */
	private static final Log log = LogFactory.getLog(GradebookTransferService.class);

	private AssessmentService assessmentService;

	private GradebookService gradebookService;

	private SiteService siteService;

	private String[] toolIdArray;

	public String[] myToolIds()
	{
		return toolIdArray;
	}

	public void setAssessmentService(AssessmentService assessmentService)
	{
		this.assessmentService = assessmentService;
	}

	public void setGradebookService(GradebookService gradebookService)
	{
		this.gradebookService = gradebookService;
	}

	public void setSiteService(SiteService siteService)
	{
		this.siteService = siteService;
	}

	public void setToolIds(List toolIds)
	{
		if (log.isDebugEnabled()) log.debug("setToolIds(" + toolIds + ")");
		if (toolIds != null)
		{
			toolIdArray = (String[]) toolIds.toArray(new String[0]);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void transferCopyEntities(String fromContext, String toContext, List ids)
	{
		boolean fromContextGB = gradebookService.isGradebookDefined(fromContext);
		boolean toContextGB = gradebookService.isGradebookDefined(toContext);

		Long termId = this.siteService.getSiteTermId(toContext);

		if (fromContextGB && toContextGB)
		{
			gradebookService.transferGradebookSettings(fromContext, toContext);

			List fromContextAssignments = gradebookService.getAssignments(fromContext);

			for (Iterator iter = fromContextAssignments.iterator(); iter.hasNext();)
			{
				Assignment assignment = (Assignment) iter.next();

				if ((assignment.getExternalAppName() == null) && (assignment.getExternalId() == null))
				{
					if ((assignment.getName() != null) && (assignment.getName().trim().length() != 0))
					{
						// manually/directly added assignments to gradebook
						
						// for terms before or in F14 (7..42), import into the gradebook
						if ((termId != null) && (termId >= 7) && (termId <= 42))
						{
							if (!gradebookService.isAssignmentDefined(toContext, assignment.getName().trim()))
							{
								String name = assignment.getName().trim();
								double points = assignment.getPoints();
								Date dueDate = assignment.getDueDate();
								boolean isNotCounted = assignment.isNotCounted();
								boolean isReleased = assignment.isReleased();

								try
								{
									// create the assignment
									gradebookService.createAssignment(toContext, name, points, dueDate, isNotCounted, isReleased);
								}
								catch (Exception e)
								{
									if (log.isWarnEnabled())
									{
										log.warn(e.toString(), e);
									}
								}
							}
						}

						// merge into AT&S as an Offline
						else
						{
							try
							{
								String name = assignment.getName().trim();
								Float points = Float.valueOf(assignment.getPoints().floatValue());
								Date dueDate = assignment.getDueDate();

								Assessment assessment = this.assessmentService.newAssessment(toContext);
								assessment.setType(AssessmentType.offline);
								assessment.setTitle(name);
								assessment.setPoints(points);
								if (dueDate != null) assessment.getDates().setDueDate(dueDate);

								this.assessmentService.saveAssessment(assessment);
							}
							catch (AssessmentPermissionException e)
							{
								log.warn("transferCopyEntities: " + e.toString());
							}
							catch (AssessmentPolicyException e)
							{
								log.warn("transferCopyEntities: " + e.toString());
							}
						}
					}
				}
			}
		}
	}

	public void transferCopyEntities(String fromContext, String toContext, List ids, boolean cleanup)
	{
		// TODO: implement cleanup...
		transferCopyEntities(fromContext, toContext, ids);
	}
}
