/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/db/db-impl/ext/src/java/org/sakaiproject/springframework/orm/hibernate/AdditionalHibernateMappings.java $
 * $Id: AdditionalHibernateMappings.java 3 2008-10-20 18:44:42Z ggolden $
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

package org.sakaiproject.springframework.orm.hibernate;

import java.io.IOException;

import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;

public interface AdditionalHibernateMappings
{
	void processConfig(Configuration config) throws IOException, MappingException;
}
