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

<%
		response.setContentType("text/html; charset=UTF-8");
		response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
		response.addDateHeader("Last-Modified", System.currentTimeMillis());
		response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
		response.addHeader("Pragma", "no-cache");
%>


<f:loadBundle basename="uk.ac.lancs.e_science.sakai.tools.blogger.bundle.Messages" var="msgs"/>

<f:view>
<sakai:view_container title="#{msgs.title}">
    <h:form id="mainForm">
        <sakai:view_content>
            <f:subview id="toolBar">
                <jsp:include page="Toolbar.jsp"></jsp:include>
            </f:subview>
            <sakai:messages />
            <h:panelGrid columns="3">
	            <sakai:group_box title="#{msgs.search}">
    	            <h:inputText id="idSearch" value="#{query.queryString}" size="30"  />
        	        <h:commandButton  action="#{postListViewerController.doSearch}"  value="#{msgs.search}"/>
            	</sakai:group_box>
				<h:outputText value="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" escape="false"></h:outputText>
	            <sakai:group_box title= "#{msgs.filterByVisibility}">
    	            <h:selectOneMenu id="selectVisibility"
        	            value ="#{postListViewerController.filterByVisibility}">
            	        <f:selectItems value="#{postListViewerController.visibilityList}"/>
                	</h:selectOneMenu>
	                <h:commandButton  action="#{postListViewerController.doApplyFilters}"  value="#{msgs.applyFilter}"/>
    	        </sakai:group_box>
    	    </h:panelGrid>
    	    <br/>
            <sakai:pager 
                         totalItems="#{postListViewerController.pagerTotalItems}"
                         firstItem="#{postListViewerController.pagerFirstItem}"
                         pageSize="#{postListViewerController.pagerNumItems}"
                         accesskeys="true"
                         immediate="true"/>


             <h:dataTable id="result"
                styleClass="listHier speciallistHier"
                value="#{postListViewerController.postList}"
                var="post">
                 <h:column>
                    <f:facet name="header">
                        <h:commandLink
                            title="#{msgs.postTitle}"
                            action="#{postListViewerController.doSortByTitle}">
                            <h:outputText value="#{msgs.postTitle}"/>
                        </h:commandLink>
                    </f:facet>
                    <h:commandLink
                        id="title"
                        title="#{post.title}"
                        action="#{postListViewerController.doShowPost}">
                        <h:outputText value="#{post.title}"/>
                    </h:commandLink>
                 </h:column>
                 <h:column>
                    <f:facet name="header">
                        <h:commandLink
                            title="#{msgs.postDate}"
                            action="#{postListViewerController.doSortByDate}">
                            <h:outputText value="#{msgs.postDate}"/>
                        </h:commandLink>
                    </f:facet>
                    <h:outputText value="#{post.date}">
                        <f:convertDateTime/>
                    </h:outputText>
                </h:column>
                 <h:column>
                    <f:facet name="header">
                        <h:commandLink
                            title="#{msgs.postCreator}"
                            action="#{postListViewerController.doSortByCreator}">
                            <h:outputText value="#{msgs.postCreator}"/>
                        </h:commandLink>
                    </f:facet>
                    <h:outputText value="#{post.creator.id}">
                    </h:outputText>
                </h:column>
                 <h:column>
                    <f:facet name="header">
                        <h:commandLink
                            title="#{msgs.postVisibility}"
                            action="#{postListViewerController.doSortByVisibility}">
                            <h:outputText value="#{msgs.postVisibility}"/>
                        </h:commandLink>
                    </f:facet>
                    <h:outputText value="#{post.state.visibility}">
                        <f:converter converterId="VisibilityCode"/>
                    </h:outputText>
                </h:column>
             </h:dataTable>
        </sakai:view_content>
    </h:form>
</sakai:view_container>

</f:view>