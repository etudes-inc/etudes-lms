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

import javax.faces.FactoryFinder;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.servlet.http.HttpServletRequest;

import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Comment;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Creator;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.File;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Image;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.LinkRule;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Paragraph;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.Post;
import uk.ac.lancs.e_science.sakaiproject.api.blogger.post.PostElement;

public class UIOutputPost extends UIOutput {
	private String contextPath="";
	public void encodeBegin(FacesContext context) throws IOException{
		ResponseWriter writer = context.getResponseWriter();
		Post post = (Post)getAttributes().get("post");
		HttpServletRequest req =((HttpServletRequest)context.getExternalContext().getRequest());
		contextPath = req.getContextPath();
		if (post!=null){
			writePost(writer, post);
		}
	}
	public void endodeEnd(FacesContext context) throws IOException{
	}
	
	private void writePost(ResponseWriter writer, Post post) throws IOException{
		
		writer.write("<br/>");
		writeHeader(writer,post);
		writer.write("<br/>");
		writeElements(writer,post);
		writer.write("<br/>");
		writeComments(writer,post);
		
		
	}
	
	private void writeHeader(ResponseWriter writer,Post post) throws IOException{
		
		writer.write(getStyleDefinitionForHeader());
		writer.startElement("table",this);
		writer.writeAttribute("cellpading","0",null);
		writer.writeAttribute("cellspacing","0",null);
		writer.writeAttribute("width","100%",null);
		
		writer.startElement("tr",this);
		writer.startElement("td",this);
		writeTitleAndAuthorAndDate(writer,post);
		writer.endElement("td");
		writer.endElement("tr");

		writer.startElement("tr",this);
		writer.startElement("td",this);
		writer.write("<br/>");
		writer.endElement("td");
		writer.endElement("tr");
		
		if (post.getKeywords()!=null && post.getKeywords().length!=0){
			writer.startElement("tr",this);
			writer.startElement("td",this);
			writer.write("Keywords:");
			StringBuffer sb = new StringBuffer();
			for (int i=0;i<post.getKeywords().length;i++){
				sb.append(post.getKeywords()[i]).append(", ");
			}
			writer.write(sb.toString().substring(0,sb.toString().lastIndexOf(", ")));
			writer.endElement("td");
			writer.endElement("tr");
		}
		
		if (post.getShortText()!=null && !(post.getShortText().trim().equals(""))){
			writeGap(writer);
			writer.startElement("tr",this);
			writer.startElement("td",this);
			writer.startElement("span",this);
			writer.writeAttribute("class","spanShortText",null);
			writer.write(post.getShortText());
			writer.endElement("span");
			writer.endElement("td");
			writer.endElement("tr");
		}
		writeGap(writer);
		
		writer.endElement("table");
		
	}
	private void writeTitleAndAuthorAndDate(ResponseWriter writer, Post post) throws IOException{
		writer.startElement("table",this);
		writer.writeAttribute("width","100%",null);
		writer.writeAttribute("cellpading","0",null);
		writer.writeAttribute("cellspacing","0",null);
		writer.startElement("tr",this);
		//td
		writer.startElement("td",this);
		writer.writeAttribute("class","tdTitle",null);
		writer.startElement("span",this);
		writer.writeAttribute("class","spanTitle",null);
		writer.write(post.getTitle());
		writer.endElement("span");
		writer.endElement("td");
		//td
		writer.startElement("td",this);
		writer.writeAttribute("class","tdAuthor",null);
		Creator creator = post.getCreator();
		if (creator!=null){
			writer.startElement("span",this);
			writer.writeAttribute("style","font-size:12px; font-family:Verdana, Arial, Helvetica, sans-serif",null);
			writer.write(post.getCreator().getId());
			Date date = new Date(post.getDate());
			writer.write(" ("+DateFormat.getDateInstance(DateFormat.SHORT).format(date)+")");
			writer.endElement("span");
		}
		writer.endElement("td");
		
		writer.endElement("tr");
		writer.endElement("table");
		
	}
	private void writeGap(ResponseWriter writer) throws IOException{
		writer.startElement("tr",this);
		writer.startElement("td",this);
		writer.writeAttribute("class","tdGap",null);
		writer.write("&nbsp;");
		writer.endElement("td");
		writer.endElement("tr");
		
	}

