<?xml version="1.0" encoding="UTF-8" ?>
<!--
/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/rwiki/rwiki-tool/tool/src/webapp/WEB-INF/command-pages/footer.jsp $
 * $Id: footer.jsp 3 2008-10-20 18:44:42Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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
-->
<jspf:root xmlns:jspf="http://java.sun.com/JSP/Page" version="2.0" 
  xmlns:cfooter="http://java.sun.com/jsp/jstl/core"
  >
  <jspf:directive.page language="java"
    contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"/>
    <!-- this fixes the guillotine bug for IE and the resizing issue for Safari -->
    <div id="guillotineFixer">&#160;</div>
  <script type="text/javascript" ><cfooter:out value="${requestScope.footerScript}" escapeXml="false" /></script>
  <jspf:scriptlet>
    {
    long endofpage = System.currentTimeMillis();
    uk.ac.cam.caret.sakai.rwiki.utils.TimeLogger.printTimer("END Of Page:",endofpage,endofpage);
    }
  </jspf:scriptlet>
</jspf:root>
