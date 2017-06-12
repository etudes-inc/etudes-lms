/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/announcement/announcement-impl/impl/src/java/org/sakaiproject/announcement/impl/BaseAnnouncementService.java $
 * $Id: BaseAnnouncementService.java 12190 2015-12-02 20:20:14Z mallikamt $
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

package org.sakaiproject.announcement.impl;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.etudes.util.HtmlHelper;
import org.etudes.util.XrefHelper;
import org.etudes.util.api.Translation;
import org.sakaiproject.announcement.api.AnnouncementChannel;
import org.sakaiproject.announcement.api.AnnouncementChannelEdit;
import org.sakaiproject.announcement.api.AnnouncementMessage;
import org.sakaiproject.announcement.api.AnnouncementMessageEdit;
import org.sakaiproject.announcement.api.AnnouncementMessageHeader;
import org.sakaiproject.announcement.api.AnnouncementMessageHeaderEdit;
import org.sakaiproject.announcement.api.AnnouncementService;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.entity.api.ContextObserver;
import org.sakaiproject.entity.api.Edit;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.EntityAccessOverloadException;
import org.sakaiproject.entity.api.EntityCopyrightException;
import org.sakaiproject.entity.api.EntityNotDefinedException;
import org.sakaiproject.entity.api.EntityPermissionException;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.EntityTransferrer;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.NotificationEdit;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdInvalidException;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.IdUsedException;
import org.sakaiproject.exception.InUseException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.javax.Filter;
import org.sakaiproject.message.api.Message;
import org.sakaiproject.message.api.MessageChannel;
import org.sakaiproject.message.api.MessageChannelHeader;
import org.sakaiproject.message.api.MessageChannelHeaderEdit;
import org.sakaiproject.message.api.MessageHeader;
import org.sakaiproject.message.api.MessageHeaderEdit;
import org.sakaiproject.message.impl.BaseMessageService;
import org.sakaiproject.scheduler.api.DbScheduleService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.StringUtil;
import org.sakaiproject.util.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <p>
 * BaseAnnouncementService extends the BaseMessageService for the specifics of Announcement.
 * </p>
 */
public abstract class BaseAnnouncementService extends BaseMessageService implements AnnouncementService, ContextObserver, EntityTransferrer
{
	/** Our logger. */
	private static Log M_log = LogFactory.getLog(BaseAnnouncementService.class);

	/** Messages, for the http access. */
	protected static ResourceLoader rb = new ResourceLoader("annc-access");

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Constructors, Dependencies and their setter methods
	 *********************************************************************************************************************************************************************************************************************************************************/

	/** Dependency: NotificationService. */
	protected NotificationService m_notificationService = null;

	protected DbScheduleService m_scheduleService = null;

	/**
	 * Dependency: NotificationService.
	 *
	 * @param service
	 *        The NotificationService.
	 */
	public void setNotificationService(NotificationService service)
	{
		m_notificationService = service;
	}

