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
<%@ taglib uri="http://java.sun.com/upload" prefix="corejsf" %>
<%@ taglib uri="http://e_science.lancs.ac.uk/sakai-blogger-tool" prefix="blogger" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

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
<script src="/sakai-blogger-tool/script/blogger.js"></script> 
<style>
td.tabStyle{
	width:720px;
}
td.td1{
	width:80px;
}
td.td2{
	width:440px;
}
</style>
<script language="javascript">
	isChanged =false;
	tabContentIsChanged = false;
	buttonPressed="";
	doSubmit = false;
	desactivateVerify=false;
	function verifySave(otherChanges){
		var result = false;
		if (!desactivateVerify){
			if (buttonPressed=='SAVE' && tabContentIsChanged){
				result=window.confirm("You have changed an element but you do not have add or modify it in the document. Press OK to return to the editor. Press Cancel to ignore this message.");
			}
			if (buttonPressed=='PREVIEW' && tabContentIsChanged){
				result=window.confirm("You have changed an element but you do not have add or modify it in the document. Press OK to return to the editor. Press Cancel to ignore this message.");
			}
			if (buttonPressed=='CANCEL' && (isChanged || tabContentIsChanged || otherChanges)){
				result=window.confirm("You have modified the document, but you do not have save the changes. Press OK to return to the editor. Press Cancel to ignore this message.");
			}
		}
		doSubmit = !result;
		return !result;
	}
	function functionOnSubmitForTextArea(){
		return doSubmit;
	}
	function functionOnChangeInAbstract(){
		isChanged = true;
	}
	function functionOnChangeInText(){
		tabContentIsChanged = true;
	}
	
	
