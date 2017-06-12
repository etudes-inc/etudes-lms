<f:view>
<style type='text/css'>
.oddRow
{
background-color:#D0D0D0;
}
.colorRow
{
background-color:#D0D0D0;
}
.colorEvenRow
{
background-color:#FFFFFF;
}
.lefttable {
    position: fixed;
   left:10;
   z-index: 88;
  width:auto;
}
.righttable {
    position: relative;
    width: auto;
    overflow: auto;

}
.tdclassnowrap {
    width: 10px;
    border: 1px solid #ddd;
    white-space:nowrap;
}
.lefttd {
text-align:left;
width: 10px;
    border: 1px solid #ddd;
}
.centertd {
text-align:center;
width: 10px;
    border: 1px solid #ddd;
}  
</style>   
<script type="text/javascript" language="JavaScript" src="/e3/support/jquery-1.8.2.min.js"></script>
	 	
<script type='text/javascript'>//<![CDATA[ 
$(window).load(function(){
var headerWidth,
    contentWidth,
    leftWidth,
    clientWidth,
    documentWidth;

var performWidth = function () {
    leftWidth = $('.lefttable').outerWidth(true);
     $('.header').width("50em");
    headerWidth = $('.header > *').first().outerWidth(true);
    $('.header').width(headerWidth);
    $('.righttable').width("50em");
    contentWidth = $('.righttable > *').first().outerWidth(true);
    $('.righttable').css({lefttable: leftWidth});
    $('.righttable').width(contentWidth);
    //window.document.getElementById("rosterdiv").style.marginLeft = "400px";
    document.getElementById("rosterdiv").style.marginLeft =  $('.lefttable').outerWidth(false)+"px";
    
    //document.getElementById("rosterdiv").style.marginLeft =  document.getElementById("studdiv").offsetWidth+"px";
    //document.getElementById("rosterdiv").style.marginLeft = "400px";
    
    //$('.righttable')).css( 'margin-left','400px');
    clientWidth = window.document.body.clientWidth;
    documentWidth = $(document).outerWidth(true);
}
$(document).ready(function () {
	performWidth();
    $(document).scroll(function (e) {
        var ratio = (document.body.scrollLeft) / (documentWidth - clientWidth);   
        $('.header').css({left:-(headerWidth-clientWidth)*ratio});
    });
});
$(window).resize(function () {
	var onResize = function() {
    	performWidth();
	}
  //New height and width
    var winNewWidth = $(window).width(),
    winNewHeight = $(window).height();

    // compare the new height and width with old one
    if((typeof winWidth != 'undefined' && winWidth!=winNewWidth )|| (typeof winHeight != 'undefined' && winHeight!=winNewHeight))
    {
        window.clearTimeout(resizeTimeout);
        resizeTimeout = window.setTimeout(onResize, 10);
    }
    //Update the width and height
    winWidth = winNewWidth;
    winHeight = winNewHeight;

});
});//]]>  

</script>


	<div class="portletBody">
	  <h:form id="gbForm">
		<x:aliasBean alias="#{bean}" value="#{rosterBean}">
			<%@include file="/inc/appMenu.jspf"%>
		</x:aliasBean>

		<sakai:flowState bean="#{rosterBean}" />

		<h2><h:outputText value="#{msgs.roster_page_title}"/></h2>

		<x:aliasBean alias="#{bean}" value="#{rosterBean}">
			<%@include file="/inc/filterPaging.jspf"%>
		</x:aliasBean>
		<div id="studdiv" name="studdiv" class="lefttable">

		<x:dataTable cellpadding="0" cellspacing="0"
			id="studTable"
			styleClass="listHier"
			value="#{rosterBean.studentRows}"
			sortColumn="#{rosterBean.sortColumn}"
            sortAscending="#{rosterBean.sortAscending}"
			var="row"
			rowClasses="colorEvenRow,colorRow"
			columnClasses="tdclassnowrap,tdclassnowrap">
			<h:column id="studentNameData">
				<f:facet name="header">
		            <x:commandSortHeader columnName="studentSortName" immediate="true" arrow="true" actionListener="#{rosterBean.sort}">
		                <h:outputText value="#{msgs.roster_student_name}"/>
		            </x:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row.sortName}"/>
			</h:column>
			<h:column id="studentIdData">
				<f:facet name="header">
		            <x:commandSortHeader columnName="studentDisplayId" immediate="true" arrow="true" actionListener="#{rosterBean.sort}">
		                <h:outputText value="#{msgs.roster_student_id}"/>
		            </x:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row.displayId}"/>
			</h:column>
			<h:column id="sectionIdData">
				<f:facet name="header">
		            <x:commandSortHeader columnName="sectionId" immediate="true" arrow="true" actionListener="#{rosterBean.sort}">
		                <h:outputText value="#{msgs.roster_section_id}"/>
		            </x:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row.sectionId}"/>
			</h:column>
			<%/* Assignment columns will be dynamically appended, starting here. */%>
		</x:dataTable>
</div>
<div id="rosterdiv" name="rosterdiv" class="righttable">

		<x:dataTable cellpadding="0" cellspacing="0"
			id="rosterTable"
			styleClass="listHier"
			value="#{rosterBean.studentRows}"
			binding="#{rosterBean.rosterDataTable}"
			sortColumn="#{rosterBean.sortColumn}"
            sortAscending="#{rosterBean.sortAscending}"
			var="row"
			rowClasses="evenRow,oddRow">

			<%/* Assignment columns will be dynamically appended, starting here. */%>
		</x:dataTable>
</div>
		<p class="instruction">
			<h:outputText value="#{msgs.roster_no_enrollments}" rendered="#{rosterBean.emptyEnrollments}" />
		</p>

		<p class="act">
			<h:commandButton
				id="exportExcel"
				value="#{msgs.roster_export_excel}"
				actionListener="#{exportBean.exportRosterExcel}"
				rendered="#{!rosterBean.emptyEnrollments}"
				/>
			<h:commandButton
				id="exportCsv"
				value="#{msgs.roster_export_csv}"
				actionListener="#{exportBean.exportRosterCsv}"
				rendered="#{!rosterBean.emptyEnrollments}"
				/>
		</p>
		<div class="instruction"><h:outputText value="#{msgs.roster_lowest_footer}" escape="false" rendered="#{rosterBean.dropGradeDisplayed}"/></div>
		
	  </h:form>
	</div>
</f:view>
