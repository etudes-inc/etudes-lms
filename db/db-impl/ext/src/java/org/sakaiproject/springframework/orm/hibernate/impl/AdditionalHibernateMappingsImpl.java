/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/db/db-impl/ext/src/java/org/sakaiproject/springframework/orm/hibernate/impl/AdditionalHibernateMappingsImpl.java $
 * $Id: AdditionalHibernateMappingsImpl.java 3 2008-10-20 18:44:42Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.springframework.orm.hibernate.impl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class AdditionalHibernateMappingsImpl implements AdditionalHibernateMappings, Comparable
{
	protected final transient Log logger = LogFactory.getLog(getClass());

	private Resource[] mappingLocations;

	private Integer sortOrder = new Integer(Integer.MAX_VALUE);

	public void setMappingResources(String[] mappingResources)
	{
		this.mappingLocations = new Resource[mappingResources.length];
		for (int i = 0; i < mappingResources.length; i++)
		{
			this.mappingLocations[i] = new ClassPathResource(mappingResources[i].trim());
		}
	}

	public Resource[] getMappingLocations()
	{
		return mappingLocations;
	}

	public void processConfig(Configuration config) throws IOException, MappingException
	{
		for (int i = 0; i < this.mappingLocations.length; i++)
		{
			config.addInputStream(this.mappingLocations[i].getInputStream());
		}
	}

	public int compareTo(Object o)
	{
		return getSortOrder().compareTo(((AdditionalHibernateMappingsImpl) o).getSortOrder());
	}

	public Integer getSortOrder()
	{
		return sortOrder;
	}

	public void setSortOrder(Integer sortOrder)
	{
		this.sortOrder = sortOrder;
	}
}
