/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/site/site-impl/impl/src/java/org/sakaiproject/site/impl/BaseToolConfiguration.java $
 * $Id: BaseToolConfiguration.java 3 2008-10-20 18:44:42Z ggolden $
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

package org.sakaiproject.site.impl;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Xml;
import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.ToolManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <p>
 * BaseToolConfiguration is an implementation of the Site API's ToolConfiguration.
 * </p>
 */
public class BaseToolConfiguration extends org.sakaiproject.util.Placement implements ToolConfiguration, Identifiable
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(BaseToolConfiguration.class);

	/** A fixed class serian number. */
	private static final long serialVersionUID = 1L;

	/** The layout hints. */
	protected String m_layoutHints = null;

	/** The SitePage I belong to. */
	protected SitePage m_page = null;

	/** The site id I belong to, in case I have no m_page. */
	protected String m_siteId = null;

	/** The page id I belong to, in case I have no m_page. */
	protected String m_pageId = null;

	/** The site skin, in case I have no m_page. */
	protected String m_skin = null;

	/** True if the placement conf has not been read yet. */
	protected boolean m_configLazy = false;

	/** The order within the page. */
	protected int m_pageOrder = -1;

	/**
	 * ReConstruct
	 * 
	 * @param page
	 *        The page in which this tool lives.
	 * @param id
	 *        The tool (placement) id.
	 * @param toolId
	 *        The id (registration code) of the tool to place here.
	 * @param title
	 *        The tool title.
	 * @param layoutHints
	 *        The layout hints.
	 * @param pageOrder
	 *        The order within the page.
	 */
	protected BaseToolConfiguration(SitePage page, String id, String toolId, String title, String layoutHints, int pageOrder)
	{
		super(id, toolId, ToolManager.getTool(toolId), null, null, title);

		m_page = page;
		m_layoutHints = layoutHints;
		m_pageOrder = pageOrder;

		m_configLazy = true;
	}

	/**
	 * ReConstruct - if we don't have a page to follow up to get to certain page and site info.
	 * 
	 * @param id
	 *        The tool (placement) id.
	 * @param toolId
	 *        The id (registration code) of the tool to place here.
	 * @param title
	 *        The tool title.
	 * @param layoutHints
	 *        The layout hints.
	 * @param pageId
	 *        The page id in which this tool lives.
	 * @param siteId
	 *        The site id in which this tool lives.
	 * @param skin
	 *        The site's skin.
	 * @param pageOrder
	 *        The order within the page.
	 */
	protected BaseToolConfiguration(String id, String toolId, String title, String layoutHints, String pageId, String siteId,
			String skin, int pageOrder)
	{
		super(id, toolId, ToolManager.getTool(toolId), null, null, title);

		m_page = null;

		m_layoutHints = layoutHints;
		m_pageId = pageId;
		m_siteId = siteId;
		m_skin = skin;
		m_pageOrder = pageOrder;

		m_configLazy = true;
	}

	/**
	 * Construct as a copy of another.
	 * 
	 * @param other
	 *        The other to copy.
	 * @param page
	 *        The page in which this tool lives.
	 * @param exact
	 *        If true, we copy ids - else we generate a new one.
	 */
	protected BaseToolConfiguration(ToolConfiguration other, SitePage page, boolean exact)
	{
		m_page = page;
		BaseToolConfiguration bOther = (BaseToolConfiguration) other;

		if (exact)
		{
			m_id = other.getId();
		}
		else
		{
			m_id = IdManager.createUuid();
		}
		m_toolId = other.getToolId();
		m_tool = other.getTool();
		m_title = other.getTitle();
		m_layoutHints = other.getLayoutHints();
		m_pageId = bOther.m_pageId;
		m_pageOrder = bOther.m_pageOrder;
		
		m_siteId = getContainingPage().getContainingSite().getId();
		m_skin = bOther.m_skin;

		Hashtable h = other.getPlacementConfig();
		// exact copying of ToolConfiguration items vs replacing occurence of site id within item value, depending on "exact" setting	-zqian
		if (exact)
		{
			m_config.putAll(other.getPlacementConfig());
		}
		else
		{
			for (Enumeration e=h.keys(); e.hasMoreElements();)
			{
				// replace site id string inside configuration
				String pOtherConfig = (String) e.nextElement();
				String pOtherConfigValue = (String) h.get(pOtherConfig);
				m_config.put(pOtherConfig, pOtherConfigValue.replaceAll(bOther.getSiteId(), m_siteId));
			}
		}
		m_configLazy = bOther.m_configLazy;
	}

	/**
	 * Construct using a tool registration for default information.
	 * 
	 * @param reg
	 *        The tool registration.
	 * @param page
	 *        The page in which this tool lives.
	 */
	protected BaseToolConfiguration(SitePage page)
	{
		super(IdManager.createUuid(), null, null, null, null, null);

		m_page = page;
	}

	/**
	 * Construct using a tool registration for default information.
	 * 
	 * @param reg
	 *        The tool registration.
	 * @param page
	 *        The page in which this tool lives.
	 */
	protected BaseToolConfiguration(Tool reg, SitePage page)
	{
		super(IdManager.createUuid(), reg.getId(), reg, null, null, null);

		m_page = page;
	}

	/**
	 * Construct using a tool id.
	 * 
	 * @param toolId
	 *        The tool id.
	 * @param page
	 *        The page in which this tool lives.
	 */
	protected BaseToolConfiguration(String toolId, SitePage page)
	{
		super(IdManager.createUuid(), toolId, null, null, null, null);

		m_page = page;
	}

	/**
	 * Construct from XML element.
	 * 
	 * @param el
	 *        The XML element.
	 * @param page
	 *        The page in which this tool lives.
	 */
	protected BaseToolConfiguration(Element el, SitePage page)
	{
		super();

		m_page = page;

		m_id = el.getAttribute("id");
		m_toolId = StringUtil.trimToNull(el.getAttribute("toolId"));
		if (m_toolId != null)
		{
			m_tool = ToolManager.getTool(m_toolId);
		}
		m_title = StringUtil.trimToNull(el.getAttribute("title"));
		m_layoutHints = StringUtil.trimToNull(el.getAttribute("layoutHints"));

		// the children (properties)
		NodeList children = el.getChildNodes();
		final int length = children.getLength();
		for (int i = 0; i < length; i++)
		{
			Node child = children.item(i);
			if (child.getNodeType() != Node.ELEMENT_NODE) continue;
			Element element = (Element) child;

			// look for properties
			if (element.getTagName().equals("properties"))
			{
				// re-create properties
				Xml.xmlToProperties(m_config, element);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Properties getPlacementConfig()
	{
		// if the config has not yet been read, read it
		if (m_configLazy)
		{
			((BaseSiteService) (SiteService.getInstance())).m_storage.readToolProperties(this, m_config);
			m_configLazy = false;
		}

		return m_config;
	}

	/**
	 * Acces the m_config, which is inherited and not visible to this package outside this class -ggolden
	 */
	protected Properties getMyConfig()
	{
		return m_config;
	}

	/**
	 * @inheritDoc
	 */
	public String getLayoutHints()
	{
		return m_layoutHints;
	}

	/**
	 * @inheritDoc
	 */
	public int[] parseLayoutHints()
	{
		try
		{
			if (m_layoutHints == null) return null;
			String[] parts = StringUtil.split(m_layoutHints, ",");
			if (parts.length < 2) return null;
			int[] rv = new int[2];
			rv[0] = Integer.parseInt(parts[0]);
			rv[1] = Integer.parseInt(parts[1]);
			return rv;
		}
		catch (Throwable t)
		{
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPageOrder()
	{
		return m_pageOrder;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSkin()
	{
		// use local copy if no page is set
		if (m_page == null)
		{
			return m_skin;
		}

		return m_page.getSkin();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPageId()
	{
		// use local copy if no page is set
		if (m_page == null)
		{
			return m_pageId;
		}

		return getContainingPage().getId();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSiteId()
	{
		// use local copy if no page is set
		if (m_page == null)
		{
			return m_siteId;
		}

		return getContainingPage().getContainingSite().getId();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getContext()
	{
		// the context of a site based placement is the site id
		return getSiteId();
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLayoutHints(String hints)
	{
		m_layoutHints = hints;
	}

	/**
	 * {@inheritDoc}
	 */
	public void moveUp()
	{
		if (m_page == null)
		{
			M_log.warn("moveUp: null page: " + m_id);
			return;
		}

		((ResourceVector) m_page.getTools()).moveUp(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public void moveDown()
	{
		if (m_page == null)
		{
			M_log.warn("moveDown: null page: " + m_id);
			return;
		}

		((ResourceVector) m_page.getTools()).moveDown(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public SitePage getContainingPage()
	{
		return m_page;
	}

	/**
	 * {@inheritDoc}
	 */
	public Element toXml(Document doc, Stack stack)
	{
		Element element = doc.createElement("tool");
		((Element) stack.peek()).appendChild(element);
		stack.push(element);

		element.setAttribute("id", getId());
		String toolId = getToolId();
		if (toolId != null) element.setAttribute("toolId", toolId);
		if (m_title != null) element.setAttribute("title", m_title);
		if (m_layoutHints != null) element.setAttribute("layoutHints", m_layoutHints);

		// properties
		Xml.propertiesToXml(getPlacementConfig(), doc, stack);

		stack.pop();

		return (Element) element;
	}

	/**
	 * {@inheritDoc}
	 */
	public void save()
	{
		// TODO: security? version?
		((BaseSiteService) (SiteService.getInstance())).m_storage.saveToolConfig(null, this);

		// track the site change
		EventTrackingService.post(EventTrackingService.newEvent(SiteService.SECURE_UPDATE_SITE, SiteService
				.siteReference(getSiteId()), true));
	}
}
