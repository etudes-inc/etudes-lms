/**********************************************************************************
*
* $Id: PassNotPassMapping.java 5185 2013-06-13 17:54:57Z mallikamt $
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http:cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.gradebook;

import java.util.*;

/**
 * A PassNotPassMapping defines the set of grades available to a
 * gradebook as "P", "NP", each of which can be mapped to a minimum percentage
 * value.
 *
 * @deprecated
 */
public class PassNotPassMapping extends GradeMapping {
	private List grades;
	private List defaultValues;
	public Collection getGrades() {
		return grades;
	}
	public List getDefaultValues() {
        return defaultValues;
    }

    public PassNotPassMapping() {
        setGradeMap(new LinkedHashMap());

        grades = new ArrayList();
        grades.add("P");
        grades.add("NP");
        grades.add("I");

        defaultValues = new ArrayList();
        defaultValues.add(new Double(75));
        defaultValues.add(new Double(0));
        defaultValues.add(new Double(0));
    }

	/**
	 * @see org.sakaiproject.tool.gradebook.GradeMapping#getName()
	 */
	public String getName() {
        return "Pass / Not Pass";
	}

}



