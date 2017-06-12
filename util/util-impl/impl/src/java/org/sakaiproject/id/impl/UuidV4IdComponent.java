/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/util/util-impl/impl/src/java/org/sakaiproject/id/impl/UuidV4IdComponent.java $
 * $Id: UuidV4IdComponent.java 3 2008-10-20 18:44:42Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.id.impl;

import org.apache.commons.id.uuid.VersionFourGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.id.api.IdManager;

/**
 * <p>
 * UuidV4IdComponent implements the IdManager with a version 4 UUID generator from apache commons.
 * </p>
 */
public class UuidV4IdComponent implements IdManager
{
	/** Our log (commons). */
	private static Log M_log = LogFactory.getLog(UuidV4IdComponent.class);

	/** Our Id Generator. */
	protected static final VersionFourGenerator VFG = new VersionFourGenerator();

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		M_log.info("init()");
	}

	/**
	 * Final cleanup.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Work interface methods: IdManager
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * @inheritDoc
	 */
	public String createUuid()
	{
		String id = VFG.nextIdentifier().toString();

		return id;
	}
}
