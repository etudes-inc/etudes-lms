/**********************************************************************************
* $URL: https://source.etudes.org/svn/etudes/source/trunk/jsf/widgets/src/java/org/sakaiproject/jsf/tag/AppletTag.java $
* $Id: AppletTag.java 3 2008-10-20 18:44:42Z ggolden $
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


package org.sakaiproject.jsf.tag;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;
import org.sakaiproject.jsf.util.TagUtil;

/**
 * <p>Description:<br />
 * This class is the tag handler that evaluates the <code>applet</code>
 * custom tag.</p>
 * <p>Display a java applet.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id: AppletTag.java 3 2008-10-20 18:44:42Z ggolden $
 */

public class AppletTag extends UIComponentTag
{
  private String name = null;
  private String version;
  private String codebase;
  private String width;
  private String height;
  private String vspace;
  private String hspace;
  private String javaClass;
  private String javaArchive;
  private String paramList;

  public void setName(String name)
  {
    this.name = name;
  }


  public String getComponentType()
  {
    return ("javax.faces.Output");
  }

  public String getRendererType()
  {
    return "org.sakaiproject.Applet";
  }

  protected void setProperties(UIComponent component)
  {

    super.setProperties(component);
    TagUtil.setString(component, "name", name);
    TagUtil.setString(component, "version", version);
    TagUtil.setString(component, "codebase", codebase);
    TagUtil.setString(component, "width", width);
    TagUtil.setString(component, "height", height);
    TagUtil.setString(component, "vspace", vspace);
    TagUtil.setString(component, "hspace", hspace);
    TagUtil.setString(component, "javaClass", javaClass);
    TagUtil.setString(component, "javaArchive", javaArchive);
    TagUtil.setString(component, "paramList", paramList);
  }
  public void setVersion(String version)
  {
    this.version = version;
  }
  public void setCodebase(String codebase)
  {
    this.codebase = codebase;
  }
  public void setWidth(String width)
  {
    this.width = width;
  }
  public void setHeight(String height)
  {
    this.height = height;
  }
  public void setVspace(String vspace)
  {
    this.vspace = vspace;
  }
  public void setHspace(String hspace)
  {
    this.hspace = hspace;
  }
  public void setJavaClass(String javaClass)
  {
    this.javaClass = javaClass;
  }
  public void setJavaArchive(String javaArchive)
  {
    this.javaArchive = javaArchive;
  }
  public String getParamList()
  {
    return paramList;
  }
  public void setParamList(String paramList)
  {
    this.paramList = paramList;
  }

}
