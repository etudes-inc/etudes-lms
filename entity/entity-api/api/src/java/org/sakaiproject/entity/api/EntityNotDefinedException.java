/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/entity/entity-api/api/src/java/org/sakaiproject/entity/api/EntityNotDefinedException.java $
 * $Id: EntityNotDefinedException.java 3 2008-10-20 18:44:42Z ggolden $
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

package org.sakaiproject.entity.api;

/**
 * <p>
 * EntityNotDefinedException is thrown whenever an Entity access is attempted with an id that is not found.
 * </p>
 */
public class EntityNotDefinedException extends Exception
{
	private String m_id = null;

	public EntityNotDefinedException(String id)
	{
		m_id = id;

	}

	public String getId()
	{
		return m_id;
	}

	public String toString()
	{
		return super.toString() + " id=" + m_id;
	}
}
