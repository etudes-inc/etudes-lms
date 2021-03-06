/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/db/db-api/api/src/java/org/sakaiproject/db/api/SqlServiceDeadlockException.java $
 * $Id: SqlServiceDeadlockException.java 63 2008-10-21 16:12:47Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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

package org.sakaiproject.db.api;

/**
 * SqlServiceAlreadyExistsException indicates that the write operation failed due to a db deadlock.
 */
public class SqlServiceDeadlockException extends RuntimeException
{
	public SqlServiceDeadlockException(Exception cause)
	{
		super(cause);
	}
}
