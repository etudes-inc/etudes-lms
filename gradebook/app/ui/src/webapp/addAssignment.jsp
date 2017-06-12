<%@ page import="org.sakaiproject.tool.gradebook.ui.AddAssignmentBean"%>
<f:view>
  <div class="portletBody">
	<h:form id="gbForm">

		<x:aliasBean alias="#{bean}" value="#{addAssignmentBean}">
			<%@include file="/inc/appMenu.jspf"%>
		</x:aliasBean>

		<sakai:flowState bean="#{addAssignmentBean}" />

		<%
		final javax.faces.context.FacesContext facesContext = javax.faces.context.FacesContext.getCurrentInstance();
		final AddAssignmentBean b = (AddAssignmentBean)facesContext.getApplication().getVariableResolver().resolveVariable(facesContext, "addAssignmentBean");

		if (b.getAssignmentsDisabled()) {
		%>

		<h2>Add Assignment Has Moved!</h2>
		<ul>
			<li style="margin-bottom:1em;">Any assignments you had added manually or imported in your gradebook <b>have been moved</b> into Assignments, Tests and Surveys (AT&amp;S) for you.</li>
			<li style="margin-bottom:1em;">To <b>add additional offline assignments</b> in your site, click on AT&amp;S on the left menu, click on Add, and choose &quot;Offline&quot; under the assessment type. For more information, see <a href="http://etudes.org/help/instructors/ats-add-offline-assignment" target="_blank">How to Add an Offline Assignment</a>.</li>
			<li> You may also <b>create offline assignments and import their scores</b> from a single spreadsheet (csv format). See <a href="http://etudes.org/help/instructors/import-assessments-and-scores-in-ats" target="_blank">Import Assessments and Scores</a>.</li>
		</ul>

		<% } else { %>

		<h2><h:outputText value="#{msgs.add_assignment_page_title}"/></h2>

		<div class="instruction"><h:outputText value="#{msgs.add_assignment_instruction}" escape="false"/></div>
		<p class="instruction"><h:outputText value="#{msgs.flag_required}"/></p>

		<h4><h:outputText value="#{msgs.add_assignment_header}"/></h4>

		<x:aliasBean alias="#{bean}" value="#{addAssignmentBean}">
			<%@include file="/inc/assignmentEditing.jspf"%>
		</x:aliasBean>

		<p class="act calendarPadding">
			<h:commandButton
				id="saveButton"
				styleClass="active"
				value="#{msgs.add_assignment_submit}"
				action="#{addAssignmentBean.saveNewAssignment}"/>
			<h:commandButton
				value="#{msgs.add_assignment_cancel}"
				action="overview" immediate="true"/>
		</p>

		<% } %>

	</h:form>
  </div>
</f:view>
