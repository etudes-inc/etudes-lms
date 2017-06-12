/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/chat/chat-tool/tool/src/java/org/sakaiproject/chat/tool/ChatAction.java $
 * $Id: ChatAction.java 9077 2014-10-27 20:25:12Z mallikamt $
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.PermissionsHelper;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.chat.api.ChannelCountObj;
import org.sakaiproject.chat.api.ChatChannel;
import org.sakaiproject.chat.api.ChatChannelEdit;
import org.sakaiproject.chat.api.ChatMessage;
import org.sakaiproject.chat.api.ChatMessageEdit;
import org.sakaiproject.chat.cover.ChatService;
import org.sakaiproject.chat.tool.ChatActionState;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.cheftool.ControllerState;
import org.sakaiproject.cheftool.JetspeedRunData;
import org.sakaiproject.cheftool.PortletConfig;
import org.sakaiproject.cheftool.RunData;
import org.sakaiproject.cheftool.VelocityPortlet;
import org.sakaiproject.cheftool.VelocityPortletPaneledAction;
import org.sakaiproject.cheftool.api.Menu;
import org.sakaiproject.cheftool.api.MenuItem;
import org.sakaiproject.cheftool.menu.MenuEntry;
import org.sakaiproject.cheftool.menu.MenuImpl;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.event.api.SessionState;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.message.api.MessageChannelEdit;
import org.sakaiproject.message.api.MessageChannel;
import org.sakaiproject.message.api.MessageChannelHeader;
import org.sakaiproject.message.api.MessageEdit;
import org.sakaiproject.message.api.MessageService;
import org.sakaiproject.presence.cover.PresenceService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.PresenceObservingCourier;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.SortedIterator;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.sakaiproject.vm.ActionURL;

/**
 * <p>
 * ChatAction is the Sakai chat tool.
 * </p>
 */
public class ChatAction extends VelocityPortletPaneledAction
{
	/** Our logger. */
	//private static Log M_log = LogFactory.getLog(ChatAction.class);

	/** Resource bundle using current language locale */
	private static ResourceLoader rb = new ResourceLoader("chat");

	private static final String MODE_CONFIRM_DELETE_MESSAGE = "confirmmdeletemessage";

	/** portlet configuration parameter names. */
	private static final String PARAM_CHANNEL = "channel";

	private static final String PARAM_DISPLAY_DATE = "display-date";

	private static final String PARAM_DISPLAY_TIME = "display-time";

	private static final String PARAM_DISPLAY_USER = "display-user";

	private static final String PARAM_SOUND_ALERT = "sound-alert";

	private static final String PARAM_MEMBER_FILTER = "member-filter";

	private static final String PARAM_FILTER_TYPE = "filter-type";

	private static final String PARAM_FILTER_PARAM = "filter-param";

	/** Configure form field names. */
	private static final String FORM_CHANNEL = "channel";

	private static final String FORM_NEW_CHANNEL = "new-channel";

	private static final String FORM_FILTER_TYPE = "filter-type";

	private static final String FORM_FILTER_PARAM_DAYS = "filter-param-days";

	private static final String FORM_FILTER_PARAM_NUMBER = "filter-param-number";

	/** Message filter names */
	private static final String FILTER_BY_NUMBER = "SelectMessagesByNumber";

	private static final String FILTER_BY_TIME = "SelectMessagesByTime";

	private static final String FILTER_TODAY = "SelectTodaysMessages";

	private static final String FILTER_ALL = "SelectAllMessages";

	private static final String[] ALL_FILTERS = { FILTER_BY_NUMBER, FILTER_BY_TIME, FILTER_TODAY, FILTER_ALL };

	/** Default values to use in case of input errors */
	private static final int DEFAULT_PARAM = 0;

	private static final int DEFAULT_DAYS = 3;

	private static final int DEFAULT_MSGS = 12;

	/** Control form field names. */
	private static final String FORM_MESSAGE = "message";

	/** names and values of request parameters to select sub-panels */
	private static final String MONITOR_PANEL = "List";

	private static final String CONTROL_PANEL = "Control";

	private static final String PRESENCE_PANEL = "Presence";

	private static final String TOOLBAR_PANEL = "Toolbar";

	private static final String ROOMS_PANEL = "Rooms";

	/** state attribute names. */
	private static final String STATE_CHANNEL_REF = "channelId";

	private static final String STATE_EDIT_CHANNEL = "editChannelId";

	private static final String STATE_SITE = "siteId";

	private static final String STATE_DISPLAY_DATE = "display-date";

	private static final String STATE_DISPLAY_TIME = "display-time";

	private static final String STATE_DISPLAY_USER = "display-user";

	private static final String STATE_SOUND_ALERT = "sound-alert";

	private static final String STATE_UPDATE = "update";

	private static final String STATE_CHANNEL_PROBLEM = "channel-problem";

	private static final String STATE_FILTER_TYPE = "filter-type";

	private static final String STATE_FILTER_PARAM = "filter-param";

	private static final String STATE_MESSAGE_FILTER = "message-filter";

	private static final String STATE_MORE_SELECTED = "more-selected";

	private static final String STATE_MORE_MESSAGES_LABEL = "more-messages-label";

	private static final String STATE_MORE_MESSAGES_FILTER = "more-messages-filter";

	private static final String STATE_FEWER_MESSAGES_LABEL = "fewer-messages-label";

	private static final String STATE_FEWER_MESSAGES_FILTER = "fewer-messages-filter";

	private static final String STATE_BROWSER = "browser";

	private static final String STATE_CHAT_PRESENCE_OBSERVER = STATE_OBSERVER2;

	private static final String STATE_CHAT_ROOMS_OBSERVER = "rooms_observer";

	private static final String STATE_COLOR_MAPPER = "color-mapper";

	private static final String STATE_MAIN_MESSAGE = "message-for-chat-layout";

	private static final String NEW_CHAT_CHANNEL = "new-chat-channel";

	private static final String CHANNELID = "channelid";

	/** Resource property on the message indicating that the message had been deleted */
	private static final String PROPERTY_MESSAGE_DELETED = "deleted";

	private static final String TIME_DATE_SELECT = "selected-time-date-display";

	/** State attribute set when we need to go into permissions mode. */
	private static final String STATE_PERMISSIONS = "sakai:chat:permissions";

	public static final String MODE_MANAGE = "manage";

	public static final String MODE_ADD = "add";
	
	public static final String MODE_DELETECHANNEL = "deletechannel";
	
	public static final String DELETE_CHANNELS = "delete-channels";
	
	public static final String MODE_LAYOUT = "layout";
	
	public static final String MODE_CONFIRMPUBLISH = "confirmpublish";
	
	public static final String PUBLISH_CHANNELS = "publish-channels";
	
	public static final String GROUP_CHANNEL = "group-channel";
	
	public static final String NONE_SELECTED = "none-selected";
	
    private static final String SORT_POSITION = "position";
	
	private static final String SORT_ID = "id";
	
	private static final String SORT_GROUPTITLE = "grouptitle";

	private static final String SORT_GROUPDESCRIPTION = "groupdescription";	
	
	private static final String STATE_CURRENT_SORTED_BY = "session.state.sorted.by";

	private static final String STATE_CURRENT_SORT_ASC = "session.state.sort.asc";
	
	private static final int TITLE_SIZE = 48;
	
	/** The property that needs to be set ("true") on a site group to distinguish it from a "section" */
	private static final String GROUP_PROP_WSETUP_CREATED = "group_prop_wsetup_created";


	/**
	 * Populate the state object, if needed.
	 */
	protected void initState(SessionState state, VelocityPortlet portlet, JetspeedRunData rundata)
	{
		super.initState(state, portlet, rundata);

		// detect that we have not done this, yet
		if (state.getAttribute(STATE_CHANNEL_REF) == null)
		{
			PortletConfig config = portlet.getPortletConfig();

			// read the channel from configuration, or, if not specified, use the default for the page
			String channel = StringUtil.trimToNull(config.getInitParameter(PARAM_CHANNEL));
			if (channel == null)
			{
				channel = ChatService.channelReference(ToolManager.getCurrentPlacement().getContext(), SiteService.MAIN_CONTAINER);
			}
			populatePositions(ChatService.extractChannelId(channel));

			state.setAttribute(STATE_CHANNEL_REF, channel);

			if (state.getAttribute(STATE_DISPLAY_DATE) == null)
			{
				state.setAttribute(STATE_DISPLAY_DATE, new Boolean(config.getInitParameter(PARAM_DISPLAY_DATE)));
			}

			if (state.getAttribute(STATE_DISPLAY_TIME) == null)
			{
				state.setAttribute(STATE_DISPLAY_TIME, new Boolean(config.getInitParameter(PARAM_DISPLAY_TIME)));
			}

			if (state.getAttribute(STATE_DISPLAY_USER) == null)
			{
				state.setAttribute(STATE_DISPLAY_USER, new Boolean(config.getInitParameter(PARAM_DISPLAY_USER)));
			}

			if (state.getAttribute(STATE_SOUND_ALERT) == null)
			{
				state.setAttribute(STATE_SOUND_ALERT, new Boolean(config.getInitParameter(PARAM_SOUND_ALERT)));
			}

			if (state.getAttribute(STATE_COLOR_MAPPER) == null)
			{
				ColorMapper mapper = new ColorMapper();

				// always set this user's color to first color (red)
				mapper.getColor(StringUtil.trimToZero(SessionManager.getCurrentSessionUserId()));

				state.setAttribute(STATE_COLOR_MAPPER, mapper);
			}

			if (state.getAttribute(STATE_FILTER_TYPE) == null)
			{
				String filter_type = config.getInitParameter(PARAM_FILTER_TYPE, FILTER_BY_TIME);
				String filter_param = config.getInitParameter(PARAM_FILTER_PARAM, String.valueOf(DEFAULT_DAYS));

				updateMessageFilters(state, filter_type, filter_param);
			}

			// the event resource reference pattern to watch for
			// setup the observer to notify our MONITOR_PANEL panel (inside the Main panel)
			if (state.getAttribute(STATE_OBSERVER) == null)
			{
				// get the current tool placement
				Placement placement = ToolManager.getCurrentPlacement();

				// location is placement + name of chat room
				String channelref = (String)state.getAttribute(STATE_CHANNEL_REF);
				String location = placement.getId() + "|" + ChatService.extractChannelId(channelref);


				// the html element to update on delivery
				String elementId = MONITOR_PANEL;
				Reference r = EntityManager.newReference(channel);
				String pattern = ChatService.messageReference(r.getContext(), r.getId(), "");
				boolean wantsBeeps = ((Boolean) state.getAttribute(STATE_SOUND_ALERT)).booleanValue();

				state.setAttribute(STATE_OBSERVER, new ChatObservingCourier(location, elementId, pattern, wantsBeeps));
			}

			// the event resource reference pattern to watch for
			// setup the observer to notify our PRESENCE_PANEL panel (inside the Main panel)
			if (state.getAttribute(STATE_CHAT_PRESENCE_OBSERVER) == null)
			{
				// get the current tool placement
				Placement placement = ToolManager.getCurrentPlacement();

				// location is placement + name of chat room
				String channelref = (String)state.getAttribute(STATE_CHANNEL_REF);
				String location = placement.getId() + "|" + ChatService.extractChannelId(channelref);

				// the html element to update on delivery
				String elementId = PRESENCE_PANEL;

				PresenceService.setPresence(location);

				// setup an observer to notify us when presence at this location changes
				PresenceObservingCourier observer = new PresenceObservingCourier(location, elementId);

				state.setAttribute(STATE_CHAT_PRESENCE_OBSERVER, observer);
			}
			
			// setup the observer to notify our ROOMS_PANEL panel (inside the Main panel)
			if (state.getAttribute(STATE_CHAT_ROOMS_OBSERVER) == null)
			{
				// get the current tool placement
				Placement placement = ToolManager.getCurrentPlacement();

				// location is placement (to match all rooms)
				String location = placement.getId();

				// the html element to update on delivery
				String elementId = ROOMS_PANEL;

				// setup an observer to notify us when presence in any chat room changes
				PresenceObservingCourier observer = new PresenceObservingCourier(location, elementId);

				state.setAttribute(STATE_CHAT_ROOMS_OBSERVER, observer);
			}
		}
		// repopulate state object and title bar when default chat room changes
		else
		{
			PortletConfig config = portlet.getPortletConfig();

			// read the channel from configuration, or, if not specified, use the default for the page
			String channel = StringUtil.trimToNull(config.getInitParameter(PARAM_CHANNEL));
			if (channel == null)
			{
				channel = ChatService.channelReference(ToolManager.getCurrentPlacement().getContext(), SiteService.MAIN_CONTAINER);
			}
			populatePositions(ChatService.extractChannelId(channel));
			/*PortletConfig config = portlet.getPortletConfig();
			// read the channel from configuration, or, if not specified, use the default for the page
			String channel = StringUtil.trimToNull(config.getInitParameter(PARAM_CHANNEL));
			if (channel == null)
			{
				channel = ChatService.channelReference(ToolManager.getCurrentPlacement().getContext(), SiteService.MAIN_CONTAINER);
			}
			state.setAttribute(STATE_CHANNEL_REF, channel);
			String channelName = rundata.getParameters().getString(FORM_CHANNEL);
			// update the tool config
			Placement placement = ToolManager.getCurrentPlacement();
			placement.setTitle(rb.getString("chat"));*/
			
		}
		// make sure the observer is in sync with state
		updateObservationOfChannel(state, portlet.getID());

	} // initState

