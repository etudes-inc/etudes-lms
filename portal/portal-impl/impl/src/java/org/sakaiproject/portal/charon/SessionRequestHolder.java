/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/portal/portal-impl/impl/src/java/org/sakaiproject/portal/charon/SessionRequestHolder.java $
 * $Id: SessionRequestHolder.java 3 2008-10-20 18:44:42Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2006 The Sakai Foundation.
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

package org.sakaiproject.portal.charon;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

public class SessionRequestHolder
{
	private Hashtable headers;

	private String contextPath;

	private String method;

	private String queryString;

	private Map parameterMap;

	public SessionRequestHolder(HttpServletRequest request, String marker, String replacement)
	{
		headers = new Hashtable();
		Enumeration e = request.getHeaderNames();
		while (e.hasMoreElements())
		{
			String s = (String) e.nextElement();
			Vector v = new Vector();
			Enumeration e1 = request.getHeaders(s);
			while (e1.hasMoreElements())
			{
				v.add(e1.nextElement());
			}
			headers.put(s, request.getHeaders(s));
		}
		Map m = request.getParameterMap();
		parameterMap = new HashMap();
		for (Iterator i = m.keySet().iterator(); i.hasNext();)
		{
			Object o = i.next();
			parameterMap.put(o, m.get(o));
		}
		contextPath = PortalStringUtil.replaceFirst(request.getContextPath(),marker,replacement);
		method = request.getMethod();
		queryString = request.getQueryString();
	}


	public String getContextPath()
	{
		return contextPath;
	}

	public long getDateHeader(String arg0)
	{
		try
		{
			SimpleDateFormat f = new SimpleDateFormat();
			Date d = f.parse(getHeader(arg0));
			return d.getTime();
		}
		catch (Throwable t)
		{
			return 0;
		}
	}

	public String getHeader(String arg0)
	{
		try
		{
			Vector v = (Vector) headers.get(arg0);
			return (String) v.get(0);
		}
		catch (Throwable t)
		{
			return null;
		}
	}

	public Enumeration getHeaderNames()
	{
		return headers.keys();
	}

	public Enumeration getHeaders(String arg0)
	{
		try
		{
			return ((Vector) headers.get(arg0)).elements();
		}
		catch (Throwable t)
		{
			return null;
		}
	}

	public int getIntHeader(String arg0)
	{
		try
		{
			return Integer.parseInt(getHeader(arg0));
		}
		catch (Throwable t)
		{
			return 0;
		}
	}

	public String getMethod()
	{
		return method;
	}

	public String getQueryString()
	{
		return queryString;
	}

	public String getParameter(String arg0)
	{
		Object o = parameterMap.get(arg0);
		if (o instanceof String[])
		{
			String[] s = (String[]) o;
			return s[0];
		}
		else if (o instanceof String)
		{
			return (String) o;
		}
		else if (o != null)
		{
			return o.toString();
		}
		else
		{
			return null;
		}
	}

	public Map getParameterMap()
	{
		return parameterMap;
	}

	public Enumeration getParameterNames()
	{
		final Iterator i = parameterMap.keySet().iterator();
		return new Enumeration()
		{

			public boolean hasMoreElements()
			{
				return i.hasNext();
			}

			public Object nextElement()
			{
				return i.next();
			}

		};
	}

	public String[] getParameterValues(String arg0)
	{
		Object o = parameterMap.get(arg0);
		if (o instanceof String[])
		{
			String[] s = (String[]) o;
			return s;
		}
		else if (o instanceof String)
		{
			return new String[] { (String) o };
		}
		else if (o != null)
		{
			return new String[] { o.toString() };
		}
		else
		{
			return null;
		}
	}
}
