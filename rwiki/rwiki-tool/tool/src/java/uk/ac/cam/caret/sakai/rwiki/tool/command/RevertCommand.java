/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/rwiki/rwiki-tool/tool/src/java/uk/ac/cam/caret/sakai/rwiki/tool/command/RevertCommand.java $
 * $Id: RevertCommand.java 3 2008-10-20 18:44:42Z ggolden $
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

package uk.ac.cam.caret.sakai.rwiki.tool.command;

import java.io.IOException;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.tool.cover.SessionManager;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.exception.PermissionException;
import uk.ac.cam.caret.sakai.rwiki.service.exception.VersionException;
import uk.ac.cam.caret.sakai.rwiki.tool.RWikiServlet;
import uk.ac.cam.caret.sakai.rwiki.tool.RequestScopeSuperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.api.HttpCommand;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.EditBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ErrorBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.HistoryBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.ViewParamsHelperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.command.helper.ErrorBeanHelper;

/**
 * @author andrew
 */
// FIXME: Tool
public class RevertCommand implements HttpCommand
{

	private static Log log = LogFactory.getLog(RevertCommand.class);

	private String successfulPath;

	private RWikiObjectService objectService;

	private String noUpdatePath;

	private String contentChangedPath;

	public void init()
	{
		ComponentManager cm = org.sakaiproject.component.cover.ComponentManager
				.getInstance();
		objectService = (RWikiObjectService) load(cm, RWikiObjectService.class
				.getName());
	}

	private Object load(ComponentManager cm, String name)
	{
		Object o = cm.get(name);
		if (o == null)
		{
			log.error("Cant find Spring component named " + name);
		}
		return o;
	}

	public void execute(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{

		RequestScopeSuperBean rssb = RequestScopeSuperBean
				.getFromRequest(request);

		ViewParamsHelperBean vphb = rssb.getNameHelperBean();

		String revision = request.getParameter(HistoryBean.REVISION_PARAM);
		String version = request.getParameter(HistoryBean.VERSION_PARAM);

		// interpret version as Date and revision as int
		Date versionDate = new Date(Long.parseLong(version));
		int revisionInt = Integer.parseInt(revision);

		String name = vphb.getGlobalName();
		String realm = vphb.getLocalSpace();

		try
		{
			objectService.revert(name, realm, versionDate, revisionInt);
			// objectService.update(name, user, realm, versionDate, content);
		}
		catch (VersionException e)
		{
			// treat like a save that hasn't completed properly.
			EditBean editBean = rssb.getEditBean();
			editBean.setPreviousRevision(revisionInt);
			editBean.setPreviousVersion(version);
			editBean.setSaveType("revert");

			this.contentChangedDispatch(request, response);
			return;
		}
		catch (PermissionException e)
		{
			// Fail to revert!
			this.noUpdateAllowed(request, response);
			return;
		}
		// Successful reversion
		this.successfulUpdateDispatch(request, response);
		// set the state to view
		ViewBean vb = new ViewBean(name, realm);
		String requestURL = request.getRequestURL().toString();
		SessionManager.getCurrentToolSession().setAttribute(
				RWikiServlet.SAVED_REQUEST_URL, requestURL + vb.getViewUrl());
		return;

	}

	private void successfulUpdateDispatch(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		RequestDispatcher rd = request.getRequestDispatcher(successfulPath);
		rd.forward(request, response);
	}

	private void contentChangedDispatch(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		ErrorBean errorBean = ErrorBeanHelper.getErrorBean(request);
		// FIXME internationalise this!!
		errorBean
				.addError("Content has changed since you last viewed it. Please update the new content or overwrite it with the submitted content.");

		RequestDispatcher rd = request.getRequestDispatcher(contentChangedPath);
		rd.forward(request, response);
	}

	private void noUpdateAllowed(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException
	{
		ErrorBean errorBean = ErrorBeanHelper.getErrorBean(request);
		// FIXME internationalise this!!
		errorBean.addError("You do not have permission to update this page.");

		RequestDispatcher rd = request.getRequestDispatcher(noUpdatePath);
		rd.forward(request, response);
	}

	public String getContentChangedPath()
	{
		return contentChangedPath;
	}

	public void setContentChangedPath(String contentChangedPath)
	{
		this.contentChangedPath = contentChangedPath;
	}

	public String getNoUpdatePath()
	{
		return noUpdatePath;
	}

	public void setNoUpdatePath(String noUpdatePath)
	{
		this.noUpdatePath = noUpdatePath;
	}

	public String getSuccessfulPath()
	{
		return successfulPath;
	}

	public void setSuccessfulPath(String successfulPath)
	{
		this.successfulPath = successfulPath;
	}

}