	public void setScheduleService(DbScheduleService service)
	{
		m_scheduleService = service;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * Init and Destroy
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init()
	{
		try
		{
			super.init();

			// register a transient notification for announcements
			NotificationEdit edit = m_notificationService.addTransientNotification();

			// set functions
			edit.setFunction(eventId(SECURE_ADD));
			edit.addFunction(eventId(SECURE_UPDATE_OWN));
			edit.addFunction(eventId(SECURE_UPDATE_ANY));

			// set the filter to any announcement resource (see messageReference())
			edit.setResourceFilter(getAccessPoint(true) + Entity.SEPARATOR + REF_TYPE_MESSAGE);

			// set the action
			edit.setAction(new SiteEmailNotificationAnnc());

			// register functions
			FunctionManager.registerFunction(eventId(SECURE_READ));
			FunctionManager.registerFunction(eventId(SECURE_ADD));
			FunctionManager.registerFunction(eventId(SECURE_REMOVE_ANY));
			FunctionManager.registerFunction(eventId(SECURE_REMOVE_OWN));
			FunctionManager.registerFunction(eventId(SECURE_UPDATE_ANY));
			FunctionManager.registerFunction(eventId(SECURE_UPDATE_OWN));
			FunctionManager.registerFunction(eventId(SECURE_READ_DRAFT));
			FunctionManager.registerFunction(eventId(SECURE_ALL_GROUPS));

			// entity producer registration
			m_entityManager.registerEntityProducer(this, REFERENCE_ROOT);


			//Checking to see if we run email scheduler on this server
			// read the server's id
			/*String id = m_serverConfigurationService.getServerId();
			if (id != null)
			{
				// read configuration for this server
				String emailServer = m_serverConfigurationService.getString("announcement.delayedEmailServer");
				if ((emailServer != null)&&(emailServer.trim().length()>0))
				{
					if (id.equals(emailServer.trim()))
					{
						scRunner = new ScheduleRunner();
						scRunner.init();
					}
				}
			}*/
			M_log.info("init()");
		}
		catch (Throwable t)
		{
			M_log.warn("init(): ", t);
		}

	} // init

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		M_log.info("destroy()");
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * StorageUser implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Construct a new continer given just ids.
	 *
	 * @param ref
	 *        The container reference.
	 * @return The new containe Resource.
	 */
	public Entity newContainer(String ref)
	{
		return new BaseAnnouncementChannelEdit(ref);
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
		return new BaseAnnouncementChannelEdit(element);
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
		return new BaseAnnouncementChannelEdit((MessageChannel) other);
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
		return new BaseAnnouncementMessageEdit((MessageChannel) container, id);
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
		return new BaseAnnouncementMessageEdit((MessageChannel) container, element);
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
		return new BaseAnnouncementMessageEdit((MessageChannel) container, (Message) other);
	}

	/**
	 * Construct a new continer given just ids.
	 *
	 * @param ref
	 *        The container reference.
	 * @return The new containe Resource.
	 */
	public Edit newContainerEdit(String ref)
	{
		BaseAnnouncementChannelEdit rv = new BaseAnnouncementChannelEdit(ref);
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
		BaseAnnouncementChannelEdit rv = new BaseAnnouncementChannelEdit(element);
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
		BaseAnnouncementChannelEdit rv = new BaseAnnouncementChannelEdit((MessageChannel) other);
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
		BaseAnnouncementMessageEdit rv = new BaseAnnouncementMessageEdit((MessageChannel) container, id);
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
		BaseAnnouncementMessageEdit rv = new BaseAnnouncementMessageEdit((MessageChannel) container, element);
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
		BaseAnnouncementMessageEdit rv = new BaseAnnouncementMessageEdit((MessageChannel) container, (Message) other);
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
		rv[2] = ((AnnouncementMessage) r).getAnnouncementHeader().getDraft() ? "1" : "0";
		rv[3] = r.getProperties().getProperty(ResourceProperties.PROP_PUBVIEW) == null ? "0" : "1";
		// rv[3] = ((AnnouncementMessage) r).getAnnouncementHeader().getAccess() == MessageHeader.MessageAccess.PUBLIC ? "1" : "0";

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
		return ((AnnouncementMessage) r).getAnnouncementHeader().getDraft();
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
		return AnnouncementService.class.getName();
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
		return new BaseAnnouncementMessageHeaderEdit(msg, id);

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
		return new BaseAnnouncementMessageHeaderEdit(msg, el);

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
		return new BaseAnnouncementMessageHeaderEdit(msg, other);

	} // newMessageHeader

	protected MessageChannelHeaderEdit newChannelHeader(MessageChannel chan/*, String id*/)
	{
		return null;
	}
	
	protected MessageChannelHeaderEdit newChannelHeader(MessageChannel chan, Element e1)
	{
		return null;
	}
	
	protected MessageChannelHeaderEdit newChannelHeader(MessageChannel chan, MessageChannelHeader other)
	{
		return null;
	}

	/**
	 * Form a tracking event string based on a security function string.
	 *
	 * @param secure
	 *        The security function string.
	 * @return The event tracking string.
	 */
	protected String eventId(String secure)
	{
		return SECURE_ANNC_ROOT + secure;

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
		if (toolPlacement) enableMessageChannel(context);
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
		String[] toolIds = {"sakai.announcements"};
		return toolIds;
	}

	/**
	 * {@inheritDoc}
	 */
	public HttpAccess getHttpAccess()
	{
		return new HttpAccess()
		{
			public void handleAccess(HttpServletRequest req, HttpServletResponse res, Reference ref, Collection copyrightAcceptedRefs)
					throws EntityPermissionException, EntityNotDefinedException, EntityAccessOverloadException, EntityCopyrightException
			{
				/** Resource bundle using current language locale */
				// final ResourceBundle rb = ResourceBundle.getBundle("access");
				// check security on the message (throws if not permitted)
				try
				{
					unlock(SECURE_READ, ref.getReference());
				}
				catch (PermissionException e)
				{
					throw new EntityPermissionException(e.getUser(), e.getLock(), e.getResource());
				}

				try
				{
					AnnouncementMessage msg = (AnnouncementMessage) ref.getEntity();
					AnnouncementMessageHeader hdr = (AnnouncementMessageHeader) msg.getAnnouncementHeader();

					res.setContentType("text/html; charset=UTF-8");
					PrintWriter out = res.getWriter();
					out
							.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
									+ "<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">\n"
									+ "<head>\n<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n"
									+ "<style type=\"text/css\">body{margin:0px;padding:1em;font-family:Verdana,Arial,Helvetica,sans-serif;font-size:80%;}</style>\n"
									+ "<title>"
									+ rb.getString("announcement")
									+ ": "
									+ Validator.escapeHtml(hdr.getSubject())
									+ "</title>"
									+ "</head>\n<body>");

					out.println("<h1>" + rb.getString("announcement") + "</h1>");

					// header
					out.println("<table><tr><td><b>" + rb.getString("from") + ":</b></td><td>" + Validator.escapeHtml(hdr.getFrom().getDisplayName())
							+ "</td></tr>");
					out.println("<tr><td><b>" + rb.getString("date") + ":</b></td><td>" + Validator.escapeHtml(hdr.getDate().toStringLocalFull())
							+ "</td></tr>");
					out.println("<tr><td><b>" + rb.getString("subject") + ":</b></td><td>" + Validator.escapeHtml(hdr.getSubject())
							+ "</td></tr></table>");

					// body
					out.println("<p>" + Validator.escapeHtmlFormattedText(msg.getBody()) + "</p>");

					// attachments
					List attachments = hdr.getAttachments();
					if (attachments.size() > 0)
					{
						out.println("<p><b>" + rb.getString("attachments") + ":</b></p><p>");
						for (Iterator iAttachments = attachments.iterator(); iAttachments.hasNext();)
						{
							Reference attachment = (Reference) iAttachments.next();
							out.println("<a href=\"" + Validator.escapeHtml(attachment.getUrl()) + "\">" + Validator.escapeHtml(attachment.getUrl())
									+ "</a><br />");
						}
						out.println("</p>");
					}

					out.println("</body></html>");
				}
				catch (Throwable t)
				{
					throw new EntityNotDefinedException(ref.getReference());
				}
			}
		};
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AnnouncementService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * Return a specific announcement channel.
	 *
	 * @param ref
	 *        The channel reference.
	 * @return the AnnouncementChannel that has the specified name.
	 * @exception IdUnusedException
	 *            If this name is not defined for a announcement channel.
	 * @exception PermissionException
	 *            If the user does not have any permissions to the channel.
	 */
	public AnnouncementChannel getAnnouncementChannel(String ref) throws IdUnusedException, PermissionException
	{
		return (AnnouncementChannel) getChannel(ref);

	} // getAnnouncementChannel

	/**
	 * Add a new announcement channel.
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
	public AnnouncementChannelEdit addAnnouncementChannel(String ref) throws IdUsedException, IdInvalidException, PermissionException
	{
		return (AnnouncementChannelEdit) addChannel(ref);

	} // addAnnouncementChannel

	/**********************************************************************************************************************************************************************************************************************************************************
	 * ResourceService implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	/**
	 * {@inheritDoc}
	 */
	public String getLabel()
	{
		return "announcement";
	}

    public Date getMinStartDate(String course_id)
	{
		// get the channel associated with this site
		String oChannelRef = channelReference(course_id, SiteService.MAIN_CONTAINER);
		AnnouncementChannel oChannel = null;
		Date minStartDate = null;
		Time minStartTime = null;
		try
		{
			oChannel = (AnnouncementChannel) getChannel(oChannelRef);

			if (oChannel != null)
			{
				List oMessageList = oChannel.getMessages(null, true);
				AnnouncementMessage oMessage = null;
				List timeList = new ArrayList();
				for (int i = 0; i < oMessageList.size(); i++)
				{
					// the "from" message
					oMessage = (AnnouncementMessage) oMessageList.get(i);
					try
					{
					  if (oMessage.getProperties().getTimeProperty(RELEASE_DATE) != null)
					  {
						timeList.add(oMessage.getProperties().getTimeProperty(RELEASE_DATE));
					  }
				    }
				    catch (Exception e)
				    {
					  //If release date isn't defined, do not add to timeList
				    }
				}
				 if (timeList != null)
			      {
			    	  if (timeList.size() > 0)
			    	  {
			    		  Iterator i = timeList.iterator();
			    		  if (i.hasNext())
			    		  {
			    			  minStartTime = (Time)i.next();
			    			  while (i.hasNext())
			    			  {
			    				  Time msgTime = (Time)i.next();
			    				  if (msgTime.before(minStartTime))
			    				  {
			    					  minStartTime = msgTime;
			    				  }
			    			  }
			    		  }
			    	  }
			      }
				 if (minStartTime != null)
				 {
					 minStartDate = new Date();
					 minStartDate.setTime(minStartTime.getTime());
				 }

			} // if
		}
		catch (IdUnusedException e)
		{
			M_log.warn(" getMinStartDate: MessageChannel " + course_id + " cannot be found. ");
		}
		catch (Exception any)
		{
			M_log.warn("getMinStartDate: exception in handling " + serviceName() + " : ", any);
		}
    return minStartDate;
	}

    public Date getMaxStartDate(String course_id)
	{
		// get the channel associated with this site
		String oChannelRef = channelReference(course_id, SiteService.MAIN_CONTAINER);
		AnnouncementChannel oChannel = null;
		Date maxStartDate = null;
		Time maxStartTime = null;
		try
		{
			oChannel = (AnnouncementChannel) getChannel(oChannelRef);

			if (oChannel != null)
			{
				List oMessageList = oChannel.getMessages(null, true);
				AnnouncementMessage oMessage = null;
				List timeList = new ArrayList();
				for (int i = 0; i < oMessageList.size(); i++)
				{
					// the "from" message
					oMessage = (AnnouncementMessage) oMessageList.get(i);
					try
					{
					  if (oMessage.getProperties().getTimeProperty(RELEASE_DATE) != null)
					  {
						timeList.add(oMessage.getProperties().getTimeProperty(RELEASE_DATE));
					  }
				    }
				    catch (Exception e)
				    {
					  //If release date isn't defined, do not add to timeList
				    }
				}
				 if (timeList != null)
			      {
			    	  if (timeList.size() > 0)
			    	  {
			    		  Iterator i = timeList.iterator();
			    		  if (i.hasNext())
			    		  {
			    			  maxStartTime = (Time)i.next();
			    			  while (i.hasNext())
			    			  {
			    				  Time msgTime = (Time)i.next();
			    				  if (msgTime.after(maxStartTime))
			    				  {
			    					  maxStartTime = msgTime;
			    				  }
			    			  }
			    		  }
			    	  }
			      }
				 if (maxStartTime != null)
				 {
					 maxStartDate = new Date();
					 maxStartDate.setTime(maxStartTime.getTime());
				 }

			} // if
		}
		catch (IdUnusedException e)
		{
			M_log.warn(" getMaxStartDate: MessageChannel " + course_id + " cannot be found. ");
		}
		catch (Exception any)
		{
			M_log.warn("getMaxStartDate: exception in handling " + serviceName() + " : ", any);
		}
    return maxStartDate;
	}
    
    public void applyBaseDateTx(String course_id, int days_diff)
    {
    	int noti = NotificationService.NOTI_OPTIONAL;

    	if (course_id == null)
    	{
    		M_log.warn("applyBaseDateTx: course_id is null");
    	}
    	if (days_diff == 0)
    	{
    		return;
    	}
    	// get the channel associated with this site
    	String oChannelRef = channelReference(course_id, SiteService.MAIN_CONTAINER);
    	AnnouncementChannel oChannel, nChannel = null;
    	GregorianCalendar nowGc = new GregorianCalendar();
    	GregorianCalendar gc = new GregorianCalendar();
    	boolean result = false;
    	List idList = new ArrayList();
    	try
    	{
    		oChannel = (AnnouncementChannel) getChannel(oChannelRef);

    		if (oChannel != null)
    		{
    			List oMessageList = oChannel.getMessages(null, true);
    			AnnouncementMessage oMessage = null;
    			for (int i = 0; i < oMessageList.size(); i++)
    			{
    				// the "from" message
    				oMessage = (AnnouncementMessage) oMessageList.get(i);
    				idList.add(oMessage.getId());
    			}
    		}	// if
    	}
    	catch (Exception any)
    	{
    		M_log.warn(".applyBaseDateTx: exception in handling: " + serviceName() + " messageChannel: "
    				+ course_id);
    	}
    	try
    	{
    		nChannel = (AnnouncementChannel) getChannel(oChannelRef);

    		if (nChannel != null)
    		{
    			if (idList != null)
    			{
    				if (idList.size() > 0)
    				{
    					Iterator i = idList.iterator();
    					while (i.hasNext())
    					{
    						AnnouncementMessageEdit oMessageEdit = (AnnouncementMessageEdit) nChannel.editAnnouncementMessage((String)i.next());
    						Time releaseTime = null;
    						try
    						{
    							releaseTime = oMessageEdit.getProperties().getTimeProperty(RELEASE_DATE);
    							if (releaseTime != null)
        						{
    								Date now = nowGc.getTime();
        							Date releaseDate = new Date();
        							releaseDate.setTime(oMessageEdit.getProperties().getTimeProperty(RELEASE_DATE).getTime());
        							gc.setTime(releaseDate);
        							gc.add(Calendar.DATE, days_diff);
        							if (oMessageEdit.getProperties().getProperty(RELEASE_DATE) != null)
        							{
        								oMessageEdit.getPropertiesEdit().removeProperty(RELEASE_DATE);
        							}
        							oMessageEdit.getPropertiesEdit().addProperty(RELEASE_DATE, (TimeService.newTime(gc)).toString());
        							DbScheduleService m_scheduleService = (DbScheduleService)
        							ComponentManager.get(org.sakaiproject.scheduler.api.DbScheduleService.class);

        							if (oMessageEdit.getProperties().getProperty(AnnouncementService.SCHED_INV_UUID) != null)
        							{
        								m_scheduleService.deleteDelayedInvocation(oMessageEdit.getProperties().getProperty(AnnouncementService.SCHED_INV_UUID));
        								oMessageEdit.getPropertiesEdit().removeProperty(AnnouncementService.SCHED_INV_UUID);
        							}
        							String notification = oMessageEdit.getProperties().getProperty(AnnouncementService.NOTIFICATION_LEVEL);
        							if ("r".equals(notification))
        							{
        								noti = NotificationService.NOTI_REQUIRED;
        							}
        							else if ("n".equals(notification))
        							{
        								noti = NotificationService.NOTI_NONE;
        							}
        							if (oMessageEdit.getAnnouncementHeader().getDraft() == false &&  noti != NotificationService.NOTI_NONE && now.before(gc.getTime()))
        							{
        								String uuid = m_scheduleService.createDelayedInvocation(TimeService.newTime(gc),
        										"org.sakaiproject.announcement.impl.SiteEmailNotificationAnnc", oMessageEdit.getReference());

        								oMessageEdit.getPropertiesEdit().addProperty(AnnouncementService.SCHED_INV_UUID, uuid);
        							}
        						}
    							nChannel.commitMessage(oMessageEdit, NotificationService.NOTI_NONE);
    						}
    						catch (Exception e)
    						{
                              if (oMessageEdit != null) nChannel.cancelMessage(oMessageEdit);
    						}
    						
    					}
    				}
    			}
    		}	// if
    	}
    	catch (Exception any)
    	{
    		M_log.warn(".applyBaseDateTx: error for service: " + serviceName() + " messageChannel: "
    				+ course_id);
    	}
    	return;
    }

	/**
	 * {@inheritDoc}
	 */
	public void transferCopyEntities(String fromContext, String toContext, List resourceIds)
	{
		// get the channel associated with this site
		String oChannelRef = channelReference(fromContext, SiteService.MAIN_CONTAINER);
		AnnouncementChannel oChannel = null;
		try
		{
			oChannel = (AnnouncementChannel) getChannel(oChannelRef);

			// the "to" message channel
			String nChannelRef = channelReference(toContext, SiteService.MAIN_CONTAINER);
			AnnouncementChannel nChannel = null;
			try
			{
				nChannel = (AnnouncementChannel) getChannel(nChannelRef);
			}
			catch (IdUnusedException e)
			{
				try
				{
					commitChannel(addChannel(nChannelRef));
					nChannel = (AnnouncementChannel) getChannel(nChannelRef);
				}
				catch (Exception ee)
				{
				}
			}

			if (nChannel != null)
			{
				// pass the DOM to get new message ids, record the mapping from old to new, and adjust attachments
				List oMessageList = oChannel.getAllMessages(null, true);

				for (int i = 0; i < oMessageList.size(); i++)
				{
					// the "from" message
					AnnouncementMessage oMessage = (AnnouncementMessage) oMessageList.get(i);
					String oMessageId = oMessage.getId();

					AnnouncementMessageHeader oMessageHeader = (AnnouncementMessageHeaderEdit) oMessage.getHeader();
					ResourceProperties oProperties = oMessage.getProperties();

					// translate old message embed data
					String nMessageData = oMessage.getBody();
					Set<String> refs = XrefHelper.harvestEmbeddedReferences(nMessageData, null);
					M_log.debug("embed data found:" + refs.toString());
					if (!refs.isEmpty())
					{
						List<Translation> translations = XrefHelper.importTranslateResources(refs, toContext, "Announcements");
						nMessageData = XrefHelper.translateEmbeddedReferences(nMessageData, translations, toContext);
					}

					// the "to" message
					AnnouncementMessageEdit nMessage = (AnnouncementMessageEdit) nChannel.addMessage();
					nMessage.setBody(nMessageData);

					// message header
					AnnouncementMessageHeaderEdit nMessageHeader = (AnnouncementMessageHeaderEdit) nMessage.getHeaderEdit();
					nMessageHeader.setDate(oMessageHeader.getDate());
					// when importing, always mark the announcement message as draft
					nMessageHeader.setDraft(true);
					nMessageHeader.setFrom(oMessageHeader.getFrom());
					nMessageHeader.setSubject(oMessageHeader.getSubject());

					// properties
					ResourcePropertiesEdit p = nMessage.getPropertiesEdit();
					p.clear();
					p.addAll(oProperties);
					// Remove SCHED_INV_UUID property, we do not want to import this into a new site
					if (p.getProperty(AnnouncementService.SCHED_INV_UUID) != null)
					{
						p.removeProperty(AnnouncementService.SCHED_INV_UUID);
					}
					// Remove NOTIFICATION_LEVEL property, we do not want to import this into a new site
					/*if (p.getProperty(AnnouncementService.NOTIFICATION_LEVEL) != null)
					{
						p.removeProperty(AnnouncementService.NOTIFICATION_LEVEL);
					}*/

					// groups
					try
					{
						Site s = SiteService.getSite(toContext);
						Collection<Group> groups = (Collection<Group>) s.getGroups();

						Set<Group> eventGroups = new HashSet<Group>();
						for (Group oGroup : (Collection<Group>) oMessage.getHeader().getGroupObjects())
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
							nMessage.getHeaderEdit().setGroupAccess(eventGroups);
						}
					}
					catch (IdUnusedException iue)
					{
						M_log.warn("transferCopyEntities: missing site: " + toContext);
					}

					// before we bring over attachments, lets see if we want to skip this because we have one too much like it
					if (nChannel.containsMatchingMessage(nMessage))
					{
						nChannel.removeMessage(nMessage);
					}

					else
					{
						// attachment
						List oAttachments = oMessageHeader.getAttachments();
						List nAttachments = m_entityManager.newReferenceList();
						for (int n = 0; n < oAttachments.size(); n++)
						{
							Reference oAttachmentRef = (Reference) oAttachments.get(n);
							String oAttachmentId = ((Reference) oAttachments.get(n)).getId();
							if (oAttachmentId.indexOf(fromContext) != -1)
							{
								// replace old site id with new site id in attachments
								String nAttachmentId = oAttachmentId.replaceAll(fromContext, toContext);
								try
								{
									ContentResource attachment = ContentHostingService.getResource(nAttachmentId);
									nAttachments.add(m_entityManager.newReference(attachment.getReference()));
								}
								catch (IdUnusedException e)
								{
									try
									{
										ContentResource oAttachment = ContentHostingService.getResource(oAttachmentId);
										try
										{
											if (ContentHostingService.isAttachmentResource(nAttachmentId))
											{
												// add the new resource into attachment collection area
												ContentResource attachment = ContentHostingService.addAttachmentResource(
														Validator.escapeResourceName(oAttachment.getProperties().getProperty(
																ResourceProperties.PROP_DISPLAY_NAME)), toContext,
														ToolManager.getTool("sakai.announcements").getTitle(), oAttachment.getContentType(),
														oAttachment.getContent(), oAttachment.getProperties());

												// harvest any embedded references into resources
												XrefHelper.harvestTranslateResource(attachment, toContext, "Announcements");

												// add to attachment list
												nAttachments.add(m_entityManager.newReference(attachment.getReference()));
											}
											else
											{
												// add the new resource into resource area
												ContentResource attachment = ContentHostingService.addResource(
														Validator.escapeResourceName(oAttachment.getProperties().getProperty(
																ResourceProperties.PROP_DISPLAY_NAME)), toContext, 1, oAttachment.getContentType(),
														oAttachment.getContent(), oAttachment.getProperties(), NotificationService.NOTI_NONE);

												// harvest any embedded references into resources
												XrefHelper.harvestTranslateResource(attachment, toContext, "Announcements");

												// add to attachment list
												nAttachments.add(m_entityManager.newReference(attachment.getReference()));
											}
										}
										catch (Exception eeAny)
										{
											// if the new resource cannot be added
											M_log.warn(" cannot add new attachment with id=" + nAttachmentId);
										}
									}
									catch (Exception eAny)
									{
										// if cannot find the original attachment, do nothing.
										M_log.warn(" cannot find the original attachment with id=" + oAttachmentId);
									}
								}
								catch (Exception any)
								{
									M_log.info(any.getMessage());
								}
							}
							else
							{
								nAttachments.add(oAttachmentRef);
							}
						}
						nMessageHeader.replaceAttachments(nAttachments);

						// complete the edit
						nChannel.commitMessage(nMessage);
					}
				}
			}
		}
		catch (IdUnusedException e)
		{
			M_log.warn(" MessageChannel " + fromContext + " cannot be found. ");
		}
		catch (Exception any)
		{
			M_log.warn(".importResources(): exception in handling " + serviceName() + " : ", any);
		}
	}

