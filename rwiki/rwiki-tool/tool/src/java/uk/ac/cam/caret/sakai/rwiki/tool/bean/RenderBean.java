/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/rwiki/rwiki-tool/tool/src/java/uk/ac/cam/caret/sakai/rwiki/tool/bean/RenderBean.java $
 * $Id: RenderBean.java 3 2008-10-20 18:44:42Z ggolden $
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

package uk.ac.cam.caret.sakai.rwiki.tool.bean;

import java.util.List;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.dao.ObjectProxy;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.service.exception.PermissionException;
import uk.ac.cam.caret.sakai.rwiki.tool.api.ToolRenderService;
import uk.ac.cam.caret.sakai.rwiki.utils.NameHelper;

/**
 * Bean that renders the current rwikiObject using a set objectService and
 * toolRenderService.
 * 
 * @author andrew
 */
// FIXME: Tool
public class RenderBean
{

	private ToolRenderService toolRenderService;

	private RWikiObjectService objectService;

	private RWikiObject rwo;

	private boolean withBreadcrumbs = true;

	private boolean canEdit = false;

	// FIXME internationalise this!
	private static final String PERMISSION_PROBLEM = "You do not have permission to view this page";

	/**
	 * Create new RenderBean from given RWikiObject
	 * 
	 * @param rwo
	 *        the current RWikiObject
	 * @param user
	 *        the current user
	 * @param toolRenderService
	 *        the ToolRenderService
	 * @param objectService
	 *        the RWikiObjectService
	 */
	public RenderBean(RWikiObject rwo, ToolRenderService toolRenderService,
			RWikiObjectService objectService, boolean withBreadcrumbs)
	{
		this.rwo = rwo;
		this.toolRenderService = toolRenderService;
		this.objectService = objectService;
		this.withBreadcrumbs = withBreadcrumbs;
		this.canEdit = objectService.checkUpdate(rwo);
	}

	/**
	 * Create new RenderBean and get the chosen RWikiObject
	 * 
	 * @param name
	 *        the name of the RWikiObject to render
	 * @param user
	 *        the current user
	 * @param defaultRealm
	 *        the defaultRealm to globalise the name with
	 * @param toolRenderService
	 *        the ToolRenderService
	 * @param objectService
	 *        the RWikiObjectService
	 */
	public RenderBean(String name, String defaultRealm,
			ToolRenderService toolRenderService,
			RWikiObjectService objectService, boolean withBreadcrumbs)
	{
		this.objectService = objectService;
		this.toolRenderService = toolRenderService;
		this.withBreadcrumbs = withBreadcrumbs;
		String pageName = NameHelper.globaliseName(name, defaultRealm);
		String pageRealm = defaultRealm;
		try
		{
			this.rwo = objectService.getRWikiObject(pageName, pageRealm);
			this.canEdit = objectService.checkUpdate(rwo);
		}
		catch (PermissionException e)
		{
			this.rwo = objectService.createNewRWikiCurrentObject();
			rwo.setName(pageName);
			rwo.setContent(PERMISSION_PROBLEM);
		}
	}

	/**
	 * Get the RWikiObjectService
	 * 
	 * @return objectService
	 */
	public RWikiObjectService getObjectService()
	{
		return objectService;
	}

	/**
	 * Set the RWikiObjectService for this bean.
	 * 
	 * @param objectService
	 *        The RWikiObjectService to use.
	 */
	public void setObjectService(RWikiObjectService objectService)
	{
		this.objectService = objectService;
	}

	/**
	 * Get the ToolRenderService
	 * 
	 * @return toolRenderService
	 */
	public ToolRenderService getToolRenderService()
	{
		return toolRenderService;
	}

	/**
	 * Set the ToolRenderService for this bean.
	 * 
	 * @param toolRenderService
	 *        the ToolRenderService to use.
	 */
	public void setRenderService(ToolRenderService toolRenderService)
	{
		this.toolRenderService = toolRenderService;

	}

	public String getPreviewPage()
	{
		return toolRenderService.renderPage(rwo, false);
	}

	/**
	 * Render the current RWikiObject
	 * 
	 * @return XHTML as a String representing the content of the current
	 *         RWikiObject
	 */
	public String getRenderedPage()
	{
		return toolRenderService.renderPage(rwo);
	}

	/**
	 * Render the current RWikiObject with public links
	 * 
	 * @return XMTML as a String representing the content of the current
	 *         RWikiObject
	 */
	public String getPublicRenderedPage()
	{
		return toolRenderService.renderPublicPage(rwo, withBreadcrumbs);
	}

	/**
	 * Render the current RWikiObject
	 * 
	 * @return XHTML as a String representing the content of the current
	 *         RWikiObject
	 */
	public String renderPage()
	{
		return toolRenderService.renderPage(rwo);
	}

	/**
	 * Render the current RWikiObject with public links
	 * 
	 * @return XMTML as a String representing the content of the current
	 *         RWikiObject
	 */
	public String publicRenderedPage()
	{
		return toolRenderService.renderPublicPage(rwo, withBreadcrumbs);
	}

	/**
	 * Render the rwikiObject represented by the given name and realm
	 * 
	 * @param name
	 *        a possible non-globalised name
	 * @param defaultRealm
	 *        the default realm of that should be globalised against
	 * @return XHTML as a String representing the content of the RWikiObject
	 */
	public String renderPage(String name, String defaultRealm)
	{
		String pageName = NameHelper.globaliseName(name, defaultRealm);
		String pageRealm = NameHelper.localizeSpace(pageName, defaultRealm);

		try
		{
			RWikiObject page = objectService
					.getRWikiObject(pageName, pageRealm);
			return toolRenderService.renderPage(page, defaultRealm);
		}
		catch (PermissionException e)
		{
			RWikiObject page = objectService.createNewRWikiCurrentObject();
			page.setName(pageName);
			page.setContent(PERMISSION_PROBLEM);
			return toolRenderService.renderPage(page, defaultRealm);
		}

	}

