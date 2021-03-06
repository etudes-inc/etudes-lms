/**********************************************************************************
* $URL: https://source.etudes.org/svn/etudes/source/trunk/jsf/widgets/src/java/org/sakaiproject/jsf/renderer/CourierRenderer.java $
* $Id: CourierRenderer.java 3 2008-10-20 18:44:42Z ggolden $
***********************************************************************************
*
* Copyright (c) 2005 The Sakai Foundation.
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


package org.sakaiproject.jsf.renderer;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.jsf.util.RendererUtil;


public class CourierRenderer extends Renderer
{
	public boolean supportsComponentType(UIComponent component)
	{
		return (component instanceof UIOutput);
	}

	public void decode(FacesContext context, UIComponent component)
	{
	}

	public void encodeBegin(FacesContext context, UIComponent component) throws IOException
	{
	}

	public void encodeChildren(FacesContext context, UIComponent component) throws IOException
	{
	}


	public void encodeEnd(FacesContext context, UIComponent component) throws IOException
	{
		ResponseWriter writer = context.getResponseWriter();
		HttpServletRequest req = (HttpServletRequest) context.getExternalContext().getRequest();

		// update time, in seconds
		String updateTime = (String) RendererUtil.getAttribute(context, component, "refresh");
		if (updateTime == null || updateTime.length() == 0)
		{
			updateTime = "10";
		}
		
		// the current tool's placement ID
		String placementId = (String) req.getAttribute("sakai.tool.placement.id");
		if (placementId == null)
		{
			// FIXME:
			// TODO: Report an error
		}
		writer.write("<script type=\"text/javascript\" language=\"JavaScript\">\n");
		writer.write("updateTime = " + updateTime + "000;\n");
		writer.write("updateUrl = \"" + serverUrl(req) + "/courier/" + placementId + "\";\n");
		writer.write("scheduleUpdate();\n");
		writer.write("</script>\n");
	}
	
	/** 
	 * This method is a duplicate of org.sakaiproject.util.web.Web.serverUrl()
	 * Duplicated here from org.sakaiproject.util.web.Web.java so that 
	 * the JSF tag library doesn't have a direct jar dependency on more of Sakai.
	 */
	private static String serverUrl(HttpServletRequest req)
	{
		StringBuffer url = new StringBuffer();
		url.append(req.getScheme());
		url.append("://");
		url.append(req.getServerName());
		if (((req.getServerPort() != 80) && (!req.isSecure())) || ((req.getServerPort() != 443) && (req.isSecure())))
		{
			url.append(":");
			url.append(req.getServerPort());
		}

		return url.toString();
	}
}



