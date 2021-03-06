/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-app/src/java/org/sakaiproject/tool/section/jsf/JsfUtil.java $
 * $Id: JsfUtil.java 3 2008-10-20 18:44:42Z ggolden $
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
package org.sakaiproject.tool.section.jsf;

import java.sql.Time;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.facade.manager.Context;
import org.sakaiproject.api.section.facade.manager.ResourceLoader;
import org.sakaiproject.jsf.util.ConversionUtil;
import org.sakaiproject.tool.section.jsf.backingbean.MessagingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * A utility to help deal with common tasks in JSF.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class JsfUtil {
	private static final Log log = LogFactory.getLog(JsfUtil.class);

	/**
	 * As part of the crutch for JSF's inability to do validation on relative
	 * values in different components.  This pattern defines how times should
	 * be displayed in the Section Info UI.
	 */
	public static final String TIME_PATTERN_DISPLAY = "h:mm";
	

	/**
	 * As part of the crutch for JSF's inability to do validation on relative
	 * values in different components.  This pattern defines how to parse and
	 * format complete times (with hours, minutes, and am/pm marker).
	 */
	public static final String TIME_PATTERN_LONG = "h:mm a";
	
	/**
	 * As part of the crutch for JSF's inability to do validation on relative
	 * values in different components.  This pattern defines how to parse and
	 * format abberviated times (with only hours and am/pm marker).
	 */
	public static final String TIME_PATTERN_SHORT = "h a";
	
	/**
	 * To cut down on configuration noise, allow access to request-scoped beans from
	 * session-scoped beans, and so on, this method lets the caller try to find
	 * anything anywhere that Faces can look for it.
	 *
	 * WARNING: If what you're looking for is a managed bean and it isn't found,
	 * it will be created as a result of this call.
	 */
	public static final Object resolveVariable(String name) {
		FacesContext context = FacesContext.getCurrentInstance();
		Object result = context.getApplication().getVariableResolver().resolveVariable(context, name);
		if(log.isDebugEnabled()) log.debug("JSF variable " + name + " resolved to " + result);
		return result;
	}

	/**
     * Adds an error message for display on a page when the page is guaranteed
     * not to be displayed via a redirect.
	 * 
	 * @param message
	 */
	public static void addErrorMessage(String message) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
	}

	/**
     * Adds an error message for display on a component when the page is guaranteed
     * not to be displayed via a redirect.
	 * 
	 * @param message
	 * @param componentId
	 */
	public static void addErrorMessage(String message, String componentId) {
		FacesContext.getCurrentInstance().addMessage(componentId, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
	}

	/**
     * Adds an info message for display on a page when the page is guaranteed
     * not to be displayed via a redirect.
	 * 
	 * @param message
	 */
	public static void addInfoMessage(String message) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
	}

    /**
     * Adds an info message for display on a page even if faces sends the user
     * to the page via a redirect.
     * 
     * @param message
     */
	public static void addRedirectSafeInfoMessage(String message) {
        MessagingBean mb = (MessagingBean)resolveVariable("messagingBean");
        mb.addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    /**
     * Adds a warning message for display on a page even if faces sends the user
     * to the page via a redirect.
     * 
     * @param message
     */
    public static void addRedirectSafeWarnMessage(String message) {
        MessagingBean mb = (MessagingBean)resolveVariable("messagingBean");
        mb.addMessage(new FacesMessage(FacesMessage.SEVERITY_WARN, message, null));
    }

    /**
     * Gets a localized message from the message bundle.
     */
    public static String getLocalizedMessage(String key) {
    	ResourceLoader rl = (ResourceLoader)resolveVariable("org.sakaiproject.api.section.facade.manager.ResourceLoader");
        return rl.getString(key);
	}
	
    /**
     * Gets a localized message from the message bundle and formats it using the
     * parameter array.
     * 
     * @param key
     * @param params
     * @return
     */
    public static String getLocalizedMessage(String key, String[] params) {
    	String rawString = getLocalizedMessage(key);
        MessageFormat format = new MessageFormat(rawString);
        return format.format(params);
    }


	/**
	 * Gets a value from the request parameter map, as provided by the faces
	 * context.
	 * 
	 * @param string
	 * @return
	 */
    public static String getStringFromParam(String string) {
		return (String)FacesContext.getCurrentInstance()
		.getExternalContext().getRequestParameterMap().get(string);
	}
	
	/**
	 * Converts a string and a boolean (am) into a java.sql.Time object.
	 * 
	 * @param str
	 * @param am
	 * @return
	 */
	public static Time convertStringToTime(String str, boolean am) {
		if(StringUtils.trimToNull(str) == null) {
			return null;
		}

		// Set the am/pm flag to ensure that the time is parsed properly
		if(am) {
			str = str + " AM";
		} else {
			str = str + " PM";
		}
		
		String pattern = (str.indexOf(':') != -1) ? JsfUtil.TIME_PATTERN_LONG : JsfUtil.TIME_PATTERN_SHORT;
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		Date date;
		try {
			date = sdf.parse(str);
		} catch (ParseException pe) {
			throw new RuntimeException("A bad date made it through validation!  This should never happen!");
		}
		return ConversionUtil.convertDateToTime(date, am);
	}

	/**
	 * As part of the crutch for JSF's inability to do validation on relative
	 * values in different components, this method checks whether a string can
	 * represent a valid time.
	 * 
	 * Returns true if the string fails to represent a time.  Java's date formatters
	 * allow for impossible field values (eg hours > 12) so we do manual checks here.
	 * Ugh.
	 * 
	 * @param str The string that might represent a time.
	 * 
	 * @return
	 */
	public static  boolean isInvalidTime(String str) {
		if(StringUtils.trimToNull(str) == null) {
			// Empty strings are ok
			return false;
		}
		
		if(str.indexOf(':') != -1) {
			// This is a fully specified time
			String[] sa = str.split(":");
			if(sa.length != 2) {
				if(log.isDebugEnabled()) log.debug("This is not a valid time... it has more than 1 ':'.");
				return true;
			}
			return outOfRange(sa[0], 2, 1, 12) || outOfRange(sa[1], 2, 0, 59);
		} else {
			return outOfRange(str, 2, 1, 12);
		}
	}

	/**
	 * Returns true if the string is longer than len, less than low, or higher than high.
	 * 
	 * @param str The string
	 * @param len The max length of the string
	 * @param low The lowest possible numeric value
	 * @param high The highest possible numeric value
	 * @return
	 */
	private static boolean outOfRange(String str, int len, int low, int high) {
		if(str.length() > len) {
			return true;
		}
		try {
			int i = Integer.parseInt(str);
			if(i < low || i > high) {
				return true;
			}
		} catch (NumberFormatException nfe) {
			if(log.isDebugEnabled()) log.debug("time must be a number");
			return true;
		}
		return false;
	}

	/**
	 * As part of the crutch for JSF's inability to do validation on relative
	 * values in different components, this method checks whether an end time has
	 * been entered without a start time.
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public static boolean isEndTimeWithoutStartTime(String startTime, String endTime) {
		if(startTime == null & endTime != null) {
			if(log.isDebugEnabled()) log.debug("You can not set an end time without setting a start time.");
			return true;
		}
		return false;
	}
	
	/**
	 * As part of the crutch for JSF's inability to do validation on relative
	 * values in different components, this method checks whether two times, as
	 * expressed by string start and end times and booleans indicating am/pm,
	 * express times where the end time proceeds a start time.
	 * 
	 * @param startTime
	 * @param startTimeAm
	 * @param endTime
	 * @param endTimeAm
	 * @return
	 */
	public static boolean isEndTimeBeforeStartTime(String startTime, boolean startTimeAm, String endTime, boolean endTimeAm) {
		if(StringUtils.trimToNull(startTime) != null & StringUtils.trimToNull(endTime) != null) {
			Time start = JsfUtil.convertStringToTime(startTime, startTimeAm);
			Time end = JsfUtil.convertStringToTime(endTime, endTimeAm);
			if(start.after(end)) {
				if(log.isDebugEnabled()) log.debug("You can not set an end time earlier than the start time.");
				return true;
			}
		}
		return false;
	}

	public static final  Comparator getSelectItemComparator() {
		return new Comparator() {
			public int compare(Object o1, Object o2) {
				SelectItem item1 = (SelectItem)o1;
				SelectItem item2 = (SelectItem)o2;
				return item1.getLabel().toString().compareTo(item2.getLabel().toString());
			}
		};
	}

}
