/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/jsf/widgets-sun/src/java/org/sakaiproject/jsf/util/JSFDepends.java $
 * $Id: JSFDepends.java 3 2008-10-20 18:44:42Z ggolden $
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

package org.sakaiproject.jsf.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This source file collects the dependencies of the Sakai tag library
 * on the JSF implementation into one place.
 * This is where JSF tags, renderers, and components that extend the
 * Sun JSF implementation (or the MyFaces implementation) live.
 * To switch between Sun RI vs. MyFaces, just comment/uncomment
 * the appropriate block of inner classes and recompile.
 */
public class JSFDepends
{
    private static final Log logger = LogFactory.getLog(JSFDepends.class);

	  /** Sun JSF RI dependent classes */
	  public static class CommandButtonTag extends com.sun.faces.taglib.html_basic.CommandButtonTag {}
	  public static class InputTextTag extends com.sun.faces.taglib.html_basic.InputTextTag {}
	  public static class OutputTextTag extends com.sun.faces.taglib.html_basic.OutputTextTag {}
	  public static class PanelGridTag extends com.sun.faces.taglib.html_basic.PanelGridTag {}
	  public static class DataTableTag extends com.sun.faces.taglib.html_basic.DataTableTag {}
	  public static class MessagesTag extends com.sun.faces.taglib.html_basic.MessagesTag {}
	  public static class ColumnTag extends com.sun.faces.taglib.html_basic.ColumnTag {}

	  public static class ButtonRenderer extends com.sun.faces.renderkit.html_basic.ButtonRenderer {}
	  public static class CommandLinkRenderer extends com.sun.faces.renderkit.html_basic.CommandLinkRenderer {}

}




