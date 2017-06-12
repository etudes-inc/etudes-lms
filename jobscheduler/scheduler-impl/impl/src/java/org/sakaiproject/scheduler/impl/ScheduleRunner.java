/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/portal/portal-impl/impl/src/java/org/sakaiproject/portal/charon/CharonPortal.java $
 * $Id: CharonPortal.java 720 2010-09-13 19:08:47Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2010, 2011 ETUDES, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.scheduler.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.scheduler.api.DelayedInvocation;
import org.sakaiproject.scheduler.api.ScheduledInvocationCommand;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeService;

public class ScheduleRunner implements Runnable
{

	private static final Log LOG = LogFactory.getLog(ScheduleRunner.class);
	/** The checker thread. */
	protected Thread checkerThread = null;

	/** The thread quit flag. */
	protected boolean threadStop = false;

	/** How long to wait (ms) between checks for timed-out submission in the db. 0 disables. */
	protected long timeoutCheckMs = 1000L * 900L;
	
	/**
	 * Final initialization, once all dependencies are set.
	 */
	public void init(int timeoutSeconds)
	{
		timeoutCheckMs = 1000L * timeoutSeconds;
		// Checking to see if we run scheduler on this server
		// read the server's id
		String id = ServerConfigurationService.getServerId();
		if (id != null)
		{
			// read configuration for this server
			String schServer = ServerConfigurationService.getString("schedule.server");
			if ((schServer != null) && (schServer.trim().length() > 0))
			{
				if (id.equals(schServer.trim()))
				{
					try
					{
						// start the checking thread
						if (timeoutCheckMs > 0)
						{
							start();
						}

						LOG.info("init(): timout check seconds: " + timeoutCheckMs / 1000);
					}
					catch (Throwable t)
					{
						LOG.warn("init(): ", t);
					}
				}
			}
		}

	}

	private class Invocation
	{
		public ScheduledInvocationCommand command;
		public String context;
		public Date when;
	}

	/**
	 * Run the expiration checking thread.
	 */
	public void run()
	{
		// since we might be running while the component manager is still being created and populated,
		// such as at server startup, wait here for a complete component manager
		ComponentManager.waitTillConfigured();

		// loop till told to stop
		while ((!threadStop) && (!Thread.currentThread().isInterrupted()))
		{
			try
			{
				if (LOG.isDebugEnabled()) LOG.debug("run: running");

				SqlService sqlService = ((SqlService) ComponentManager.get("org.sakaiproject.db.api.SqlService"));
				Time now = ((TimeService) ComponentManager.get("org.sakaiproject.time.api.TimeService")).newTime();

				String sql = "SELECT INVOCATION_ID, INVOCATION_TIME, COMPONENT, CONTEXT FROM ET_SCH_DELAYED_INVOCATION WHERE INVOCATION_TIME < ? ORDER BY INVOCATION_TIME ASC";

				Object[] fields = new Object[1];

				fields[0] = now;

				LOG.debug("SQL: " + sql + " NOW:" + now);
				List invocations = sqlService.dbRead(sql, fields, new DelayedInvocationReader());

				// make commands
				List<Invocation> todo = new ArrayList<Invocation>();
				for (Iterator i = invocations.iterator(); i.hasNext();)
				{
					DelayedInvocation invocation = (DelayedInvocation) i.next();

					if (invocation != null)
					{

						LOG.debug("processing invocation: [" + invocation + "]");

						try
						{
							ScheduledInvocationCommand command = (ScheduledInvocationCommand) ComponentManager.get(invocation.componentId);
							Invocation ivc = new Invocation();
							ivc.command = command;
							ivc.context = invocation.contextId;
							ivc.when = invocation.date;
							if ((ivc.command != null) && (ivc.when != null))
							{
								todo.add(ivc);
							}

							// will run in a moment
							// command.execute(invocation.contextId);
						}
						catch (Exception e)
						{
							LOG.error("Failed to setup component: [" + invocation.componentId + "]: " + e);
						}
						finally
						{

							sql = "DELETE FROM ET_SCH_DELAYED_INVOCATION WHERE INVOCATION_ID = ?";

							fields[0] = invocation.uuid;

							LOG.debug("SQL: " + sql);
							sqlService.dbWrite(sql, fields);

						}
					}
				}

				// sort
				Collections.sort(todo, new Comparator<Invocation>()
				{
					public int compare(Invocation arg0, Invocation arg1)
					{
						// first by time
						int rv = arg0.when.compareTo(arg1.when);

						// next by priority, reversed, if needed
						if (rv == 0)
						{
							rv = arg1.command.getPriority().compareTo(arg0.command.getPriority());
						}

						return rv;
					}
				});

				// run
				for (Invocation ivc : todo)
				{
					try
					{
						ivc.command.execute(ivc.context);
					}
					catch (Exception e)
					{
						LOG.error("Failed to run component: [" + ivc.command + "]: " + e);
					}
				}
			}
			catch (Throwable e)
			{
				LOG.warn("run: will continue: ", e);
			}
			finally
			{
				// clear out any current current bindings
		        ThreadLocalManager.clear();
			}

			// take a small nap
			try
			{
				Thread.sleep(timeoutCheckMs);
			}
			catch (Exception ignore)
			{
			}
		}
	}

	/**
	 * Start the clean and report thread.
	 */
	protected void start()
	{
		threadStop = false;

		checkerThread = new Thread(this, getClass().getName());
		checkerThread.start();
	}

	/**
	 * Stop the clean and report thread.
	 */
	protected void stop()
	{
		if (checkerThread == null) return;

		// signal the thread to stop
		threadStop = true;

		// wake up the thread
		checkerThread.interrupt();

		checkerThread = null;
	}

	/**
	 * Returns to uninitialized state.
	 */
	public void destroy()
	{
		// stop the checking thread
		stop();

		LOG.info("destroy()");
	}

}
