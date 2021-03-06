/**********************************************************************************
* $URL: https://source.etudes.org/svn/etudes/source/trunk/gradebook/app/ui/src/java/org/sakaiproject/tool/gradebook/jsf/NontrailingDoubleConverter.java $
* $Id: NontrailingDoubleConverter.java 3 2008-10-20 18:44:42Z ggolden $
***********************************************************************************
*
* Copyright (c) 2006 The Regents of the University of California
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

package org.sakaiproject.tool.gradebook.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.NumberConverter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The standard JSF NumberConverter handles output formatting of double
 * numbers nicely by printing them as an integer if there's nothing past
 * the decimal point. On input either a Long or a Double might be
 * returned from "getAsObject". In earlier versions of MyFaces, if the
 * input value was going to a Double bean property, conversion would
 * happen silently. In MyFaces 1.1.1, a IllegalArgumentException is
 * thrown. This converter emulates the old behavior by converting Long
 * values to Double values before passing them to the backing bean.
 */
public class NontrailingDoubleConverter extends NumberConverter {
	private static final Log log = LogFactory.getLog(NontrailingDoubleConverter.class);

	public NontrailingDoubleConverter() {
		setType("number");
	}

	/**
	 * Always returns either a null or a Double.
	 */
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Object number = super.getAsObject(context, component, value);
		if (number != null) {
			if (number instanceof Long) {
				number = new Double(((Long)number).doubleValue());
			}
		}
		return number;
	}

}
