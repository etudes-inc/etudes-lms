/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/jsf/widgets/src/java/org/sakaiproject/jsf/tag/ToolBarMessageTag.java $
 * $Id: ToolBarMessageTag.java 3 2008-10-20 18:44:42Z ggolden $
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005 The Sakai Foundation.
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


public class ToolBarMessageTag extends UIComponentTag
{
  private String value = null;

  public String getComponentType()
  {
    return "org.sakaiproject.ToolBarMessage";
  }

  public String getRendererType()
  {
    return "org.sakaiproject.ToolBarMessage";
  }

  public String getValue()
  {
    return value;
  }

  protected void setProperties(UIComponent component)
  {
    super.setProperties(component);
    TagUtil.setString(component, "value", value);
  }

  public void setValue(String string)
  {
    value = string;
  }
}



