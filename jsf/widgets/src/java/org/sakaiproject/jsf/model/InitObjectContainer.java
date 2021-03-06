/**********************************************************************************
* $URL: https://source.etudes.org/svn/etudes/source/trunk/jsf/widgets/src/java/org/sakaiproject/jsf/model/InitObjectContainer.java $
* $Id: InitObjectContainer.java 3 2008-10-20 18:44:42Z ggolden $
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
package org.sakaiproject.jsf.model;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: John Ellis
 * Date: Nov 21, 2005
 * Time: 4:19:36 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * Implement this interface for a component that collapses a div.
 * Any children that require re-init during div collapsing will pass a script
 * These scripts must be executed during collapsing and expanding the div
 */
public interface InitObjectContainer {

   public void addInitScript(String script);

   public List getInitScripts();

}
