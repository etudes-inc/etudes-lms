/**********************************************************************************
*
* $Id: GradebookBean.java 8043 2014-05-29 22:06:12Z ggolden $
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.ui;

import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.api.section.SectionAwareness;

import org.sakaiproject.service.gradebook.shared.GradebookNotFoundException;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.ContextManagement;
import org.sakaiproject.tool.gradebook.facades.UserDirectoryService;

/**
 * Provide a UI handle to the selected gradebook.
 *
 * Since all application-specific backing beans (a group that doesn't include
 * authentication handlers) require a gradebook ID to use with any of the
 * business or facade services, this bean is also a reasonable place to centralize
 * configuration of and access to those services.
 */
public class GradebookBean extends InitializableBean {
	private static final Log logger = LogFactory.getLog(GradebookBean.class);

	private Long gradebookId;
	private String gradebookUid;


	// These interfaces are defined application-wide (through Spring, although the
	// UI classes don't know that).
	private GradebookManager gradebookManager;
	private SectionAwareness sectionAwareness;
	private UserDirectoryService userDirectoryService;
	private Authn authnService;
	private Authz authzService;
	private ContextManagement contextManagementService;

	/**
	 * @return Returns the gradebookId.
	 */
	public final Long getGradebookId() {
		refreshFromRequest();
		return gradebookId;
	}

	private final void setGradebookId(Long gradebookId) {
		this.gradebookId = gradebookId;
	}

	/**
	 * @param newGradebookUid The gradebookId to set.
	 * Since this is coming from the client, the application should NOT
	 * trust that the current user actually has access to the gradebook
	 * with this UID. This design assumes that authorization will come
	 * into play on each request.
	 */
	public final void setGradebookUid(String newGradebookUid) {
		Long newGradebookId = null;
		if (newGradebookUid != null) {
            Gradebook gradebook = null;
            try {
                gradebook = getGradebookManager().getGradebook(newGradebookUid);
            } catch (GradebookNotFoundException gnfe) {
                logger.error("Request made for inaccessible gradebookUid=" + newGradebookUid);
                newGradebookUid = null;
            }
			newGradebookId = gradebook.getId();
		//	if (logger.isInfoEnabled()) logger.info("setGradebookUid gradebookUid=" + newGradebookUid + ", gradebookId=" + newGradebookId);
		}
		this.gradebookUid = newGradebookUid;
		setGradebookId(newGradebookId);
	}

	private final void refreshFromRequest() {
		String requestUid = contextManagementService.getGradebookUid(FacesContext.getCurrentInstance().getExternalContext().getRequest());
		if ((requestUid != null) && (!requestUid.equals(gradebookUid))) {
			// if (logger.isInfoEnabled()) logger.info("resetting gradebookUid from " + gradebookUid);
			setGradebookUid(requestUid);
		}
	}

	/**
	 * Static method to pick up the gradebook UID, if any, held by the current GradebookBean, if any.
	 * Meant to be called from a servlet filter.
	 */
	public static String getGradebookUidFromRequest(ServletRequest request) {
		String gradebookUid = null;
		HttpSession session = ((HttpServletRequest)request).getSession();
		GradebookBean gradebookBean = (GradebookBean)session.getAttribute("gradebookBean");
		if (gradebookBean != null) {
			gradebookUid = gradebookBean.gradebookUid;
		}
		return gradebookUid;
	}


	// The following getters are used by other backing beans. The setters are used only by
	// the bean factory.

	/**
	 * @return Returns the gradebookManager.
	 */
	public GradebookManager getGradebookManager() {
		return gradebookManager;
	}

	/**
	 * @param gradebookManager The gradebookManager to set.
	 */
	public void setGradebookManager(GradebookManager gradebookManager) {
		this.gradebookManager = gradebookManager;
	}

	public SectionAwareness getSectionAwareness() {
		return sectionAwareness;
	}
	public void setSectionAwareness(SectionAwareness sectionAwareness) {
		this.sectionAwareness = sectionAwareness;
	}

	/**
	 * @return Returns the userDirectoryService.
	 */
	public UserDirectoryService getUserDirectoryService() {
		return userDirectoryService;
	}
	/**
	 * @param userDirectoryService The userDirectoryService to set.
	 */
	public void setUserDirectoryService(UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}
	/**
	 * @return Returns the authnService.
	 */
	public Authn getAuthnService() {
		return authnService;
	}
	/**
	 * @param authnService The authnService to set.
	 */
	public void setAuthnService(Authn authnService) {
		this.authnService = authnService;
	}

	public Authz getAuthzService() {
		return authzService;
	}
	public void setAuthzService(Authz authzService) {
		this.authzService = authzService;
	}

	/**
	 * @return Returns the contextManagementService.
	 */
	public ContextManagement getContextManagementService() {
		return contextManagementService;
	}
	/**
	 * @param contextManagementService The contextManagementService to set.
	 */
	public void setContextManagementService(ContextManagement contextManagementService) {
		this.contextManagementService = contextManagementService;
	}
}



