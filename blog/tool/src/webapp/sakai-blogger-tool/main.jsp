<%-- Copyright (c) 2006. Centre for e-Science. Lancaster University. United Kingdom. 

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>

<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://e_science.lancs.ac.uk/sakai-blogger-tool" prefix="blogger" %>

<%
    response.setContentType("text/html; charset=UTF-8");
    response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
    response.addDateHeader("Last-Modified", System.currentTimeMillis());
    response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
    response.addHeader("Pragma", "no-cache");
%>


<f:loadBundle basename="uk.ac.lancs.e_science.sakai.tools.blogger.bundle.Messages" var="msgs"/>

<f:view>

<sakai:view title="#{msgs.title}">
    <h:form>
           <f:subview id="toolBar">
               <jsp:include page="Toolbar.jsp"></jsp:include>
           </f:subview>
           <sakai:instruction_message value="Last entries"/>
		<blogger:postListing postListing="#{postListViewerController.lastPosts}"  action="#{postListViewerController.showPostFromPostListingJSFComponent}"></blogger:postListing>
    </h:form>
</sakai:view>
</f:view>