	/**
	 * Removes the position property from the channels of this site
	 * 
	 */
	private void removePositions()
	{
		MessageChannelEdit channelEdit = null;
		MessageChannel channel = null;
		List channel_list = ChatService.getChannels("/chat/channel/" + ToolManager.getCurrentPlacement().getContext());
		if (channel_list == null || channel_list.size() == 0) return;
		Iterator itr2 = channel_list.iterator();
		while (itr2.hasNext())
		{
			channel = (MessageChannel) itr2.next();

			try
			{
				// First, set the position for main
				channelEdit = ChatService.editChannel(ChatService.channelReference(ToolManager.getCurrentPlacement().getContext(), channel.getId()));
				if (channelEdit.getPropertiesEdit().getProperty("position") != null) channelEdit.getPropertiesEdit().removeProperty("position");
				ChatService.commitChannel(channelEdit);
			}
			catch (IdUnusedException ignore)
			{
				if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "removePositions()" + ignore);
			}
			catch (PermissionException ignore)
			{
				if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "removePositions()" + ignore);
			}
			catch (InUseException ignore)
			{
				if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "removePositions()" + ignore);
			}
			catch (Exception e)
			{
				// do nothing
				if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "removePositions()" + e);
			}

		}

	}
	
	private void populatePositions(String defaultChannel)
	{
		ChatService.populatePositions(ToolManager.getCurrentPlacement().getContext(), defaultChannel);
	}
	
	/**
	 * Returns the name of the default channel, or main if one does not exist
	 * in the format /chat/channel/site_id/chatroomname
	 * @return
	 */
	private String getDefaultChannel()
	{
		Placement placement = ToolManager.getCurrentPlacement();
		String defaultChannel = StringUtil.trimToNull((String) placement.getPlacementConfig().getProperty(PARAM_CHANNEL));
		if (defaultChannel == null)
		{
			defaultChannel = ChatService.channelReference(placement.getContext(), SiteService.MAIN_CONTAINER);
		}
		return defaultChannel;
	}

	/**
	 * Setup our observer to be watching for change events for our channel.
	 * 
	 * @param peid
	 *        The portlet id.
	 */
	private void updateObservationOfChannel(SessionState state, String peid)
	{
		// make sure the pattern matches the channel we are looking at
		String channel = (String) state.getAttribute(STATE_CHANNEL_REF);
		Reference r = EntityManager.newReference(channel);
		String pattern = ChatService.messageReference(r.getContext(), r.getId(), "");

		// update the observer looking for new messages
		ChatObservingCourier observer1 = (ChatObservingCourier) state.getAttribute(STATE_OBSERVER);
		observer1.setResourcePattern(pattern);

		// location is placement + name of chat room
		String location = observer1.getDeliveryId().substring(0, observer1.getDeliveryId().indexOf("|")) + "|" + ChatService.extractChannelId(channel);
		observer1.setDeliveryId(location);

		// update where the deliveries to the room observer get sent
		PresenceObservingCourier roomObserver = (PresenceObservingCourier) state.getAttribute(STATE_CHAT_ROOMS_OBSERVER);
		roomObserver.setDeliveryId(location);

	} // updateObservationOfChannel

	/**
	 * Get the channel from ChatService or create it.
	 */
	private ChatChannel getChannel(SessionState state, String name)
	{
		// deal with the channel not yet existing
		ChatChannel channel = null;
		try
		{
			channel = ChatService.getChatChannel(name);
		}
		catch (IdUnusedException ignore)
		{
		}
		catch (PermissionException ignore)
		{
		}

		if ((channel == null) && (state.getAttribute(STATE_CHANNEL_PROBLEM) == null))
		{
			// create the channel
			try
			{
				ChatChannelEdit edit = ChatService.addChatChannel(name);
				ChatService.commitChannel(edit);
				channel = edit;
			}
			catch (IdUsedException e)
			{
				// strange, the channel already exists!
				try
				{
					channel = ChatService.getChatChannel(name);
				}
				catch (IdUnusedException ignore)
				{
				}
				catch (PermissionException ignore)
				{
				}
			}
			catch (IdInvalidException e)
			{
				// stranger, we cannot use this id!
				state.setAttribute(STATE_CHANNEL_PROBLEM, rb.getString("thischat"));
				if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "getChannel()" + e);
			}
			catch (PermissionException e)
			{
				// rats, this user cannot create the channel
				state.setAttribute(STATE_CHANNEL_PROBLEM, rb.getString("youdonot2"));
			}
		}
		return channel;

	} // getChannel

	/**
	 * Update the state message-filtering attributes to use the filtering criteria specified by the filter_type and filter_param parameters.
	 * 
	 * @param state
	 *        The session state.
	 * @param filter_type
	 *        A string specifying the filter type.
	 * @param filter_param
	 *        A string specifying the filter param.
	 */
	private void updateMessageFilters(SessionState state, String filter_type, String filter_param)
	{
		state.setAttribute(STATE_MORE_MESSAGES_LABEL, rb.getString("showall"));
		state.setAttribute(STATE_MORE_MESSAGES_FILTER, new SelectAllMessages());
		state.setAttribute(STATE_FILTER_PARAM, String.valueOf(DEFAULT_PARAM));

		state.setAttribute(STATE_MORE_SELECTED, new Boolean(false));
		state.setAttribute(STATE_FILTER_TYPE, filter_type);
		if (filter_type.equals(FILTER_ALL))
		{
			state.setAttribute(STATE_FEWER_MESSAGES_FILTER, new SelectMessagesByTime(DEFAULT_DAYS));
			state.setAttribute(STATE_FEWER_MESSAGES_LABEL, rb.getString("showpast") + " " + DEFAULT_DAYS + " "
					+ rb.getString("days"));
			state.setAttribute(STATE_MESSAGE_FILTER, state.getAttribute(STATE_MORE_MESSAGES_FILTER));
			state.setAttribute(STATE_MORE_SELECTED, new Boolean(true));
		}
		else if (filter_type.equals(FILTER_TODAY))
		{
			state.setAttribute(STATE_FEWER_MESSAGES_FILTER, new SelectTodaysMessages());
			state.setAttribute(STATE_FEWER_MESSAGES_LABEL, rb.getString("showtoday"));
			state.setAttribute(STATE_MESSAGE_FILTER, state.getAttribute(STATE_FEWER_MESSAGES_FILTER));
		}
		else if (filter_type.equals(FILTER_BY_NUMBER))
		{
			int number = DEFAULT_MSGS;
			try
			{
				number = Integer.parseInt(filter_param);
				if (number <= 0)
				{
					throw new Exception();
				}
				state.setAttribute(STATE_FEWER_MESSAGES_FILTER, new SelectMessagesByNumber(number));
				state.setAttribute(STATE_FILTER_PARAM, filter_param);
			}
			catch (Exception e)
			{
				if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "updateMessageFilters()" + e);
			}
			state.setAttribute(STATE_FILTER_PARAM, String.valueOf(number));
			state.setAttribute(STATE_FEWER_MESSAGES_FILTER, new SelectMessagesByNumber(number));
			state
					.setAttribute(STATE_FEWER_MESSAGES_LABEL, rb.getString("showlast") + " " + number + " "
							+ rb.getString("messages"));
			state.setAttribute(STATE_MESSAGE_FILTER, state.getAttribute(STATE_FEWER_MESSAGES_FILTER));
		}
		else if (filter_type.equals(FILTER_BY_TIME))
		{
			int number = DEFAULT_DAYS;
			try
			{
				number = Integer.parseInt(filter_param);
				if (number <= 0)
				{
					throw new Exception();
				}
			}
			catch (Exception e)
			{
				if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "updateMessageFilters()" + e);
			}
			state.setAttribute(STATE_FILTER_PARAM, String.valueOf(number));
			state.setAttribute(STATE_FEWER_MESSAGES_FILTER, new SelectMessagesByTime(number));
			state.setAttribute(STATE_FEWER_MESSAGES_LABEL, rb.getString("showpast") + " " + number + " " + rb.getString("days"));
			state.setAttribute(STATE_MESSAGE_FILTER, state.getAttribute(STATE_FEWER_MESSAGES_FILTER));
		}
		else
		{
			state.setAttribute(STATE_FILTER_PARAM, String.valueOf(DEFAULT_DAYS));
			state.setAttribute(STATE_FEWER_MESSAGES_FILTER, new SelectMessagesByTime(DEFAULT_DAYS));
			state.setAttribute(STATE_FEWER_MESSAGES_LABEL, rb.getString("showpast") + " " + DEFAULT_DAYS + " "
					+ rb.getString("days"));
			state.setAttribute(STATE_MESSAGE_FILTER, state.getAttribute(STATE_FEWER_MESSAGES_FILTER));
		}

	} // updateMessageFilters

	/**
	 * build the context for the Main (Layout) panel
	 * 
	 * @return (optional) template name for this panel
	 */
	public String buildMainPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// The WIRIS javascript is messing the javascript that appends messages to the list in response to courier updates - so we can just leave that out for chat -ggolden
		String headInclude = (String) rundata.getRequest().getAttribute("sakai.html.head.no.wiris");
		if (headInclude != null)
		{
			setVmReference("sakai_head", headInclude, rundata.getRequest());
		}

		context.put("tlang", rb);
		// if there's an alert message specifically targetted for main fraim, display it
		String msg = (String) state.getAttribute(STATE_MAIN_MESSAGE);
		context.put("alertMessage", msg);
		state.removeAttribute(STATE_MAIN_MESSAGE);

		List focus_elements = new Vector();
		focus_elements.add(CONTROL_PANEL);
		focus_elements.add(FORM_MESSAGE);

		context.put("focus_path", focus_elements);

		context.put("panel-control", CONTROL_PANEL);
		context.put("panel-monitor", MONITOR_PANEL);
		context.put("panel-presence", PRESENCE_PANEL);
		context.put("panel-toolbar", TOOLBAR_PANEL);
		context.put("panel-rooms", ROOMS_PANEL);

		String placementContext = ToolManager.getCurrentPlacement().getContext();
		/*String defaultChannel = ChatService.channelReference(placementContext, SiteService.MAIN_CONTAINER);
		String sitePrefix = defaultChannel.substring(0, defaultChannel.lastIndexOf(SiteService.MAIN_CONTAINER));*/
		String currentChannel = ChatService.extractChannelId((String) state.getAttribute(STATE_CHANNEL_REF));
		context.put("current_chat_channel", currentChannel);
		ChatChannel channel = null;
		try
		{
			channel = ChatService.getChatChannel((String) state.getAttribute(STATE_CHANNEL_REF));
			context.put("channel", channel);
		}
		catch (IdUnusedException ignore)
		{
			if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "buildMainPanelContext()" + ignore);
		}
		catch (PermissionException ignore)
		{
			if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "buildMainPanelContext()" + ignore);
		}
		context.put("groupAccess", MessageChannelHeader.MessageAccess.GROUPED);

		// the url for the chat courier, using a quick 10 second refresh
		setVmCourier(rundata.getRequest(), 10);

		String mode = (String) state.getAttribute(STATE_MODE);
		if (MODE_OPTIONS.equals(mode))
		{
			return buildOptionsPanelContext(portlet, context, rundata, state);
		}
		else if (MODE_MANAGE.equals(mode))
		{
			return buildManagePanelContext(portlet, context, rundata, state);
		}
		else if (MODE_CONFIRM_DELETE_MESSAGE.equals(mode))
		{
			return buildConfirmDeleteMessagePanelContext(portlet, context, rundata, state);
		}
		else if (MODE_ADD.equals(mode))
		{
			return buildAddPanelContext(portlet, context, rundata, state);
		}
		else if (MODE_DELETECHANNEL.equals(mode))
		{
			return buildDeleteChannelPanelContext(portlet, context, rundata, state);
		}
		else if (MODE_CONFIRMPUBLISH.equals(mode))
		{
			return buildPublishChannelPanelContext(portlet, context, rundata, state);
		}
		else if (NONE_SELECTED.equals(mode))
		{
			addAlert(state, rb.getString("needselect"));
			return buildManagePanelContext(portlet, context, rundata, state);
		}
		else
		{
			state.setAttribute(STATE_MODE, MODE_LAYOUT);
		}
		
		return (String) getContext(rundata).get("template") + "-Layout";

	} // buildLayoutPanelContext

	/**
	 * Handle a user clicking on the view-date menu.
	 */
	public void doToggle_date_display(RunData runData, Context context)
	{
		toggleState(runData, STATE_DISPLAY_DATE);

		// schedule a refresh of the monitor panel
		String peid = ((JetspeedRunData) runData).getJs_peid();
		schedulePeerFrameRefresh(mainPanelUpdateId(peid) + "." + MONITOR_PANEL);

		// schedule a return of focus to Control panel (from the parent's perspective)
		String[] focusPath = { CONTROL_PANEL, FORM_MESSAGE };
		scheduleFocusRefresh(focusPath);

	} // doToggle_date_display

	/**
	 * Handle a user clicking on the view-time menu.
	 */
	public void doToggle_time_display(RunData runData, Context context)
	{
		toggleState(runData, STATE_DISPLAY_TIME);

		// schedule a refresh of the monitor panel
		String peid = ((JetspeedRunData) runData).getJs_peid();
		schedulePeerFrameRefresh(mainPanelUpdateId(peid) + "." + MONITOR_PANEL);

		// schedule a return of focus to Control panel (from the parent's perspective)
		String[] focusPath = { CONTROL_PANEL, FORM_MESSAGE };
		scheduleFocusRefresh(focusPath);

	} // doToggle_time_display

	/**
	 * Handle a user clicking on the time-date dropdown.
	 */
	public void doChange_time_date_display(RunData runData, Context context)
	{
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		String time_date = runData.getParameters().getString("changeView");

		boolean oldTime = ((Boolean) state.getAttribute(STATE_DISPLAY_TIME)).booleanValue();
		boolean oldDate = ((Boolean) state.getAttribute(STATE_DISPLAY_DATE)).booleanValue();

		if (time_date.equals(rb.getString("bar.onlytime")))
		{
			// if the time is not shown, toggle to show it
			if (!oldTime)
			{
				toggleState(runData, STATE_DISPLAY_TIME);
			}
			// if the date is being shown, toggle to hide it
			if (oldDate)
			{
				toggleState(runData, STATE_DISPLAY_DATE);
			}
		}
		else if (time_date.equals(rb.getString("bar.datetime")))
		{
			// if the time is not shown, toggle to show it
			if (!oldTime)
			{
				toggleState(runData, STATE_DISPLAY_TIME);
			}
			// if the date is not shown, toggle to show it
			if (!oldDate)
			{
				toggleState(runData, STATE_DISPLAY_DATE);
			}
		}
		else if (time_date.equals(rb.getString("bar.onlydate")))
		{
			// if the time is being shown, toggle to hide it
			if (oldTime)
			{
				toggleState(runData, STATE_DISPLAY_TIME);
			}
			// if the date is not shown, toggle to show it
			if (!oldDate)
			{
				toggleState(runData, STATE_DISPLAY_DATE);
			}
		}
		else if (time_date.equals(rb.getString("bar.nodatetime")))
		{
			// if the time is being shown, toggle to hide it
			if (oldTime)
			{
				toggleState(runData, STATE_DISPLAY_TIME);
			}
			// if the date is being shown, toggle to hide it
			if (oldDate)
			{
				toggleState(runData, STATE_DISPLAY_DATE);
			}
		}

		state.setAttribute(TIME_DATE_SELECT, time_date);

		schedulePeerFrameRefresh(mainPanelUpdateId(peid) + "." + MONITOR_PANEL);

		// schedule a return of focus to Control panel (from the parent's perspective)
		String[] focusPath = { CONTROL_PANEL, FORM_MESSAGE };
		scheduleFocusRefresh(focusPath);

	} // doChange_time_date_display

	/**
	 * Handle a user clicking on the sound-alert button.
	 */
	public void doToggle_sound_alert(RunData runData, Context context)
	{
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// toggle the state setting
		boolean newValue = !((Boolean) state.getAttribute(STATE_SOUND_ALERT)).booleanValue();
		state.setAttribute(STATE_SOUND_ALERT, new Boolean(newValue));
		ChatObservingCourier observer = (ChatObservingCourier) state.getAttribute(STATE_OBSERVER);
		observer.alertEnabled(newValue);

		// schedule a return of focus to Control panel (from the parent's perspective)
		String[] focusPath = { CONTROL_PANEL, FORM_MESSAGE };
		scheduleFocusRefresh(focusPath);

	} // doToggle_sound_alert

	/**
	 * Toggle the state attribute
	 * 
	 * @param stateName
	 *        The name of the state attribute to toggle
	 */
	private void toggleState(RunData runData, String stateName)
	{
		// access the portlet element id to find our state
		// %%% use CHEF api instead of Jetspeed to get state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// toggle the state setting
		boolean newValue = !((Boolean) state.getAttribute(stateName)).booleanValue();
		state.setAttribute(stateName, new Boolean(newValue));

	} // toggleState

	/**
	 * Handle a user clicking on the show-all/show-some button.
	 */
	public void doToggle_filter(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		// %%% use CHEF api instead of Jetspeed to get state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// toggle the filter setting between show-more and show-fewer
		if (((Boolean) state.getAttribute(STATE_MORE_SELECTED)).booleanValue())
		{
			state.setAttribute(STATE_MESSAGE_FILTER, state.getAttribute(STATE_FEWER_MESSAGES_FILTER));
		}
		else
		{
			state.setAttribute(STATE_MESSAGE_FILTER, state.getAttribute(STATE_MORE_MESSAGES_FILTER));
		}
		toggleState(runData, STATE_MORE_SELECTED);

		if (((String) state.getAttribute(STATE_FILTER_TYPE)).equals(FILTER_BY_NUMBER))
		{
			if (!((Boolean) state.getAttribute(STATE_MORE_SELECTED)).booleanValue())
			{
				try
				{
					int number = Integer.parseInt((String) state.getAttribute(STATE_FILTER_PARAM));
					state.setAttribute(STATE_FEWER_MESSAGES_FILTER, new SelectMessagesByNumber(number));
					state.setAttribute(STATE_MESSAGE_FILTER, state.getAttribute(STATE_FEWER_MESSAGES_FILTER));
				}
				catch (NumberFormatException e)
				{
				}
			}
		}

		// schedule a refresh of the monitor panel
		schedulePeerFrameRefresh(mainPanelUpdateId(peid) + "." + MONITOR_PANEL);

		// schedule a return of focus to Control panel (from the parent's perspective)
		String[] focusPath = { CONTROL_PANEL, FORM_MESSAGE };
		scheduleFocusRefresh(focusPath);

	} // doToggle_filter

	/**
	 * build the context for the Toolbar panel
	 * 
	 * @return (optional) template name for this panel
	 */
	public String buildToolbarPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// we might be on the way to a permissions...
		if (state.getAttribute(STATE_PERMISSIONS) != null)
		{
			state.removeAttribute(STATE_PERMISSIONS);
			doPermissionsNow(rundata, context);
		}

		context.put("tlang", rb);
		if (MODE_LAYOUT.equals(state.getAttribute(STATE_MODE))) context.put("listview", "true");
		
		// build the menu
		Menu bar = new MenuImpl(portlet, rundata, (String) state.getAttribute(STATE_ACTION));
		/*
		 * boolean displayDate = ((Boolean)state.getAttribute(STATE_DISPLAY_DATE)).booleanValue(); bar.add( new MenuEntry((displayDate ? rb.getString("hided") + " " : " " + rb.getString("showd")), null, true, (displayDate ? MenuItem.CHECKED_TRUE :
		 * MenuItem.CHECKED_FALSE), "doToggle_date_display") ); boolean displayTime = ((Boolean)state.getAttribute(STATE_DISPLAY_TIME)).booleanValue(); bar.add( new MenuEntry((displayTime ? rb.getString("hidet") + " " : " " + rb.getString("showt")), null,
		 * true, (displayTime ? MenuItem.CHECKED_TRUE : MenuItem.CHECKED_FALSE), "doToggle_time_display") );
		 */
		context.put("selectedView", state.getAttribute(TIME_DATE_SELECT));

		// if the java beep is disabled, don't offer the alert
		if (ServerConfigurationService.getBoolean("java.beep", false))
		{
			boolean soundAlert = ((Boolean) state.getAttribute(STATE_SOUND_ALERT)).booleanValue();
			bar.add(new MenuEntry((soundAlert ? rb.getString("turnoff") + " " : " " + rb.getString("turnon")), null, true,
					(soundAlert ? MenuItem.CHECKED_TRUE : MenuItem.CHECKED_FALSE), "doToggle_sound_alert"));
		}

		boolean moreSelected = ((Boolean) state.getAttribute(STATE_MORE_SELECTED)).booleanValue();
		/*
		 * bar.add( new MenuEntry((moreSelected ? (String)state.getAttribute(STATE_FEWER_MESSAGES_LABEL) : (String)state.getAttribute(STATE_MORE_MESSAGES_LABEL)), null, true, (moreSelected ? MenuItem.CHECKED_TRUE : MenuItem.CHECKED_FALSE),
		 * "doToggle_filter") );
		 */
		String pastLabel = "";
		String fewerLabel = (String) state.getAttribute(STATE_FEWER_MESSAGES_LABEL);
		String moreLabel = (String) state.getAttribute(STATE_MORE_MESSAGES_LABEL);
		context.put("pastLabel", (moreSelected ? moreLabel : fewerLabel));
		context.put("fewerLabel", fewerLabel);
		context.put("moreLabel", moreLabel);

		// add options if allowed
		//addOptionsMenu(bar, (JetspeedRunData) rundata);
		if (allowedToOptions())
		{
			bar.add(new MenuEntry(rb.getString("home"), "/sakai-chat-tool/images/home.png", true, MenuItem.CHECKED_NA,  "doHome"));
			bar.add(new MenuEntry(rb.getString("manage"), "/sakai-chat-tool/images/manage.png", true, MenuItem.CHECKED_NA,  "doManage"));
			bar.add(new MenuEntry(rb.getString("options"), "/sakai-chat-tool/images/user1_preferences.gif", true, MenuItem.CHECKED_NA,  "doOptions"));
		} 
		// add permissions, if allowed
		if (SiteService.allowUpdateSite(ToolManager.getCurrentPlacement().getContext()))
		{
			bar.add(new MenuEntry(rb.getString("permis"),"/sakai-chat-tool/images/permissions.png", true, MenuItem.CHECKED_NA, "doPermissions"));
		}

		context.put(Menu.CONTEXT_MENU, bar);
		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));

		return null; // (String)getContext(rundata).get("template") + "-Toolbar";

	} // buildToolbarPanelContext

	/**
	 * build the context for the List panel
	 * 
	 * @return (optional) template name for this panel
	 */
	public String buildListPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		// display info
		context.put("tlang", rb);
		context.put("display_date", state.getAttribute(STATE_DISPLAY_DATE));
		context.put("display_time", state.getAttribute(STATE_DISPLAY_TIME));
		context.put("display_user", state.getAttribute(STATE_DISPLAY_USER));
		context.put("sound_alert", state.getAttribute(STATE_SOUND_ALERT));

		// provide a color mapper to keep track of user color coding for the session
		context.put("color_mapper", (ColorMapper) state.getAttribute(STATE_COLOR_MAPPER));

		// find the channel and get the messages
		try
		{
			ChatFilter filter = (ChatFilter) state.getAttribute(STATE_MESSAGE_FILTER);

			// // TODO: TIMING
			// if (CurrentService.getInThread("DEBUG") == null)
			// CurrentService.setInThread("DEBUG", new StringBuffer());
			// long startTime = System.currentTimeMillis();

			List msgs = ChatService.getMessages((String) state.getAttribute(STATE_CHANNEL_REF), filter.getAfterDate(), filter
					.getLimitedToLatest(), true, // asc
					true, // TODO: inc drafts
					false // not pubview onyl
					);

			// // TODO: TIMING
			// long endTime = System.currentTimeMillis();
			// if (endTime-startTime > /*5*/000)
			// {
			// StringBuffer buf = (StringBuffer) CurrentService.getInThread("DEBUG");
			// if (buf != null)
			// {
			// buf.insert(0,"ChatAction.list: "
			// + state.getAttribute(STATE_CHANNEL_REF)
			// + " time: " + (endTime - startTime));
			// }
			// }

			context.put("chat_messages", msgs);

			// boolean allowed = ChatService.allowRemoveChannel((String)state.getAttribute(STATE_CHANNEL_REF));
			// context.put("allowRemoveMessage", new Boolean(allowed));

			ChatChannel channel = getChannel(state, (String) state.getAttribute(STATE_CHANNEL_REF));
			context.put("channel", channel);

		}
		catch (PermissionException e)
		{
			context.put("alertMessage", rb.getString("youdonot1"));
		}
		catch (Exception e)
		{
			if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "buildListPanelContext()" + e);
		}

		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		justDelivered(state);

		return null;

	} // buildListPanelContext

	/**
	 * build the context for the Control panel (has a send field)
	 * 
	 * @return (optional) template name for this panel
	 */
	public String buildControlPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		// put this pannel's name for the return url
		context.put("panel-control", CONTROL_PANEL);

		// set the action for form processing
		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));

		// set the form field name for the send button
		context.put("form-submit", BUTTON + "doSend");

		// set the form field name for the send button
		context.put("form-message", FORM_MESSAGE);

		// is this user going to be able to post (add permission on the channel)
		boolean allowed = ChatService.allowAddChannel((String) state.getAttribute(STATE_CHANNEL_REF));
		if (!allowed)
		{
			context.put("alertMessage", rb.getString("youdonot3")); // %%% or no message?
		}
		context.put("allow-send", new Boolean(allowed));

		return null;

	} // buildControlPanelContext
	
	/**
	 * Check if current user has the site.observer permission
	 * @return true if the user does, false if not.
	 */
	protected boolean checkSiteObserver()
	{
		Placement placement = ToolManager.getCurrentPlacement();
		String context = null;
		if (placement != null) context = placement.getContext();

		// TODO: stolen from site -ggolden
		if (SecurityService.unlock("site.observer", "/site/" + context))
		{
			return true;
		}

		return false;
	}	

	/**
	 * build the context for the rooms
	 * 
	 * @return (optional) template name for this panel
	 */
	public String buildRoomsPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState sstate)
	{
		context.put("tlang", rb);
		// put this pannel's name for the return url
		context.put("panel-rooms", ROOMS_PANEL);

		// set the action for form processing
		context.put(Menu.CONTEXT_ACTION, sstate.getAttribute(STATE_ACTION));

		context.put("groupAccess", MessageChannelHeader.MessageAccess.GROUPED);

		// provide "default_chat_channel" with the dafault channel-id for the user/group
		// context.put("default_chat_channel", SiteService.MAIN_CONTAINER);

		// provide "chat_channel" with the current channel's id
		String placementContext = ToolManager.getCurrentPlacement().getContext();
		/*
		 * String defaultChannel = ChatService.channelReference(placementContext, SiteService.MAIN_CONTAINER); String sitePrefix = defaultChannel.substring(0, defaultChannel.lastIndexOf(SiteService.MAIN_CONTAINER));
		 */
		String currentChannel = ChatService.extractChannelId((String) sstate.getAttribute(STATE_CHANNEL_REF));
		context.put("room_chat_channel", currentChannel);

		// provide "chat_channels" as a list of channels belonging to this site

		// // TODO: TIMING
		// if (CurrentService.getInThread("DEBUG") == null)
		// CurrentService.setInThread("DEBUG", new StringBuffer());
		// long startTime = System.currentTimeMillis();

		/*
		 * Iterator aChannel = ChatService.getChannelIds(placementContext).iterator();
		 * 
		 * List channel_list = new Vector(); while (aChannel.hasNext()) { String theChannel = (String) aChannel.next(); if (!theChannel.equals(SiteService.MAIN_CONTAINER) && !theChannel.equals(currentChannel)) { channel_list.add(theChannel); } }
		 */

		List channel_list = ChatService.getChannels("/chat/channel/" + ToolManager.getCurrentPlacement().getContext());
		List<ChannelCountObj> channel_count_list = new Vector();

		Iterator itr = channel_list.iterator();
		List site_channel_list = new Vector();

		while (itr.hasNext())
		{
			MessageChannel channel = (MessageChannel) itr.next();

			try
			{
				if (!allowedToOptions() && channel.getProperties().getProperty("published") != null && channel.getProperties().getProperty("published").equals("0"))
				{

				}
				else
				{
					if (channel.getHeader() == null || channel.getHeader().getAccess() == MessageChannelHeader.MessageAccess.CHANNEL)
					{
						site_channel_list.add(channel);
					}
					else
					{
						if (channel.getHeader().getAccess() == MessageChannelHeader.MessageAccess.GROUPED)
						{
							if (!checkSiteObserver())
							{
								Collection allowedGroups = null;
								// get allowed group refs of the channel
								Collection chGroups = channel.getHeader().getGroups();

								// get user's allowed groups
								if (allowedGroups == null)
								{
									// BaseAnnouncementChannelEdit ba = new BaseAnnouncementChannelEdit(channel);
									// allowedGroups = channel.getGroupsAllowGetMessage();
									allowedGroups = channel.getGroupsAllowGetRooms();
								}

								boolean channelAdded = false;
								// if user's group is allowed then add it
								for (Iterator iRefs = chGroups.iterator(); iRefs.hasNext();)
								{
									String findThisGroupRef = (String) iRefs.next();
									for (Iterator iGroups = allowedGroups.iterator(); iGroups.hasNext();)
									{
										String thisGroupRef = ((Group) iGroups.next()).getReference();
										if (thisGroupRef.equals(findThisGroupRef))
										{
											site_channel_list.add(channel);
											channelAdded = true;
											break;
										}
									}
									if (channelAdded) break;
								}
							}
							else
							{
								site_channel_list.add(channel);
							}
						}
					}

				}
			}
			catch (Exception e)
			{
				if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "buildRoomsPanelContext()" + e);
			}

		}
		ChatActionState state = (ChatActionState) getState(context, rundata, ChatActionState.class);

		/*String sort = (String) state.getCurrentSortedBy();
		boolean asc = false;

		if (sort != null && sort.equals(SORT_POSITION))
		{
			asc = true;
		}
		if (sort != null && sort.equals(SORT_ID))
		{
			asc = state.getCurrentSortAsc();
		}
		if (sort == null || sort.trim().length() == 0 || sort.equals(SORT_GROUPTITLE) || sort.equals(SORT_GROUPDESCRIPTION))
		{*/
			String sort = SORT_POSITION;
			boolean asc = true;
		//}

		Collections.sort(site_channel_list, new ChatComparator(sort, asc));

		itr = site_channel_list.iterator();
		while (itr.hasNext())
		{
			MessageChannel channel = (MessageChannel) itr.next();
			channel_count_list.add(new ChannelCountObj(channel, PresenceService.getPresentUsers(
					ToolManager.getCurrentPlacement().getId() + "|" + channel.getId()).size()));
		}

		if (channel_count_list != null && channel_count_list.size() > 0) context.put("channel_counts", channel_count_list.iterator());

		// provide "new_chat_channel" as flag to create a new channel
		/*
		 * context.put("new_chat_channel", NEW_CHAT_CHANNEL);
		 * 
		 * // provide "form_new_channel" with form field name for specifying a new channel name context.put("form_new_channel", FORM_NEW_CHANNEL);
		 */

		// get the observer
		PresenceObservingCourier observer = (PresenceObservingCourier) sstate.getAttribute(STATE_CHAT_ROOMS_OBSERVER);

		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		observer.justDelivered();

		// provide "chat_channel_form" with the form name for the new channel selection field
		context.put("chat_channel_form", FORM_CHANNEL);
		return null;

	} // buildRoomsPanelContext
	
	/**
	 * build the context for the delete page
	 * 
	 * @return (optional) template name for this panel
	 */
	public String buildDeleteChannelPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		Vector v = (Vector) state.getAttribute(DELETE_CHANNELS);
		if (v == null) v = new Vector();
		
		context.put("delete_channels", v.iterator());

		//state.removeAttribute(DELETE_CHANNELS);
		String template = (String) getContext(rundata).get("template");
		return template + "-deletechannel";

	} // buildDeletePanelContext
	
	/**
	 * build the context for the publish confirm page
	 * 
	 * @return (optional) template name for this panel
	 */
	public String buildPublishChannelPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		String chan = (String) state.getAttribute(GROUP_CHANNEL);
		
		context.put("group_channel", chan);
		
		//state.removeAttribute(DELETE_CHANNELS);
		String template = (String) getContext(rundata).get("template");
		return template + "-publishchannel";

	} // buildPublishPanelContext
	

	/**
	 * build the context for the Presence panel (has a send field)
	 * 
	 * @return (optional) template name for this panel
	 */
	public String buildPresencePanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		String template = null;

		// get the observer
		PresenceObservingCourier observer = (PresenceObservingCourier) state.getAttribute(STATE_CHAT_PRESENCE_OBSERVER);

		String location = null;
		// put into context a list of sessions with chat presence

		String channelref = (String) state.getAttribute(STATE_CHANNEL_REF);
		
		if (!observer.getLocation().contains("|"))  location = observer.getLocation() + "|" + ChatService.extractChannelId(channelref);
		else location = observer.getLocation();

		// get the current presence list (User objects) for this page
		List users = PresenceService.getPresentUsers(location);
		context.put("users", users);

		// inform the observing courier that we just updated the page...
		// if there are pending requests to do so they can be cleared
		observer.justDelivered();

		return null;

	} // buildPresencePanelContext

	/**
	 * Handle a user posting a new chat message.
	 */
	public void doSend(RunData runData, Context context)
	{
		// access the portlet element id to find our state
		// %%% use CHEF api instead of Jetspeed to get state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// read in the message input
		// %%% JANDERSE - The user enters plaintext, but messages are now stored as formatted text;
		// therefore, the plaintext must be converted to formatted text when it is returned from the browser.
		String message = runData.getParameters().getCleanString(FORM_MESSAGE);
		message = FormattedText.convertPlaintextToFormattedText(message);

		// ignore empty messages
		if ((message == null) || (message.length() == 0)) return;

		// deal with the channel not yet existing
		// TODO: we don't really need to read the channel to post... -ggolden

		// // TODO: TIMING
		// if (CurrentService.getInThread("DEBUG") == null)
		// CurrentService.setInThread("DEBUG", new StringBuffer());
		// long startTime = System.currentTimeMillis();

		ChatChannel channel = getChannel(state, (String) state.getAttribute(STATE_CHANNEL_REF));

		// // TODO: TIMING
		// long endTime = System.currentTimeMillis();
		// if (endTime-startTime > /*5*/000)
		// {
		// StringBuffer buf = (StringBuffer) CurrentService.getInThread("DEBUG");
		// if (buf != null)
		// {
		// buf.insert(0,"ChatAction.doSend: "
		// + state.getAttribute(STATE_CHANNEL_REF)
		// + " time: " + (endTime - startTime));
		// }
		// }

		// post the message
		if (channel != null)
		{
			try
			{
				ChatMessageEdit edit = channel.addChatMessage();
				edit.setBody(message);
				channel.commitMessage(edit);
			}
			catch (PermissionException e)
			{
				if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "doSend()" + e);
				addAlert(state, rb.getString("youdonot3"));
			}
			catch (Exception e) // %%% why?
			{
				if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "doSend()" + e);
				addAlert(state, rb.getString("therewaspro"));
			}
		}
		else
		{
			addAlert(state, (String) state.getAttribute(STATE_CHANNEL_PROBLEM));
		}

	} // doSend

	/**
	 * Setup for the manage panel.
	 */
	public String buildManagePanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState sstate)
	{
		/*context.put("tlang", rb);
		
		// provide "default_chat_channel" with the dafault channel-id for the user/group
		context.put("default_chat_channel", SiteService.MAIN_CONTAINER);*/

		// provide "chat_channel" with the current channel's id
		/*String defaultChannel = ChatService.channelReference(placementContext, SiteService.MAIN_CONTAINER);
		String sitePrefix = defaultChannel.substring(0, defaultChannel.lastIndexOf(SiteService.MAIN_CONTAINER));*/
		
		String defaultChannel = ChatService.extractChannelId(getDefaultChannel());
		if (defaultChannel != null) context.put("default_chat_channel", defaultChannel);
		
		context.put("groupAccess", MessageChannelHeader.MessageAccess.GROUPED);

		ChatActionState state = (ChatActionState) getState(context, rundata, ChatActionState.class);

		if (state.getCurrentSortedBy().equalsIgnoreCase(SORT_POSITION))
		{
			sstate.setAttribute(STATE_CURRENT_SORTED_BY, SORT_POSITION);
			sstate.setAttribute(STATE_CURRENT_SORT_ASC, true);
			state.setCurrentSortedBy(SORT_POSITION);
			state.setCurrentSortAsc(true);
		}
		
		List site_channel_list = ChatService.getChannels("/chat/channel/"+ToolManager.getCurrentPlacement().getContext());
		
		String sort = (String) state.getCurrentSortedBy();
		boolean asc = false;
		
		if (sort != null && sort.equals(SORT_POSITION)) 
		{
			asc = true;
		}
		if (sort != null && sort.equals(SORT_ID))
		{
			asc = state.getCurrentSortAsc();
		}
		if (sort == null || sort.trim().length() == 0 || sort.equals(SORT_GROUPTITLE) || sort.equals(SORT_GROUPDESCRIPTION))
		{
			sort = SORT_POSITION;
			asc = true;
		}
		
		if (asc)
			context.put("currentSortAsc", "true");
		else
			context.put("currentSortAsc", "false");
		
		context.put("currentSortedBy", (String) state.getCurrentSortedBy());
		
		Collections.sort(site_channel_list,new ChatComparator(sort, asc));
		
		if (site_channel_list != null && site_channel_list.size() > 0) context.put("chat_channels", site_channel_list);
		context.put("numRooms", site_channel_list.size());

		// provide "new_chat_channel" as flag to create a new channel
		/*context.put("new_chat_channel", NEW_CHAT_CHANNEL);

		// provide "form_new_channel" with form field name for specifying a new channel name
		context.put("form_new_channel", FORM_NEW_CHANNEL);

		// provide "chat_channel_form" with the form name for the new channel selection field
		context.put("chat_channel_form", FORM_CHANNEL);

		// set the action for form processing
		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));
		context.put("form-submit", BUTTON + "doUpdate");*/
		context.put("form-cancel", BUTTON + "doCancel");
		
		// pick the "-customize" template based on the standard template name
		String template = (String) getContext(rundata).get("template");
		return template + "-manage";

	} // buildManagePanelContext
	
	/**
	 * Setup for the add panel.
	 */
	public String buildAddPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState sstate)
	{
		/* context.put("tlang", rb); */
		// provide "default_chat_channel" with the dafault channel-id for the user/group
		Collection<Group> origgroups, groups;
		ChatActionState state = (ChatActionState) getState(context, rundata, ChatActionState.class);

		// context.put("default_chat_channel", SiteService.MAIN_CONTAINER);
		context.put("form_new_channel", FORM_NEW_CHANNEL);
		context.put("channelAccess", MessageChannelHeader.MessageAccess.CHANNEL);
		context.put("groupAccess", MessageChannelHeader.MessageAccess.GROUPED);

		// provide "chat_channel" with the current channel's id
		/*
		 * String defaultChannel = ChatService.channelReference(placementContext, SiteService.MAIN_CONTAINER); String sitePrefix = defaultChannel.substring(0, defaultChannel.lastIndexOf(SiteService.MAIN_CONTAINER));
		 */
		String defaultChannel = ChatService.extractChannelId(getDefaultChannel());
		if (defaultChannel != null) context.put("chat_channel", defaultChannel);
		if (sstate.getAttribute(STATE_EDIT_CHANNEL) != null)
		{
			String editChannel = ChatService.extractChannelId((String) sstate.getAttribute(STATE_EDIT_CHANNEL));
			// Channel being edited
			if (editChannel != null)
			{
				if (sstate.getAttribute(FORM_NEW_CHANNEL) != null)
				{
					context.put("test_chat_channel", (String) sstate.getAttribute(FORM_NEW_CHANNEL));
					context.put("edit_chat_channel", editChannel);
				}
				else
				{
					context.put("test_chat_channel", editChannel);
					context.put("edit_chat_channel", editChannel);
				}
				context.put("editing", "true");
				ChatChannel channel = null;
				try
				{
					channel = ChatService.getChatChannel((String) sstate.getAttribute(STATE_EDIT_CHANNEL));
					context.put("channel", channel);
					context.put("allowAddChannelMessage", new Boolean(channel.allowAddChannelMessage()));
					if (channel.getHeader() == null)
					{
						context.put("announceTo", "site");
					}
					else
					{
						if (channel.getHeader().getAccess() == MessageChannelHeader.MessageAccess.GROUPED)
						{
							context.put("announceTo", "groups");
						}
						else
						{
							context.put("announceTo", "site");
						}
					}

				}
				catch (IdUnusedException ignore)
				{
					if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "buildAddPanelContext()" + ignore);
				}
				catch (PermissionException ignore)
				{
					if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "buildAddPanelContext()" + ignore);
				}
				// Below should not apply for chat rooms
				// group list which user can remove message from
				// TODO: this is almost right (see chef_announcements-revise.vm)... ideally, we would let the check groups that they can add to,
				// and uncheck groups they can remove from... only matters if the user does not have both add and remove -ggolden
				/*
				 * boolean own = edit == null ? true : edit.getHeader().getFrom().getId().equals(SessionManager.getCurrentSessionUserId()); Collection groups = channel.getGroupsAllowRemoveMessage(own); context.put("allowedRemoveGroups", groups);
				 */

				// group list which user can add message to
				origgroups = channel.getGroupsAllowAddMessage(ChatService.SECURE_UPDATE);

				groups = filterGroups(origgroups);

				// add to these any groups that the message already has
				if (channel != null)
				{
					if (channel.getChatHeader() != null)
					{
						Collection otherGroups = channel.getChatHeader().getGroupObjects();
						for (Iterator i = otherGroups.iterator(); i.hasNext();)
						{
							Group g = (Group) i.next();

							if (!groups.contains(g))
							{
								groups.add(g);
							}
						}
						context.put("announceToGroups", channel.getChatHeader().getGroups());
					}

				}

				Site site = null;
				try
				{
					site = SiteService.getSite(channel.getContext());
					context.put("site", site);
				}
				catch (Exception ignore)
				{
					if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "buildAddPanelContext()" + ignore);
				}

			}
			else
			{
				if (sstate.getAttribute(FORM_NEW_CHANNEL) != null)
				{
					context.put("test_chat_channel", (String) sstate.getAttribute(FORM_NEW_CHANNEL));
					context.put("edit_chat_channel", "");
				}
				else
				{
					context.put("test_chat_channel", "");
					context.put("edit_chat_channel", "");
				}
				context.put("editing", "");
				// default to make site selection
				context.put("announceTo", "site");
				ChatChannel channel = null;
				try
				{
					channel = ChatService.getChatChannel(getDefaultChannel());
				}
				catch (IdUnusedException ignore)
				{
					if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "buildAddPanelContext()" + ignore);
				}
				catch (PermissionException ignore)
				{
					if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "buildAddPanelContext()" + ignore);
				}

				// group list which user can add message to
				origgroups = channel.getGroupsAllowAddMessage(ChatService.SECURE_UPDATE);

				groups = filterGroups(origgroups);

			}
		}
		else
		{

			if (sstate.getAttribute(FORM_NEW_CHANNEL) != null)
			{
				context.put("test_chat_channel", (String) sstate.getAttribute(FORM_NEW_CHANNEL));
				context.put("edit_chat_channel", "");
			}
			else
			{
				context.put("test_chat_channel", "");
				context.put("edit_chat_channel", "");
			}
			context.put("editing", "");
			context.put("announceTo", "site");
			ChatChannel channel = null;
			try
			{
				channel = ChatService.getChatChannel(getDefaultChannel());
			}
			catch (IdUnusedException ignore)
			{
				if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "buildAddPanelContext()" + ignore);
				;
			}
			catch (PermissionException ignore)
			{
				if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "buildAddPanelContext()" + ignore);
			}
			// group list which user can add message to
			origgroups = channel.getGroupsAllowAddMessage(ChatService.SECURE_UPDATE);
			groups = filterGroups(origgroups);
		}
		if (groups.size() > 0)
		{
			String sort = (String) sstate.getAttribute(STATE_CURRENT_SORTED_BY);
			String prevSort = sort;
			boolean asc = sstate.getAttribute(STATE_CURRENT_SORT_ASC) != null ? ((Boolean) sstate.getAttribute(STATE_CURRENT_SORT_ASC))
					.booleanValue() : true;
			boolean prevAsc = asc;
			if (sort == null || (!sort.equals(SORT_GROUPTITLE) && !sort.equals(SORT_GROUPDESCRIPTION)))
			{
				sort = SORT_GROUPTITLE;
				sstate.setAttribute(STATE_CURRENT_SORTED_BY, sort);
				state.setCurrentSortedBy(sort);
				state.setCurrentSortAsc(Boolean.TRUE.booleanValue());
			}
			Collection sortedGroups = new Vector();
			for (Iterator i = new SortedIterator(groups.iterator(), new ChatComparator(sort, asc)); i.hasNext();)
			{
				sortedGroups.add(i.next());
			}
			context.put("groups", sortedGroups);
			sstate.setAttribute(STATE_CURRENT_SORTED_BY, prevSort);
			if (prevSort != null) state.setCurrentSortedBy(prevSort);
			state.setCurrentSortAsc(prevAsc);
		}
		sstate.removeAttribute(STATE_EDIT_CHANNEL);

		// provide "chat_channels" as a list of channels belonging to this site

		// // TODO: TIMING
		// if (CurrentService.getInThread("DEBUG") == null)
		// CurrentService.setInThread("DEBUG", new StringBuffer());
		// long startTime = System.currentTimeMillis();

		// provide "new_chat_channel" as flag to create a new channel
		context.put("new_chat_channel", NEW_CHAT_CHANNEL);

		// provide "form_new_channel" with form field name for specifying a new channel name
		context.put("form_new_channel", FORM_NEW_CHANNEL);

		// provide "chat_channel_form" with the form name for the new channel selection field
		context.put("chat_channel_form", FORM_CHANNEL);
		context.put("panel-toolbar", TOOLBAR_PANEL);

		// set the action for form processing
		context.put(Menu.CONTEXT_ACTION, sstate.getAttribute(STATE_ACTION));
		context.put("form-submit", BUTTON + "doReviseChat");
		context.put("form-cancel", BUTTON + "doCancelManage");

		// pick the "-customize" template based on the standard template name
		String template = (String) getContext(rundata).get("template");
		return template + "-revise";

	} // buildAddPanelContext	
	
	private Collection<Group> filterGroups(Collection<Group> origgroups)
	{
		/*Collection<Group> groups = new Vector();
		
		//Filtering groups out with this property the same way the group
		//listing does under Site Setup
		for (Group g : origgroups)
		{
			try
			{
				// let this throw exceptions if this property is not defined
				g.getProperties().getBooleanProperty(GROUP_PROP_WSETUP_CREATED);
				groups.add(g);
			}
			catch (EntityPropertyNotDefinedException e)
			{
				
			}
			catch (EntityPropertyTypeException e)
			{
				
			}
		}
		return groups;*/
		return origgroups;
	}

	/**
	 * Setup for the options panel.
	 */
	public String buildOptionsPanelContext(VelocityPortlet portlet, Context context, RunData rundata, SessionState state)
	{
		context.put("tlang", rb);
		// provide "filter_type" with the current default value for filtering messages
		context.put("filter_type", (String) state.getAttribute(STATE_FILTER_TYPE));

		// provide "filter_type_form" with form field name for selecting a message filter
		context.put("filter_type_form", FORM_FILTER_TYPE);

		// provide "filter_days_param" as current value or default value for number of days
		context.put("filter_days_param", (String) state.getAttribute(STATE_FILTER_PARAM));

		// provide "filter_days_param_form" with form field name for filter parameter (number of days/messages)
		context.put("filter_days_param_form", FORM_FILTER_PARAM_DAYS);

		// provide "filter_param" as current value or default value for number of days/messages
		context.put("filter_number_param", (String) state.getAttribute(STATE_FILTER_PARAM));

		// provide "filter_param_form" with form field name for filter parameter (number of days/messages)
		context.put("filter_number_param_form", FORM_FILTER_PARAM_NUMBER);

		// provide "default_chat_channel" with the dafault channel-id for the user/group
		/*context.put("default_chat_channel", SiteService.MAIN_CONTAINER);

		// provide "chat_channel" with the current channel's id
		String placementContext = ToolManager.getCurrentPlacement().getContext();
		String defaultChannel = ChatService.channelReference(placementContext, SiteService.MAIN_CONTAINER);
		String sitePrefix = defaultChannel.substring(0, defaultChannel.lastIndexOf(SiteService.MAIN_CONTAINER));
		String currentChannel = ((String) state.getAttribute(STATE_CHANNEL_REF)).substring(sitePrefix.length());
		context.put("chat_channel", currentChannel);

		*/

		// set the action for form processing
		context.put(Menu.CONTEXT_ACTION, state.getAttribute(STATE_ACTION));
		context.put("form-submit", BUTTON + "doUpdate");
		context.put("form-cancel", BUTTON + "doCancel");

		// pick the "-customize" template based on the standard template name
		String template = (String) getContext(rundata).get("template");
		return template + "-customize";

	} // buildOptionsPanelContext

	public String buildConfirmDeleteMessagePanelContext(VelocityPortlet portlet, Context context, RunData rundata,
			SessionState state)
	{
		context.put("tlang", rb);
		// Put the message object into the context (the message that is about to be deleted)
		try
		{
			String messageid = (String) state.getAttribute("messageid");
			ChatChannel channel = ChatService.getChatChannel((String) state.getAttribute(STATE_CHANNEL_REF));
			ChatMessage msg = channel.getChatMessage(messageid);
			context.put("message", msg);
		}
		catch (PermissionException e)
		{
			if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "buildConfirmDeleteMessagePanelContext()" + e);
			context.put("alertMessage", rb.getString("youdonot4"));
		}
		catch (IdUnusedException e)
		{
			if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "buildConfirmDeleteMessagePanelContext()" + e);
		}
		catch (Exception e)
		{
			if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "buildConfirmDeleteMessagePanelContext()" + e);
		}

		String template = (String) getContext(rundata).get("template");
		return template + "-delete";
	}

	/**
	 * Handle a user clicking the "Done" button in the Options panel
	 */
	public void doUpdate(RunData data, Context context)
	{
		// access the portlet element id to find our state
		// %%% use CHEF api instead of Jetspeed to get state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		String placementContext = ToolManager.getCurrentPlacement().getContext();
		/*String newChannel = data.getParameters().getString(FORM_CHANNEL);
		String currentChannel = ((String) state.getAttribute(STATE_CHANNEL_REF)).substring(placementContext.length() + 1);

		if (newChannel != null && newChannel.equals(NEW_CHAT_CHANNEL))
		{
			newChannel = data.getParameters().getString(FORM_NEW_CHANNEL);
			// make sure channel name is valid Resource ID (for items entered by user)
			if (!Validator.checkResourceId(newChannel))
			{
				// if name is not valid, save error message and return to Options panel
				addAlert(state, rb.getString("youent") + " \" " + newChannel + " \" " + rb.getString("forchat"));
				return;
			}
		}
		if (newChannel != null && !newChannel.equals(currentChannel))
		{
			state.setAttribute(STATE_CHANNEL_REF, ChatService.channelReference(placementContext, newChannel));
			if (Log.isDebugEnabled()) Log.debug("doUpdate(): newChannel: " + newChannel);
			// ChatChannel chan = getChannel(state, newChannel);
			updateObservationOfChannel(state, peid);

			// update the tool config
			Placement placement = ToolManager.getCurrentPlacement();
			placement.getPlacementConfig().setProperty(PARAM_CHANNEL, (String) state.getAttribute(STATE_CHANNEL_REF));
			placement.setTitle(rb.getString("chatroom") + " \" " + newChannel + " \" ");

			// deliver an update to the title panel (to show the new title)
			String titleId = titlePanelUpdateId(peid);
			schedulePeerFrameRefresh(titleId);
		}*/

		// filter
		String filter_type = data.getParameters().getString(FORM_FILTER_TYPE);
		if (filter_type != null)
		{
			if (filter_type.equals(FILTER_ALL))
			{
				if (!filter_type.equals((String) state.getAttribute(STATE_FILTER_TYPE)))
				{
					updateMessageFilters(state, filter_type, null);

					// update the tool config
					Placement placement = ToolManager.getCurrentPlacement();
					placement.getPlacementConfig().setProperty(PARAM_FILTER_TYPE, (String) state.getAttribute(STATE_FILTER_TYPE));
					placement.getPlacementConfig().setProperty(PARAM_FILTER_PARAM, (String) state.getAttribute(STATE_FILTER_PARAM));
				}
			}
			else if (filter_type.equals(FILTER_BY_TIME))
			{
				String filter_days_param = data.getParameters().getString(FORM_FILTER_PARAM_DAYS);
				if (filter_days_param != null)
				{
					if (!filter_type.equals((String) state.getAttribute(STATE_FILTER_TYPE))
							|| !filter_days_param.equals((String) state.getAttribute(STATE_FILTER_PARAM)))
					{
						updateMessageFilters(state, filter_type, filter_days_param);

						// update the tool config
						Placement placement = ToolManager.getCurrentPlacement();
						placement.getPlacementConfig().setProperty(PARAM_FILTER_TYPE,
								(String) state.getAttribute(STATE_FILTER_TYPE));
						placement.getPlacementConfig().setProperty(PARAM_FILTER_PARAM,
								(String) state.getAttribute(STATE_FILTER_PARAM));
					}
				}
			}
			else if (filter_type.equals(FILTER_BY_NUMBER))
			{
				String filter_number_param = data.getParameters().getString(FORM_FILTER_PARAM_NUMBER);
				if (filter_number_param != null)
				{
					if (!filter_type.equals((String) state.getAttribute(STATE_FILTER_TYPE))
							|| !filter_number_param.equals((String) state.getAttribute(STATE_FILTER_PARAM)))
					{
						updateMessageFilters(state, filter_type, filter_number_param);

						// update the tool config
						Placement placement = ToolManager.getCurrentPlacement();
						placement.getPlacementConfig().setProperty(PARAM_FILTER_TYPE,
								(String) state.getAttribute(STATE_FILTER_TYPE));
						placement.getPlacementConfig().setProperty(PARAM_FILTER_PARAM,
								(String) state.getAttribute(STATE_FILTER_PARAM));
					}
				}
			}
		}

		// we are done with customization... back to the main mode
		state.removeAttribute(STATE_MODE);

		// re-enable auto-updates when leaving options
		enableObservers(state);

		// commit the change
		saveOptions();

	} // doUpdate

	/**
	 * Handle a user clicking the "Add Chat" link on Manage
	 */
	public void doAddChat(RunData data, Context context)
	{
		String pid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(pid);

		// go into add mode
		state.setAttribute(STATE_MODE, MODE_ADD);
		state.removeAttribute(STATE_EDIT_CHANNEL);
		state.removeAttribute(FORM_NEW_CHANNEL);
		//state.removeAttribute(STATE_CHANNEL_REF);

		// disable auto-updates while editing
		//disableObservers(state);

	} // doAddChat
	
	/**
	 * Handle editing of chat rooms
	 */
	public void doEditChat(RunData data, Context context)
	{
		// access the portlet element id to find our state
		// %%% use CHEF api instead of Jetspeed to get state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState sstate = ((JetspeedRunData) data).getPortletSessionState(peid);
		ChatActionState state = (ChatActionState) getState(context, data, ChatActionState.class);

		String placementContext = ToolManager.getCurrentPlacement().getContext();

		String channelid = data.getParameters().getString(CHANNELID);
		//state.setAttribute(STATE_CHANNEL_REF, ChatService.channelReference(placementContext, channelid));
		String channelRef = ChatService.channelReference(placementContext, channelid);
		sstate.setAttribute(STATE_EDIT_CHANNEL, ChatService.channelReference(placementContext, channelid));
		sstate.removeAttribute(FORM_NEW_CHANNEL);
		// go into add mode
		sstate.setAttribute(STATE_MODE, MODE_ADD);

		// disable auto-updates while editing
		//disableObservers(sstate);
		
		// ChatChannel chan = getChannel(state, newChannel);
		/*updateObservationOfChannel(state, peid);

		// update the tool config
		Placement placement = ToolManager.getCurrentPlacement();
		placement.getPlacementConfig().setProperty(PARAM_CHANNEL, (String) state.getAttribute(STATE_CHANNEL_REF));
		
		// deliver an update to the title panel (to show the new title)
		String titleId = titlePanelUpdateId(peid);
		schedulePeerFrameRefresh(titleId);
		String pid = null;
		if (placement != null) pid = placement.getId();
		String mainPanelId = mainPanelUpdateId(pid);
		schedulePeerFrameRefresh(mainPanelId);
		
		//state.removeAttribute("channelid");
		state.removeAttribute(STATE_MODE);
		
		saveOptions();*/
	}	
	
	
	/**
	 * Action is to use when doDeleteChat requested, corresponding to chef_chat-manage menu's Delete link
	 */
	public void doDeleteChat(RunData rundata, Context context)
	{
		// retrieve the state from state object
		// ChatActionState state = (ChatActionState) getState(context, rundata, ChatActionState.class);
		List nonEmptyRooms = new ArrayList();

		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState state = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		// go into delete channel mode
		state.setAttribute(STATE_MODE, MODE_DELETECHANNEL);
		
		String defaultChannel = getDefaultChannel();
		
		// get the current tool placement
		Placement placement = ToolManager.getCurrentPlacement();

		// location is placement (to match all rooms)
		String location = placement.getId();
		// put into context a list of sessions with chat presence

		
		// then, read in the selected chat items
		String[] channelReferences = rundata.getParameters().getStrings("selectedMembers");
		if (channelReferences != null)
		{
			Vector v = new Vector();

			if (channelReferences.length > 0)
			{
				for (int i = 0; i < channelReferences.length; i++)
				{
					String channelRef = ChatService.channelReference(ToolManager.getCurrentPlacement().getContext(), channelReferences[i]);
					if (ChatService.allowRemoveChannel(channelRef))
					{
						v.addElement(channelReferences[i]);
						try
						{
							List msgs = ChatService.getMessages(channelRef, null, 0, true, // asc
									true, // TODO: inc drafts
									false // not pubview onyl
									);
							
							List users = PresenceService.getPresentUsers(location + "|" + ChatService.extractChannelId(channelRef));
							
							if ((msgs != null && msgs.size() > 0)||(users != null && users.size() > 0))
							{
								nonEmptyRooms.add(channelReferences[i]);
							}
						}
						catch (PermissionException e)
						{
							if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "doDeleteChat()" + e);
							context.put("alertMessage", rb.getString("youdonot1"));
						}
						catch (Exception e)
						{
							if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "doDeleteChat()" + e);
						}
					}
				}

				if (nonEmptyRooms != null && nonEmptyRooms.size() > 0 && nonEmptyRooms.size() == channelReferences.length)
				{
					addAlert(state, rb.getString("cannotdelete") + fetchCommaString(nonEmptyRooms));
					state.setAttribute(STATE_MODE, MODE_MANAGE);
				}
				else
				{
					if (channelReferences.length == 1 && defaultChannel.equals(ChatService.channelReference(ToolManager.getCurrentPlacement().getContext(), channelReferences[0])))
					{
						addAlert(state, rb.getString("cannotdeletedef"));
						state.setAttribute(STATE_MODE, MODE_MANAGE);
					}
					else
					{	
					if (!v.isEmpty()) state.setAttribute(DELETE_CHANNELS, v);
					}
				}

				// disable auto-updates while in view mode
				//disableObservers(state);
			}
			else
			{
				state.setAttribute(STATE_MODE, NONE_SELECTED);
			}
		}
		else
		{
			state.setAttribute(STATE_MODE, NONE_SELECTED);
		}
		/*
		 * else { state.setIsListVM(true); state.setStatus("noSelectedForDeletion");
		 * 
		 * // make sure auto-updates are enabled enableObservers(sstate); }
		 */

	} // doDeleteChat
	
	/**
	 * Delete chat room method
	 * @param data
	 * @param context
	 */
	public void doDeleteChannel(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		
		String defaultChannel = getDefaultChannel();
		// get the current tool placement
		Placement placement = ToolManager.getCurrentPlacement();

		// location is placement (to match all rooms)
		String location = placement.getId();
		
		Vector v = (Vector) state.getAttribute(DELETE_CHANNELS);
		List nonEmptyRooms = new ArrayList();

		boolean deleteDefault = false;
		if (v != null && v.size() > 0)
		{
			Iterator itr = v.iterator();
			while (itr.hasNext())
			{
				String channelId = (String) itr.next();
				String channelRef = ChatService.channelReference(ToolManager.getCurrentPlacement().getContext(), channelId);

				if (ChatService.allowRemoveChannel(channelRef))
				{
					try
					{
						List msgs = ChatService.getMessages(channelRef, null, 0, true, // asc
								true, // TODO: inc drafts
								false // not pubview onyl
								);
						
						List users = PresenceService.getPresentUsers(location+ "|" + ChatService.extractChannelId(channelRef));
						if (channelRef.equals(defaultChannel)) deleteDefault = true;
						if ((msgs == null || msgs.size() == 0)&&(users == null || users.size() == 0)&&!channelRef.equals(defaultChannel))
						{
							try
							{
								ChatService.removeChannel(ChatService.editChannel(channelRef));
							}
							catch (IdUnusedException e)
							{
								if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "doDeleteChannel()" + e);
							}
							catch (PermissionException e)
							{
								if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "doDeleteChannel()" + e);
							}
							catch (InUseException e)
							{
								if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "doDeleteChannel()" + e);
							}
						}
						else
						{
							if ((msgs != null && msgs.size() > 0)||(users != null && users.size() > 0)) nonEmptyRooms.add(channelId);
						}
					}
					catch (PermissionException e)
					{
						if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "doDeleteChannel()" + e);
						context.put("alertMessage", rb.getString("youdonot1"));
					}
					catch (Exception e)
					{
						if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "doDeleteChannel()" + e);
					}
				}

			}
		}

		if (nonEmptyRooms != null && nonEmptyRooms.size() > 0) addAlert(state, rb.getString("cannotdelete") + fetchCommaString(nonEmptyRooms));
		if (nonEmptyRooms != null && nonEmptyRooms.size() > 0 && v.size() > 0 && deleteDefault) addAlert(state, "\n");
		if (v.size() > 0 && deleteDefault) addAlert(state, rb.getString("cannotdeletedef"));
		
		state.setAttribute(STATE_MODE, MODE_MANAGE);
		state.removeAttribute(DELETE_CHANNELS);
		
		//disableObservers(state);
	}
	
	/**
	 * Publish chat room method (if a group chat is moved to the first position)
	 * @param data
	 * @param context
	 */
	public void doPublishChannel(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState sstate = ((JetspeedRunData) data).getPortletSessionState(peid);
		
		String newOrder = (String) sstate.getAttribute(PUBLISH_CHANNELS);
		ChatActionState state = (ChatActionState) getState(context, data, ChatActionState.class);
		String[] parts = newOrder.split("%");
		confirmModifyPositions(parts,state, sstate);
		
		sstate.setAttribute(STATE_MODE, MODE_MANAGE);
		sstate.removeAttribute(PUBLISH_CHANNELS);
		//disableObservers(sstate);
	}	
	
	/**
	 * Returns a comma delimited string using the list entries
	 * @param nonEmptyRooms List containing titles
	 * @return Comma delimited string of titles
	 */
	private String fetchCommaString(List nonEmptyRooms)
	{
		return delimitList(nonEmptyRooms,", ");
	}
	
	/**
	 * Returns a pow sign(caret) delimited string using the list entries
	 * @param nonEmptyRooms List containing titles
	 * @return Pow delimited string of titles
	 */
	private String fetchPowString(List nonEmptyRooms)
	{
		return delimitList(nonEmptyRooms,"%");
	}
	
	private String delimitList(List nonEmptyRooms, String delimiter)
	{
		if (nonEmptyRooms == null || nonEmptyRooms.size() == 0) return null;
		Object[] nonEmptyArray = (Object[]) nonEmptyRooms.toArray();
		StringBuffer commaStrBuf = new StringBuffer();
		for (int i=0; i<nonEmptyArray.length; i++)
		{
			commaStrBuf.append((String)nonEmptyArray[i]);
			commaStrBuf.append(delimiter);
		}
		//commaStrBuf.deleteCharAt(commaStrBuf.length()-1);
		int lastIndex = commaStrBuf.lastIndexOf(delimiter);
		commaStrBuf.delete(lastIndex, commaStrBuf.length());
		return commaStrBuf.toString();
	}
	
	/**
	 * Action is to use when doPublishChat requested, corresponding to chef_chat-manage menu's Publish link
	 */
	public void doPublishChat(RunData rundata, Context context)
	{
		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState state = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		// go into delete channel mode
		state.setAttribute(STATE_MODE, MODE_MANAGE);
		// then, read in the selected chat items
		String[] channelReferences = rundata.getParameters().getStrings("selectedMembers");
		boolean alertAdded = false;
		List groupEmptyRooms = new ArrayList();

		if (channelReferences != null)
		{
			if (channelReferences.length > 0)
			{
				for (int i = 0; i < channelReferences.length; i++)
				{
					try
					{
						MessageChannelEdit chanEdit = ChatService.editChannel(ChatService.channelReference(ToolManager.getCurrentPlacement()
								.getContext(), channelReferences[i]));
						boolean groupsEmpty = false;
						if (chanEdit.getHeader().getAccess() == MessageChannelHeader.MessageAccess.GROUPED)
						{
							// get allowed group refs of the channel
							Collection chGroups = chanEdit.getHeader().getGroups();

							int emptyGroups = 0;
							// if user's group is allowed then add it
							for (Iterator iRefs = chGroups.iterator(); iRefs.hasNext();)
							{
								String findThisGroupRef = (String) iRefs.next();
								try
								{
									AuthzGroup group = AuthzGroupService.getAuthzGroup(findThisGroupRef);
									if (group.getUsers().size() == 0) 
									{
										emptyGroups++;
									}
								}
								catch (GroupNotDefinedException e)
								{
									emptyGroups++;
									
								}
							}

							if (chGroups.size() == emptyGroups) groupsEmpty = true;
						}

						if (!groupsEmpty)
						{
							if (chanEdit.getPropertiesEdit().getProperty("published") != null)
								chanEdit.getPropertiesEdit().removeProperty("published");

							chanEdit.getPropertiesEdit().addProperty("published", "1");
						}
						ChatService.commitChannel(chanEdit);
						if (groupsEmpty)
						{
							groupEmptyRooms.add(channelReferences[i]);
						}
						if (groupsEmpty && !alertAdded)
						{
							alertAdded = true;
						}
					}
					catch (Exception e)
					{
						addAlert(state, rb.getString("publishfail"));
						if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "doPublishChat()" + e);
						return;
					}
				}
                if (alertAdded) addAlert(state, rb.getString("grouppublishfail") + fetchCommaString(groupEmptyRooms));
				
				// disable auto-updates while in view mode
				//disableObservers(state);
			}
			else
			{
				state.setAttribute(STATE_MODE, NONE_SELECTED);
			}
		}
		else
		{
			state.setAttribute(STATE_MODE, NONE_SELECTED);
		}
	} // doPublishChat	
	
	/**
	 * Action is to use when doUnpublishChat requested, corresponding to chef_chat-manage menu's Unpublish link
	 */
	public void doUnpublishChat(RunData rundata, Context context)
	{
		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState state = ((JetspeedRunData) rundata).getPortletSessionState(peid);

		// go into delete channel mode
		state.setAttribute(STATE_MODE, MODE_MANAGE);
		String defaultChannel = ChatService.extractChannelId(getDefaultChannel());
		
		// then, read in the selected chat items
		String[] channelReferences = rundata.getParameters().getStrings("selectedMembers");
		
		try
		{
			if (channelReferences != null)
			{
				if (channelReferences.length > 0)
				{
					if (channelReferences.length == 1)
					{
						MessageChannelEdit chanEdit = ChatService.editChannel(ChatService.channelReference(ToolManager.getCurrentPlacement()
								.getContext(), channelReferences[0]));
						if (chanEdit.getId().equals(defaultChannel))
							addAlert(state, rb.getString("cannotunpublishdefault"));
						else
						{
							if (chanEdit.getPropertiesEdit().getProperty("published") != null)
								chanEdit.getPropertiesEdit().removeProperty("published");
							chanEdit.getPropertiesEdit().addProperty("published", "0");
						}
						ChatService.commitChannel(chanEdit);
					}
					else
					{
						for (int i = 0; i < channelReferences.length; i++)
						{
							MessageChannelEdit chanEdit = ChatService.editChannel(ChatService.channelReference(ToolManager.getCurrentPlacement()
									.getContext(), channelReferences[i]));
							//Don't unpublish default channel
							if (chanEdit.getPropertiesEdit().getProperty("position") != null
									&& !chanEdit.getPropertiesEdit().getProperty("position").equals("1"))
							{
								if (chanEdit.getPropertiesEdit().getProperty("published") != null)
									chanEdit.getPropertiesEdit().removeProperty("published");

								chanEdit.getPropertiesEdit().addProperty("published", "0");
							}
							ChatService.commitChannel(chanEdit);
						}
					}
					// disable auto-updates while in view mode
					//disableObservers(state);
				}
				else
				{
					state.setAttribute(STATE_MODE, NONE_SELECTED);
				}
			}
			else
			{
				state.setAttribute(STATE_MODE, NONE_SELECTED);
			}
		}
		catch (Exception e)
		{
			addAlert(state, rb.getString("unpublishfail"));
			if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "doPublishChat()" + e);
			return;
		}

	} // doUnpublishChat	
	
	/**
	 * Returns a list of published channels for the current site.
	 * Channels which are published either do not have the published property set
	 * or have the published property set to 1
	 * @return List of published channels
	 */
	private List<String> checkPublishedChannels()
	{
		List channel_list = ChatService.getChannels("/chat/channel/"+ToolManager.getCurrentPlacement().getContext());
		List<String> site_channel_list = new Vector();
		Iterator itr = channel_list.iterator();
		
		while (itr.hasNext())
		{
			MessageChannel channel = (MessageChannel) itr.next();
			if ((channel.getProperties().getProperty("published") == null || channel.getProperties().getProperty("published").equals("1")))
			{
				site_channel_list.add(channel.getId());
			}
		}
		return site_channel_list;
	}
	
	/**
	 * Handle changing of rooms
	 */
	public void doChangeRoom(RunData data, Context context)
	{
		// access the portlet element id to find our state
		// %%% use CHEF api instead of Jetspeed to get state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		Placement placement = ToolManager.getCurrentPlacement();
		String placementContext = placement.getContext();

		String channelref = (String) state.getAttribute(STATE_CHANNEL_REF);
		
		String channelid = data.getParameters().getString(FORM_CHANNEL);
		state.setAttribute(STATE_CHANNEL_REF, ChatService.channelReference(placementContext, channelid));
	
		// ChatChannel chan = getChannel(state, newChannel);
		updateObservationOfChannel(state, peid);

		// update the tool config
		
		//placement.getPlacementConfig().setProperty(PARAM_CHANNEL, (String) state.getAttribute(STATE_CHANNEL_REF));
		
		// deliver an update to the title panel (to show the new title)
		String titleId = titlePanelUpdateId(peid);
		schedulePeerFrameRefresh(titleId);
		String pid = null;
		if (placement != null) pid = placement.getId();
		String mainPanelId = mainPanelUpdateId(pid);
		schedulePeerFrameRefresh(mainPanelId);
		
		//state.removeAttribute("channelid");
		state.removeAttribute(STATE_MODE);
		
		// location is just placement
		String location = placement.getId() + "|" + channelid;

		// the html element to update on delivery
		String elementId = PRESENCE_PANEL;

		// remove the current presence
		String oldLocation = placement.getId() + "|" + ChatService.extractChannelId(channelref);
		PresenceService.removePresence(oldLocation);

		// set the new presence
		PresenceService.setPresence(location);
		
		// setup an observer to notify us when presence at this location changes
		PresenceObservingCourier observer = new PresenceObservingCourier(location, elementId);
		
		state.setAttribute(STATE_CHAT_PRESENCE_OBSERVER, observer);		
	}

	/**
	 * Handle a user clicking the "Cancel" button
	 */
	public void doCancel(RunData data, Context context)
	{
		// access the portlet element id to find our state
		// %%% use CHEF api instead of Jetspeed to get state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// we are done with customization... back to the main mode
		state.removeAttribute(STATE_MODE);
		state.removeAttribute(DELETE_CHANNELS);
		state.removeAttribute(PUBLISH_CHANNELS);
		if (state.getAttribute(STATE_CHANNEL_REF) == null)
		{
			state.setAttribute(STATE_CHANNEL_REF, getDefaultChannel());
		}
		state.removeAttribute(FORM_NEW_CHANNEL);
		// re-enable auto-updates when leaving options
		enableObservers(state);
		// cancel the options
		cancelOptions();

	} // doCancel
	
	/**
	 * Handle a user clicking the "Cancel" button from the features off of the Manage screen
	 */
	public void doCancelManage(RunData data, Context context)
	{
		// access the portlet element id to find our state
		// %%% use CHEF api instead of Jetspeed to get state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// we are done with customization... back to the main mode
		state.setAttribute(STATE_MODE, MODE_MANAGE);
		state.removeAttribute(DELETE_CHANNELS);
		String channel = getDefaultChannel();
		if (state.getAttribute(STATE_CHANNEL_REF) == null)
		{
			state.setAttribute(STATE_CHANNEL_REF, getDefaultChannel());
		}
		// re-enable auto-updates when leaving options
		enableObservers(state);

		// cancel the options
		cancelOptions();

	} // doCancel

	/**
	 * Handle a user deleting a message - put up a confirmation page
	 */
	public void doConfirmDeleteMessage(RunData data, Context context)
	{
		// access the portlet element id to find our state
		// %%% use CHEF api instead of Jetspeed to get state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		String messageid = data.getParameters().getString("messageid");

		state.setAttribute("messageid", messageid);
		state.setAttribute(STATE_MODE, MODE_CONFIRM_DELETE_MESSAGE);

		// schedule a main refresh
		schedulePeerFrameRefresh(mainPanelUpdateId(peid));
	}

	/**
	 * Handle a user deleting a message - they've already confirmed the deletion, just delete it now
	 */
	public void doDeleteMessage(RunData data, Context context)
	{
		// access the portlet element id to find our state
		// %%% use CHEF api instead of Jetspeed to get state
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		// find the message and delete it now!
		try
		{
			String messageid = (String) state.getAttribute("messageid");
			ChatChannel channel = ChatService.getChatChannel((String) state.getAttribute(STATE_CHANNEL_REF));
			channel.removeMessage(messageid);
		}
		catch (PermissionException e)
		{
			context.put("alertMessage", rb.getString("youdonot4"));
		}
		catch (IdUnusedException e)
		{
		}
		catch (Exception e)
		{
			if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "doDeleteMessage()" + e);
		}

		state.removeAttribute("messageid");
		state.removeAttribute(STATE_MODE);
	}

	interface ChatFilter
	{
		Time getAfterDate();

		int getLimitedToLatest();
	}

	/** A filter */
	class SelectMessagesByTime implements ChatFilter
	{
		/** The number of days back to accept messages. */
		private int m_days = 0;

		/** The cutoff time - messages before this are rejected. */
		private Time m_cutoff = null;

		/**
		 * Constructor
		 * 
		 * @param days
		 *        The number of days back to accept messages.
		 */
		public SelectMessagesByTime(int days)
		{
			// Log.info("chef", this + ".SelectMessagesByTime(" + days + ")");
			m_days = days;

			// compute the cutoff - Note: use the filter fast - the clock is ticking.
			m_cutoff = TimeService.newTime(System.currentTimeMillis() - ((long) days * 24l * 60l * 60l * 1000l));

		} // SelectMessagesByTime

		public Time getAfterDate()
		{
			return m_cutoff;
		}

		public int getLimitedToLatest()
		{
			return 0;
		}

		public String toString()
		{
			return this.getClass().getName() + " " + Integer.toString(m_days);
		}
	}

	/** A filter */
	class SelectMessagesByNumber implements ChatFilter
	{
		/** The cutoff value - messages before this date/time are rejected. */
		private Time m_first;

		/** the number of messages to select */
		private int m_number;

		/**
		 * Constructor
		 * 
		 * @param number
		 *        The number of the messages to be returned
		 */
		public SelectMessagesByNumber(int number)
		{
			// Log.info("chef", this + ".SelectMessagesByNumber(" + number + ")");
			m_number = number;
		}

		public Time getAfterDate()
		{
			return null;
		}

		public int getLimitedToLatest()
		{
			return m_number;
		}

		public String toString()
		{
			return this.getClass().getName() + " " + m_number;

		} // toString

	} // SelectMessagesByNumber

	/** A filter */
	class SelectAllMessages implements ChatFilter
	{
		/** Constructor for the SelectAllMessages object */
		public SelectAllMessages()
		{
			// Log.info("chef", this + ".SelectAllMessages()");
		} // SelectAllMessages

		public Time getAfterDate()
		{
			return null;
		}

		public int getLimitedToLatest()
		{
			return 0;
		}

		public String toString()
		{
			return this.getClass().getName();

		} // toString

	} // SelectAllMessages

	/** A filter that gets all messages since midnight today, local time */
	class SelectTodaysMessages implements ChatFilter
	{
		/** The cutoff time - messages before this are rejected. */
		private Time m_cutoff = null;

		/** Constructor for the SelectTodaysMessages object */
		public SelectTodaysMessages()
		{
			super();
			// Log.info("chef", this + ".SelectTodaysMessages()");

			TimeBreakdown now = (TimeService.newTime(System.currentTimeMillis())).breakdownLocal();
			// compute the cutoff for midnight today.
			m_cutoff = TimeService.newTimeLocal(now.getYear(), now.getMonth(), now.getDay(), 0, 0, 0, 0);

		} // SelectTodaysMessages

		public Time getAfterDate()
		{
			return m_cutoff;
		}

		public int getLimitedToLatest()
		{
			return 0;
		}

		public String toString()
		{
			return this.getClass().getName();

		} // toString

	} // SelectTodaysMessages

	
	/**
	 * Handle a request to set manage.
	 */
	public void doManage(RunData runData, Context context)
	{
		// ignore if not allowed
		if (!allowedToOptions())
		{
			return;
			//msg = "you do not have permission to set options for this Worksite.";
		}

		Placement placement = ToolManager.getCurrentPlacement();
		String pid = null;
		if (placement != null) pid = placement.getId();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(pid);

		// go into options mode
		state.setAttribute(STATE_MODE, MODE_MANAGE);

		// disable auto-updates while editing
		//disableObservers(state);

		// if we're not in the main panel for this tool, schedule an update of the main panel
		String currentPanelId = runData.getParameters().getString(ActionURL.PARAM_PANEL);
		if (!LAYOUT_MAIN.equals(currentPanelId))
		{
			String mainPanelId = mainPanelUpdateId(pid);
			schedulePeerFrameRefresh(mainPanelId);
		}
		
		// remove the current presence
		String oldLocation = placement.getId() + "|" + ChatService.extractChannelId((String)state.getAttribute(STATE_CHANNEL_REF));
		PresenceService.removePresence(oldLocation);
		
		// if there's an alert message, divert it to the main frame
		String msg = (String) state.getAttribute(STATE_MESSAGE);
		state.setAttribute(STATE_MAIN_MESSAGE, msg);
		state.removeAttribute(VelocityPortletPaneledAction.STATE_MESSAGE);
		doSortbyPosition(runData,context);

	} // doManage
	
	/**
	 * Handle a request to set home.
	 */
	public void doHome(RunData runData, Context context)
	{
		// ignore if not allowed
		if (!allowedToOptions())
		{
			return;
			//msg = "you do not have permission to set options for this Worksite.";
		}

		Placement placement = ToolManager.getCurrentPlacement();
		String pid = null;
		if (placement != null) pid = placement.getId();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(pid);

		// go into options mode
		state.setAttribute(STATE_MODE, MODE_LAYOUT);

		enableObservers(state);

		// if we're not in the main panel for this tool, schedule an update of the main panel
		String currentPanelId = runData.getParameters().getString(ActionURL.PARAM_PANEL);
		if (!LAYOUT_MAIN.equals(currentPanelId))
		{
			String mainPanelId = mainPanelUpdateId(pid);
			schedulePeerFrameRefresh(mainPanelId);
		}
		
		// if there's an alert message, divert it to the main frame
		String msg = (String) state.getAttribute(STATE_MESSAGE);
		state.setAttribute(STATE_MAIN_MESSAGE, msg);
		state.removeAttribute(VelocityPortletPaneledAction.STATE_MESSAGE);
        
		if (state.getAttribute(STATE_CHANNEL_REF) == null)
		{
			state.setAttribute(STATE_CHANNEL_REF, getDefaultChannel());
		}
		

	} // doHome	
	
	/**
	 * Check for a valid channel id using the invalid character set.
	 * 
	 * @return true if valid, false if not
	 */
	public static boolean checkChannelId(String id, String invalidCharSet)
	{
		// the rules:
		// Null is rejected
		// all blank is rejected
		
		if (id == null) return false;
		if (id.trim().length() == 0) return false;

		// we must reject certain characters that we cannot even escape and get into Tomcat via a URL
		for (int i = 0; i < id.length(); i++)
		{
			if (invalidCharSet.indexOf(id.charAt(i)) != -1) return false;
		}

		return true;

	} // checkResourceId
	/**
	 * Handle a request to add or edit a chat room.
	 */
	public void doReviseChat(RunData runData, Context context)
	{
		List msgs = null;
		boolean checkPositions = false;
		String[] groupChoice = new String[25];

		// ignore if not allowed
		if (!allowedToOptions())
		{
			return;
			// msg = "you do not have permission to set options for this Worksite.";
		}

		// retrieve the state from state object
		ChatActionState state = (ChatActionState) getState(context, runData, ChatActionState.class);

		Placement placement = ToolManager.getCurrentPlacement();
		String pid = null;
		if (placement != null) pid = placement.getId();
		SessionState sstate = ((JetspeedRunData) runData).getPortletSessionState(pid);

		// disable auto-updates while editing
		//disableObservers(sstate);
		String newChannel = runData.getParameters().getString(FORM_NEW_CHANNEL);
		String origname = runData.getParameters().getString("origname");
		String announceTo = runData.getParameters().getString("announceTo");
		
		if (newChannel.trim().length() == 0)
		{
			if (origname != null && origname.trim().length() != 0) sstate.setAttribute(STATE_EDIT_CHANNEL, ChatService.channelReference(placement.getContext(),origname));
			sstate.setAttribute(FORM_NEW_CHANNEL, newChannel);
			addAlert(sstate, rb.getString("titleempty"));
			return;
		}
		if (newChannel.trim().length() > TITLE_SIZE)
		{
			if (origname != null && origname.trim().length() != 0) sstate.setAttribute(STATE_EDIT_CHANNEL, ChatService.channelReference(placement.getContext(),origname));
			sstate.setAttribute(FORM_NEW_CHANNEL, newChannel);
			addAlert(sstate, rb.getString("titletoolong"));
			return;
		}
		// make sure channel name is valid Resource ID (for items entered by user)
		if (!checkChannelId(newChannel, MessageService.INVALID_CHARS_IN_CHANNEL_ID))
		{
			if (origname != null && origname.trim().length() != 0) sstate.setAttribute(STATE_EDIT_CHANNEL, ChatService.channelReference(placement.getContext(),origname));
			// if name is not valid,display error msg and return
			sstate.setAttribute(FORM_NEW_CHANNEL, newChannel);
			addAlert(sstate, rb.getString("invalidchars"));
			return;
		}
		
		if (announceTo.equals("groups"))
		{
			groupChoice = runData.getParameters().getStrings("selectedGroups");
			
			if (groupChoice == null || groupChoice.length == 0)
			{
				if (origname != null && origname.trim().length() != 0) sstate.setAttribute(STATE_EDIT_CHANNEL, ChatService.channelReference(placement.getContext(),origname));
				sstate.setAttribute(FORM_NEW_CHANNEL, newChannel);
				addAlert(sstate, rb.getString("java.alert.youchoosegroup"));
				return;
			}
		}
		
		// ChatChannel chan;
		// if (origname != null || origname.trim().length() != 0) chan = editChannel(state, ChatService.channelReference(placement.getContext(), newChannel));

		List site_channel_list = ChatService.getChannels("/chat/channel/"+ToolManager.getCurrentPlacement().getContext());
		
		checkPositions = checkIfPositions(site_channel_list, state);
		try
		{
			//Edit chat room
			if (origname != null && origname.trim().length() != 0)
			{
				ChatChannelEdit chanEdit = null;
				
				if (!origname.trim().equals(newChannel.trim()))
				{
					ChatChannel existingChannel = null;
					//Check if another channel with this name exists
					try
					{
						existingChannel = ChatService.getChatChannel(ChatService.channelReference(placement.getContext(), newChannel));
					}
					catch (IdUnusedException ignore)
					{
					}
					catch (PermissionException ignore)
					{
					}
					if (existingChannel != null)
					{
						sstate.setAttribute(STATE_EDIT_CHANNEL, ChatService.channelReference(placement.getContext(),origname));
						addAlert(sstate, rb.getString("channelexists"));
						sstate.setAttribute(FORM_NEW_CHANNEL, newChannel);
						return;
					}
				}
				

				if (!origname.trim().equals(newChannel.trim()))
				{
					//Change the channel name for the chatroom
					chanEdit = ChatService.updateChatChannel(ChatService.channelReference(placement.getContext(), origname),
							ChatService.channelReference(placement.getContext(), newChannel));
				}
				else
				{
					//If something else is being changed about the chat room (other than title)
					if (chanEdit == null)
					{
						chanEdit = (ChatChannelEdit) ChatService.editChannel(ChatService.channelReference(placement.getContext(), origname));
					}	
				}
				// announce to site?
				if (announceTo.equals("site"))
				{
					chanEdit.getChatChannelHeaderEdit().clearGroupAccess();
				}
				else if (announceTo.equals("groups"))
				{
					// get the group ids selected
					Collection groupChoiceColl = new ArrayList(Arrays.asList(groupChoice));

					Site site = null;
					Collection groups = new Vector();

					// announce to?
					try
					{
						site = SiteService.getSite(chanEdit.getContext());

						// make a collection of Group objects
						for (Iterator iGroups = groupChoiceColl.iterator(); iGroups.hasNext();)
						{
							String groupId = (String) iGroups.next();
							groups.add(site.getGroup(groupId));
						}
					}
					/*catch (PermissionException e)
					{
						addAlert(sstate, rb.getString("java.alert.youpermi")// "You don't have permissions to create this announcement -"
						);
						return;
					}*/
					catch (Exception ignore)
					{
						// No site available.
					}
					// set access
					chanEdit.getChatChannelHeaderEdit().setGroupAccess(groups);
				}
				
				// If default chat room is the one being edited, set STATE_CHANNEL_REF and change
				// the default chat room name because the name 
				//of the chat room may have changed
				if ((getDefaultChannel()).equals(ChatService.channelReference(placement.getContext(), origname)))
				{
					if (((String)sstate.getAttribute(STATE_CHANNEL_REF)).equals(ChatService.channelReference(placement.getContext(), origname)))
					{
						sstate.removeAttribute(STATE_CHANNEL_REF);
					    sstate.setAttribute(STATE_CHANNEL_REF, ChatService.channelReference(placement.getContext(), newChannel));
					}
					sstate.removeAttribute(STATE_CHAT_PRESENCE_OBSERVER);
					// location is just placement
					String location = placement.getId() + "|" + newChannel;

					// the html element to update on delivery
					String elementId = PRESENCE_PANEL;

					// setup an observer to notify us when presence at this location changes
					PresenceObservingCourier observer = new PresenceObservingCourier(location, elementId);

					sstate.setAttribute(STATE_CHAT_PRESENCE_OBSERVER, observer);
					placement.getPlacementConfig().setProperty(PARAM_CHANNEL,
							ChatService.channelReference(ToolManager.getCurrentPlacement().getContext(), newChannel));
					chanEdit.getChatChannelHeaderEdit().clearGroupAccess();
					saveOptions();
				}
				// If the current chat room is the one being edited, set the attributes
				//and observers accordingly as the name may have changed
				if (((String)sstate.getAttribute(STATE_CHANNEL_REF)).equals(ChatService.channelReference(placement.getContext(), origname)))
				{
					sstate.removeAttribute(STATE_CHANNEL_REF);
					sstate.setAttribute(STATE_CHANNEL_REF, ChatService.channelReference(placement.getContext(), newChannel));
					
					sstate.removeAttribute(STATE_CHAT_PRESENCE_OBSERVER);
					// location is just placement
					String location = placement.getId() + "|" + newChannel;

					// the html element to update on delivery
					String elementId = PRESENCE_PANEL;

					// setup an observer to notify us when presence at this location changes
					PresenceObservingCourier observer = new PresenceObservingCourier(location, elementId);

					sstate.setAttribute(STATE_CHAT_PRESENCE_OBSERVER, observer);
				}
				ChatService.commitChannel(chanEdit);
				
			}
			else
			{
				//Add chat room
				ChatChannel existingChannel = null;
				//Check if chat room exists with this name already
				try
				{
					existingChannel = ChatService.getChatChannel(ChatService.channelReference(placement.getContext(), newChannel));
				}
				catch (IdUnusedException ignore)
				{
				}
				catch (PermissionException ignore)
				{
				}
				if (existingChannel != null)
				{
					addAlert(sstate, rb.getString("channelexists"));
					sstate.setAttribute(FORM_NEW_CHANNEL, newChannel);
					return;
				}
				
				//New chat room
				ChatChannel chan = getChannel(sstate, ChatService.channelReference(placement.getContext(), newChannel));

				ChatChannelEdit chanEdit = (ChatChannelEdit) ChatService
						.editChannel(ChatService.channelReference(placement.getContext(), newChannel));
				chanEdit.getPropertiesEdit().addProperty("published", "0");

				if (!checkPositions)
					chanEdit.getPropertiesEdit().addProperty("position", "1");
				else
					chanEdit.getPropertiesEdit().addProperty("position", Integer.toString(getMaxPosition(site_channel_list) + 1));

				// announce to site?
				if (announceTo.equals("site"))
				{
					chanEdit.getChatChannelHeaderEdit().clearGroupAccess();
				}
				else if (announceTo.equals("groups"))
				{
					// get the group ids selected
					Collection groupChoiceColl = new ArrayList(Arrays.asList(groupChoice));

					Site site = null;
					Collection groups = new Vector();

					try
					{
						site = SiteService.getSite(chanEdit.getContext());

						// make a collection of Group objects
						for (Iterator iGroups = groupChoiceColl.iterator(); iGroups.hasNext();)
						{
							String groupId = (String) iGroups.next();
							groups.add(site.getGroup(groupId));
						}
					}
					/*catch (PermissionException e)
					{
						addAlert(sstate, rb.getString("java.alert.youpermi")// "You don't have permissions to create this announcement -"
						);
						return;
					}*/
					catch (Exception ignore)
					{
						// No site available.
					}
					// set access
					chanEdit.getChatChannelHeaderEdit().setGroupAccess(groups);
				}
				ChatService.commitChannel(chanEdit);

			}
		}
		catch (IdUsedException ignore)
		{
			ignore.printStackTrace();
		}
		catch (IdUnusedException ignore)
		{
			ignore.printStackTrace();
		}
		catch (PermissionException ignore)
		{
			ignore.printStackTrace();
		}
		catch (InUseException ignore)
		{
			ignore.printStackTrace();
		}
		catch (IdInvalidException ignore)
		{
			ignore.printStackTrace();
		}

		String peid = ((JetspeedRunData) runData).getJs_peid();
		updateObservationOfChannel(sstate, peid);

		// if we're not in the main panel for this tool, schedule an update of the main panel
		String currentPanelId = runData.getParameters().getString(ActionURL.PARAM_PANEL);
		if (!LAYOUT_MAIN.equals(currentPanelId))
		{
			String mainPanelId = mainPanelUpdateId(pid);
			schedulePeerFrameRefresh(mainPanelId);
		}

		if (checkPositions)
		{
			state.setCurrentSortedBy(SORT_POSITION);
			state.setCurrentSortAsc(Boolean.TRUE.booleanValue());
			sstate.setAttribute(STATE_CURRENT_SORTED_BY, SORT_POSITION);
			sstate.setAttribute(STATE_CURRENT_SORT_ASC, Boolean.TRUE);
		}
		else
		{
			state.setCurrentSortedBy(SORT_ID);
			state.setCurrentSortAsc(Boolean.FALSE.booleanValue());
			sstate.setAttribute(STATE_CURRENT_SORTED_BY, SORT_ID);
			sstate.setAttribute(STATE_CURRENT_SORT_ASC, Boolean.FALSE);
		}
		// if there's an alert message, divert it to the main frame
		String msg = (String) sstate.getAttribute(STATE_MESSAGE);
		sstate.setAttribute(STATE_MAIN_MESSAGE, msg);
		sstate.removeAttribute(VelocityPortletPaneledAction.STATE_MESSAGE);
		
		// go into manage mode
		sstate.setAttribute(STATE_MODE, MODE_MANAGE);

	} // doReviseChat	
	
	/**
	 * Handle a request to set options.
	 */
	public void doOptions(RunData runData, Context context)
	{
		super.doOptions(runData, context);

		// access the portlet element id to find our state
		String peid = ((JetspeedRunData) runData).getJs_peid();
		SessionState state = ((JetspeedRunData) runData).getPortletSessionState(peid);

		// if there's an alert message, divert it to the main frame
		String msg = (String) state.getAttribute(STATE_MESSAGE);
		state.setAttribute(STATE_MAIN_MESSAGE, msg);
		state.removeAttribute(VelocityPortletPaneledAction.STATE_MESSAGE);
		
		Placement placement = ToolManager.getCurrentPlacement();
		// remove the current presence
		String oldLocation = placement.getId() + "|" + ChatService.extractChannelId((String)state.getAttribute(STATE_CHANNEL_REF));
		PresenceService.removePresence(oldLocation);
		

	} // doOptions

	/**
	 * Fire up the permissions editor, next request cycle
	 */
	public void doPermissions(RunData data, Context context)
	{
		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);
		
		// trigger the switch on the next request (which is going to happen after this action is processed with its redirect response to the build)
		state.setAttribute(STATE_PERMISSIONS, STATE_PERMISSIONS);

		// schedule a main refresh to excape from the toolbar panel
		schedulePeerFrameRefresh(mainPanelUpdateId(peid));
		Placement placement = ToolManager.getCurrentPlacement();
		// remove the current presence
		String oldLocation = placement.getId() + "|" + ChatService.extractChannelId((String)state.getAttribute(STATE_CHANNEL_REF));
		PresenceService.removePresence(oldLocation);
	}

	/**
	 * Fire up the permissions editor
	 */
	protected void doPermissionsNow(RunData data, Context context)
	{
		// get into helper mode with this helper tool
		startHelper(data.getRequest(), "sakai.permissions.helper");

		String peid = ((JetspeedRunData) data).getJs_peid();
		SessionState state = ((JetspeedRunData) data).getPortletSessionState(peid);

		String channelRefStr = (String) state.getAttribute(STATE_CHANNEL_REF);
		Reference channelRef = EntityManager.newReference(channelRefStr);
		String siteRef = SiteService.siteReference(channelRef.getContext());

		// setup for editing the permissions of the site for this tool, using the roles of this site, too
		state.setAttribute(PermissionsHelper.TARGET_REF, siteRef);

		// ... with this description
		state.setAttribute(PermissionsHelper.DESCRIPTION, rb.getString("setpermis") + " "
				+ SiteService.getSiteDisplay(channelRef.getContext()));

		// ... showing only locks that are prpefixed with this
		state.setAttribute(PermissionsHelper.PREFIX, "chat.");
	}
	
	// ********
	// ******** functions copied from VelocityPortletStateAction ********
	// ********
	/**
	 * Get the proper state for this instance (if portlet is not known, only context).
	 *
	 * @param context
	 *        The Template Context (it contains a reference to the portlet).
	 * @param rundata
	 *        The Jetspeed (Turbine) rundata associated with the request.
	 * @param stateClass
	 *        The Class of the ControllerState to find / create.
	 * @return The proper state object for this instance.
	 */
	protected ControllerState getState(Context context, RunData rundata, Class stateClass)
	{
		return getState(((JetspeedRunData) rundata).getJs_peid(), rundata, stateClass);

	} // getState

	/**
	 * Get the proper state for this instance (if portlet is known).
	 *
	 * @param portlet
	 *        The portlet being rendered.
	 * @param rundata
	 *        The Jetspeed (Turbine) rundata associated with the request.
	 * @param stateClass
	 *        The Class of the ControllerState to find / create.
	 * @return The proper state object for this instance.
	 */
	protected ControllerState getState(VelocityPortlet portlet, RunData rundata, Class stateClass)
	{
		if (portlet == null)
		{
			Log.warn("chef", this + ".getState(): portlet null");
			return null;
}

		return getState(portlet.getID(), rundata, stateClass);

	} // getState

	/**
	 * Get the proper state for this instance (if portlet id is known).
	 *
	 * @param peid
	 *        The portlet id.
	 * @param rundata
	 *        The Jetspeed (Turbine) rundata associated with the request.
	 * @param stateClass
	 *        The Class of the ControllerState to find / create.
	 * @return The proper state object for this instance.
	 */
	protected ControllerState getState(String peid, RunData rundata, Class stateClass)
	{
		if (peid == null)
		{
			Log.warn("chef", this + ".getState(): peid null");
			return null;
		}

		try
		{
			// get the PortletSessionState
			SessionState ss = ((JetspeedRunData) rundata).getPortletSessionState(peid);

			// get the state object
			ControllerState state = (ControllerState) ss.getAttribute("state");

			if (state != null) return state;

			// if there's no "state" object in there, make one
			state = (ControllerState) stateClass.newInstance();
			state.setId(peid);

			// remember it!
			ss.setAttribute("state", state);

			return state;
		}
		catch (Exception e)
		{
			Log.warn("chef", "", e);
		}

		return null;

	} // getState

	/**
	 * Release the proper state for this instance (if portlet is not known, only context).
	 *
	 * @param context
	 *        The Template Context (it contains a reference to the portlet).
	 * @param rundata
	 *        The Jetspeed (Turbine) rundata associated with the request.
	 */
	protected void releaseState(Context context, RunData rundata)
	{
		releaseState(((JetspeedRunData) rundata).getJs_peid(), rundata);

	} // releaseState

	/**
	 * Release the proper state for this instance (if portlet is known).
	 *
	 * @param portlet
	 *        The portlet being rendered.
	 * @param rundata
	 *        The Jetspeed (Turbine) rundata associated with the request.
	 */
	protected void releaseState(VelocityPortlet portlet, RunData rundata)
	{
		releaseState(portlet.getID(), rundata);

	} // releaseState

	/**
	 * Release the proper state for this instance (if portlet id is known).
	 *
	 * @param peid
	 *        The portlet id being rendered.
	 * @param rundata
	 *        The Jetspeed (Turbine) rundata associated with the request.
	 */
	protected void releaseState(String peid, RunData rundata)
	{
		try
		{
			// get the PortletSessionState
			SessionState ss = ((JetspeedRunData) rundata).getPortletSessionState(peid);

			// get the state object
			ControllerState state = (ControllerState) ss.getAttribute("state");

			// recycle the state object
			state.recycle();

			// clear out the SessionState for this Portlet
			ss.removeAttribute("state");

			ss.clear();

		}
		catch (Exception e)
		{
			Log.warn("chef", "", e);
		}

	} // releaseState
	
	
	/**
	 * Check if all messages have the position property
	 * @param messageList
	 * @param state
	 * @return
	 */
	private boolean checkIfPositions(List chatList, ChatActionState state)
	{
		boolean found = ChatService.checkPositions(chatList);

		if ((state != null) && (state.getCurrentSortedBy() == null || state.getCurrentSortedBy().length() == 0))
		{
			if (!found)
			{
				state.setCurrentSortedBy(SORT_ID);
				state.setCurrentSortAsc(true);
			}
			else
			{
				state.setCurrentSortedBy(SORT_POSITION);
				state.setCurrentSortAsc(true);
			}
		}
		return found;
	}	
	
	/**
	 * Returns the max position from the list of channels
	 * @param chatList List of channels
	 * @return -1 if list is empty, max position otherwise
	 */
	private int getMaxPosition(List chatList)
	{
        return ChatService.getMaxPosition(chatList);
	}	
		
	
	/**
	 * Sort to new positions and assign positions to all items.
	 * 
	 * @param rundata
	 * @param context
	 */
	public void sortToNewPositions(RunData rundata, Context context)
	{
		ParameterParser params = rundata.getParameters();
		ChatActionState state = (ChatActionState) getState(context, rundata, ChatActionState.class);

		String oldPosition = params.getString("oldPosition");
		String newPosition = params.getString("newPosition");
		String newOrder = params.getString("newOrderChatToSend");

		if (newOrder == null || newOrder.length() == 0) return;
		try
		{
			String[] parts = newOrder.split("%");
			int oldPos = Integer.parseInt(oldPosition);
			int newPos = Integer.parseInt(newPosition);

			String moveId = (oldPos > 0) ? parts[oldPos - 1] : parts[oldPos];
			// 3 became 10
			if (oldPos < newPos)
			{
				for (int i = oldPos; i < newPos; i++)
				{
					parts[i - 1] = parts[i];
				}
			}
			else if (oldPos > newPos)
			{
				for (int i = oldPos - 1; i >= newPos; i--)
				{
					parts[i] = parts[i - 1];
				}
			}
			parts[newPos - 1] = moveId;
			
			String peid = ((JetspeedRunData) rundata).getJs_peid();
			SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);

			modifyPositions(parts, state, sstate);
		}
		catch (Exception ex)
		{
			// do nothing
		}

	}

	/**
	 * Dnd puts the items in new order. Save it on clicking of Save button
	 * @param rundata
	 * @param context
	 */
	public void savePositions(RunData rundata, Context context)
	{
		ParameterParser params = rundata.getParameters();
		ChatActionState state = (ChatActionState) getState(context, rundata, ChatActionState.class);

		// read hidden field noewOrdersToSend
		String newOrder = params.getString("newOrderChatToSend");
	    if (newOrder == null || newOrder.length() == 0) return;

		String[] parts = newOrder.split("%");
		String peid = ((JetspeedRunData) rundata).getJs_peid();
		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(peid);
		ChatChannel channel = null;
		try
		{
			channel = ChatService.getChatChannel(ChatService.channelReference(ToolManager.getCurrentPlacement().getContext(), parts[0]));
			if (channel.getHeader().getAccess() == MessageChannelHeader.MessageAccess.GROUPED)
			{
				sstate.setAttribute(STATE_MODE, MODE_CONFIRMPUBLISH);
				sstate.setAttribute(PUBLISH_CHANNELS, newOrder);
				sstate.setAttribute(GROUP_CHANNEL, parts[0]);
				return;
			}
		}
		catch (IdUnusedException ignore)
		{
			if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "buildMainPanelContext()" + ignore);
		}
		catch (PermissionException ignore)
		{
			if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "buildMainPanelContext()" + ignore);
		}

		confirmModifyPositions(parts, state, sstate);
	}

	/**
	 * Checks to see if first channel is grouped. If so, seeks confirm. Otherwise, saves positions
	 * @param parts
	 * @param state
	 */
	private void modifyPositions(String[] parts, ChatActionState state, SessionState sstate)
	{
		Integer position = new Integer(1);
		ChatChannelEdit channel = null;
		
		ChatChannel chan = null;

		try
		{
			chan = ChatService.getChatChannel(ChatService.channelReference(ToolManager.getCurrentPlacement().getContext(), parts[0]));
			if (chan.getHeader().getAccess() == MessageChannelHeader.MessageAccess.GROUPED)
			{
				sstate.setAttribute(STATE_MODE, MODE_CONFIRMPUBLISH);
				sstate.setAttribute(PUBLISH_CHANNELS, fetchPowString(Arrays.asList(parts)));
				sstate.setAttribute(GROUP_CHANNEL, parts[0]);
				return;
			}
		}
		catch (IdUnusedException ignore)
		{
			if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "buildMainPanelContext()" + ignore);
		}
		catch (PermissionException ignore)
		{
			if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", this + "buildMainPanelContext()" + ignore);
		}
		confirmModifyPositions(parts, state, sstate);
	}
	
	private void confirmModifyPositions(String[] parts, ChatActionState state, SessionState sstate)
	{
		Integer position = new Integer(1);
		ChatChannelEdit channel = null;
		
		try
		{
			for (String p : parts)
			{
				try
				{
					channel = (ChatChannelEdit) ChatService.editChannel(ChatService.channelReference(ToolManager.getCurrentPlacement().getContext(), p));
					if (channel.getPropertiesEdit().getProperty("position") != null) channel.getPropertiesEdit().removeProperty("position");
					channel.getPropertiesEdit().addProperty("position", position.toString());
					if (position == 1)
					{
						Placement placement = ToolManager.getCurrentPlacement();
						placement.getPlacementConfig().setProperty(PARAM_CHANNEL,
								ChatService.channelReference(ToolManager.getCurrentPlacement().getContext(), p));
						if (channel.getPropertiesEdit().getProperty("published") != null) channel.getPropertiesEdit().removeProperty("published");
						channel.getPropertiesEdit().addProperty("published", "1");
						channel.getChatChannelHeaderEdit().clearGroupAccess();
						//sstate.setAttribute(STATE_CHANNEL_REF, ChatService.channelReference(ToolManager.getCurrentPlacement().getContext(), p));
					}
					position++;
					ChatService.commitChannel(channel);
				}
				catch (IdUnusedException ignore)
				{
					ignore.printStackTrace();
				}
				catch (PermissionException ignore)
				{
					ignore.printStackTrace();
				}
				catch (InUseException ignore)
				{
					ignore.printStackTrace();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}

			saveOptions();
			state.setCurrentSortedBy(SORT_POSITION);
			state.setCurrentSortAsc(true);
		}
		catch (Exception ex)
		{
			// do nothing
		}
	}
	
	/**
	 * Does initialization of sort parameters in the state.
	 */
	private void setupSort(RunData rundata, Context context, String field)
	{
		// retrieve the state from state object
		ChatActionState state = (ChatActionState) getState(context, rundata, ChatActionState.class);

		SessionState sstate = ((JetspeedRunData) rundata).getPortletSessionState(((JetspeedRunData) rundata).getJs_peid());
		sstate.setAttribute(STATE_CURRENT_SORTED_BY, field);

		if (state.getCurrentSortedBy().equals(field))
		{
			// current sorting sequence
			boolean asc = state.getCurrentSortAsc();

			// toggle between the ascending and descending sequence
			if (asc)
				asc = false;
			else
				asc = true;

			state.setCurrentSortAsc(asc);
			sstate.setAttribute(STATE_CURRENT_SORT_ASC, new Boolean(asc));
		}
		else
		{
			// if the messages are not already sorted by subject, set the sort sequence to be ascending
			state.setCurrentSortedBy(field);
			state.setCurrentSortAsc(true);
			sstate.setAttribute(STATE_CURRENT_SORT_ASC, Boolean.TRUE);
		}
	} // setupSort
	
	/**
	 * Do sort by group title
	 */
	public void doSortbygrouptitle(RunData rundata, Context context)
	{
		if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", "ChatAction.doSortbyfrom get Called");

		setupSort(rundata, context, SORT_GROUPTITLE);
	} // doSortbygrouptitle

	/**
	 * Do sort by group description
	 */
	public void doSortbygroupdescription(RunData rundata, Context context)
	{
		if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", "ChatAction.doSortbyfrom get Called");

		setupSort(rundata, context, SORT_GROUPDESCRIPTION);
	} // doSortbygroupdescription
	

	/**
	 * Do sort by id
	 */
	public void doSortbyId(RunData rundata, Context context)
	{
		if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", "ChatAction.doSortbyId get Called");

		setupSort(rundata, context, SORT_ID);
	} // doSortbysubject
	
	/**
	 * Do sort by position
	 */
	public void doSortbyPosition(RunData rundata, Context context)
	{
		if (Log.getLogger("chef").isDebugEnabled()) Log.debug("chef", "ChatAction.doSortbyPosition get Called");

		setupSort(rundata, context, SORT_POSITION);
	} // doSortbyposition	
	
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