	/**
	 * Render the rwikiObject as a public page represented by the given name and
	 * realm
	 * 
	 * @param name
	 *        a possible non-globalised name
	 * @param defaultRealm
	 *        the default space of that should be globalised against
	 * @return XHTML as a String representing the content of the RWikiObject
	 */
	public String publicRenderPage(String name, String defaultRealm,
			boolean withBreadcrumbs)
	{
		String pageName = NameHelper.globaliseName(name, defaultRealm);
		String pageSpace = NameHelper.localizeSpace(pageName, defaultRealm);

		try
		{
			RWikiObject page = objectService
					.getRWikiObject(pageName, pageSpace);
			return toolRenderService.renderPublicPage(page, defaultRealm,
					withBreadcrumbs);
		}
		catch (PermissionException e)
		{
			RWikiObject page = objectService.createNewRWikiCurrentObject();
			page.setName(pageName);
			page.setContent(PERMISSION_PROBLEM);
			return toolRenderService.renderPublicPage(page, defaultRealm,
					withBreadcrumbs);
		}

	}

	/**
	 * The current RWikiObject
	 * 
	 * @return rwikiObject
	 */
	public RWikiObject getRwikiObject()
	{
		return rwo;
	}

	/**
	 * The localised name of the rwikiObject
	 * 
	 * @return localised page name
	 */
	public String getLocalisedPageName()
	{
		return NameHelper.localizeName(rwo.getName(), rwo.getRealm());
	}

	/**
	 * Returns an url that generate a view string to the current rwikiObject
	 * 
	 * @return url as String
	 */
	public String getEditUrl()
	{
		ViewBean vb = new ViewBean(rwo.getName(), rwo.getRealm());
		return vb.getEditUrl();
	}

	/**
	 * Returns true if the underlying page exists.
	 * 
	 * @return
	 */
	public boolean getExists()
	{
		return objectService.exists(rwo.getName(), rwo.getRealm());
	}

	/**
	 * Returns true if the page has content and it has length
	 * 
	 * @return
	 */
	public boolean getHasContent()
	{
		if (!getExists()) return false;
		String content = rwo.getContent();
		return (content != null && content.trim().length() > 0);
	}

	/**
	 * returns a list of comments that are render beans
	 * 
	 * @return
	 */
	public List getComments()
	{
		List commentsList = objectService
				.findRWikiSubPages(rwo.getName() + ".");
		ObjectProxy lop = new ObjectProxy()
		{
			public Object proxyObject(Object o)
			{
				if (o instanceof RWikiObject)
				{
					RenderBean rb = new RenderBean((RWikiObject) o,
							toolRenderService, objectService, withBreadcrumbs);

					return rb;
				}
				if (o instanceof RenderBean)
				{
					return o;
				}
				throw new RuntimeException(
						"Proxied list does not contain expected object type, found:"
								+ o);
			}
		};
		return objectService.createListProxy(commentsList, lop);
	}

	/**
	 * @return Returns the canEdit.
	 */
	public boolean getCanEdit()
	{
		return canEdit;
	}

	/**
	 * @param canEdit
	 *        The canEdit to set.
	 */
	public void setCanEdit(boolean canEdit)
	{
		this.canEdit = canEdit;
	}

	public String getListCommentsURL()
	{
		ViewBean vb = new ViewBean(rwo.getName(), NameHelper.localizeSpace(rwo
				.getName(), rwo.getRealm()));
		return vb.getListCommentsURL();
	}

	public String getNewCommentURL()
	{
		ViewBean vb = new ViewBean(rwo.getName(), NameHelper.localizeSpace(rwo
				.getName(), rwo.getRealm()));
		return vb.getNewCommentURL();
	}

	public String getEditCommentURL()
	{
		ViewBean vb = new ViewBean(rwo.getName(), NameHelper.localizeSpace(rwo
				.getName(), rwo.getRealm()));
		return vb.getEditCommentURL();
	}

	public String getCommentLevel()
	{
		String localName = NameHelper.localizeName(rwo.getName(), NameHelper
				.localizeSpace(rwo.getName(), rwo.getRealm()));
		int p = localName.indexOf(".", 0);
		int i = 1;
		while (p > 0 && i <= 5)
		{
			i++;
			p = localName.indexOf(".", p + 1);
		}
		return String.valueOf(i);
	}

	public String getListPresenceURL()
	{
		ViewBean vb = new ViewBean(rwo.getName(), NameHelper.localizeSpace(rwo
				.getName(), rwo.getRealm()));
		return vb.getListPresenceURL();
	}

	public String getOpenPageChatURL()
	{
		ViewBean vb = new ViewBean(rwo.getName(), NameHelper.localizeSpace(rwo
				.getName(), rwo.getRealm()));
		return vb.getOpenPageChatURL();
	}

	public String getOpenSpaceChatURL()
	{
		ViewBean vb = new ViewBean(rwo.getName(), NameHelper.localizeSpace(rwo
				.getName(), rwo.getRealm()));
		return vb.getOpenSpaceChatURL();
	}

	public String getListPageChatURL()
	{
		ViewBean vb = new ViewBean(rwo.getName(), NameHelper.localizeSpace(rwo
				.getName(), rwo.getRealm()));
		return vb.getListPageChatURL();
	}

	public String getListSpaceChatURL()
	{
		ViewBean vb = new ViewBean(rwo.getName(), NameHelper.localizeSpace(rwo
				.getName(), rwo.getRealm()));
		return vb.getListSpaceChatURL();
	}

}
