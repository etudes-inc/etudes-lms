/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/chat/chat-impl/impl/src/java/org/sakaiproject/chat/impl/BaseChatService.java $
 * $Id: BaseChatService.java 8315 2014-06-25 02:57:00Z mallikamt $
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

package org.sakaiproject.chat.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.chat.api.ChatChannel;
import org.sakaiproject.chat.api.ChatChannelEdit;
import org.sakaiproject.chat.api.ChatChannelHeader;
import org.sakaiproject.chat.api.ChatChannelHeaderEdit;
import org.sakaiproject.chat.api.ChatMessage;
import org.sakaiproject.chat.api.ChatMessageEdit;
import org.sakaiproject.chat.api.ChatMessageHeader;
import org.sakaiproject.chat.api.ChatMessageHeaderEdit;
import org.sakaiproject.chat.api.ChatService;
import org.sakaiproject.entity.api.ContextObserver;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.message.api.MessageChannel;
import org.sakaiproject.message.api.MessageChannelEdit;
import org.sakaiproject.message.api.MessageChannelHeader;
import org.sakaiproject.message.api.MessageChannelHeaderEdit;
import org.sakaiproject.message.api.MessageEdit;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.message.api.MessageHeaderEdit;
import org.sakaiproject.message.impl.BaseMessageService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.StringUtil;
import org.w3c.dom.Element;

/**
 * <p>
 * BaseChatService extends the BaseMessageService for the specifics of Chat.
 * </p>
 */
