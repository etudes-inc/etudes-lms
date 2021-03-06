/**********************************************************************************
* $URL: https://source.etudes.org/svn/etudes/source/trunk/jsf/widgets/src/java/org/sakaiproject/jsf/renderer/DebugRenderer.java $
* $Id: DebugRenderer.java 3 2008-10-20 18:44:42Z ggolden $
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Sakai Foundation.
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
import java.util.Iterator;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;


public class DebugRenderer extends Renderer
{
	public boolean supportsComponentType(UIComponent component)
	{
		return (component instanceof UIOutput);
	}

	public void encodeBegin(FacesContext context, UIComponent component) throws IOException
	{
		if (!component.isRendered()) return;

		ResponseWriter writer = context.getResponseWriter();
		writer.write("<xmp>");
		writer.write("***** DEBUG TAG RENDER OUTPUT *****\n\n");

		dumpJSFVariable("applicationScope", context);
		dumpJSFVariable("sessionScope", context);
		dumpJSFVariable("requestScope", context);
		dumpJSFVariable("toolScope", context);
		dumpJSFVariable("toolConfig", context);
		dumpJSFVariable("param", context);
		writer.write("</xmp>");
	}

	private void dumpJSFVariable(String varName, FacesContext context)
		throws IOException
	{
		Object varValue = context.getApplication().getVariableResolver().resolveVariable(context, varName);
		ResponseWriter writer = context.getResponseWriter();

		if (varValue instanceof Map)
		{
		    dumpMap((Map) varValue, varName, writer);
		}
		else
		{
		    writer.write(varName);
		    writer.write(": ");
		    writer.write(String.valueOf(varValue));
		    writer.write("\n\n");
		}

	}

	private void dumpMap(Map map, String mapName, ResponseWriter writer)
		throws IOException
	{
	    writer.write(mapName);
	    if (map == null)
	    {
	        writer.write(" is null\n\n");
	        return;
	    }

	    writer.write(" " + map + " contains: \n");
	    Iterator i = map.keySet().iterator();
	    while (i.hasNext())
	    {
	        Object name = i.next();
	        Object value = map.get(name);
	        writer.write(String.valueOf(name));
	        writer.write(" -> ");
	        writer.write(String.valueOf(value));
	        writer.write('\n');
	    }

	    writer.write("\n\n");
	}
}



