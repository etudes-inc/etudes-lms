/**********************************************************************************
 * $URL: https://source.etudes.org/svn/etudes/source/trunk/sections/sections-app/src/java/org/sakaiproject/tool/section/jsf/backingbean/PreferencesBean.java $
 * $Id: PreferencesBean.java 3 2008-10-20 18:44:42Z ggolden $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Regents of the University of California and The Regents of the University of Michigan
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
package org.sakaiproject.tool.section.jsf.backingbean;

import org.sakaiproject.api.section.facade.manager.ResourceLoader;

/**
 * Stores user preferences for table sorting and paging.  These preferences are
 * currently implemented in session-scope, though this could be reimplemented
 * to store preferences across sessions.
 * 
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 *
 */
public class PreferencesBean extends CourseDependentBean {
	
	private static final long serialVersionUID = 1L;

	private ResourceLoader resourceLoader;
	
	public PreferencesBean() {
		overviewSortColumn = "title";
		overviewSortAscending = true;

		rosterSortColumn = "studentName";
		rosterSortAscending = true;
		rosterMaxDisplayedRows = 10;
	}

	public void init() {
		// Get the max name length for displaying names from the app's properties file.
		// We can't do this in the constructor, since we need to wait for our dependencies.
        maxNameLength = Integer.parseInt(resourceLoader.getString("max_name_length"));
	}
	
	protected int maxNameLength;
	protected String overviewSortColumn;
	protected boolean overviewSortAscending;
	
	protected String rosterSortColumn;
	protected boolean rosterSortAscending;
	protected int rosterMaxDisplayedRows;

	public boolean isOverviewSortAscending() {
		return overviewSortAscending;
	}
	public void setOverviewSortAscending(boolean overviewSortAscending) {
		this.overviewSortAscending = overviewSortAscending;
	}
	public String getOverviewSortColumn() {
		return overviewSortColumn;
	}
	public void setOverviewSortColumn(String overviewSortColumn) {
		this.overviewSortColumn = overviewSortColumn;
	}
	public int getRosterMaxDisplayedRows() {
		return rosterMaxDisplayedRows;
	}
	public void setRosterMaxDisplayedRows(int rosterMaxDisplayedRows) {
		this.rosterMaxDisplayedRows = rosterMaxDisplayedRows;
	}
	public boolean isRosterSortAscending() {
		return rosterSortAscending;
	}
	public void setRosterSortAscending(boolean rosterSortAscending) {
		this.rosterSortAscending = rosterSortAscending;
	}
	public String getRosterSortColumn() {
		return rosterSortColumn;
	}
	public void setRosterSortColumn(String rosterSortColumn) {
		this.rosterSortColumn = rosterSortColumn;
	}
	public int getMaxNameLength() {
		return maxNameLength;
	}
	
	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	// Dependency injection
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
		
		// TODO This is a hack... fix it
		init();
	}
}
