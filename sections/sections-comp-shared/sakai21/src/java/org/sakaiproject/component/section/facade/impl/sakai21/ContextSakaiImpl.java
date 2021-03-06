/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-comp-shared/sakai21/src/java/org/sakaiproject/component/section/facade/impl/sakai21/ContextSakaiImpl.java $
 * $Id: ContextSakaiImpl.java 3 2008-10-20 18:44:42Z ggolden $
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
package org.sakaiproject.component.section.facade.impl.sakai21;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.facade.manager.Context;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * Uses Sakai's ToolManager to determine the current context.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class ContextSakaiImpl implements Context {
	private static final Log log = LogFactory.getLog(ContextSakaiImpl.class);

	/**
	 * @inheritDoc
	 */
	public String getContext(Object request) {
        Placement placement = ToolManager.getCurrentPlacement();        
        if(placement == null) {
            log.error("Placement is null");
        }
        return placement.getContext();
	}

	public String getContextTitle(Object request) {
		String siteContext = getContext(null);
		String siteTitle = null;
		try {
			siteTitle = SiteService.getSite(siteContext).getTitle();
		} catch (IdUnusedException e) {
			log.error("Unable to get site for context " + siteContext);
			siteTitle = siteContext; // Better than nothing???
		}
		return siteTitle;
	}
}
