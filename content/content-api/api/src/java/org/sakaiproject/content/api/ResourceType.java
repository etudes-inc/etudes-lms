/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/content/content-api/api/src/java/org/sakaiproject/content/api/ResourceType.java $
 * $Id: ResourceType.java 770 2010-10-01 17:41:45Z rashmim $
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.api;

public interface ResourceType 
{
	public static final String TYPE_TEXT = "org.sakaiproject.content.types.TextDocumentType";
	public static final String TYPE_HTML = "org.sakaiproject.content.types.HtmlDocumentType";
	public static final String TYPE_URL = "org.sakaiproject.content.types.urlResource";
	public static final String TYPE_UPLOAD = "org.sakaiproject.content.types.fileUpload";
	public static final String TYPE_FOLDER = "org.sakaiproject.content.types.folder";
	public static final String TYPE_METAOBJ = "org.sakaiproject.metaobj.shared.FormHelper";

	public static final String MIME_TYPE_TEXT = "text/plain";
	public static final String MIME_TYPE_HTML = "text/html";
	public static final String MIME_TYPE_METAOBJ = "application/x-osp";
	public static final String MIME_TYPE_URL = "text/url";
	
	public static final int EXPANDABLE_FOLDER_SIZE_LIMIT = 256;
	
	public static final int MAX_LENGTH_SHORT_SIZE_LABEL = 18;
	public static final int MAX_LENGTH_LONG_SIZE_LABEL = 80;
}