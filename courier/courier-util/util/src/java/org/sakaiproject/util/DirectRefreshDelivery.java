/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/courier/courier-util/util/src/java/org/sakaiproject/util/DirectRefreshDelivery.java $
 * $Id: DirectRefreshDelivery.java 3 2008-10-20 18:44:42Z ggolden $
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

package org.sakaiproject.util;

/**
 * <p>
 * DirectRefreshDelivery is a Delivery that does a direct location refresh.
 * </p>
 */
public class DirectRefreshDelivery extends BaseDelivery
{
	/**
	 * Construct.
	 * 
	 * @param address
	 *        The address.
	 */
	public DirectRefreshDelivery(String address)
	{
		super(address, null);
	}

	/**
	 * Construct.
	 * 
	 * @param address
	 *        The address.
	 * @param elementId
	 *        The elementId.
	 */
	public DirectRefreshDelivery(String address, String elementId)
	{
		super(address, elementId);
	}

	/**
	 * Compose a javascript message for delivery to the browser client window.
	 * 
	 * @return The javascript message to send to the browser client window.
	 */
	public String compose()
	{
		return "try { " + ((m_elementId == null) ? "" : m_elementId + ".") + "location.replace("
				+ ((m_elementId == null) ? "" : m_elementId + ".") + "location); } catch(error) {}";
	}
}