	/*
	 * @inherited
	 * This method is called for synoptic view of announcements.
	 * It adds filtering of grouped message for a user.
	 */
	public List getMessages(String channelRef, Time afterDate, int limitedToLatest, boolean ascending, boolean includeDrafts,
			boolean pubViewOnly) throws PermissionException
	{
		List msgs = super.getMessages(channelRef, afterDate, limitedToLatest, ascending, includeDrafts, pubViewOnly);

		List filtered = new Vector();

		// check for the allowed groups of the current end use if we need it, and only once
		Collection allowedGroups = null;

		for (int i = 0; i < msgs.size(); i++)
		{
			Message msg = (Message) msgs.get(i);

			if (msg.getHeader().getAccess() == MessageHeader.MessageAccess.GROUPED)
			{
				// get allowed group refs of the message
				Collection msgGroups = msg.getHeader().getGroups();

				// get user's allowed groups
				if (allowedGroups == null)
				{
					BaseAnnouncementChannelEdit ba = new BaseAnnouncementChannelEdit(channelRef);
					allowedGroups = ba.getGroupsAllowGetMessage();
				}

				// if user's group is allowed then add it
				boolean allowed = false;
				for (Iterator iRefs = msgGroups.iterator(); iRefs.hasNext();)
				{
					String findThisGroupRef = (String) iRefs.next();
					for (Iterator iGroups = allowedGroups.iterator(); iGroups.hasNext();)
					{
						String thisGroupRef = ((Group) iGroups.next()).getReference();
						if (thisGroupRef.equals(findThisGroupRef))
						{
							allowed = true;
							break;
						}
					}
				}
				if(!allowed)	continue;
			}
			boolean archived = false;
			String archivedStr = msg.getProperties().getProperty("archived");
			if (archivedStr != null && archivedStr.length() != 0 && archivedStr.equals("1")) archived = true;
			if (!archived) filtered.add(msg);
		}
		return filtered;
	}

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AnnouncementChannel implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseAnnouncementChannelEdit extends BaseMessageChannelEdit implements AnnouncementChannelEdit
	{
		/**
		 * Construct with a reference.
		 *
		 * @param ref
		 *        The channel reference.
		 */
		public BaseAnnouncementChannelEdit(String ref)
		{
			super(ref);

		} // BaseAnnouncementChannelEdit

		/**
		 * Construct as a copy of another message.
		 *
		 * @param other
		 *        The other message to copy.
		 */
		public BaseAnnouncementChannelEdit(MessageChannel other)
		{
			super(other);

		} // BaseAnnouncementChannelEdit

		/**
		 * Construct from a channel (and possibly messages) already defined in XML in a DOM tree. The Channel is added to storage.
		 *
		 * @param el
		 *        The XML DOM element defining the channel.
		 */
		public BaseAnnouncementChannelEdit(Element el)
		{
			super(el);

		} // BaseAnnouncementChannelEdit

		/**
		 * Return a specific announcement channel message, as specified by message name.
		 *
		 * @param messageId
		 *        The id of the message to get.
		 * @return the AnnouncementMessage that has the specified id.
		 * @exception IdUnusedException
		 *            If this name is not a defined message in this announcement channel.
		 * @exception PermissionException
		 *            If the user does not have any permissions to read the message.
		 */
		public AnnouncementMessage getAnnouncementMessage(String messageId) throws IdUnusedException, PermissionException
		{
			AnnouncementMessage msg = (AnnouncementMessage) getMessage(messageId);

			// filter out drafts not by this user (unless this user is a super user or has access_draft ability)
			if ((msg.getAnnouncementHeader()).getDraft() && (!SecurityService.isSuperUser())
					&& (!msg.getHeader().getFrom().getId().equals(SessionManager.getCurrentSessionUserId()))
					&& (!unlockCheck(SECURE_READ_DRAFT, msg.getReference())))
			{
				throw new PermissionException(SessionManager.getCurrentSessionUserId(), SECURE_READ, msg.getReference());
			}

			return msg;

		} // getAnnouncementMessage

		/**
		 * Return a list of all or filtered messages in the channel. The order in which the messages will be found in the iteration is by date, oldest
		 * first if ascending is true, newest first if ascending is false. Does not return archived messages.
		 *
		 * @param filter
		 *        A filtering object to accept messages, or null if no filtering is desired.
		 * @param ascending
		 *        Order of messages, ascending if true, descending if false
		 * @return a list on channel Message objects or specializations of Message objects (may be empty).
		 * @exception PermissionException
		 *            if the user does not have read permission to the channel.
		 */
		public List getMessages(Filter filter, boolean ascending) throws PermissionException
		{
			// filter out drafts this user cannot see
			filter = new PrivacyFilter(filter);

			List msgs = super.getMessages(filter, ascending);
			if (msgs == null) return msgs;
			List filtered = new Vector();
			
			for (int i = 0; i < msgs.size(); i++)
			{
				Message msg = (Message) msgs.get(i);
				boolean archived = false;
				String archivedStr = msg.getProperties().getProperty("archived");
				if (archivedStr != null && archivedStr.length() != 0 && archivedStr.equals("1")) archived = true;
				if (!archived) filtered.add(msg);
			}
			return filtered;

		} // getMessages
		
		/**
		 * Return a list of all or filtered messages in the channel. The order in which the messages will be found in the iteration is by date, oldest
		 * first if ascending is true, newest first if ascending is false. Also returns archived messages.
		 *
		 * @param filter
		 *        A filtering object to accept messages, or null if no filtering is desired.
		 * @param ascending
		 *        Order of messages, ascending if true, descending if false
		 * @return a list on channel Message objects or specializations of Message objects (may be empty).
		 * @exception PermissionException
		 *            if the user does not have read permission to the channel.
		 */
		public List getAllMessages(Filter filter, boolean ascending) throws PermissionException
		{
			// filter out drafts this user cannot see
			filter = new PrivacyFilter(filter);
			
			return super.getMessages(filter, ascending);

		} // getMessages

		/**
		 * Return a list of all or filtered archived messages in the channel. The order in which the messages will be found in the iteration is by date, oldest
		 * first if ascending is true, newest first if ascending is false. 
		 *
		 * @param filter
		 *        A filtering object to accept messages, or null if no filtering is desired.
		 * @param ascending
		 *        Order of messages, ascending if true, descending if false
		 * @return a list on channel Message objects or specializations of Message objects (may be empty).
		 * @exception PermissionException
		 *            if the user does not have read permission to the channel.
		 */
		public List getArchivedMessages(Filter filter, boolean ascending) throws PermissionException
		{
			// filter out drafts this user cannot see
			filter = new PrivacyFilter(filter);
			
			List msgs = super.getMessages(filter, ascending);
			if (msgs == null) return msgs;
			List filtered = new Vector();
			
			for (int i = 0; i < msgs.size(); i++)
			{
				Message msg = (Message) msgs.get(i);
				boolean archived = false;
				String archivedStr = msg.getProperties().getProperty("archived");
				if (archivedStr != null && archivedStr.length() != 0 && archivedStr.equals("1")) archived = true;
				if (archived) filtered.add(msg);
			}
			return filtered;

		} // getArchivedMessages		

		/**
		 * A (AnnouncementMessageEdit) cover for editMessage. Return a specific channel message, as specified by message name, locked for update. Must
		 * commitEdit() to make official, or cancelEdit() when done!
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
		public AnnouncementMessageEdit editAnnouncementMessage(String messageId) throws IdUnusedException, PermissionException, InUseException
		{
			return (AnnouncementMessageEdit) editMessage(messageId);

		} // editAnnouncementMessage

		/**
		 * A (AnnouncementMessageEdit) cover for addMessage. Add a new message to this channel. Must commitEdit() to make official, or cancelEdit()
		 * when done!
		 *
		 * @return The newly added message, locked for update.
		 * @exception PermissionException
		 *            If the user does not have write permission to the channel.
		 */
		public AnnouncementMessageEdit addAnnouncementMessage() throws PermissionException
		{
			return (AnnouncementMessageEdit) addMessage();

		} // addAnnouncementMessage

		/**
		 * a (AnnouncementMessage) cover for addMessage to add a new message to this channel.
		 *
		 * @param subject
		 *        The message header subject.
		 * @param draft
		 *        The message header draft indication.
		 * @param attachments
		 *        The message header attachments, a vector of Reference objects.
		 * @param body
		 *        The message body.
		 * @return The newly added message.
		 * @exception PermissionException
		 *            If the user does not have write permission to the channel.
		 */
		public AnnouncementMessage addAnnouncementMessage(String subject, boolean draft, List attachments, String body) throws PermissionException
		{
			AnnouncementMessageEdit edit = (AnnouncementMessageEdit) addMessage();
			AnnouncementMessageHeaderEdit header = edit.getAnnouncementHeaderEdit();
			edit.setBody(body);
			header.replaceAttachments(attachments);
			header.setSubject(subject);
			header.setDraft(draft);

			commitMessage(edit);

			return edit;

		} // addAnnouncementMessage

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("unchecked")
		public boolean containsMatchingMessage(AnnouncementMessage message)
		{
			try
			{
				List<AnnouncementMessage> messages = this.getMessages(null, true);
				for (AnnouncementMessage m : messages)
				{
					if (m.getId().equals(message.getId())) continue;

					// consider it a match if the subject, body and groups match
					if (different(m.getAnnouncementHeader().getSubject(), message.getAnnouncementHeader().getSubject())) continue;
					if (different(m.getBody(), message.getBody())) continue;

					// groups
					Collection<String> eGroups = m.getHeader().getGroups();
					Collection<String> eventGroups = message.getHeader().getGroups();
					Set<String> eSet = new HashSet<String>();
					eSet.addAll(eGroups);
					Set<String> eventSet = new HashSet<String>();
					eventSet.addAll(eventGroups);
					if (!eSet.equals(eventSet)) continue;

					// found a close enough match
					return true;
				}
			}
			catch (PermissionException e)
			{
			}
			return false;
		}

		/**
		 * Compare two objects for differences, either may be null
		 * 
		 * @param a
		 *        One object.
		 * @param b
		 *        The other object.
		 * @return true if the object are different, false if they are the same.
		 */
		protected boolean different(Object a, Object b)
		{
			// if both null, they are the same
			if ((a == null) && (b == null)) return false;

			// if either are null (they both are not), they are different
			if ((a == null) || (b == null)) return true;

			// now we know neither are null, so compare
			return (!a.equals(b));
		}

	} // class BaseAnnouncementChannelEdit

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AnnouncementMessage implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseAnnouncementMessageEdit extends BaseMessageEdit implements AnnouncementMessageEdit
	{
		/**
		 * Construct.
		 *
		 * @param channel
		 *        The channel in which this message lives.
		 * @param id
		 *        The message id.
		 */
		public BaseAnnouncementMessageEdit(MessageChannel channel, String id)
		{
			super(channel, id);

		} // BaseAnnouncementMessageEdit

		/**
		 * Construct as a copy of another message.
		 *
		 * @param other
		 *        The other message to copy.
		 */
		public BaseAnnouncementMessageEdit(MessageChannel channel, Message other)
		{
			super(channel, other);

		} // BaseAnnouncementMessageEdit

		/**
		 * Construct from an existing definition, in xml.
		 *
		 * @param channel
		 *        The channel in which this message lives.
		 * @param el
		 *        The message in XML in a DOM element.
		 */
		public BaseAnnouncementMessageEdit(MessageChannel channel, Element el)
		{
			super(channel, el);

		} // BaseAnnouncementMessageEdit

		/**
		 * Access the announcement message header.
		 *
		 * @return The announcement message header.
		 */
		public AnnouncementMessageHeader getAnnouncementHeader()
		{
			return (AnnouncementMessageHeader) getHeader();

		} // getAnnouncementHeader

		/**
		 * Access the announcement message header.
		 *
		 * @return The announcement message header.
		 */
		public AnnouncementMessageHeaderEdit getAnnouncementHeaderEdit()
		{
			return (AnnouncementMessageHeaderEdit) getHeader();

		} // getAnnouncementHeaderEdit

		/**
		 * Access the body text, as a string.
		 * 
		 * @return The body text, as a string.
		 */
		public String getBody()
		{
			// clean first
			String clean = HtmlHelper.clean((m_body == null) ? "" : m_body);
			return clean;
		}

		/**
		 * @return true if the announcement is released, false if it has a release date in the future.
		 */
		public boolean isReleased()
		{
			try
			{
				Time releaseDate = getProperties().getTimeProperty(AnnouncementService.RELEASE_DATE);
				final Time now = TimeService.newTime();
				if (now.before(releaseDate))
				{
					return false;
				}
			}
			catch (Exception e)
			{
				// Just not using/set Release Date
			}

			return true;
		}

	} // class BasicAnnouncementMessageEdit

