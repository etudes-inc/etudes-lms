/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/site-manage/site-manage-impl/impl/src/java/org/sakaiproject/site/impl/SiteUnpublishImpl.java $
 * $Id: SiteUnpublishImpl.java 4863 2013-05-16 18:03:43Z mallikamt $
 ***********************************************************************************
 *
 * Copyright (c) 2010 Etudes, Inc.
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

package org.sakaiproject.site.impl;

import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.scheduler.api.ScheduledInvocationCommand;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

/**
 * <p>
 * SiteUnpublishImpl is a class used when sites are meant to unpublished at a future date
 * </p>
 */
public class SiteUnpublishImpl implements ScheduledInvocationCommand
{
	/** Dependency: SecurityService */
	protected SecurityService securityService = null;

	/** Dependency: SessionManager */
	protected SessionManager sessionManager = null;

	/**
	 * For ScheduledInvocationCommand, use a low (0 .. 1000) priority
	 */
	public Integer getPriority()
	{
		return Integer.valueOf(100);
	}

	/**
	 * Implementation of command pattern. Will be called by ScheduledInvocationManager
	 * for delayed site unpublishings
	 *
	 * @param opaqueContext
	 * 			reference (context) for message
	 */
	public void execute(String opaqueContext)
	{
		try {
			Site site = SiteService.getSite(opaqueContext);
			site.setPublished(false);
			// get the current user
			final String userId = this.sessionManager.getCurrentSessionUserId() == null ? "admin"
					: this.sessionManager.getCurrentSessionUserId();
			try {
				// set the user into the thread
				Session s = sessionManager.getCurrentSession();
				if (s != null) {
					s.setUserId(userId);
				}

				pushAdvisor();
				try {
					SiteService.save(site);
				} catch (IdUnusedException e) {
					// TODO:
				} catch (PermissionException e) {
					// TODO:
				}
			} finally {
				popAdvisor();
			}
		} catch (IdUnusedException e) {
		}
	}

	/**
	 * Setup a security advisor.
	 */
	protected void pushAdvisor()
	{
		// setup a security advisor
		this.securityService.pushAdvisor(new SecurityAdvisor()
		{
			public SecurityAdvice isAllowed(String userId, String function, String reference)
			{
				return SecurityAdvice.ALLOWED;
			}
		});
	}

	/**
	 * Remove our security advisor.
	 */
	protected void popAdvisor()
	{
		this.securityService.popAdvisor();
	}


	/**
	 * Dependency: SecurityService.
	 *
	 * @param service
	 *        The SecurityService.
	 */
	public void setSecurityService(SecurityService service)
	{
		this.securityService = service;
	}

	/**
	 * Dependency: SessionManager.
	 *
	 * @param service
	 *        The SessionManager.
	 */
	public void setSessionManager(SessionManager service)
	{
		this.sessionManager = service;
	}


}