/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.scheduler.impl;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/*import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.sakaiproject.api.app.scheduler.DelayedInvocation;
import org.sakaiproject.api.app.scheduler.ScheduledInvocationManager;
import org.sakaiproject.api.app.scheduler.SchedulerManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.app.scheduler.jobs.ScheduledInvocationRunner;*/
import org.sakaiproject.scheduler.api.DbScheduleService;
import org.sakaiproject.scheduler.api.DelayedInvocation;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.time.api.Time;

public class DbScheduleServiceImpl implements DbScheduleService {

	private int timeoutSeconds;

	private static final Log LOG = LogFactory.getLog(DbScheduleServiceImpl .class);

	/** Dependency: IdManager */
	protected IdManager m_idManager = null;

	protected ScheduleRunner scRunner;

	public void setIdManager(IdManager service) {
		m_idManager = service;
	}

	/** Dependency: SqlService */
	protected SqlService m_sqlService = null;

	public void setSqlService(SqlService service) {
		m_sqlService = service;
	}


	public void init() {
		scRunner = new ScheduleRunner();
		scRunner.init(this.timeoutSeconds);
	    }


   public void destroy() {
      LOG.info("destroy()");
      scRunner.stop();
   }

  	/* (non-Javadoc)
	 * @see org.sakaiproject.scheduler.api.DbScheduleService#createDelayedInvocation(org.sakaiproject.time.api.Time, java.lang.String, java.lang.String)
	 */
	public String createDelayedInvocation(Time time, String componentId, String opaqueContext) {

		String uuid = m_idManager.createUuid();
    	String sql = "INSERT INTO ET_SCH_DELAYED_INVOCATION VALUES(?,?,?,?)";

		Object[] fields = new Object[4];

		fields[0] = uuid;
		fields[1] = time;
		fields[2] = componentId;
		fields[3] = opaqueContext;

		LOG.debug("SQL: " + sql);
		if (m_sqlService.dbWrite(sql, fields)) {
			LOG.debug("Created new Delayed Invocation: uuid=" + uuid);
			return uuid;
		} else {
			LOG.error("Failed to create new Delayed Invocation: componentId=" + componentId +
					", opaqueContext=" + opaqueContext);
			return null;
		}

	}

	private String updateDelayedInvocation(Time time, String componentId, String opaqueContext) {

		String uuid = m_idManager.createUuid();
    	LOG.debug("Updating Delayed Invocation: " + uuid);
		String sql = "UPDATE ET_SCH_DELAYED_INVOCATION SET INVOCATION_TIME=? WHERE COMPONENT = ? AND CONTEXT = ?";

		Object[] fields = new Object[3];

		fields[0] = time;
		fields[1] = componentId;
		fields[2] = opaqueContext;

		LOG.debug("SQL: " + sql);
		if (m_sqlService.dbWrite(sql, fields)) {
			LOG.debug("Updated Delayed Invocation: uuid=" + uuid);
			return uuid;
		} else {
			LOG.error("Failed to update Delayed Invocation: componentId=" + componentId +
					", opaqueContext=" + opaqueContext);
			return null;
		}

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.scheduler.api.DbScheduleService#deleteDelayedInvocation(java.lang.String)
	 */
	public void deleteDelayedInvocation(String uuid) {

		LOG.debug("Removing Delayed Invocation: " + uuid);
		String sql = "DELETE FROM ET_SCH_DELAYED_INVOCATION WHERE INVOCATION_ID = ?";

		Object[] fields = new Object[1];
		fields[0] = uuid;

		LOG.debug("SQL: " + sql);
		m_sqlService.dbWrite(sql, fields);

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.scheduler.api.DbScheduleService#deleteDelayedInvocation(java.lang.String, java.lang.String)
	 */
	public void deleteDelayedInvocation(String componentId, String opaqueContext) {
		LOG.debug("componentId=" + componentId + ", opaqueContext=" + opaqueContext);

		//String sql = "DELETE FROM ET_SCH_DELAYED_INVOCATION WHERE COMPONENT = ? AND CONTEXT = ?";
		String sql = "DELETE FROM ET_SCH_DELAYED_INVOCATION";

		Object[] fields = new Object[0];
		if (componentId.length() > 0 && opaqueContext.length() > 0) {
			// both non-blank
			sql += " WHERE COMPONENT = ? AND CONTEXT = ?";
			fields = new Object[2];
			fields[0] = componentId;
			fields[1] = opaqueContext;
		} else if (componentId.length() > 0) {
			// context blank
			sql += " WHERE COMPONENT = ?";
			fields = new Object[1];
			fields[0] = componentId;
		} else if (opaqueContext.length() > 0) {
			// component blank
			sql += " WHERE CONTEXT = ?";
			fields = new Object[1];
			fields[0] = opaqueContext;
		} else {
			// both blank
		}

		LOG.debug("SQL: " + sql);
		if ( m_sqlService.dbWrite(sql, fields) ) {
			LOG.debug("Removed all scheduled invocations matching: componentId=" + componentId + ", opaqueContext=" + opaqueContext);
		} else {
			LOG.error("Failure while attempting to remove invocations matching: componentId=" + componentId + ", opaqueContext=" + opaqueContext);
		}

	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.scheduler.api.DbScheduleService#findDelayedInvocations(java.lang.String, java.lang.String)
	 */
	public DelayedInvocation[] findDelayedInvocations(String componentId, String opaqueContext) {
		LOG.debug("componentId=" + componentId + ", opaqueContext=" + opaqueContext);

		String sql = "SELECT * FROM ET_SCH_DELAYED_INVOCATION WHERE COMPONENT = ? AND CONTEXT = ?";
		//String sql = "SELECT * FROM ET_SCH_DELAYED_INVOCATION";

		Object[] fields = new Object[0];
		if (componentId.length() > 0 && opaqueContext.length() > 0) {
			// both non-blank
			sql += " WHERE COMPONENT = ? AND CONTEXT = ?";
			fields = new Object[2];
			fields[0] = componentId;
			fields[1] = opaqueContext;
		} else if (componentId.length() > 0) {
			// context blank
			sql += " WHERE COMPONENT = ?";
			fields = new Object[1];
			fields[0] = componentId;
		} else if (opaqueContext.length() > 0) {
			// component blank
			sql += " WHERE CONTEXT = ?";
			fields = new Object[1];
			fields[0] = opaqueContext;
		} else {
			// both blank
		}

		List invocations = m_sqlService.dbRead(sql, fields, new DelayedInvocationReader());
		return (DelayedInvocation[]) invocations.toArray( new DelayedInvocation[] {} );
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.scheduler.api.DbScheduleService#findDelayedInvocation(java.lang.String)
	 */
	public DelayedInvocation findDelayedInvocation(String invocationId) {
		LOG.debug("invocationId=" + invocationId);

		String sql = "SELECT * FROM ET_SCH_DELAYED_INVOCATION WHERE INVOCATION_ID = ?";

		Object[] fields = new Object[0];
		if (invocationId.length() > 0) {
			fields = new Object[1];
			fields[0] = invocationId;

		} else
		{
			return null;
		}

		List invocations = m_sqlService.dbRead(sql, fields, new DelayedInvocationReader());
		if (invocations.size() == 0) return null;
		return (DelayedInvocation)invocations.get(0);
	}

	/**
	 * Set timeout for schedule runner
	 *
	 * @param timeoutSeconds timeout value in seconds
	 */
	public void setTimeoutSeconds(int timeoutSeconds) {
		this.timeoutSeconds = timeoutSeconds;
	}
}
