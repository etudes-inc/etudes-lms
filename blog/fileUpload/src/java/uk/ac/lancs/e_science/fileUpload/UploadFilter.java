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
package uk.ac.lancs.e_science.fileUpload;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;


public class UploadFilter implements Filter {
   private String repositoryPath=null;
   private long sizeMax = -1;   

   public void init(FilterConfig config) throws ServletException {
		try {
			String paramValue = config.getInitParameter("uk.ac.lancs.e_science.fileUpload.UploadFilter.sizeMax");
			if (paramValue != null)
				sizeMax = Long.parseLong(paramValue)*1024*1024;
		} catch (NumberFormatException ex) {
			ServletException servletEx = new ServletException();
			servletEx.initCause(ex);
			throw servletEx;
		}
   }
   

   public void destroy() {
   }

   public void doFilter(ServletRequest request,ServletResponse response, FilterChain chain)throws IOException, ServletException {
	  
	  if (!(request instanceof HttpServletRequest)) {
         chain.doFilter(request, response);
         return;
      }

      HttpServletRequest httpRequest = (HttpServletRequest) request;
      String contentLength = httpRequest.getHeader("Content-Length");
      try{
    	  if (sizeMax!=-1 && contentLength!=null && Long.parseLong(contentLength)>sizeMax){
    		  ServletException servletEx = new ServletException("Uploaded file size excess maximun legal");
    		  throw servletEx;
    	  }
      } catch (NumberFormatException e){
    	  e.printStackTrace();
    	  //nothing
      }

      boolean isMultipartContent = FileUpload.isMultipartContent(httpRequest);
      if (!isMultipartContent) {
         chain.doFilter(request, response);
         return;
      }

      DiskFileUpload upload = new DiskFileUpload();
      if (repositoryPath != null) 
         upload.setRepositoryPath(repositoryPath);
      
      try {
         List list = upload.parseRequest(httpRequest);
         final Map map = new HashMap();
         for (int i = 0; i < list.size(); i ++) {
            FileItem item = (FileItem) list.get(i);
            String str = new String(item.getString().getBytes(),"UTF-8"); 
            if (item.isFormField())
               map.put(item.getFieldName(), new String[] { str });
            else
               httpRequest.setAttribute(item.getFieldName(), item);
         }
            
         chain.doFilter(new HttpServletRequestWrapper(httpRequest) {
               public Map getParameterMap() {
                  return map;
               }                   
               
               public String[] getParameterValues(String name) {
                  Map map = getParameterMap();
                  return (String[]) map.get(name);
               }
               public String getParameter(String name) {
                  String[] params = getParameterValues(name);
                  if (params == null) return null;
                  return params[0];
               }
               public Enumeration getParameterNames() {
                  Map map = getParameterMap();
                  return Collections.enumeration(map.keySet());
               }
            }, response);
      } catch (FileUploadException ex) {
         ServletException servletEx = new ServletException();
         servletEx.initCause(ex);
         throw servletEx;
      }      
   }   
}
