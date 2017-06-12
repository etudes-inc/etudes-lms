/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/web/trunk/web-impl/impl/src/java/org/sakaiproject/web/impl/WebServiceImpl.java$
 * $Id: WebServiceImpl.java 8730 2014-09-10 18:56:32Z rashmim $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2014 The Sakai Foundation.
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
package org.sakaiproject.web.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.dv.util.Base64;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.web.api.WebService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WebServiceImpl implements WebService, EntityTransferrer
{
	
	private static Log M_log = LogFactory.getLog(WebServiceImpl.class);
	private static final String TOOL_ID = "sakai.iframe";
	
  private static final String WEB_CONTENT = "web_content";
  private static final String REDIRECT_TAB = "redirect_tab";
  private static final String WEB_CONTENT_TITLE = "title";
  private static final String WEB_CONTENT_URL = "url";
	private static final String PAGE_TITLE = "page_title";
	private static final String NEW_WINDOW = "open_in_new_window";
	
	private static final String WEB_CONTENT_URL_PROP = "source";
	private static final String HEIGHT_PROP = "height";
	private static final String SPECIAL_PROP = "special";

	public static final String ATTR_TOP_REFRESH = "sakai.vppa.top.refresh";

	public void init()
	{
		// register as an entity producer
		EntityManager.registerEntityProducer(this, REFERENCE_ROOT);

	} // init

	public String archive(String siteId, Document doc, Stack stack, String archivePath, List attachments)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Entity getEntity(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getEntityAuthzGroups(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityDescription(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceProperties getEntityResourceProperties(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityUrl(Reference ref)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public HttpAccess getHttpAccess()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel()
	{
		return "web";
	}

	public String merge(String siteId, Element root, String archivePath, String fromSiteId, Map attachmentNames, Map userIdTrans,
			Set userListAllowImport)
	{
		M_log.info("merge starts for Web Content...");
    if (siteId != null && siteId.trim().length() > 0)
    {
      try
      {
      	Site site = SiteService.getSite(siteId);
        NodeList allChildrenNodes = root.getChildNodes();
        int length = allChildrenNodes.getLength();
        for (int i = 0; i < length; i++)
        {
          Node siteNode = allChildrenNodes.item(i);
          if (siteNode.getNodeType() == Node.ELEMENT_NODE)
          {
            Element siteElement = (Element) siteNode;
            if (siteElement.getTagName().equals(WEB_CONTENT))
            {
            	NodeList allContentNodes = siteElement.getChildNodes();
              int lengthContent = allContentNodes.getLength();
              for (int j = 0; j < lengthContent; j++)
              {
                Node child1 = allContentNodes.item(j);
                if (child1.getNodeType() == Node.ELEMENT_NODE)
                {
                  Element contentElement = (Element) child1;
                  if (contentElement.getTagName().equals(REDIRECT_TAB))
                  {
                  	String contentTitle = contentElement.getAttribute(WEB_CONTENT_TITLE);
                  	String trimBody = null;
                  	if(contentTitle != null && contentTitle.length() >0)
                  	{
                      trimBody = trimToNull(contentTitle);
                      if (trimBody != null && trimBody.length() >0)
                      {
                      	byte[] decoded = Base64.decode(trimBody);
                      	contentTitle = new String(decoded, "UTF-8");
                      }
                  	}
                  	String contentUrl = contentElement.getAttribute(WEB_CONTENT_URL);
                  	trimBody = null;
                  	if(contentUrl != null && contentUrl.length() >0)
                  	{
                      trimBody = trimToNull(contentUrl);
                      if (trimBody != null && trimBody.length() >0)
                      {
                      	byte[] decoded = Base64.decode(trimBody);
                      	contentUrl = new String(decoded, "UTF-8");
                      }
                  	}
                  	
                  	if(contentTitle != null && contentUrl != null && contentTitle.length() >0 && contentUrl.length() >0)
                  	{
                			Tool tr = ToolManager.getTool("sakai.iframe");
                			SitePage page = site.addPage();
                			page.setTitle(contentTitle);
                			ToolConfiguration tool = page.addTool();
                			tool.setTool("sakai.iframe", tr);
                			tool.setTitle(contentTitle);
    									tool.getPlacementConfig().setProperty("source", contentUrl);
                  	}
                  }
                }
              }
            }
          }
        }
        SiteService.save(site);
    		ToolSession session = SessionManager.getCurrentToolSession();

    		if (session.getAttribute(ATTR_TOP_REFRESH) == null)
    		{
    			session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);
    		}
      }
      catch(Exception e)
      {
      	M_log.error("errors in merge for WebServiceImpl");
      	e.printStackTrace();
      }
    }
    
		return null;
	}

	public boolean parseEntityReference(String reference, Reference ref)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public boolean willArchiveMerge()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public String[] myToolIds()
	{
		String[] toolIds =
		{ TOOL_ID };
		return toolIds;
	}

	public void transferCopyEntities(String fromContext, String toContext, List ids)
	{
		try
		{
			// retrieve all of the web content tools to copy
			Site fromSite = SiteService.getSite(fromContext);
		
			Site toSite = SiteService.getSite(toContext);

			List fromSitePages = fromSite.getPages();
			List toSitePages = toSite.getPages();

			if (fromSitePages != null && !fromSitePages.isEmpty())
			{
				Iterator pageIter = fromSitePages.iterator();
				// Iterate through pages of source site
				while (pageIter.hasNext())
				{
					SitePage currPage = (SitePage) pageIter.next();

					List toolList = currPage.getTools();
					Iterator toolIter = toolList.iterator();
					// Iterate through tools of source site
					while (toolIter.hasNext())
					{
						ToolConfiguration toolConfig = (ToolConfiguration) toolIter.next();

						// we do not want to import "special" uses of sakai.iframe, such as worksite info
						String special = toolConfig.getPlacementConfig().getProperty(SPECIAL_PROP);

						if (toolConfig.getToolId().equals(TOOL_ID) && special == null)
						{
							String contentUrl = toolConfig.getPlacementConfig().getProperty(WEB_CONTENT_URL_PROP);
							String toolTitle = toolConfig.getTitle();
							String pageTitle = currPage.getTitle();
							String height = toolConfig.getPlacementConfig().getProperty(HEIGHT_PROP);
							Properties toolConfigProps = toolConfig.getPlacementConfig();
							String thirdService = toolConfigProps.getProperty("thirdPartyService");
							
							// in some cases the new site already has all of this. so make
							// sure we don't make a duplicate

							boolean skip = false;
							
							// don't transfer external service. site setup will transfer them
							if ("Yes".equalsIgnoreCase(thirdService))
							{
								skip = true;
								continue;
							}

							Collection<ToolConfiguration> toolConfs = toSite.getTools(TOOL_ID);
							// Iterate through tools of destination site to see if this tool exists already
							if (toolConfs != null && !toolConfs.isEmpty())
							{
								for (ToolConfiguration config : toolConfs)
								{
									if (config.getToolId().equals(TOOL_ID))
									{
										SitePage p = config.getContainingPage();
										
										//Do not import items that already exist or if content url is null
										if (pageTitle != null && pageTitle.equals(p.getTitle()) && ((contentUrl != null
												&& contentUrl.equals(config.getPlacementConfig().getProperty(WEB_CONTENT_URL_PROP)))||(contentUrl == null && config.getPlacementConfig().getProperty(WEB_CONTENT_URL_PROP) == null)))
										{
											skip = true;
											break;
	}
									}
								}
							}

							// If tool does not exist in destination site ,add a page and tool to
							// destination site
							if (!skip && toolTitle != null && toolTitle.length() > 0 && pageTitle != null && pageTitle.length() > 0)
							{
								Tool tr = ToolManager.getTool(TOOL_ID);
								SitePage page = toSite.addPage();
								page.setTitle(pageTitle);
								ToolConfiguration tool = page.addTool();
								tool.setTool(TOOL_ID, tr);
								tool.setTitle(toolTitle);
								if (contentUrl != null)
								{
									tool.getPlacementConfig().setProperty(WEB_CONTENT_URL_PROP, contentUrl);
								}

								Properties config = tool.getPlacementConfig();
								Iterator keys = toolConfigProps.keySet().iterator();
								// transfer all properties including LTI if the tool hasn't set for its own
								while (keys.hasNext())
								{
									String k = (String) keys.next();
									if (!config.containsKey(k)) config.setProperty(k, toolConfigProps.getProperty(k));
								}

								if (currPage.isPopUp())
									page.setPopup(true);
								else
									page.setPopup(false);

							}
						}// End if special == null
					}// End loop iterate through tools of source site
				}// End loop iterate through pages of source site

			}// End if source site has pages

			try
			{
				SiteService.save(toSite);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			ToolSession session = SessionManager.getCurrentToolSession();
			if (session !=  null)
			{
				if (session.getAttribute(ATTR_TOP_REFRESH) == null)
				{
					session.setAttribute(ATTR_TOP_REFRESH, Boolean.TRUE);
				}
			}
		}

		catch (Exception any)
		{
			M_log.warn("transferCopyEntities(): exception in handling webcontent data: ", any);
		}
	}

	public String trimToNull(String value)
	{
		if (value == null) return null;
		value = value.trim();
		if (value.length() == 0) return null;
		return value;
	}

	public Collection getEntityAuthzGroups(Reference ref, String userId)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