	/**********************************************************************************************************************************************************************************************************************************************************
	 * AnnouncementMessageHeaderEdit implementation
	 *********************************************************************************************************************************************************************************************************************************************************/

	public class BaseAnnouncementMessageHeaderEdit extends BaseMessageHeaderEdit implements AnnouncementMessageHeaderEdit
	{
		/** The subject for the announcement. */
		protected String m_subject = null;

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
		public BaseAnnouncementMessageHeaderEdit(Message msg, String id)
		{
			super(msg, id);

		} // BaseAnnouncementMessageHeaderEdit

		/**
		 * Construct, from an already existing XML DOM element.
		 *
		 * @param el
		 *        The header in XML in a DOM element.
		 */
		public BaseAnnouncementMessageHeaderEdit(Message msg, Element el)
		{
			super(msg, el);

			// extract the subject
			m_subject = el.getAttribute("subject");

		} // BaseAnnouncementMessageHeaderEdit

		/**
		 * Construct as a copy of another header.
		 *
		 * @param other
		 *        The other message header to copy.
		 */
		public BaseAnnouncementMessageHeaderEdit(Message msg, MessageHeader other)
		{
			super(msg, other);

			m_subject = ((AnnouncementMessageHeader) other).getSubject();

		} // BaseAnnouncementMessageHeaderEdit