</script>

	<sakai:view_content>
	    <h:form id="PostForm"  onsubmit="javascript:return verifySave(#{postCreateController.isChanged});" enctype="multipart/form-data">
            <f:subview id="toolBar">
                <jsp:include page="Toolbar.jsp"></jsp:include>
            </f:subview>
            <sakai:messages />
            <sakai:instruction_message value="#{msgs.postEditor}"/>
                <h:panelGrid columns="2">
	                <h:panelGrid columns="2"  columnClasses="td1,td2">
		                <h:outputText value="#{msgs.postTitle} *:"/>
			            <h:inputText id="idTitle" value="#{postCreateController.post.title}" size="71" required="true" onkeypress="javascript:isChanged=true;" />
	        	        <h:outputText value="Keywords:"/>
	            	    <h:inputText size="71" value="#{postCreateController.keywords}" style="color:#CCCCCC" onkeyup="javascript:checkInputOnKeyUp(this,'#{postCreateController.keywordsMessage}');" onkeypress="javascript:checkInputOnKeyPress(this,'#{postCreateController.keywordsMessage}');isChanged=true;"/>
		                <h:outputText value="Abstract:"/>
	    	            <blogger:rich_text_area  onChange="functionOnChangeInAbstract" onSubmit="functionOnSubmitForTextArea" height="50" width="448" value="#{postCreateController.shortText}" toolbarButtonRows="0"/>
	                </h:panelGrid>                
	                <h:panelGrid columns="2">
			                <h:outputText value="#{msgs.postVisibility}:"/>
	        		        <h:selectOneMenu id="selectVisibility" value ="#{postCreateController.post.state.visibility}" onchange="javascript: isChanged=true;">
		            	        <f:selectItems value="#{postCreateController.visibilityList}"/>
	    	            	    <f:converter converterId="VisibilityCode"/>
	        		        </h:selectOneMenu>

			                <h:outputText value="#{msgs.readOnly}:"/>
			                <h:selectBooleanCheckbox id="readOnlyCheckBox" value ="#{postCreateController.post.state.readOnly}"></h:selectBooleanCheckbox>

			                <h:outputText value="#{msgs.allowComments}:" id="allowCommentsLabel"/>
			                <h:selectBooleanCheckbox id="allowCommentsCheckBox"  value ="#{postCreateController.post.state.allowComments}" ></h:selectBooleanCheckbox>
	                </h:panelGrid>                
                </h:panelGrid>                
	               
                
				<t:panelTabbedPane id="tabbedPane" bgcolor="#DDDFE4" tabContentStyleClass="tabStyle" >
					<t:panelTab id="tab0" label="Text">
               			<blogger:rich_text_area onChange="functionOnChangeInText"  onSubmit="functionOnSubmitForTextArea" rows="15" columns="92" value="#{postCreateController.editingText}" toolbarButtonRows="1"/>
		                <sakai:button_bar>
        		            <h:commandButton action="#{postCreateController.addParagraph}" value="Add to document" onclick="javascript:desactivateVerify=true;"/>
	            	        <h:commandButton action="#{postCreateController.modifyParagraph}" value="Modifiy in document (index: #{postCreateController.currentElementIndex})" rendered="#{postCreateController.showModifyParagraphButton}"  onclick="javascript:desactivateVerify=true;"/>
		                    <h:commandButton action="" value="Reset editor" immediate="true"  onclick="javascript:desactivateVerify=true;"/>
		                </sakai:button_bar>
					</t:panelTab>
					<t:panelTab id="tab1" label="Images" style="vertical-align:top">
						<h:panelGrid columns="2">
							<h:outputText value="Image name:"  rendered="#{postCreateController.showModifyImageButton}"></h:outputText>
		                	<h:outputText value="#{postCreateController.imageDescription}"  rendered="#{postCreateController.showModifyImageButton}"/>
							<h:outputText value="Image:"></h:outputText>
			        		<corejsf:upload target="image.jpg" value="#{postCreateController.image}"></corejsf:upload>
							<h:outputText value="#{msgs.note}:"></h:outputText>
							<h:outputText value="#{msgs.noteImages}"></h:outputText>
							<h:outputText value=""></h:outputText>
							<h:outputText value="#{msgs.maxFileSize}"></h:outputText>
			        	</h:panelGrid>
		                <sakai:button_bar>
		                    <h:commandButton action="#{postCreateController.addImage}" value="Add to document"  onclick="javascript:desactivateVerify=true;"/>
		                    <h:commandButton action="#{postCreateController.modifyImage}" value="Modifiy in document (index: #{postCreateController.currentElementIndex})" rendered="#{postCreateController.showModifyImageButton}"  onclick="javascript:desactivateVerify=true;"/>
		                </sakai:button_bar>
					</t:panelTab>
					<t:panelTab id="tab2" label="Links">
						<h:panelGrid columns="2">
							<h:outputText value="Description:"></h:outputText>
			    	        <h:panelGrid columns="2">
			                	<h:inputText id="idLinkDescription" value="#{postCreateController.linkDescription}" size="50" onkeypress="javascript: tabContentIsChanged=true;"/>
			    	            <h:outputText value=""></h:outputText>
			    	        </h:panelGrid>
			    	        <h:outputText value="URL:"></h:outputText>
							<h:panelGrid columns="2">
		    	            	<h:inputText id="idLinkExpression" value="#{postCreateController.linkExpression}" size="50" onkeypress="javascript: tabContentIsChanged=true;"/>
			    	            <h:outputText value="#{msgs.exampleURL}"></h:outputText>
			    	        </h:panelGrid>
		                </h:panelGrid>
		                <sakai:button_bar>
		                    <h:commandButton action="#{postCreateController.addLink}" value="Add to document"  onclick="javascript:desactivateVerify=true;"/>
		                    <h:commandButton action="#{postCreateController.modifyLink}" value="Modifiy in document (index: #{postCreateController.currentElementIndex})" rendered="#{postCreateController.showModifyLinkButton}"  onclick="javascript:desactivateVerify=true;"/>
		                </sakai:button_bar>
					</t:panelTab>
					<t:panelTab id="tab3" label="Files">
						<h:panelGrid columns="2">
							<h:outputText value="File name:"  rendered="#{postCreateController.showModifyFileButton}"></h:outputText>
		                	<h:outputText value="#{postCreateController.fileDescription}"  rendered="#{postCreateController.showModifyFileButton}"></h:outputText>
							<h:outputText value="URL:"></h:outputText>
			        		<corejsf:upload target="image.jpg" value="#{postCreateController.file}"></corejsf:upload>
							<h:outputText value="#{msgs.note}:"></h:outputText>
							<h:outputText value="#{msgs.maxFileSize}"></h:outputText>		                </h:panelGrid>
		                <sakai:button_bar>
		                    <h:commandButton action="#{postCreateController.addFile}" value="Add to document"  onclick="javascript:desactivateVerify=true;"/>
		                    <h:commandButton action="#{postCreateController.modifyFile}" value="Modifiy in document (index: #{postCreateController.currentElementIndex})" rendered="#{postCreateController.showModifyFileButton}"  onclick="javascript:desactivateVerify=true;"/>
		                </sakai:button_bar>
					
					</t:panelTab>
				</t:panelTabbedPane>
				

                <sakai:button_bar>
                    <h:commandButton action="#{postCreateController.doPreview}" value="Preview" onclick="javascript:buttonPressed='PREVIEW'; " />
                    <h:commandButton action="#{postCreateController.doSave}" value="#{msgs.save}" onclick="javascript:buttonPressed='SAVE';"/>
                    <h:commandButton action="main" value="#{msgs.cancel}" immediate="true" onclick="javascript:buttonPressed='CANCEL';"/>
                </sakai:button_bar>
			    <sakai:group_box title="Current structure:">
					<blogger:editPost post="#{postCreateController.post}" controller="#{postCreateController}"></blogger:editPost>
				</sakai:group_box>	    
	    </h:form>
    </sakai:view_content>
    
</sakai:view_container>

</f:view>