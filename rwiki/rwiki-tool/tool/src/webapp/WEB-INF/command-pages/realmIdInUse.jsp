<?xml version="1.0" encoding="UTF-8" ?>
<!--
/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/rwiki/rwiki-tool/tool/src/webapp/WEB-INF/command-pages/realmIdInUse.jsp $
 * $Id: realmIdInUse.jsp 3 2008-10-20 18:44:42Z ggolden $
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
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="2.0"
  xmlns:c="http://java.sun.com/jsp/jstl/core"
  xmlns:fn="http://java.sun.com/jsp/jstl/functions"
  ><jsp:directive.page language="java"
		contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
		errorPage="/WEB-INF/command-pages/errorpage.jsp" 
	/><jsp:text
	><![CDATA[<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"> ]]>
  </jsp:text>
  <c:set var="rightRenderBean"
    value="${requestScope.rsacMap.infoRightRenderBean}" />
  <c:set var="homeBean" value="${requestScope.rsacMap.homeBean}"/>
  <c:set var="realmEditBean" value="${requestScope.rsacMap.realmEditBean}"/>
  <c:set var="errorBean" value="${requestScope.rsacMap.errorBean}"/>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
    <head>
      <title>Edit Section: <c:out value="${realmEditBean.localSpace}" /></title>
      <jsp:expression>request.getAttribute("sakai.html.head")</jsp:expression>
    </head>
    <jsp:element name="body">
      <jsp:attribute name="onload"><jsp:expression>request.getAttribute("sakai.html.body.onload")</jsp:expression>parent.updCourier(doubleDeep,ignoreCourier); callAllLoaders();</jsp:attribute>
      <jsp:directive.include file="header.jsp"/>
      <div id="rwiki_container">
	<div class="portletBody">
	  <c:set var="rwikiContentStyle"  value="rwiki_content" />

	  <!-- Main page -->
	  <div id="${rwikiContentStyle}" >

	    <h3>Edit Section: <c:out value="${realmEditBean.localSpace}" /></h3>
	    <c:if test="${fn:length(errorBean.errors) gt 0}">
	      <!-- XXX This is hideous -->
	      <p class="validation" style="clear: none;">
		<c:forEach var="error" items="${errorBean.errors}">
		  <c:out value="${error}"/><br/>
		</c:forEach>
	      </p>
	    </c:if>
	      <div class="rwikirenderedContent">
		<p>Section is currently being editted by some other user.</p>
		<p>Please either <a href="${realmEditBean.infoUrl}">go back to page info</a> or <a href="${realmEditBean.editRealmUrl}">attempt to edit again</a>.</p>
	      </div>
<!--	    </form> -->
	  </div>
	</div>
      </div>
      <jsp:directive.include file="footer.jsp"/>
    </jsp:element>
  </html>
</jsp:root>