		/**
		 * Access the subject of the announcement.
		 *
		 * @return The subject of the announcement.
		 */
		public String getSubject()
		{
			return ((m_subject == null) ? "" : m_subject);

		} // getSubject

		/**
		 * Set the subject of the announcement.
		 *
		 * @param subject
		 *        The subject of the announcement.
		 */
		public void setSubject(String subject)
		{
			if (StringUtil.different(subject, m_subject))
			{
				m_subject = subject;
			}

		} // setSubject

		/**
		 * Serialize the resource into XML, adding an element to the doc under the top of the stack element.
		 *
		 * @param doc
		 *        The DOM doc to contain the XML (or null for a string return).
		 * @param stack
		 *        The DOM elements, the top of which is the containing element of the new "resource" element.
		 * @return The newly added element.
		 */
		public Element toXml(Document doc, Stack stack)
		{
			// get the basic work done
			Element header = super.toXml(doc, stack);

			// add draft, subject
			header.setAttribute("subject", getSubject());
			header.setAttribute("draft", new Boolean(getDraft()).toString());

			return header;

		} // toXml

	} // BaseAnnouncementMessageHeader

	/**
	 * A filter that will reject announcement message drafts not from the current user, and otherwise use another filter, if defined, for acceptance.
	 */
	protected class PrivacyFilter implements Filter
	{
		/** The other filter to check with. May be null. */
		protected Filter m_filter = null;

