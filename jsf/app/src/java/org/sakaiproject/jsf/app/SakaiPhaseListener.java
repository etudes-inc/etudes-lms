/**********************************************************************************
*
* $Id: SakaiPhaseListener.java 3 2008-10-20 18:44:42Z ggolden $
*
***********************************************************************************
*
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

package org.sakaiproject.jsf.app;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SakaiPhaseListener implements PhaseListener {
	private static final Log logger = LogFactory.getLog(SakaiPhaseListener.class);

	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

	public void beforePhase(PhaseEvent e) {
		if (logger.isDebugEnabled()) logger.debug("BEFORE " + e.getPhaseId());
	}
	public void afterPhase(PhaseEvent e) {
		if (logger.isDebugEnabled()) logger.debug("AFTER " + e.getPhaseId());
	}
}


