/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-app/src/java/org/sakaiproject/tool/section/filter/AuthnFilter.java $
 * $Id: AuthnFilter.java 3 2008-10-20 18:44:42Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California and The Regents of the University of Michigan
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
package org.sakaiproject.tool.section.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.facade.manager.Authn;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * An authentication filter for standalone use in demos and UI tests.
 */
public class AuthnFilter implements Filter {
	private static Log log = LogFactory.getLog(AuthnFilter.class);

	private String authnRedirect;
	private String authnBean;

	public void init(FilterConfig filterConfig) throws ServletException {
		authnRedirect = filterConfig.getInitParameter("authnRedirect");
		authnBean = filterConfig.getInitParameter("authnBean");
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {
		HttpSession session = ((HttpServletRequest)request).getSession(true);
		Authn authnService = (Authn)WebApplicationContextUtils.getWebApplicationContext(session.getServletContext()).getBean(authnBean);
		String userUid = null;
		try {
			userUid = authnService.getUserUid(request);
		} catch (Exception e) {
			if(log.isDebugEnabled()) log.debug("Could not get user uuid from authn service.");
		}
		if (log.isDebugEnabled()) log.debug("userUid=" + userUid);
		if (userUid == null) {
			if (authnRedirect != null) {
				if (authnRedirect.equals(((HttpServletRequest)request).getRequestURI())) {
					// Don't redirect to the same spot.
					chain.doFilter(request, response);
				} else {
					((HttpServletResponse)response).sendRedirect(authnRedirect);
				}
			} else {
				((HttpServletResponse)response).sendError(HttpServletResponse.SC_UNAUTHORIZED);
			}
		} else {
			chain.doFilter(request, response);
		}
	}

	public void destroy() {
	}
}



