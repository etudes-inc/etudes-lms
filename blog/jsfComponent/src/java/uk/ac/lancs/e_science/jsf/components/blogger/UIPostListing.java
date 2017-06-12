/*************************************************************************************
 Copyright (c) 2006. Centre for e-Science. Lancaster University. United Kingdom.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 *************************************************************************************/
package uk.ac.lancs.e_science.jsf.components.blogger;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlForm;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.PostUtilities;

public class UIPostListing extends UICommand {

	public UIPostListing(){
		super();
	}
	public void encodeBegin(FacesContext context) throws IOException{
		Post[] postListing = (Post[])getAttributes().get("postListing");
		this.getParent();
		if (postListing!=null){
			writePosts(postListing,context);
		}
	}
	public void endodeEnd(FacesContext context) throws IOException{
		
	}
	
	private void writePosts(Post[] listOfPosts, FacesContext context) throws IOException{
		
		ResponseWriter writer = context.getResponseWriter();

		String formClientId = getFormClientId(this, context);
		writer.write("<br/>");
		writer.write(getStyleDefinition());
		
		writer.startElement("input",this);
		writer.writeAttribute("type","hidden",null);
		writer.writeAttribute("id","idSelectedPost",null);
		writer.writeAttribute("name","idSelectedPost",null);
		writer.endElement("input");
		
		writer.startElement("table",this);
		writer.writeAttribute("class","tableHeader",null);
		for (int i=0;i<listOfPosts.length;i++){
			Post post = listOfPosts[i];
			writer.startElement("tr",this);
			writer.startElement("td",this);
			writer.writeAttribute("class","tdTitleHeader",null);
			
			writer.startElement("span",null);
			writer.writeAttribute("class","spanRaquoHeader",null);
			writer.write("&raquo;&nbsp;");
			writer.endElement("span");
			
			writer.startElement("a",this);
			writer.writeAttribute("href","#",null);
			writer.writeAttribute("class","aTitleHeader",null);
			writer.writeAttribute("onClick","javascript:document.getElementById('idSelectedPost').value='"+post.getOID()+"';document.forms['"+formClientId+"'].submit();",null);
			writer.writeAttribute("class","spanTitleHeader",null);
			writer.write(post.getTitle());
			writer.endElement("a");
			writer.endElement("td");

			
			writer.startElement("td",this);
			writer.writeAttribute("class","tdAuthorHeader",null);
			writer.write(post.getCreator().getId());
			writer.endElement("td");
			
			writer.startElement("td",this);
			writer.writeAttribute("class","tdDateHeader",null);
			Date date = new Date(post.getDate());
			writer.write(DateFormat.getDateInstance(DateFormat.SHORT).format(date));
			writer.endElement("td");
			
			writer.endElement("tr");
			
			writer.startElement("tr",this);
			writer.startElement("td",this);
			writer.writeAttribute("colspan","3",null);
			if (post.getShortText()!=null && !(post.getShortText().equals("")))
				writer.write(post.getShortText());
			else {
				PostUtilities util = new PostUtilities();
				String text =util.getFirstParagraphOrNFirstCharacters(post,400);
				writer.write(text);
			}
			writer.endElement("td");
			writer.endElement("tr");
			
			writer.startElement("tr",this);
			writer.startElement("td",this);
			writer.writeAttribute("class","tdGap",null);
			writer.writeAttribute("colspan","3",null);
			writer.write("&nbsp;");
			writer.endElement("td");
			writer.endElement("tr");
		}
		writer.endElement("table");
	}
	

	private String getStyleDefinition(){
		StringBuffer sb = new StringBuffer();
		sb.append("<style title=\"css\">");
		sb.append("span.spanRaquoHeader{");
		sb.append(" 	font-size:16px;");
		sb.append(" 	color:#084A87;");
		sb.append(" 	font-weight:bold;");
		sb.append("}");		
		sb.append("a.aTitleHeader{");
		sb.append(" 	font-size:16px;");
		sb.append(" 	color:#084A87;");
		sb.append(" 	font-weight:bold;");
		sb.append("}");
		sb.append("table.tableHeader{");
		sb.append(" 	width:100%;");
		sb.append("}");
		sb.append("td.tdTitleHeader{");
		sb.append("		width:100%;");
		sb.append(" 	padding-right:15px;");
		sb.append(" 	padding-bottom:10px;");
		sb.append("}");
		sb.append("td.tdAuthorHeader{");
		sb.append("		width:150px;");
		sb.append(" 	vertical-align:top;");
		sb.append(" 	padding-bottom:10px;");
		sb.append(" 	color:#084A87;");
		sb.append("}");
		sb.append("td.tdDateHeader{");
		sb.append("		width:150px;");
		sb.append(" 	vertical-align:top;");
		sb.append(" 	padding-bottom:10px;");
		sb.append(" 	color:#084A87;");
		sb.append("}");
		sb.append("td.tdGap{");
		sb.append("		border-style:solid;");
		sb.append("		border-width:1px;");
		sb.append("		border-left:none;");
		sb.append("		border-top:none;");
		sb.append("		border-right:none;");
		sb.append("		border-color:#DDDFE4;");
		sb.append("}");		
		sb.append("</style>");
		return sb.toString();
	}
	public void decode(FacesContext context){
		Map requestMap = context.getExternalContext().getRequestParameterMap();

		if (!requestMap.containsKey("idSelectedPost"))
			return;
		String postOID=(String)requestMap.get("idSelectedPost");

		Post[] postListing = (Post[])getAttributes().get("postListing");
		
		for (int i=0;i<postListing.length;i++){
			if (postListing[i].getOID().equals(postOID)){
				context.getExternalContext().getSessionMap().put("post",postListing[i]);
			}
		}

		queueEvent(new ActionEvent(this));
		
	}
	private String getFormClientId(UIComponent component,FacesContext context){
		if (component==null)
			return null;
		UIComponent parent = component.getParent();
		if (parent == null)
			return null;
		if (parent instanceof HtmlForm)
			return parent.getClientId(context);
		return getFormClientId(parent,context);
			

	}
	public MethodBinding getAction() {
		return super.getAction();
	}

	public void setAction(MethodBinding action) {
		super.setAction(action);
	}
	public MethodBinding getActionListener() {
		return super.getActionListener();
	}

	public void setActionListener(MethodBinding actionListener) {
		super.setActionListener(actionListener);
	}	
	
	
}

