<f:view>
  <div class="portletBody">
	<h:form id="gbForm">
		<x:aliasBean alias="#{bean}" value="#{feedbackOptionsBean}">
			<%@include file="/inc/appMenu.jspf"%>
		</x:aliasBean>

		<sakai:flowState bean="#{feedbackOptionsBean}" />

		<h2><h:outputText value="#{msgs.feedback_options_page_title}"/></h2>

		<div class="instruction"><h:outputText value="#{msgs.feedback_options_instruction}" escape="false"/></div>

		<div class="indnt1">

<!-- Grade Display -->
		<h4><h:outputText value="#{msgs.feedback_options_grade_display}"/></h4>
		<h:panelGrid columns="2" columnClasses="prefixedCheckbox">
			<h:selectBooleanCheckbox id="displayAssignmentGrades" value="#{feedbackOptionsBean.localGradebook.assignmentsDisplayed}"
				onkeypress="return submitOnEnter(event, 'gbForm:saveButton');"/>
			<h:outputLabel for="displayAssignmentGrades" value="#{msgs.feedback_options_grade_display_assignment_grades}" />

			<h:selectBooleanCheckbox id="displayTodatePoints" value="#{feedbackOptionsBean.localGradebook.todatePointsDisplayed}"
				onkeypress="return submitOnEnter(event, 'gbForm:saveButton');"/>
			<h:outputLabel for="displayTodatePoints" value="#{msgs.feedback_options_grade_display_todate_points}" />
			
			 <h:selectBooleanCheckbox id="displayTodateGrades" value="#{feedbackOptionsBean.localGradebook.todateGradeDisplayed}"
				onkeypress="return submitOnEnter(event, 'gbForm:saveButton');"/>
			<h:outputLabel for="displayTodateGrades" value="#{msgs.feedback_options_grade_display_todate_grade}" />
			
			
			<h:selectBooleanCheckbox id="displayCourseGrades" value="#{feedbackOptionsBean.localGradebook.courseGradeDisplayed}"
				onkeypress="return submitOnEnter(event, 'gbForm:saveButton');"/>
			<h:outputLabel for="displayCourseGrades" value="#{msgs.feedback_options_grade_display_overall_grade}" />
		
			<h:selectBooleanCheckbox id="dropLowestGrade" value="#{feedbackOptionsBean.localGradebook.dropGradeDisplayed}"
				onkeypress="return submitOnEnter(event, 'gbForm:saveButton');"/>
			<h:outputLabel for="dropLowestGrade" value="#{msgs.feedback_options_grade_drop_lowest_grade}" />
		
		</h:panelGrid>

<!-- Grade Conversion -->
		<h4><h:outputText value="#{msgs.feedback_options_grade_conversion}"/></h4>
		<h:panelGrid cellpadding="0" cellspacing="0"
			columns="2"
			columnClasses="itemName"
			styleClass="itemSummary">

			<h:outputText value="#{msgs.feedback_options_grade_type}" />

			<h:panelGroup>
				<h:selectOneMenu id="selectGradeType" value="#{feedbackOptionsBean.selectedGradeMappingId}">
					<f:selectItems value="#{feedbackOptionsBean.gradeMappingsSelectItems}" />
				</h:selectOneMenu>
				<f:verbatim> </f:verbatim>
				<h:commandButton actionListener="#{feedbackOptionsBean.changeGradeType}" value="#{msgs.feedback_options_change_grade_type}" />
			</h:panelGroup>
		</h:panelGrid>

		<%@include file="/inc/globalMessages.jspf"%>

<!-- RESET TO DEFAULTS LINK -->
		<p>
		<h:commandLink actionListener="#{feedbackOptionsBean.resetMappingValues}" styleClass="toolUiLinkU">
			<h:outputText value="#{msgs.feedback_options_reset_mapping_values}" />
		</h:commandLink>
		</p>

<!-- GRADE MAPPING TABLE -->
		<x:dataTable cellpadding="0" cellspacing="0"
			id="mappingTable"
			value="#{feedbackOptionsBean.gradeRows}"
			var="gradeRow"
			styleClass="listHier narrowTable">
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.feedback_options_grade_header}"/>
				</f:facet>
				<h:outputText value="#{gradeRow.grade}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.feedback_options_percent_header}"/>
				</f:facet>
				<h:outputText value="#{gradeRow.mappingValue}"
					rendered="#{!gradeRow.gradeEditable}"/>
				<h:inputText id="mappingValue" value="#{gradeRow.mappingValue}"
					rendered="#{gradeRow.gradeEditable}"
					onkeypress="return submitOnEnter(event, 'gbForm:saveButton');"/>
				<h:message for="mappingValue" styleClass="validationEmbedded" />
			</h:column>
		</x:dataTable>

		</div> <!-- END INDNT1 -->

		<p class="act">
			<h:commandButton
				id="saveButton"
				styleClass="active"
				value="#{msgs.feedback_options_submit}"
				action="#{feedbackOptionsBean.save}" />
			<h:commandButton
				value="#{msgs.feedback_options_cancel}"
				action="#{feedbackOptionsBean.cancel}"
				immediate="true" />
		</p>

	</h:form>
  </div>
</f:view>