		/**
		 * Construct
		 *
		 * @param filter
		 *        The other filter we check with.
		 */
		public PrivacyFilter(Filter filter)
		{
			m_filter = filter;

		} // PrivacyFilter

		/**
		 * Does this object satisfy the criteria of the filter?
		 *
		 * @return true if the object is accepted by the filter, false if not.
		 */
		public boolean accept(Object o)
		{
			// first if o is a announcement message that's a draft from another user, reject it
			if (o instanceof AnnouncementMessage)
			{
				AnnouncementMessage msg = (AnnouncementMessage) o;

				if ((msg.getAnnouncementHeader()).getDraft() && (!SecurityService.isSuperUser())
						&& (!msg.getHeader().getFrom().getId().equals(SessionManager.getCurrentSessionUserId()))
						&& (!unlockCheck(SECURE_READ_DRAFT, msg.getReference())))
				{
					return false;
				}
			}

			// now, use the real filter, if present
			if (m_filter != null) return m_filter.accept(o);

			return true;

		} // accept

	} // PrivacyFilter

	/**
	 * Get the messages in the channel that are visible to "students", ordered descending by release date + message order, limited to the number of messages.
	 * 
	 * @param channelRef
	 *        The announcement channel reference string
	 * @param numMessages
	 *        The number of messages to include; includes all if =0
	 * @return A List of announcement messages.
	 */
	@SuppressWarnings("unchecked")
	public List<AnnouncementMessage> getRecentMessages(String channelRef, int numMessages) throws PermissionException
	{
		List<AnnouncementMessage> rv = new ArrayList<AnnouncementMessage>();

		// get all the messages, so we can apply the proper release date / order sort
		rv.addAll((List<AnnouncementMessage>) getMessages(channelRef, null, 0, true, false, false));

		// remove those not released
		List<AnnouncementMessage> released = new ArrayList<AnnouncementMessage>();
		for (AnnouncementMessage m : rv)
		{
			if (m.isReleased()) released.add(m);
		}
		rv = released;

		// sort
		Collections.sort(rv, new Comparator<AnnouncementMessage>()
		{
			public int compare(AnnouncementMessage arg0, AnnouncementMessage arg1)
			{
				int c = 0;

				// first, see if there's a manual order defined and different (lower sorts to the top of the list - missing position is treated as 0)
				Long pos0 = 0l;
				try
				{
					pos0 = arg0.getProperties().getLongProperty("position");
				}
				catch (EntityPropertyNotDefinedException e1)
				{
				}
				catch (EntityPropertyTypeException e1)
				{
				}

				Long pos1 = 0l;
				try
				{
					pos1 = arg1.getProperties().getLongProperty("position");
				}
				catch (EntityPropertyNotDefinedException e1)
				{
				}
				catch (EntityPropertyTypeException e1)
				{
				}
				c = pos0.compareTo(pos1);
				if (c != 0) return c;

				// next try release date (larger sorts to the top of the list)
				Time t0 = null;
				try
				{
					t0 = arg0.getProperties().getTimeProperty(RELEASE_DATE);
				}
				catch (EntityPropertyTypeException e)
				{
				}
				catch (EntityPropertyNotDefinedException e)
				{
				}
				if (t0 == null)
				{
					// no release date! use something else
					t0 = arg0.getHeader().getDate();
				}

				Time t1 = null;
				try
				{
					t1 = arg1.getProperties().getTimeProperty(RELEASE_DATE);
				}
				catch (EntityPropertyTypeException e)
				{
				}
				catch (EntityPropertyNotDefinedException e)
				{
				}
				if (t1 == null)
				{
					// no release date! use something else
					t1 = arg1.getHeader().getDate();					
				}

				c = t0.compareTo(t1);
				if (c != 0) return c * -1;

				// by subject
				c = arg0.getAnnouncementHeader().getSubject().compareTo(arg1.getAnnouncementHeader().getSubject());

				return c;
			}
		});

		// limit
		if ((numMessages > 0) && (rv.size() > numMessages))
		{
			rv = new ArrayList<AnnouncementMessage>(rv.subList(0, numMessages));
		}

		return rv;
	}
}
