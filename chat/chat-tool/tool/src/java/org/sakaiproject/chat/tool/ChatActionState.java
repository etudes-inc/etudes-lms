/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/announcement/announcement-tool/tool/src/java/org/sakaiproject/announcement/tool/ChatActionState.java $
 * $Id: ChatActionState.java 5194 2013-06-14 00:58:42Z rashmim $
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

package org.sakaiproject.chat.tool;

import java.util.Collection;

import org.sakaiproject.site.api.Site;
import org.sakaiproject.tool.api.SessionBindingListener;
import org.sakaiproject.tool.api.SessionBindingEvent;
import org.sakaiproject.chat.api.ChatMessageEdit;
import org.sakaiproject.cheftool.ControllerState;
import org.sakaiproject.util.ResourceLoader;

/**
 * <p>
 * ChatActionState is the state object for the ChatAction tool. This object listens for changes on the announcement, and requests a UI delivery when changes occur.
 * </p>
 */
public class ChatActionState extends ControllerState implements SessionBindingListener
{
	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("chat");
	
	private Site m_editSite;
	
	private ChatMessageEdit m_edit;
	
	/** Creates new ChatActionState */
	public ChatActionState()
	{
		init();
	} // constructor

	/**
	 * Release any resources and restore the object to initial conditions to be reused.
	 */
	public void recycle()
	{
		super.recycle();

	} // recycle

	/**
	 * Init to startup values
	 */
	protected void init()
	{
		super.init();

	} // init

	// ********* for sorting *********
	// the current sorted by property name
	private String m_currentSortedBy = "";

	// the current sort sequence: ture - acscending/false - descending
	private boolean m_currentSortAsc = false;
	
	
	//********* for sorting *********
	/**
	 * set the current sorted by property name
	 * 
	 * @param name
	 *        The sorted by property name
	 */
	protected void setCurrentSortedBy(String name)
	{
		m_currentSortedBy = name;

	} // setCurrentSortedBy

	/**
	 * get the current sorted by property name
	 * 
	 * @return "true" if the property is sorted ascendingly; "false" if the property is sorted descendingly
	 */
	protected String getCurrentSortedBy()
	{
		return m_currentSortedBy;

	} // getSortCurrentSortedBy

	/**
	 * set the current sort property
	 * 
	 * @param asc
	 *        "true" if the property is sorted ascendingly; "false" if the property is sorted descendingly
	 */
	protected void setCurrentSortAsc(boolean asc)
	{
		m_currentSortAsc = asc;

	} // setCurrentSortAsc

	/**
	 * get the current sort property
	 * 
	 * @return "true" if the property is sorted ascendingly; "false" if the property is sorted descendingly
	 */
	protected boolean getCurrentSortAsc()
	{
		return m_currentSortAsc;

	} // getCurrentSortAsc
	

	// ********* for sorting *********
	public void valueBound(SessionBindingEvent event)
	{
	}

	public void valueUnbound(SessionBindingEvent event)
	{
		// pass it on to my edits
		if ((m_editSite != null) && (m_editSite instanceof SessionBindingListener))
		{
			((SessionBindingListener) m_editSite).valueUnbound(event);
		}

		if ((m_edit != null) && (m_edit instanceof SessionBindingListener))
		{
			((SessionBindingListener) m_edit).valueUnbound(event);
		}
	}
	
}
