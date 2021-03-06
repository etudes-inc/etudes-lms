/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/rwiki/rwiki-util/util/src/java/uk/ac/cam/caret/sakai/rwiki/utils/SimpleCoverage.java $
 * $Id: SimpleCoverage.java 3 2008-10-20 18:44:42Z ggolden $
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

package uk.ac.cam.caret.sakai.rwiki.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A really simple coverage utility, prints a record of the calling method
 * 
 * @author ieb
 */
public class SimpleCoverage
{
	private static Log logger = LogFactory.getLog(SimpleCoverage.class);

	private static long last = System.currentTimeMillis();

	public static void cover(String message)
	{
		long now = System.currentTimeMillis();
		String elapsed = String.valueOf(now - last) + " ms ";
		last = now;
		Exception e = new Exception();
		StackTraceElement[] ste = e.getStackTrace();
		String method = ste[1].getMethodName();
		String file = ste[1].getFileName();
		int line = ste[1].getLineNumber();
		String className = ste[1].getClassName();
		logger.info("###### " + elapsed + " " + message + " SimpleCoverage at "
				+ className + "." + method + " (" + file + ":" + line + ") ");
	}

	public static void cover()
	{
		long now = System.currentTimeMillis();
		String elapsed = String.valueOf(now - last) + " ms ";
		Exception e = new Exception();
		StackTraceElement[] ste = e.getStackTrace();
		String method = ste[1].getMethodName();
		String file = ste[1].getFileName();
		int line = ste[1].getLineNumber();
		String className = ste[1].getClassName();
		logger.info("###### " + elapsed + " SimpleCoverage at " + className
				+ "." + method + " (" + file + ":" + line + ") ");
	}

	public static void covered()
	{
	}

}
