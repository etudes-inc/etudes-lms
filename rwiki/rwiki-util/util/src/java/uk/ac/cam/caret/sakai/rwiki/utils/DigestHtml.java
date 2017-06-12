/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/rwiki/rwiki-util/util/src/java/uk/ac/cam/caret/sakai/rwiki/utils/DigestHtml.java $
 * $Id: DigestHtml.java 7547 2014-03-07 17:58:31Z mallikamt $
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

import java.io.StringReader;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Digests XHTML into a string representation
 * 
 * @author ieb
 */
public class DigestHtml
{
	 /** External general entities id (http://xml.org/sax/features/external-general-entities) */ 
    protected static final String EXTERNAL_GENERAL_ENTITIES_ID = "http://xml.org/sax/features/external-general-entities"; 
    /** Disallow inline doctype declaration id (http://apache.org/xml/features/disallow-doctype-decl) */ 
    protected static final String DISALLOW_DOCTYPE_DECL_ID = "http://apache.org/xml/features/disallow-doctype-decl"; 
   
	public static String digest(String todigest)
	{
		Digester d = new Digester();
		try
		{
			XMLReader reader = XMLReaderFactory
					.createXMLReader("org.apache.xerces.parsers.SAXParser");
			reader.setContentHandler(d);
			reader.setFeature(EXTERNAL_GENERAL_ENTITIES_ID, false); 
			reader.setFeature(DISALLOW_DOCTYPE_DECL_ID, true);
			
			reader.parse(new InputSource(new StringReader("<content>"
					+ todigest + "</content>")));
			return d.toString();
		}
		catch (Exception ex)
		{
			return d.toString() + "\n Failed at " + ex.getMessage();
		}
	}

}
