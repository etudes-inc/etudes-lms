<%-- $Id: debug.jsp 3 2008-10-20 18:44:42Z ggolden $ --%>
<f:view>
<sakai:view title="debug tag - Sakai 2.0 JSF example">
<h:commandLink action="index"><h:outputText value="Back to examples index" /></h:commandLink>
<f:verbatim><a href="<%=request.getRequestURI()%>.source">View page source</a></f:verbatim>

<h:form id="theForm">
<hr />
<h2>debug tag example</h2>
<hr />

<pre>
    &lt;sakai:debug /&gt;
</pre>
    <sakai:debug /> 

 <br />

<pre>
    &lt;sakai:debug rendered="false" /&gt;
</pre>
    <sakai:debug rendered="false" />

 <br />

</h:form>
</sakai:view>
</f:view>
