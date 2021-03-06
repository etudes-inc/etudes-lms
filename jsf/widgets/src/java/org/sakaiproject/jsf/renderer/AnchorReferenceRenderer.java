/**********************************************************************************
* $URL: https://source.etudes.org/svn/etudes/source/trunk/jsf/widgets/src/java/org/sakaiproject/jsf/renderer/AnchorReferenceRenderer.java $
* $Id: AnchorReferenceRenderer.java 3 2008-10-20 18:44:42Z ggolden $
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
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import org.sakaiproject.jsf.util.RendererUtil;

/**
 * <p>Description: </p>
 * <p>Render an anchor component with
 * <code>name</code> attribute.</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id: AnchorReferenceRenderer.java 3 2008-10-20 18:44:42Z ggolden $
 */

public class AnchorReferenceRenderer extends Renderer
{

  public boolean supportsComponentType(UIComponent component)
  {
    return (component instanceof UIOutput);
  }


  /**
   * <p>Render a an anchor tag with name attribute.</p>
   * @param context   FacesContext for the request we are processing
   * @param component UIComponent to be rendered
   *
     * @throws IOException          if an input/output error occurs while rendering
   * @throws NullPointerException if <code>context</code>
   *                              or <code>component</code> is null
   */
  /**
   * <p>Faces render output method to output script tag.</p>
     * <p>Method Generator: org.sakaiproject.tool.assessment.devtoolsRenderMaker</p>
   *
   *  @param context   <code>FacesContext</code> for the current request
   *  @param component <code>UIComponent</code> being rendered
   *
   * @throws IOException if an input/output error occurs
   */
  public void encodeBegin(FacesContext context, UIComponent component)
    throws IOException
  {

    if (!component.isRendered())
    {
      return;
    }

    String name = (String) RendererUtil.getAttribute(context, component, "name");

    ResponseWriter writer = context.getResponseWriter();
    String contextPath = context.getExternalContext()
      .getRequestContextPath();
    writer.write("<a name=\"" + name +  "\"/>");
  }

}
