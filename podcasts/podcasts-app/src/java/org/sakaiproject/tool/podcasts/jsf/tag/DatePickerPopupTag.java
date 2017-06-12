/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/podcasts/podcasts-app/src/java/org/sakaiproject/tool/podcasts/jsf/tag/DatePickerPopupTag.java $
 * $Id: DatePickerPopupTag.java 3 2008-10-20 18:44:42Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
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



package org.sakaiproject.tool.podcasts.jsf.tag;

import javax.faces.webapp.UIComponentTag;

/**
 * <p> </p>
 * <p>Description:<br />
 * This class is the tag handler that evaluates the date picker popup
 * custom tag.</p>
 * <p>Based on example code by Sun Microsystems. </p>
 * <p>Organization: Sakai Project</p>
 * @author Ed Smiley
 * @version $Id: DatePickerPopupTag.java 3 2008-10-20 18:44:42Z ggolden $
 */

public class DatePickerPopupTag extends UIComponentTag
{

  public String getComponentType()
  {
    return ("javax.faces.Output");
  }

  public String getRendererType()
  {
    return "DatePickerPopup";
  }

}
