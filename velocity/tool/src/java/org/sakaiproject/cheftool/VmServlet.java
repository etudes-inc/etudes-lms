/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/velocity/tool/src/java/org/sakaiproject/cheftool/VmServlet.java $
 * $Id: VmServlet.java 3 2008-10-20 18:44:42Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.cheftool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.util.Validator;

/**
 * <p>
 * VmServlet adds a standard validator to the VmServlet from the velocity module {@link org.sakaiproject.vm.VmServlet}.
 * </p>
 */
public abstract class VmServlet extends org.sakaiproject.vm.VmServlet
{
	/** A validator. */
	protected final Validator m_validator = new Validator();

	/**
	 * Add some standard references to the vm context.
	 * 
	 * @param request
	 *        The request.
	 * @param response
	 *        The response.
	 */
	protected void setVmStdRef(HttpServletRequest request, HttpServletResponse response)
	{
		super.setVmStdRef(request, response);

		// include some standard references
		setVmReference("sakai_Validator", m_validator, request);
	}
}
