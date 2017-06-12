<%-- $Id: viewTitle.jsp 3 2008-10-20 18:44:42Z ggolden $ --%>
<f:view>
<sakai:view title="viewTitle tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>


<hr />
<h2>viewTitle tag example</h2>
<hr />
<h:form id="theForm">
<pre>
    &lt;sakai:viewTitle value="Item Detail Summary View" /&gt;
</pre>
    <sakai:viewTitle value="Item Detail Summary View" />

 <br />

<pre>
    &lt;sakai:viewTitle value="Assignments" indent="2" /&gt;
</pre>
    <sakai:viewTitle value="Assignments" indent="2" />

 <br />

</h:form>
</sakai:view>
</f:view>
