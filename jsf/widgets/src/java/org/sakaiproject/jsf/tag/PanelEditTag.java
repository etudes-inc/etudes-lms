/**********************************************************************************
* $URL: https://source.etudes.org/svn/etudes/source/trunk/jsf/widgets/src/java/org/sakaiproject/jsf/tag/PanelEditTag.java $
* $Id: PanelEditTag.java 3 2008-10-20 18:44:42Z ggolden $
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

import org.sakaiproject.jsf.util.JSFDepends;

/**
 * A two-column layout for when the user is editing something
 * (defined by the style guide).
 */
public class PanelEditTag extends JSFDepends.PanelGridTag
{
	public String getComponentType()
	{
		return "org.sakaiproject.PanelEdit";
	}
}



