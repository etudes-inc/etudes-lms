<%@ page import="org.sakaiproject.tool.gradebook.ui.SpreadsheetUploadBean"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<f:view>
    <div class="portletBody">
        <h:form id="gbForm">
            <x:aliasBean alias="#{bean}" value="#{spreadsheetUploadBean}">
                <%@include file="/inc/appMenu.jspf"%>
            </x:aliasBean>
                        
			<%
			final javax.faces.context.FacesContext facesContext = javax.faces.context.FacesContext.getCurrentInstance();
			final SpreadsheetUploadBean b = (SpreadsheetUploadBean)facesContext.getApplication().getVariableResolver().resolveVariable(facesContext, "spreadsheetUploadBean");
	
			if (b.getAssignmentsDisabled()) {
			%>

            <h2>Upload/Import Has Moved!</h2>
            <ul>
            	<li style="margin-bottom:1em;">To <b>create multiple offline assignments and import their scores</b> from a single spreadsheet (csv format), click on &quot;Assignments, Tests and Surveys&quot; (AT&amp;S) on the left menu, click on Import, and then choose &quot;Import Assessments and Scores.&quot; For more information, refer to <a href="http://etudes.org/help/instructors/import-assessments-and-scores-in-ats" target="_blank">Import Assessments and Scores in AT&amp;S</a>.</li>
            	<li>You can also <b>import scores into an existing AT&amp;S assessment</b>. Go to AT&amp;S, click on &quot;Grading&quot;, click on the assessment, and then select &quot;Import Scores&quot;. For more details, see <a href="http://etudes.org/help/instructors/import-scores-in-ats" target="_blank">Import Scores In AT&amp;S</a>.</li>
            </ul>
            
           	<% } else { %>
            
            <h2><h:outputText value="#{msgs.loading_dock_page_title}"/></h2>
            <div class="instruction">
                <h:outputText value="#{msgs.loading_dock_instructions}" escape="false"/>
            </div>

            <h:panelGroup rendered="#{spreadsheetUploadBean.userAbleToEditAssessments}">
                <h:commandLink action="spreadsheetUpload" immediate="true" styleClass="toolUiLinkU">
                    <h:outputText value="#{msgs.loading_dock_upload_link_text}"/>
                </h:commandLink>
            </h:panelGroup>
            <p/>
            <p/>
            <%@include file="/inc/globalMessages.jspf"%>
            <h4><h:outputText value="#{msgs.loading_dock_table_header}"/></h4>
            <t:dataTable id="table1" value="#{spreadsheetUploadBean.spreadsheets}" var="row" rowIndexVar="rowIndex"
                         columnClasses="left,left,rightpadded,rightpadded,rightpadded"                         
                         styleClass="listHier narrowTable">

                <t:column>
                    <f:facet name="header">
                        <h:outputText value="#{msgs.loading_dock_table_title}"/>
                    </f:facet>
                    <h:outputText value="#{row.name}"/>
                </t:column>
                <t:column>
                    <f:facet name="header">
                        <h:outputText value="#{msgs.loading_dock_table_creator}"/>
                    </f:facet>
                    <h:outputText value="#{row.creator}"/>
                </t:column>


                <t:column>
                    <f:facet name="header">
                        <h:outputText value="#{msgs.loading_dock_table_datecreated}"/>
                    </f:facet>
                    <h:outputText value="#{row.dateCreated}">
                        <f:convertDateTime pattern="d MMM yyyy  H:mm:ss"/>
                    </h:outputText>
                </t:column>

                <t:column>
                    <h:commandLink action="#{spreadsheetUploadBean.viewItem}" styleClass="toolUiLink">
                        <h:outputText value="#{msgs.loading_dock_table_view}"/>
                        <f:param name="spreadsheetId" value="#{row.id}"/>
                    </h:commandLink>
                </t:column>
                <t:column>
                    <h:commandLink action="spreadsheetRemove" styleClass="toolUiLink">
                        <h:outputText value="#{msgs.loading_dock_table_delete}"/>
                        <f:param name="spreadsheetId" value="#{row.id}"/>
                    </h:commandLink>
                </t:column>
            </t:dataTable>

			<% } %>

        </h:form>
    </div>
</f:view>
