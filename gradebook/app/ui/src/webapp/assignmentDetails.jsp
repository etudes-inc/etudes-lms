<link href="dhtmlpopup/dhtmlPopup.css" rel="stylesheet" type="text/css" />
<script src="dhtmlpopup/dhtmlPopup.js" type="text/javascript"></script>
<f:view>
<style type='text/css'>
.colorRow
{
background-color:#D0D0D0;
}
.colorEvenRow
{
background-color:#FFFFFF;
}
</style>
  <div class="portletBody">
	<h:form id="gbForm">

		<x:aliasBean alias="#{bean}" value="#{assignmentDetailsBean}">
			<%@include file="/inc/appMenu.jspf"%>
		</x:aliasBean>

		<sakai:flowState bean="#{assignmentDetailsBean}" />

		<h2><h:outputText value="#{assignmentDetailsBean.assignment.name}"/></h2>

		<h4><h:outputText value="#{msgs.assignment_details_page_title}"/></h4>
		<div class="indnt1">

		<p class="nav">
			<h:commandButton
				disabled="#{assignmentDetailsBean.first}"
				actionListener="#{assignmentDetailsBean.processAssignmentIdChange}"
				value="#{msgs.assignment_details_previous_assignment}"
				title="#{assignmentDetailsBean.previousAssignment.name}"
				accesskey="p"
				tabindex="4">
					<f:param name="assignmentId" value="#{assignmentDetailsBean.previousAssignment.id}"/>
			</h:commandButton>
			<h:commandButton
				action="overview"
				value="#{msgs.assignment_details_return_to_overview}"
				accesskey="l"
				tabindex="6"/>
			<h:commandButton
				disabled="#{assignmentDetailsBean.last}"
				actionListener="#{assignmentDetailsBean.processAssignmentIdChange}"
				value="#{msgs.assignment_details_next_assignment}"
				title="#{assignmentDetailsBean.nextAssignment.name}"
				accesskey="n"
				tabindex="5">
					<f:param name="assignmentId" value="#{assignmentDetailsBean.nextAssignment.id}"/>
			</h:commandButton>
		</p>

		<h:panelGrid cellpadding="0" cellspacing="0"
			columns="2"
			columnClasses="itemName"
			styleClass="itemSummary"
 			summary="Table contains information on assignment properties"
			border="0">
				<h:outputText id="titleLabel" value="#{msgs.assignment_details_title}"/>
				<h:outputText id="title" value="#{assignmentDetailsBean.assignment.name}"/>

				<h:outputText id="pointsLabel" value="#{msgs.assignment_details_point_value}"/>
				<h:outputText id="points" value="#{assignmentDetailsBean.assignment.pointsPossible}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.POINTS" />
				</h:outputText>

				<h:outputText id="averageLabel" value="#{msgs.assignment_details_average}" rendered="#{overviewBean.userAbleToGradeAll}"/>
				<h:outputText id="average" value="#{assignmentDetailsBean.assignment.formattedMean}" rendered="#{overviewBean.userAbleToGradeAll}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.PERCENTAGE"/>
				</h:outputText>

				<h:outputText id="dueDateLabel" value="#{msgs.assignment_details_due_date}"/>
				<h:outputText id="dueDate" value="#{assignmentDetailsBean.assignment.dueDate}" rendered="#{assignmentDetailsBean.assignment.dueDate != null}" >
                     <gbx:convertDateTime/>
                </h:outputText>
				<h:outputText id="dueDatePlaceholder" value="#{msgs.score_null_placeholder}" rendered="#{assignmentDetailsBean.assignment.dueDate == null}" />

				<h:outputText id="optionsLabel" value="#{msgs.assignment_details_options}" rendered="#{assignmentDetailsBean.userAbleToEditAssessments}"/>
				<h:panelGrid cellpadding="0" cellspacing="0" columns="1" rendered="#{assignmentDetailsBean.userAbleToEditAssessments}">
					<h:outputText
						value="#{msgs.score_not_counted_tooltip}"
						rendered="#{assignmentDetailsBean.assignment.notCounted}"
					/>
					<h:commandLink
						action="editAssignment"
						rendered="#{!assignmentDetailsBean.assignment.externallyMaintained}"
						accesskey="e"
						tabindex="7" styleClass="toolUiLink"
						title="#{msgs.assignment_details_edit}">
						<h:outputFormat id="editAssignment" value="#{msgs.assignment_details_edit}" />
						<f:param name="assignmentId" value="#{assignmentDetailsBean.assignment.id}"/>
					</h:commandLink>
					<h:commandLink
						action="removeAssignment"
						rendered="#{!assignmentDetailsBean.assignment.externallyMaintained}"
						accesskey="r"
						tabindex="8" styleClass="toolUiLink"
						title="#{msgs.assignment_details_remove}">
							<h:outputText id="removeAssignment" value="#{msgs.assignment_details_remove}"/>
						<f:param name="assignmentId" value="#{assignmentDetailsBean.assignment.id}"/>
					</h:commandLink>

					<h:outputLink
						value="#{assignmentDetailsBean.assignment.externalInstructorLink}"
						rendered="#{assignmentDetailsBean.assignment.externallyMaintained && not empty assignmentDetailsBean.assignment.externalInstructorLink}"
						accesskey="x"
						tabindex="9" styleClass="toolUiLink"
						title="#{msgs.assignment_details_edit}">
							<h:outputFormat value="#{msgs.assignment_details_external_edit}">
								<f:param value="#{assignmentDetailsBean.assignment.externalAppName}"/>
							</h:outputFormat>
					</h:outputLink>

					<h:outputFormat value="#{msgs.assignment_details_external_link_unavailable}" rendered="#{assignmentDetailsBean.assignment.externallyMaintained && empty assignmentDetailsBean.assignment.externalInstructorLink}">
						<f:param value="#{assignmentDetailsBean.assignment.externalAppName}"/>
					</h:outputFormat>
				</h:panelGrid>
		</h:panelGrid>

		</div> <!-- END OF INDNT1 -->

		<h4><h:outputText value="#{msgs.assignment_details_grading_table}"/></h4>
		<div class="indnt1">

		<%@include file="/inc/globalMessages.jspf"%>

		<x:aliasBean alias="#{bean}" value="#{assignmentDetailsBean}">
			<%@include file="/inc/filterPaging.jspf"%>
		</x:aliasBean>

		<x:dataTable cellpadding="0" cellspacing="0"
			id="gradingTable"
			value="#{assignmentDetailsBean.scoreRows}"
			var="scoreRow"
			rowIndexVar="scoreRowIndex"
			sortColumn="#{assignmentDetailsBean.sortColumn}"
			sortAscending="#{assignmentDetailsBean.sortAscending}"
			columnClasses="left,left,left,left,right,left"
			rowClasses="colorEvenRow,colorRow"
			styleClass="listHier narrowTable">
			<h:column>
				<f:facet name="header">
		            <x:commandSortHeader columnName="studentSortName" arrow="true" immediate="false" actionListener="#{assignmentDetailsBean.sort}">
						<h:outputText value="#{msgs.assignment_details_student_name}" styleClass="tier0"/>
		            </x:commandSortHeader>
				</f:facet>
				<h:outputText value="#{scoreRow.enrollment.enrollRec.user.sortName}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
		            <x:commandSortHeader columnName="studentDisplayId" arrow="true" immediate="false" actionListener="#{assignmentDetailsBean.sort}">
						<h:outputText value="#{msgs.assignment_details_student_id}" styleClass="tier0"/>
		            </x:commandSortHeader>
				</f:facet>
				<h:outputText value="#{scoreRow.enrollment.enrollRec.user.displayId}"/>
			</h:column>
            <h:column>
				<f:facet name="header">
		            <x:commandSortHeader columnName="sectionId" arrow="true" immediate="false" actionListener="#{assignmentDetailsBean.sort}">
						<h:outputText value="#{msgs.assignment_details_section_id}" styleClass="tier0"/>
		            </x:commandSortHeader>
				</f:facet>
				<h:outputText value="#{scoreRow.enrollment.sectionTitle}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.assignment_details_log}" styleClass="tier0"/>
				</f:facet>
				<h:outputLink value="#" styleClass="toolUiLink"
					rendered="#{not empty scoreRow.eventRows}"
					onclick="javascript:dhtmlPopupToggle('#{scoreRowIndex}', event);return false;">
					<h:graphicImage value="images/log.png" alt="Show log"/>
				</h:outputLink>
			</h:column>

			<h:column>
				<f:facet name="header">
		            <x:commandSortHeader columnName="studentScore" arrow="true" immediate="false" actionListener="#{assignmentDetailsBean.sort}">
						<h:outputText value="#{msgs.assignment_details_score}"/>
		            </x:commandSortHeader>
				</f:facet>

				<h:inputText id="Score" value="#{scoreRow.score}" size="4" rendered="#{!assignmentDetailsBean.assignment.externallyMaintained}" style="text-align:right;"
					onkeypress="return submitOnEnter(event, 'gbForm:saveButton');">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.NONTRAILING_DOUBLE" />
					<f:validateDoubleRange minimum="0"/>
					<f:validator validatorId="org.sakaiproject.gradebook.jsf.validator.ASSIGNMENT_GRADE"/>
				</h:inputText>

				<h:outputText value="#{scoreRow.score}" rendered="#{assignmentDetailsBean.assignment.externallyMaintained}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.POINTS" />
				</h:outputText>
			</h:column>
			<h:column>
				<h:message for="Score" styleClass="validationEmbedded"/>
			</h:column>
		</x:dataTable>

		<x:aliasBean alias="#{bean}" value="#{assignmentDetailsBean}">
			<%@include file="/inc/gradingEventLogs.jspf"%>
		</x:aliasBean>

		<p class="instruction">
			<h:outputText value="#{msgs.assignment_details_no_enrollments}" rendered="#{assignmentDetailsBean.emptyEnrollments}" />
		</p>

		</div> <!-- END OF INDNT1 -->

		<p class="act">
			<h:commandButton
				id="saveButton"
				styleClass="active"
				value="#{msgs.assignment_details_submit}"
				actionListener="#{assignmentDetailsBean.processUpdateScores}"
				disabled="#{assignmentDetailsBean.assignment.externallyMaintained}"
				rendered="#{!assignmentDetailsBean.emptyEnrollments}"
				accesskey="s"
				tabindex="9998"
				title="#{msgs.assignment_details_submit}"/>
			<h:commandButton
				value="#{msgs.assignment_details_cancel}"
				action="overview"
				immediate="true"
				disabled="#{assignmentDetailsBean.assignment.externallyMaintained}"
				rendered="#{!assignmentDetailsBean.emptyEnrollments}"
				accesskey="c"
				tabindex="9999"
				title="#{msgs.assignment_details_cancel}"/>
		</p>
	</h:form>
  </div>
</f:view>