public abstract class BaseChatService extends BaseMessageService implements ChatService, ContextObserver, EntityTransferrer
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(BaseChatService.class);

	private static final String PARAM_CHANNEL = "channel";
	
	private static final String PARAM_FILTER_TYPE = "filter-type";

	private static final String PARAM_FILTER_PARAM = "filter-param";
	
	private static final String SORT_POSITION = "position";
	
    private static final String SORT_ID = "id";
	
	private static final String SORT_GROUPTITLE = "grouptitle";

	private static final String SORT_GROUPDESCRIPTION = "groupdescription";	
		

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		super.init();

		// register functions
		FunctionManager.registerFunction(eventId(SECURE_READ));
		FunctionManager.registerFunction(eventId(SECURE_ADD));
		FunctionManager.registerFunction(eventId(SECURE_REMOVE_ANY));
		FunctionManager.registerFunction(eventId(SECURE_REMOVE_OWN));

		// entity producer registration
		m_entityManager.registerEntityProducer(this, REFERENCE_ROOT);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * StorageUser implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct a new continer given just ids.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return The new containe Resource.
	 */
	public Entity newContainer(String ref)
	{
		return new BaseChatChannelEdit(ref);
	}

	/**
	 * Construct a new container resource, from an XML element.
	 * 
	 * @param element
	 *        The XML.
	 * @return The new container resource.
	 */
	public Entity newContainer(Element element)
	{
		return new BaseChatChannelEdit(element);
	}

	/**
	 * Construct a new container resource, as a copy of another
	 * 
	 * @param other
	 *        The other contianer to copy.
	 * @return The new container resource.
	 */
	public Entity newContainer(Entity other)
	{
		return new BaseChatChannelEdit((MessageChannel) other);
	}

	/**
	 * Construct a new rsource given just an id.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param id
	 *        The id for the new object.
	 * @param others
	 *        (options) array of objects to load into the Resource's fields.
	 * @return The new resource.
	 */
	public Entity newResource(Entity container, String id, Object[] others)
	{
		return new BaseChatMessageEdit((MessageChannel) container, id);
	}

	/**
	 * Construct a new resource, from an XML element.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param element
	 *        The XML.
	 * @return The new resource from the XML.
	 */
	public Entity newResource(Entity container, Element element)
	{
		return new BaseChatMessageEdit((MessageChannel) container, element);
	}

	/**
	 * Construct a new resource from another resource of the same type.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param other
	 *        The other resource.
	 * @return The new resource as a copy of the other.
	 */
	public Entity newResource(Entity container, Entity other)
	{
		return new BaseChatMessageEdit((MessageChannel) container, (Message) other);
	}

	/**
	 * Construct a new continer given just ids.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return The new containe Resource.
	 */
	public Edit newContainerEdit(String ref)
	{
		BaseChatChannelEdit rv = new BaseChatChannelEdit(ref);
		rv.activate();
		return rv;
	}

	/**
	 * Construct a new container resource, from an XML element.
	 * 
	 * @param element
	 *        The XML.
	 * @return The new container resource.
	 */
	public Edit newContainerEdit(Element element)
	{
		BaseChatChannelEdit rv = new BaseChatChannelEdit(element);
		rv.activate();
		return rv;
	}

	/**
	 * Construct a new container resource, as a copy of another
	 * 
	 * @param other
	 *        The other contianer to copy.
	 * @return The new container resource.
	 */
	public Edit newContainerEdit(Entity other)
	{
		BaseChatChannelEdit rv = new BaseChatChannelEdit((MessageChannel) other);
		rv.activate();
		return rv;
	}

	/**
	 * Construct a new rsource given just an id.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param id
	 *        The id for the new object.
	 * @param others
	 *        (options) array of objects to load into the Resource's fields.
	 * @return The new resource.
	 */
	public Edit newResourceEdit(Entity container, String id, Object[] others)
	{
		BaseChatMessageEdit rv = new BaseChatMessageEdit((MessageChannel) container, id);
		rv.activate();
		return rv;
	}

	/**
	 * Construct a new resource, from an XML element.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param element
	 *        The XML.
	 * @return The new resource from the XML.
	 */
	public Edit newResourceEdit(Entity container, Element element)
	{
		BaseChatMessageEdit rv = new BaseChatMessageEdit((MessageChannel) container, element);
		rv.activate();
		return rv;
	}

	/**
	 * Construct a new resource from another resource of the same type.
	 * 
	 * @param container
	 *        The Resource that is the container for the new resource (may be null).
	 * @param other
	 *        The other resource.
	 * @return The new resource as a copy of the other.
	 */
	public Edit newResourceEdit(Entity container, Entity other)
	{
		BaseChatMessageEdit rv = new BaseChatMessageEdit((MessageChannel) container, (Message) other);
		rv.activate();
		return rv;
	}

	/**
	 * Collect the fields that need to be stored outside the XML (for the resource).
	 * 
	 * @return An array of field values to store in the record outside the XML (for the resource).
	 */
	public Object[] storageFields(Entity r)
	{
		Object[] rv = new Object[4];
		rv[0] = ((Message) r).getHeader().getDate();
		rv[1] = ((Message) r).getHeader().getFrom().getId();
		rv[2] = "0";
		rv[3] = r.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW) == null ? "0" : "1";

		return rv;
	}

	/**
	 * Check if this resource is in draft mode.
	 * 
	 * @param r
	 *        The resource.
	 * @return true if the resource is in draft mode, false if not.
	 */
	public boolean isDraft(Entity r)
	{
		return false;
	}

	/**
	 * Access the resource owner user id.
	 * 
	 * @param r
	 *        The resource.
	 * @return The resource owner user id.
	 */
	public String getOwnerId(Entity r)
	{
		return ((Message) r).getHeader().getFrom().getId();
	}

	/**
	 * Access the resource date.
	 * 
	 * @param r
	 *        The resource.
	 * @return The resource date.
	 */
	public Time getDate(Entity r)
	{
		return ((Message) r).getHeader().getDate();
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Abstractions, etc. satisfied
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Report the Service API name being implemented.
	 */
	protected String serviceName()
	{
		return ChatService.class.getName();
	}

	/**
	 * Construct a new message header from XML in a DOM element.
	 * 
	 * @param id
	 *        The message Id.
	 * @return The new message header.
	 */
	protected MessageHeaderEdit newMessageHeader(Message msg, String id)
	{
		return new BaseChatMessageHeaderEdit(msg, id);

	} // newMessageHeader

	/**
	 * Construct a new message header from XML in a DOM element.
	 * 
	 * @param el
	 *        The XML DOM element that has the header information.
	 * @return The new message header.
	 */
	protected MessageHeaderEdit newMessageHeader(Message msg, Element el)
	{
		return new BaseChatMessageHeaderEdit(msg, el);

	} // newMessageHeader

	/**
	 * Construct a new message header as a copy of another.
	 * 
	 * @param other
	 *        The other header to copy.
	 * @return The new message header.
	 */
	protected MessageHeaderEdit newMessageHeader(Message msg, MessageHeader other)
	{
		return new BaseChatMessageHeaderEdit(msg, other);

	} // newMessageHeader

	/**
	 * Construct a new channel header from XML in a DOM element.
	 * 
	 * @param id
	 *        The channel Id.
	 * @return The new channel header.
	 */
	protected MessageChannelHeaderEdit newChannelHeader(MessageChannel chan/*, String id*/)
	{
		return new BaseChatChannelHeaderEdit(chan/*, id*/);

	} // newChannelHeader

	/**
	 * Construct a new channel header from XML in a DOM element.
	 * 
	 * @param el
	 *        The XML DOM element that has the header information.
	 * @return The new channel header.
	 */
	protected MessageChannelHeaderEdit newChannelHeader(MessageChannel chan, Element el)
	{
		return new BaseChatChannelHeaderEdit(chan, el);

	} // newChannelHeader

	/**
	 * Construct a new channel header as a copy of another.
	 * 
	 * @param other
	 *        The other header to copy.
	 * @return The new channel header.
	 */
	protected MessageChannelHeaderEdit newChannelHeader(MessageChannel chan, MessageChannelHeader other)
	{
		return new BaseChatChannelHeaderEdit(chan, other);

	} // newChannelHeader


	/**
	 * Form a tracking event string based on a security function string.
	 * 
	 * @param secure
	 *        The security function string.
	 * @return The event tracking string.
	 */
	protected String eventId(String secure)
	{
		if (secure.equals(ChatService.SECURE_UPDATE)) return secure;
		if (secure.contains("all.groups")) return ChatService.SECURE_UPDATE;
		if (secure.equals("readgroup")) 
		{
			return "annc.read";
		}
		return "chat." + secure;

	} // eventId

	/**
	 * Return the reference rooot for use in resource references and urls.
	 * 
	 * @return The reference rooot for use in resource references and urls.
	 */
	protected String getReferenceRoot()
	{
		return REFERENCE_ROOT;

	} // getReferenceRoot

	/**
	 * {@inheritDoc}
	 */
	public boolean parseEntityReference(String reference, Reference ref)
	{
		if (reference.startsWith(REFERENCE_ROOT))
		{
			String[] parts = StringUtil.split(reference, Entity.SEPARATOR);

			String id = null;
			String subType = null;
			String context = null;
			String container = null;

			// the first part will be null, then next the service, the third will be "msg" or "channel"
			if (parts.length > 2)
			{
				subType = parts[2];
				if (REF_TYPE_CHANNEL.equals(subType))
				{
					// next is the context id
					if (parts.length > 3)
					{
						context = parts[3];

						// next is the channel id
						if (parts.length > 4)
						{
							id = parts[4];
						}
					}
				}
				else if (REF_TYPE_MESSAGE.equals(subType))
				{
					// next three parts are context, channel (container) and mesage id
					if (parts.length > 5)
					{
						context = parts[3];
						container = parts[4];
						id = parts[5];
					}
				}
				else
					M_log.warn("parse(): unknown message subtype: " + subType + " in ref: " + reference);
			}

			ref.set(APPLICATION_ID, subType, id, container, context);

			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextCreated(String context, boolean toolPlacement)
	{
		if (toolPlacement) enableMessageChannel(context);
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextUpdated(String context, boolean toolPlacement)
	{
		if (toolPlacement) enableChatChannel(context);
	}
	
	protected void enableChatChannel(String siteId)
	{
		String defaultChannel = null;
		try
		{
			Site site = m_siteService.getSite(siteId);

			if (site != null)
			{
				ToolConfiguration toolConfig = site.getToolForCommonId("sakai.chat");
				if (toolConfig != null)
				{
					defaultChannel = StringUtil.trimToNull(toolConfig.getPlacementConfig().getProperty(PARAM_CHANNEL));
					if (defaultChannel == null) defaultChannel = channelReference(siteId, SiteService.MAIN_CONTAINER);
				}
			}
		}
		catch (IdUnusedException e)
		{
		}
		
		// see if there's a channel
		try
		{
			getChannel(defaultChannel);
		}
		catch (IdUnusedException un)
		{
			try
			{
				// create a channel
				MessageChannelEdit edit = addChannel(defaultChannel);
				edit.getPropertiesEdit().addProperty("published", "1");
				edit.getPropertiesEdit().addProperty("position", Integer.toString(1));
				commitChannel(edit);
			}
			catch (IdUsedException e)
			{
			}
			catch (IdInvalidException e)
			{
			}
			catch (PermissionException e)
			{
			}
		}
		catch (PermissionException e)
		{
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void contextDeleted(String context, boolean toolPlacement)
	{
		disableMessageChannel(context);
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] myToolIds()
	{
		String[] toolIds = { "sakai.chat" };
		return toolIds;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ChatService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Return a specific chat channel.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return the ChatChannel that has the specified name.
	 * @exception IdUnusedException
	 *            If this name is not defined for a chat channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to the channel.
	 */
	public ChatChannel getChatChannel(String ref) throws IdUnusedException, PermissionException
	{
		return (ChatChannel) getChannel(ref);

	} // getChatChannel

	/**
	 * Add a new chat channel.
	 * 
	 * @param ref
	 *        The channel reference.
	 * @return The newly created channel.
	 * @exception IdUsedException
	 *            if the id is not unique.
	 * @exception IdInvalidException
	 *            if the id is not made up of valid characters.
	 * @exception PermissionException
	 *            if the user does not have permission to add a channel.
	 */
	public ChatChannelEdit addChatChannel(String ref) throws IdUsedException, IdInvalidException, PermissionException
	{
		return (ChatChannelEdit) addChannel(ref);

	} // addChatChannel

	public ChatChannelEdit updateChatChannel(String ref, String newRef) throws IdUsedException, IdInvalidException, PermissionException
	{
		return (ChatChannelEdit) updateChannel(ref, newRef);
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ResourceService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
		return "chat";
	}

	/**
	 * {@inheritDoc}
	 */
	public List sortByPosition(List channelList)
	{
		if (channelList == null || channelList.size() == 0) return channelList;
		if (!checkPositions(channelList)) return channelList;
		Collections.sort(channelList, new ChatComparator(SORT_POSITION, true));
		return channelList;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void transferCopyEntities(String fromContext, String toContext, List resourceIds)
	{
		// Check if source and destination sites have positions
		// If not, populate their positions
		//populatePositions("/chat/channel/" + fromContext);
		populatePositions("/chat/channel/" + toContext);

		//Get channels from from list in ascending order
		List from_channel_list = getChannels("/chat/channel/" + fromContext);
		from_channel_list = sortByPosition(from_channel_list);
		
		//Get channels in toList
		List to_channel_list = getChannels("/chat/channel/" + toContext);
		if (to_channel_list == null || to_channel_list.size() == 0) return;

		int position = getMaxPosition(to_channel_list) + 1;

		List site_channel_list = new Vector();
		String defaultChannel = "";
		Site fromSite = null, toSite = null;
		ToolConfiguration toToolConfig = null;
		Collection<Group> groups = null;

		Iterator itr = from_channel_list.iterator();
		//Iterate through from list and create channels
		while (itr.hasNext())
		{
			MessageChannel channel = (MessageChannel) itr.next();
			if (defaultChannel.equals(""))
			{
				try
				{
					fromSite = m_siteService.getSite(fromContext);
					toSite = m_siteService.getSite(toContext);

					groups = (Collection<Group>) toSite.getGroups();

					if (fromSite != null)
					{
						ToolConfiguration toolConfig = fromSite.getToolForCommonId("sakai.chat");
						if (toolConfig != null)
						{
							defaultChannel = StringUtil.trimToNull(toolConfig.getPlacementConfig().getProperty(PARAM_CHANNEL));
							if (defaultChannel == null) defaultChannel = SiteService.MAIN_CONTAINER;
							if (toSite != null)
							{
								toToolConfig = toSite.getToolForCommonId("sakai.chat");
								if (toToolConfig.getPlacementConfig().getProperty(PARAM_FILTER_TYPE) == null
										&& toolConfig.getPlacementConfig().getProperty(PARAM_FILTER_TYPE) != null)
									toToolConfig.getPlacementConfig().setProperty(PARAM_FILTER_TYPE,
											toolConfig.getPlacementConfig().getProperty(PARAM_FILTER_TYPE));
								if (toToolConfig.getPlacementConfig().getProperty(PARAM_FILTER_PARAM) == null
										&& toolConfig.getPlacementConfig().getProperty(PARAM_FILTER_PARAM) != null)
									toToolConfig.getPlacementConfig().setProperty(PARAM_FILTER_PARAM,
											toolConfig.getPlacementConfig().getProperty(PARAM_FILTER_PARAM));
							}
						}
					}
				}
				catch (IdUnusedException e)
				{
					M_log.warn("transferCopyEntities(): idunused in getsite ");
				}
			}
			// create the channel
			try
			{
				ChatChannelEdit edit = addChatChannel(channelReference(toContext, channel.getId()));
				if (extractChannelId(defaultChannel).equals(channel.getId()))
				{
					if (edit.getPropertiesEdit().getProperty("published") != null) edit.getPropertiesEdit().removeProperty("published");
					edit.getPropertiesEdit().addProperty("published", "0");
					edit.getPropertiesEdit().addProperty("position", Integer.toString(position));
				}
				else
				{
					if (edit.getPropertiesEdit().getProperty("published") != null) edit.getPropertiesEdit().removeProperty("published");
					edit.getPropertiesEdit().addProperty("published", "0");
					edit.getPropertiesEdit().addProperty("position", Integer.toString(position));
				}
				// groups
				position++;

				Set<Group> eventGroups = new HashSet<Group>();
				if (channel.getHeader() != null)
				{
					for (Group oGroup : (Collection<Group>) channel.getHeader().getGroupObjects())
					{
						for (Group g : groups)
						{
							if (g.getTitle().equals(oGroup.getTitle()))
							{
								eventGroups.add(g);
								break;
							}
						}
					}

					if (!eventGroups.isEmpty())
					{
						edit.getChatChannelHeaderEdit().setGroupAccess(eventGroups);
					}
				}
				commitChannel(edit);
			}
			catch (IdUsedException e)
			{
				// M_log.warn("transferCopyEntities(): channel already exists " + channel.getId());
			}
			catch (IdInvalidException e)
			{
				M_log.warn("transferCopyEntities(): can't use this id " + channel.getId());
			}
			catch (PermissionException e)
			{
				M_log.warn("transferCopyEntities(): not permitted to add channel");
			}
		}

		if (toSite != null)
		{
			try
			{
				m_siteService.save(toSite);
			}
			catch (IdUnusedException e)
			{
				M_log.warn("transferCopyEntities(): idunused in savesite ");
			}
			catch (PermissionException e)
			{
				M_log.warn("transferCopyEntities(): permissionexception in savesite");
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void populatePositions(String siteId, String defaultChannel)
	{
		Integer position = new Integer(1);
		ChatChannelEdit channelEdit = null;
		MessageChannel channel = null;

		//Get channels for this site
		List channel_list = getChannels("/chat/channel/" + siteId);
		List site_channel_list = new Vector();

		if (channel_list == null || channel_list.size() == 0) return;
		Iterator itr2 = channel_list.iterator();
		int i = 0, mainPos = 0;
		//Determine position of defaultChannel
		while (itr2.hasNext())
		{
			channel = (MessageChannel) itr2.next();

			site_channel_list.add(channel);
			if (channel.getId().equals(defaultChannel)) mainPos = i;
			i++;
		}

		if (site_channel_list == null || site_channel_list.size() == 0) return;

		//Check to see if the channels have positions. If so, return
		boolean checkPositions = checkPositions(site_channel_list);
		boolean checkDefaultChannel = checkDefaultPosition(site_channel_list, defaultChannel);
		if (checkPositions && checkDefaultChannel) return;
		site_channel_list.remove(mainPos);
		
		Site site = null;
		//Move default channel to position 1, publish it, clear its groups
		//position all other channels after default channel
		//
		try
		{
			// First, set the position for main
			channelEdit = (ChatChannelEdit) editChannel(channelReference(siteId,defaultChannel));
			if (channelEdit.getPropertiesEdit().getProperty("position") != null) channelEdit.getPropertiesEdit().removeProperty("position");
			channelEdit.getPropertiesEdit().addProperty("position", position.toString());
			if (position == 1)
			{
				site = m_siteService.getSite(siteId);
				if (site != null)
				{
					ToolConfiguration toolConfig = site.getToolForCommonId("sakai.chat");
					toolConfig.getPlacementConfig().setProperty(PARAM_CHANNEL,channelReference(siteId, defaultChannel));
				}
				if (channelEdit.getPropertiesEdit().getProperty("published") != null) channelEdit.getPropertiesEdit().removeProperty("published");
				channelEdit.getPropertiesEdit().addProperty("published", "1");
				channelEdit.getChatChannelHeaderEdit().clearGroupAccess();
			}

			commitChannel(channelEdit);
			Iterator itr3 = site_channel_list.iterator();
			while (itr3.hasNext())
			{
				channel = (MessageChannel) itr3.next();
				position++;
				channelEdit = (ChatChannelEdit) editChannel(channelReference(siteId,channel.getId()));
				if (channelEdit.getPropertiesEdit().getProperty("position") != null) channelEdit.getPropertiesEdit().removeProperty("position");
				channelEdit.getPropertiesEdit().addProperty("position", position.toString());
				commitChannel(channelEdit);
			}
		}
		catch (IdUnusedException ignore)
		{
			M_log.warn("populatePositions()" + ignore);
		}
		catch (PermissionException ignore)
		{
			M_log.warn("populatePositions()" + ignore);
		}
		catch (InUseException ignore)
		{
			M_log.warn("populatePositions()" + ignore);
		}
		catch (Exception e)
		{
			// do nothing
			M_log.warn("populatePositions()" + e);
		}
		//Save site
		if (site != null)
		{
			try
			{
				m_siteService.save(site);
			}
			catch (IdUnusedException e)
			{
				M_log.warn("populatePositions(): idunused in savesite ");
			}
			catch (PermissionException e)
			{
				M_log.warn("populatePositions(): permissionexception in savesite");
			}
		}
		return;
	}
	
	/**
	 * Invokes the populatePositions method for this site for the default channel
	 * 
	 * @param siteId Default channel
	 */
	private void populatePositions(String siteId)
	{
		String defaultChannel = null;
		try
		{
			Site site = m_siteService.getSite(siteId);

			if (site != null)
			{
				ToolConfiguration toolConfig = site.getToolForCommonId("sakai.chat");
				if (toolConfig != null)
				{
					defaultChannel = StringUtil.trimToNull(toolConfig.getPlacementConfig().getProperty(PARAM_CHANNEL));
					if (defaultChannel == null) defaultChannel = SiteService.MAIN_CONTAINER;
				}
			}
		}
		catch (IdUnusedException e)
		{
		}

		populatePositions(siteId, defaultChannel);

	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean checkPositions(List chatList)
	{
		if (chatList == null || chatList.size() == 0) return false;
		boolean found = true;
		Iterator itr = chatList.iterator();
		while (found && itr.hasNext())
		{
			ChatChannel channel = (ChatChannel) itr.next();
			String positionStr = channel.getProperties().getProperty("position");
			if (positionStr == null) 
			{
				found = false;
				break;
			}
		}
		return found;
	}
	
	private boolean checkDefaultPosition(List chatList, String defaultChannel)
	{
		if (chatList == null || chatList.size() == 0) return false;
		boolean found = true;
		Iterator itr = chatList.iterator();
		while (itr.hasNext())
		{
			ChatChannel channel = (ChatChannel) itr.next();
			String positionStr = channel.getProperties().getProperty("position");
			if (channel.getId().equals(defaultChannel)) 
			{
				if (positionStr != null && positionStr.equals("1")) return true;
			}
		}
		return false;
	}
	
	/**
	 * Get the max position in the chat rooms of this list
	 * 
	 * @param chatList The chat list
	 * @return max position
	 */
	public int getMaxPosition(List chatList)
	{
		String positionStr;
		if (chatList == null || chatList.size() == 0) return 0;
		int posVal = 1;
		int position = 1;
		boolean found = false;
		
		Iterator itr = chatList.iterator();
		while (itr.hasNext())
		{
			ChatChannel channel = (ChatChannel) itr.next();
			if (channel.getProperties().getProperty("position") != null)
			{
				positionStr = channel.getProperties().getProperty("position");
				if (positionStr != null) position = Integer.parseInt(positionStr);
				if (position > posVal) posVal = position;
			}
		}
	
        return posVal;
	}	

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ChatChannel implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseChatChannelEdit extends BaseMessageChannelEdit implements ChatChannelEdit
	{
		/**
		 * Construct with a reference.
		 * 
		 * @param ref
		 *        The channel reference.
		 */
		public BaseChatChannelEdit(String ref)
		{
			super(ref);

		} // BaseChatChannelEdit

		/**
		 * Construct as a copy of another message.
		 * 
		 * @param other
		 *        The other message to copy.
		 */
		public BaseChatChannelEdit(MessageChannel other)
		{
			super(other);

		} // BaseChatChannelEdit


		/**
		 * Construct from a channel (and possibly messages) already defined in XML in a DOM tree. The Channel is added to storage.
		 * 
		 * @param el
		 *        The XML DOM element defining the channel.
		 */
		public BaseChatChannelEdit(Element el)
		{
			super(el);

		} // BaseChatChannelEdit

		/**
		 * Return a specific chat channel message, as specified by message name.
		 * 
		 * @param messageId
		 *        The id of the message to get.
		 * @return the ChatMessage that has the specified id.
		 * @exception IdUnusedException
		 *            If this name is not a defined message in this chat channel.
		 * @exception PermissionException
		 *            If the user does not have any permissions to read the message.
		 */
		public ChatMessage getChatMessage(String messageId) throws IdUnusedException, PermissionException
		{
			return (ChatMessage) getMessage(messageId);

		} // getChatMessage

		/**
		 * A (ChatMessageEdit) cover for editMessage. Return a specific channel message, as specified by message name, locked for update. Must commitEdit() to make official, or cancelEdit() when done!
		 * 
		 * @param messageId
		 *        The id of the message to get.
		 * @return the Message that has the specified id.
		 * @exception IdUnusedException
		 *            If this name is not a defined message in this channel.
		 * @exception PermissionException
		 *            If the user does not have any permissions to read the message.
		 * @exception InUseException
		 *            if the current user does not have permission to mess with this user.
		 */
		public ChatMessageEdit editChatMessage(String messageId) throws IdUnusedException, PermissionException, InUseException
		{
			return (ChatMessageEdit) editMessage(messageId);

		} // editChatMessage
		
		public BaseMessageEdit updateMessage(String id, String newRef) throws PermissionException
		{
			BaseMessageEdit msg = (BaseMessageEdit) super.updateMessage(id, newRef);
			//msg.activate();
			return msg;
		}

		/**
		 * A (ChatMessageEdit) cover for addMessage. Add a new message to this channel. Must commitEdit() to make official, or cancelEdit() when done!
		 * 
		 * @return The newly added message, locked for update.
		 * @exception PermissionException
		 *            If the user does not have write permission to the channel.
		 */
		public ChatMessageEdit addChatMessage() throws PermissionException
		{
			return (ChatMessageEdit) addMessage();

		} // addChatMessage

		/**
		 * a (ChatMessage) cover for addMessage to add a new message to this channel.
		 * 
		 * @param attachments
		 *        The message header attachments, a vector of Reference objects.
		 * @param body
		 *        The body text.
		 * @return The newly added message.
		 * @exception PermissionException
		 *            If the user does not have write permission to the channel.
		 */
		public ChatMessage addChatMessage(List attachments, String body) throws PermissionException
		{
			ChatMessageEdit edit = (ChatMessageEdit) addMessage();
			ChatMessageHeaderEdit header = edit.getChatHeaderEdit();
			edit.setBody(body);
			header.replaceAttachments(attachments);

			commitMessage(edit);

			return edit;

		} // addChatMessage

		/**
		 * Access the chat channel header.
		 *
		 * @return The chat channel header.
		 */
		public ChatChannelHeader getChatHeader()
		{
			return (ChatChannelHeader) getHeader();

		} // getChatHeader
				
		
		/**
		 * Access the chat channel header.
		 *
		 * @return The chat channel header.
		 */
		public ChatChannelHeaderEdit getChatChannelHeaderEdit()
		{
			return (ChatChannelHeaderEdit) getHeader();

		} // getChatHeaderEdit
		
	} // class BaseChatChannelEdit

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ChatMessage implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseChatMessageEdit extends BaseMessageEdit implements ChatMessageEdit
	{
		/**
		 * Construct.
		 * 
		 * @param channel
		 *        The channel in which this message lives.
		 * @param id
		 *        The message id.
		 */
		public BaseChatMessageEdit(MessageChannel channel, String id)
		{
			super(channel, id);

		} // BaseChatMessageEdit

		/**
		 * Construct as a copy of another message.
		 * 
		 * @param other
		 *        The other message to copy.
		 */
		public BaseChatMessageEdit(MessageChannel channel, Message other)
		{
			super(channel, other);

		} // BaseChatMessageEdit

		/**
		 * Construct from an existing definition, in xml.
		 * 
		 * @param channel
		 *        The channel in which this message lives.
		 * @param el
		 *        The message in XML in a DOM element.
		 */
		public BaseChatMessageEdit(MessageChannel channel, Element el)
		{
			super(channel, el);

		} // BaseChatMessageEdit

		/**
		 * Access the chat message header.
		 * 
		 * @return The chat message header.
		 */
		public ChatMessageHeader getChatHeader()
		{
			return (ChatMessageHeader) getHeader();

		} // getChatHeader

		/**
		 * Access the chat message header.
		 * 
		 * @return The chat message header.
		 */
		public ChatMessageHeaderEdit getChatHeaderEdit()
		{
			return (ChatMessageHeaderEdit) getHeader();

		} // getChatHeaderEdit

	} // class BasicChatMessageEdit

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ChatMessageHeaderEdit implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseChatMessageHeaderEdit extends BaseMessageHeaderEdit implements ChatMessageHeaderEdit
	{
		/**
		 * Construct.
		 * 
		 * @param id
		 *        The unique (within the channel) message id.
		 * @param from
		 *        The User who sent the message to the channel.
		 * @param attachments
		 *        The message header attachments, a vector of Reference objects.
		 */
		public BaseChatMessageHeaderEdit(Message msg, String id)
		{
			super(msg, id);

		} // BaseChatMessageHeaderEdit

		/**
		 * Construct, from an already existing XML DOM element.
		 * 
		 * @param el
		 *        The header in XML in a DOM element.
		 */
		public BaseChatMessageHeaderEdit(Message msg, Element el)
		{
			super(msg, el);

		} // BaseChatMessageHeaderEdit

		/**
		 * Construct as a copy of another header.
		 * 
		 * @param other
		 *        The other message header to copy.
		 */
		public BaseChatMessageHeaderEdit(Message msg, MessageHeader other)
		{
			super(msg, other);
		}
	}
	
	/**********************************************************************************************************************************************************************************************************************************************************
	 * ChatMessageHeaderEdit implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseChatChannelHeaderEdit extends BaseMessageChannelHeaderEdit implements ChatChannelHeaderEdit
	{
		/**
		 * Construct.
		 */
		public BaseChatChannelHeaderEdit(MessageChannel chan/*, String id*/)
		{
			super(chan/*, id*/);

		} // BaseChatChannelHeaderEdit

		/**
		 * Construct, from an already existing XML DOM element.
		 * 
		 * @param el
		 *        The header in XML in a DOM element.
		 */
		public BaseChatChannelHeaderEdit(MessageChannel chan, Element el)
		{
			super(chan, el);

		} // BaseChatChannelHeaderEdit

		/**
		 * Construct as a copy of another header.
		 * 
		 * @param other
		 *        The other message header to copy.
		 */
		public BaseChatChannelHeaderEdit(MessageChannel chan, MessageChannelHeader other)
		{
			super(chan, other);
}
	}
	
	private class ChatComparator implements Comparator
	{
		// the criteria
		String m_criteria = null;

		// the criteria - asc
		boolean m_asc = true;

		/**
		 * constructor
		 *
		 * @param criteria
		 *        The sort criteria string
		 * @param asc
		 *        The sort order string. "true" if ascending; "false" otherwise.
		 */
		public ChatComparator(String criteria, boolean asc)
		{
			m_criteria = criteria;
			m_asc = asc;

		} // constructor

		/**
		 * implementing the compare function
		 *
		 * @param o1
		 *        The first object
		 * @param o2
		 *        The second object
		 * @return The compare result. 1 is o1 < o2; -1 otherwise
		 */
		public int compare(Object o1, Object o2)
		{
			int result = -1;

			if (m_criteria.equals(SORT_ID))
			{
				// sorted by the id
				result = ((ChatChannel) o1).getId().compareToIgnoreCase(
						((ChatChannel) o2).getId());
			}
			else if (m_criteria.equals(SORT_POSITION))
			{
				// sorted by the manual position
				String factor1 = ((ChatChannel) o1 != null) ? ((ChatChannel) o1).getProperties().getProperty("position") : "";
				String factor2 = ((ChatChannel) o2 != null) ? ((ChatChannel) o2).getProperties().getProperty("position") : "";
				Integer position1 = new Integer(0);
				Integer position2 = new Integer(0);
				
				if (factor1 != null)
				{
					position1 = new Integer(factor1);
				}
				if (factor2 != null)
				{
					position2 = new Integer(factor2);
				}
				result = position1.compareTo(position2);
				
				/*if (result == 0)
				{
					try
					{
						Time o1releaseDate = ((AnnouncementMessage) o1 != null) ? ((AnnouncementMessage) o1).getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE) : null;
						Time o2releaseDate = ((AnnouncementMessage) o2 != null) ? ((AnnouncementMessage) o2).getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE) : null;
						if (o1releaseDate != null && o2releaseDate != null)
						{
							// sorted by the discussion message date
							result = (o1releaseDate.before(o2releaseDate)) ? 1 : -1;	
							// sorted by title too if same
							if (o1releaseDate.equals(o2releaseDate)) result = 0;
							if (result == 0)
							{
								result = ((AnnouncementMessage) o1).getAnnouncementHeader().getSubject().compareToIgnoreCase(
										((AnnouncementMessage) o2).getAnnouncementHeader().getSubject());
							}
						}						
					}
					catch (Exception e)
					{
					}
				}*/
			}
			else if (m_criteria.equals(SORT_GROUPTITLE))
			{
				// sorted by the group title
				String factor1 = ((Group) o1).getTitle();
				String factor2 = ((Group) o2).getTitle();
				result = factor1.compareToIgnoreCase(factor2);
			}
			else if (m_criteria.equals(SORT_GROUPDESCRIPTION))
			{
				// sorted by the group title
				String factor1 = ((Group) o1).getDescription();
				String factor2 = ((Group) o2).getDescription();
				if (factor1 == null)
				{
					factor1 = "";
				}
				if (factor2 == null)
				{
					factor2 = "";
				}
				result = factor1.compareToIgnoreCase(factor2);
			}
			// sort ascending or descending
			if (!m_asc)
			{
				result = -result;
			}
			return result;

		} // compare

	} // ChatComparator		
}
