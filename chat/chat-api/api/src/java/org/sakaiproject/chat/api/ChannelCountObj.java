/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/branches/ETU-190/chat/chat-api/api/src/java/org/sakaiproject/chat/api/ChannelCountObj.java $
 * $Id: ChannelCountObj.java 3 2008-10-20 18:44:42Z ggolden $
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
package org.sakaiproject.chat.api;
import org.sakaiproject.message.api.MessageChannel;
/**
 * <p>
 * ChannelCountObj is a class used to combine channel and userCount information
 * </p>
 */
public class ChannelCountObj
{
	MessageChannel channel;
	int userCount;
	
	public ChannelCountObj(MessageChannel channel, int userCount)
	{
		this.channel = channel;
		this.userCount = userCount;
	}
	
	public MessageChannel getChannel()
	{
		return channel;
	}
	public void setChannel(MessageChannel channel)
	{
		this.channel = channel;
	}
	public int getUserCount()
	{
		return userCount;
	}
	public void setUserCount(int userCount)
	{
		this.userCount = userCount;
	}
}