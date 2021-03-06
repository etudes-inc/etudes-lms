/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/rwiki/rwiki-impl/impl/src/java/uk/ac/cam/caret/sakai/rwiki/component/radeox/service/impl/SpecializedRenderContext.java $
 * $Id: SpecializedRenderContext.java 3 2008-10-20 18:44:42Z ggolden $
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

package uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl;

import org.radeox.engine.context.BaseRenderContext;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiSecurityService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.service.api.radeox.CachableRenderContext;

/**
 * This class acts as a container for the Render context, making the RWiki
 * Object, the Security Service and the RWikiObject service available to the
 * Render Engine. If the operation is cachable, the RenderContext should return
 * true
 * 
 * @author andrew
 */
// FIXME: Component
public class SpecializedRenderContext extends BaseRenderContext implements
		CachableRenderContext
{

	/**
	 * Monitors cachable status
	 */
	private boolean cachable = true;

	private RWikiObjectService objectService;

	private RWikiObject rwikiObject;

	private RWikiSecurityService securityService;

	private SiteService siteService;

	public SpecializedRenderContext(RWikiObject rwikiObject,
			RWikiObjectService objectService,
			RWikiSecurityService securityService,
			SiteService siteService)
	{
		this.rwikiObject = rwikiObject;

		this.objectService = objectService;
		this.securityService = securityService;
		this.siteService = siteService;
		
		this.set(RWikiObject.class.getName(), rwikiObject);
		this.set(RWikiObject.class.getName().concat(".name"), rwikiObject.getName());
		this.set(RWikiObjectService.class.getName(), objectService);
		this.set(RWikiSecurityService.class.getName(), securityService);
		this.set(SiteService.class.getName(), siteService);
	}

	public RWikiObject getRWikiObject()
	{
		return rwikiObject;
	}

	public void setRWikiObject(RWikiObject rwikiObject)
	{
		this.rwikiObject = rwikiObject;
	}

	public RWikiObjectService getObjectService()
	{
		return objectService;
	}

	public void setObjectService(RWikiObjectService objectService)
	{
		this.objectService = objectService;
	}

	public RWikiSecurityService getSecurityService()
	{
		return securityService;
	}

	public void setSecurityService(RWikiSecurityService securityService)
	{
		this.securityService = securityService;
	}

	/*
	 * public String getUser() { cachable = false; return user; }
	 */
	/**
	 * @return true if the render operation is cachable (after rendering)
	 */
	public boolean isCachable()
	{
		return cachable;
	}

	public Site getSite()
	{
		String siteId = getSiteId();
		if (siteId == null)
		{
			return null;
		}
		
		try
		{
			return siteService.getSite(siteId);
		}
		catch (IdUnusedException ex)
		{
			return null;
		}
	}

	public String getSiteId()
	{
		RWikiObjectService rwobjService = getObjectService();
		RWikiObject rwobj = getRWikiObject();
		Reference ref = rwobjService.getReference(rwobj);

		String siteContext = ref.getContext();
		if (siteContext.startsWith("/site/"))
		{
			String siteId = siteContext.substring(6);

			int slash = siteId.indexOf("/");
			if (slash != -1)
			{
				siteId = siteId.substring(0, slash - 1);
			}
			return siteId;
		}
		return null;
	}
	
	public String convertLink(String link) {
		if (link.startsWith("sakai:/"))
		{
			String refSiteUrl = link.substring("sakai:/".length());
			if ( refSiteUrl.startsWith("/") ) {
				refSiteUrl = refSiteUrl.substring(1);
			}
			String[] parts = refSiteUrl.split("/");
			if (parts == null || parts.length < 1)
			{
				return "Link Cant be resolved";
			}

			String refSiteId = parts[0];
			String refSiteType = getSiteType(refSiteId, "group");
			
			if ((refSiteId != null && refSiteId.startsWith("~")) || refSiteType == null)
			{
				String remLink = link.substring("sakai:/".length());
				if ( remLink.startsWith("/") ) {
					remLink = remLink.substring(1);
				}
				if ( remLink.startsWith("~") ) {
					remLink = remLink.substring(1);
				}
				link = "/access/content/user/"
						+ remLink;

			}
			else
			{
				link = "/access/content/group/"
						+ link.substring("sakai:/".length());
			}
		}
		else if (link.startsWith("sakai-dropbox:/"))
		{
			String refSiteUrl = link.substring("sakai-dropbox:/".length());
			if ( refSiteUrl.startsWith("/") ) {
				refSiteUrl = refSiteUrl.substring(1);
			}
			String[] parts = refSiteUrl.split("/");
			if (parts == null || parts.length < 1)
			{
				return "Link Cant be resolved";
			}

			String refSiteId = parts[0];
			String refSiteType = getSiteType(refSiteId, "group");
			
			if ((refSiteId != null && refSiteId.startsWith("~")) || refSiteType == null)
			{
				String remLink = link.substring("sakai-dropbox:/".length());
				if ( remLink.startsWith("/") ) {
					remLink = remLink.substring(1);
				}
				if ( remLink.startsWith("~") ) {
					remLink = remLink.substring(1);
				}
				link = "/access/content/group-user/"
						+ remLink;

			}
			else
			{
				link = "/access/content/group-user/"
						+ link.substring("sakai-dropbox:/".length());
			}
		}
		else if (link.startsWith("worksite:/"))
		{
			String siteId = getSiteId();
			// need to check siteid
			String siteType = getSiteType(siteId, null);
			
			if ((siteId != null && siteId.startsWith("~")) || siteType == null)
			{
				if ( siteId.startsWith("~") ) {
					siteId = siteId.substring(1);
				}
				link = "/access/content/user/" + siteId + "/"
						+ link.substring("worksite:/".length());

			}
			else
			{
				link = "/access/content/group/" + siteId + "/"
						+ link.substring("worksite:/".length());
			}

		}
		else if (link.startsWith("dropbox:/"))
		{
			String siteId = getSiteId();
			// need to check siteid
			String siteType = getSiteType(siteId, null);
			
			if ((siteId != null && siteId.startsWith("~")) || siteType == null)
			{
				if ( siteId.startsWith("~") ) {
					siteId = siteId.substring(1);
				}
				link = "/access/content/group-user/" + siteId + "/"
						+ link.substring("dropbox:/".length());

			}
			else
			{
				link = "/access/content/group-user/" + siteId + "/"
						+ link.substring("dropbox:/".length());
			}

		}
		return link;
	}
	
	public String getSiteType(String siteId, String defaultType) {
		try
		{
			Site s = siteService.getSite(siteId);
			return s.getType();
		}
		catch (IdUnusedException e)
		{
			
			return defaultType;
		}
	}
	

}
