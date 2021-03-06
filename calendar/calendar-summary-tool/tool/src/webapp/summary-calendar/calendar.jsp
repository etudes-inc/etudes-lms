<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<% 
	response.setContentType("text/html; charset=UTF-8");
	response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
	response.addDateHeader("Last-Modified", System.currentTimeMillis());
	response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
	response.addHeader("Pragma", "no-cache");
%>
<f:loadBundle basename="org.sakaiproject.tool.summarycalendar.bundle.Messages" var="msgs"/>

<f:view>
<sakai:view title="#{msgs.tool_title}" id="sakaiview">
	<sakai:flowState bean="#{CalBean}"/>

	<style type="text/css">
		@import url("summary-calendar/css/cal.css");
	</style>	

	<h:form id="calendarForm">
	<%/*<h:panelGroup>
       <sakai:tool_bar>
			<sakai:tool_bar_item action="#{MenuBean.processPreferences}" value="#{msgs.menu_prefs}" />
		</sakai:tool_bar>
    </h:panelGroup>*/%>
	    
	
			
	<t:div style="width:100%;" id="div100">
	
		<h:panelGrid styleClass="sectionContainerNav" style="width:100%" columns="2" columnClasses="calLeft,calRight" id="panelgrid_top"> 		
			<t:div style="text-align: left; white-space: nowrap;">
				<f:verbatim><h3></f:verbatim><h:outputText value="#{CalBean.caption}"/><f:verbatim></h3></f:verbatim>
			</t:div>
	        <t:div style="text-align: right; white-space: nowrap; ">
	        	<h:commandButton value=" < " actionListener="#{CalBean.prevMonth}" />
	        	<h:commandButton value="#{msgs.today}" actionListener="#{CalBean.currMonth}"/>
	        	<h:commandButton value=" > " actionListener="#{CalBean.nextMonth}" />
	        </t:div>
		</h:panelGrid>
		
		<t:dataTable
			id="table_calendar"
			value="#{CalBean.weeks}"
			var="week"
			style="width:100%; text-align: center;"
			headerClass="calHeader"
			columnClasses="calDay"
			>
			<h:column id="sunday">
				<f:facet name="header">	 
		           <h:outputText value="#{msgs.day_sun}"/>		                
		        </f:facet>
		        <t:div style="width:100%; text-align: center;" styleClass="#{week.days[0].styleClass}">
			        <h:commandLink styleClass="toolUiLink" value="#{week.days[0].dayOfMonth}" actionListener="#{CalBean.selectDate}" rendered="#{week.days[0].hasEvents}" >
				        <f:param name="selectedDay" value="#{week.days[0].dateAsString}"/>
			        </h:commandLink>
			        <h:outputText value="#{week.days[0].dayOfMonth}" rendered="#{!week.days[0].hasEvents}" />
				</t:div>
			</h:column>
			<h:column id="monday">
				<f:facet name="header">	 
		           <h:outputText value="#{msgs.day_mon}"/>		                
		        </f:facet>		        
		        <t:div style="width:100%; text-align: center;" styleClass="#{week.days[1].styleClass}">
			        <h:commandLink styleClass="toolUiLink" value="#{week.days[1].dayOfMonth}" actionListener="#{CalBean.selectDate}" rendered="#{week.days[1].hasEvents}" >
			            <f:param name="selectedDay" value="#{week.days[1].dateAsString}"/>
			        </h:commandLink>
			        <h:outputText value="#{week.days[1].dayOfMonth}" rendered="#{!week.days[1].hasEvents}" />
				</t:div>
			</h:column>
			<h:column id="tuesday">
				<f:facet name="header">	 
		           <h:outputText value="#{msgs.day_tue}"/>		                
		        </f:facet>		        
		        <t:div style="width:100%; text-align: center;" styleClass="#{week.days[2].styleClass}">
			        <h:commandLink styleClass="toolUiLink" value="#{week.days[2].dayOfMonth}" actionListener="#{CalBean.selectDate}" rendered="#{week.days[2].hasEvents}">
			            <f:param name="selectedDay" value="#{week.days[2].dateAsString}"/>
			        </h:commandLink>
			        <h:outputText value="#{week.days[2].dayOfMonth}" rendered="#{!week.days[2].hasEvents}" />
				</t:div>
			</h:column>
			<h:column id="wednesday">
				<f:facet name="header">	 
		           <h:outputText value="#{msgs.day_wed}"/>		                
		        </f:facet>		        
		        <t:div style="width:100%; text-align: center;" styleClass="#{week.days[3].styleClass}">
			        <h:commandLink styleClass="toolUiLink" value="#{week.days[3].dayOfMonth}" actionListener="#{CalBean.selectDate}" rendered="#{week.days[3].hasEvents}" >
			            <f:param name="selectedDay" value="#{week.days[3].dateAsString}"/>
			        </h:commandLink>
			        <h:outputText value="#{week.days[3].dayOfMonth}" rendered="#{!week.days[3].hasEvents}" />
				</t:div>
			</h:column>
			<h:column id="thursday">
				<f:facet name="header">	 
		           <h:outputText value="#{msgs.day_thu}"/>		                
		        </f:facet>
		        <t:div style="width:100%; text-align: center;" styleClass="#{week.days[4].styleClass}">
			        <h:commandLink styleClass="toolUiLink" value="#{week.days[4].dayOfMonth}" actionListener="#{CalBean.selectDate}" rendered="#{week.days[4].hasEvents}" >
			            <f:param name="selectedDay" value="#{week.days[4].dateAsString}"/>
			        </h:commandLink>
			        <h:outputText value="#{week.days[4].dayOfMonth}" rendered="#{!week.days[4].hasEvents}" />
				</t:div>
			</h:column>
			<h:column id="friday">
				<f:facet name="header">	 
		           <h:outputText value="#{msgs.day_fri}"/>		                
		        </f:facet>
		        <t:div style="width:100%; text-align: center;" styleClass="#{week.days[5].styleClass}">
			        <h:commandLink styleClass="toolUiLink" value="#{week.days[5].dayOfMonth}" actionListener="#{CalBean.selectDate}" rendered="#{week.days[5].hasEvents}">
			            <f:param name="selectedDay" value="#{week.days[5].dateAsString}"/>
			        </h:commandLink>
			        <h:outputText value="#{week.days[5].dayOfMonth}" rendered="#{!week.days[5].hasEvents}" />
				</t:div>
			</h:column>
			<h:column id="saturday">
				<f:facet name="header">	 
		           <h:outputText value="#{msgs.day_sat}"/>		                
		        </f:facet>
		        <t:div style="width:100%; text-align: center;" styleClass="#{week.days[6].styleClass}">
			        <h:commandLink styleClass="toolUiLink" value="#{week.days[6].dayOfMonth}" actionListener="#{CalBean.selectDate}" rendered="#{week.days[6].hasEvents}" >
			            <f:param name="selectedDay" value="#{week.days[6].dateAsString}"/>
			        </h:commandLink>
			        <h:outputText value="#{week.days[6].dayOfMonth}" rendered="#{!week.days[6].hasEvents}" />
				</t:div>
			</h:column>
		</t:dataTable>
		
		
		<%/* Selected day events */%>
		<t:div id="div_event_list" rendered="#{CalBean.viewingSelectedDay}" style="width:100%; padding-top: 25px;">
			<f:verbatim><h4></f:verbatim><h:outputText value="#{msgs.selectedDayEvents} #{CalBean.selectedDayAsString}"/><f:verbatim></h4></f:verbatim>
			<t:dataList id="datalist_event_list"
		        var="event"
		        value="#{CalBean.selectedDayEvents}"
		        layout="simple"
       		 	style="width:100%">
		        <t:graphicImage value="#{CalBean.eventImageMap[event.type]}"/>
				<t:outputText value=" #{event.type} - "/>
			    <t:commandLink value="#{event.truncatedDisplayName}" actionListener="#{CalBean.selectEvent}" immediate="false">
			       	<f:param name="calendarRef" value="#{event.calendarRef}"/>
			       	<f:param name="eventRef" value="#{event.eventRef}"/>
			    </t:commandLink>
		        <f:verbatim><br></f:verbatim>
		    </t:dataList>
		</t:div>
		
		<%/* Selected event */%>
		<t:div id="div_selected_event" rendered="#{CalBean.viewingSelectedEvent}" style="width:100%; padding-top: 25px;">
			<f:verbatim><h4></f:verbatim><h:outputText value="#{CalBean.selectedEvent.displayName}"/><f:verbatim></h4></f:verbatim>
			<h:panelGrid id="panel_selected_event" styleClass="sectionContainerNav" style="width:100%; " columns="2" columnClasses="calTop,calTop"> 		
				<h:outputLabel for="date" value="#{msgs.date}" />
		        <h:outputText id="date" value="#{CalBean.selectedEvent.date}" />
				<h:outputLabel for="type" value="#{msgs.type}" />
				<t:div>
		        	<t:graphicImage value="#{CalBean.eventImageMap[CalBean.selectedEvent.type]}"/>
		        	<h:outputText id="type" value="#{CalBean.selectedEvent.type}" style="padding-left: 3px;"/>
		        </t:div>
				<h:outputLabel for="description" value="#{msgs.description}" />
		        <h:outputText id="description" value="#{CalBean.selectedEvent.description}" />
				
				<f:verbatim><p></f:verbatim><f:verbatim><p></f:verbatim>				
				<h:outputLabel for="location" value="#{msgs.location}" rendered="#{CalBean.selectedEvent.hasLocation}" />
		        <h:outputText id="location" value="#{CalBean.selectedEvent.location}" rendered="#{CalBean.selectedEvent.hasLocation}" />
				<h:outputLabel for="groups" value="#{msgs.groups}" rendered="#{CalBean.selectedEvent.showGroups}" />
		        <h:outputText id="groups" value="#{CalBean.selectedEvent.groups}" rendered="#{CalBean.selectedEvent.showGroups}" />
				<h:outputLabel for="site" value="#{msgs.site}" />
		        <h:outputText id="site" value="#{CalBean.selectedEvent.site}" />
				
				<f:verbatim><p></f:verbatim><f:verbatim><p></f:verbatim>			
				<t:div styleClass="act">
					<h:commandButton value="#{msgs.back}" actionListener="#{CalBean.backToEventList}" immediate="true"/>
				</t:div>
				<f:verbatim>&nbsp;</f:verbatim>
		        	<%/* <h:outputLink value="#{CalBean.selectedEvent.url}">
		        		<h:outputText value="#{msgs.openInSchedule}"/>
		        	</h:outputLink> */%>				
			</h:panelGrid>
		</t:div>
		
	</t:div>
	
	</h:form>
</sakai:view>
</f:view>