	private void writeElements(ResponseWriter writer, Post post) throws IOException{
		PostElement[] elements = post.getElements();
		if (elements!=null){
			writer.startElement("table",this);
			writer.writeAttribute("class","mainTable",null);
			writer.writeAttribute("width","100%",null);
			writer.writeAttribute("cellpading","0",null);
			writer.writeAttribute("cellspacing","0",null);
			for (int i=0;i<elements.length;i++){
				PostElement element = elements[i];
				writer.startElement("tr",this);
				writer.startElement("td",this);
				if (element instanceof Paragraph)
					writeParagraph(writer, (Paragraph) element);
				if (element instanceof Image)
					writeImage(writer, (Image) element);
				if (element instanceof LinkRule)
					writeLinkRule(writer, (LinkRule)element);
				if (element instanceof File)
					writeFile(writer, (File)element);

				writer.endElement("td"); 
				writer.endElement("tr");
				
				writer.startElement("tr",this);
				writer.startElement("td",this); //this is a gap between paragraphs and images
				writer.write("<br/>");
				writer.endElement("td"); 
				writer.endElement("tr");
			}
			writer.endElement("table");//---------------------------------- 0#
		}
	}
	private void writeParagraph(ResponseWriter writer, Paragraph paragraph) throws IOException{
		writer.startElement("table",this);
		writer.writeAttribute("cellpading","0",null);
		writer.writeAttribute("cellspacing","0",null);
		writer.writeAttribute("width","100%",null);
		writer.startElement("tr",this);
		writer.startElement("td",this);

		writer.write(paragraph.getText());

		writer.endElement("td");
		writer.endElement("tr");
		writer.endElement("table");
		
	}
	private void writeImage(ResponseWriter writer, Image image) throws IOException{
		writer.startElement("table",this);
		writer.writeAttribute("width","100%",null);
		writer.startElement("tr",this);
		writer.startElement("td",this);
		writer.writeAttribute("style","text-align:center",null);
		
		StringBuffer onClick =new StringBuffer();
		onClick.append("javascript:");
		onClick.append("var win = window.open('',\"Call\",\"width=500,height=450,status=no,resizable=yes,scrollbars=1\");");
		onClick.append("var doc = win.document;");
		onClick.append("doc.writeln('<html>');");
		onClick.append("doc.writeln('<body>');");
		onClick.append("doc.writeln('<img src="+contextPath+"/servletForImages?idImage="+image.getIdImage()+"&size=original></img>');");
		onClick.append("doc.writeln('</body>');");
		onClick.append("doc.writeln('</html>');");
		onClick.append("doc.close();");
		onClick.append("return false;");
		
		writer.startElement("input",this);
		writer.writeAttribute("type","image",null);
		writer.writeAttribute("src",contextPath+"/servletForImages?idImage="+image.getIdImage()+"&size=websize",null);
		writer.writeAttribute("onClick",onClick,null);
		writer.endElement("input");
		
		writer.endElement("td");
		writer.endElement("tr");
		writer.endElement("table");
	}
	private void writeFile(ResponseWriter writer, File file) throws IOException{
		writer.startElement("a",this);
		writer.writeAttribute("href",contextPath+"/servletForFiles?fileId="+file.getIdFile()+"&fileDescription="+file.getDescription(),null);
		writer.write(file.getDescription());
		writer.endElement("a");
	}	
	private void writeLinkRule(ResponseWriter writer, LinkRule link) throws IOException{
		StringBuffer onClick =new StringBuffer();
		onClick.append("javascript:");
		onClick.append("var win = window.open('"+link.getLinkExpression()+"',\"Call\",\"width=600,height=5 50,menubar=yes,toolbar=yes,status=yes,scrollbars=yes,location=yes,resizable=yes\");");
		onClick.append("var doc = win.document;");
		onClick.append("doc.close();");
		onClick.append("return false;");
		
		writer.startElement("a",this);
		writer.writeAttribute("href",link.getLinkExpression(),null);
		writer.writeAttribute("onClick",onClick,null);		
		writer.write(link.getDescription());
		writer.endElement("a");
	}	
	private void writeComments(ResponseWriter writer, Post post) throws IOException{
		if (post.getComments()==null || post.getComments().length==0)
			return;
		writer.write(getStyleDefinitionForComments());
		writer.startElement("table",null);
		writer.writeAttribute("cellpading","0",null);
		writer.writeAttribute("cellspacing","0",null);
		writer.writeAttribute("width","100%",null);
		writer.startElement("tr",this);
		writer.startElement("td",this);
		writer.writeAttribute("class","tdCommentHeader",null);
		writer.writeAttribute("colspan","2",null);
		writer.write("Comments");
		writer.endElement("td");
		writer.startElement("td",this);
		writer.writeAttribute("class","tdCommentHeader",null);
		writer.endElement("td");
		writer.endElement("tr");
		
		for (int i=0;i<post.getComments().length;i++){
			
			Comment comment = post.getComments()[i];
			writer.startElement("tr",this);
			writer.startElement("td",this);
			writer.writeAttribute("width","100px",null);
			writer.writeAttribute("class","tdComment tdComment1",null);
			writer.write(comment.getCreator().getId());
			writer.write("<br/>");
			Date date = new Date(comment.getDate());
			writer.write(" ("+DateFormat.getDateInstance(DateFormat.SHORT).format(date)+")");
			writer.endElement("td");
			writer.startElement("td",this);
			writer.writeAttribute("class","tdComment tdComment2",null);
			writer.write(comment.getText());
			writer.endElement("td");
			writer.endElement("tr");
		}
		writer.endElement("table");
	}
	private String getStyleDefinitionForComments(){
		StringBuffer sb = new StringBuffer();
		sb.append("<style title=\"css\">");
		sb.append("td.tdComment{");
		sb.append("		border-style:solid;");
		sb.append("		border-width:1px;");
		sb.append("		border-color:#DDDFE4;");
		sb.append("		border-top:none;");
		sb.append("		border-left:none;");
		sb.append("}");
		sb.append("td.tdComment1{");
		sb.append("}");
		sb.append("td.tdComment2{");
		sb.append("		border-right:none;");
		sb.append("}");
		sb.append("td.tdCommentHeader{");
		sb.append("		border-width:0px;");
		sb.append("		background-color:#DDDFE4;");
		sb.append("}");
		sb.append("</style>");
		return sb.toString();
	}

	private String getStyleDefinitionForHeader(){
		StringBuffer sb = new StringBuffer();
		sb.append("<style title=\"css\">");
		sb.append("span.spanTitle{");
		sb.append("		font-size:16px;");
		sb.append("		font-weight:bold;");
		sb.append("		font-family:Verdana, Arial, Helvetica, sans-serif;");
		sb.append("}");
		sb.append("span.spanShortText{");
		sb.append("		font-size:12px;");
		sb.append("		font-weight:bold;");
		sb.append("		font-family:Verdana, Arial, Helvetica, sans-serif;");
		sb.append("}");
		sb.append("td.tdTitle{");
		sb.append(" 	width:80%");
		sb.append("}");
		sb.append("td.tdAutor{");
		sb.append(" 	width:20%");
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
	private String createTableOfContents(Post post){
		for (int i=0;i<post.getElements().length;i++){
			if (post.getElements()[i] instanceof Paragraph){
				String text = ((Paragraph)post.getElements()[i]).getText();
			}
		}
		return null;
	}
	

}
