/**********************************************************************************
*
* $Id: GradebookDependentBean.java 10884 2015-05-19 16:31:19Z mallikamt $
*
***********************************************************************************
*
* Copyright (c) 2005, 2015 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.section.SectionAwareness;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.UserDirectoryService;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

public abstract class GradebookDependentBean extends InitializableBean {
	private static final Log logger = LogFactory.getLog(GradebookDependentBean.class);

	private String pageName;

	/**
	 * Marked transient to allow serializable subclasses.
	 */
	private transient GradebookBean gradebookBean;
    private transient PreferencesBean preferencesBean;
    
	class ItemPointsValue implements Comparable<ItemPointsValue>
	{
		Long id;
		Double pointsVal;
		Double pointsForDisplay;
		
		ItemPointsValue(Long id, Double pointsVal, Double pointsForDisplay)
		{
			this.id = id;
			this.pointsVal = pointsVal;
			this.pointsForDisplay = pointsForDisplay;
		}

		public int compareTo(ItemPointsValue n)
		{
			//If points earned/item points is equal, compare item points
			//Item with higher item points is ordered to first position
			if (this.pointsVal.compareTo(n.pointsVal) == 0)
			{
				return n.pointsForDisplay.compareTo(this.pointsForDisplay);
			}
			return this.pointsVal.compareTo(n.pointsVal);
		}

		public Long getId()
		{
			return this.id;
		}
		
		public Double getPointsVal()
		{
			return this.pointsVal;
		}
		
		public Double getPointsForDisplay()
		{
			return this.pointsForDisplay;
		}

	}

	/**
     * Iterates through a map, creates a list with points/score to drop the lowest points/score ratio
     * item. Flags the appropriate item
     * @param scoresMap
     */
    protected void flagDroppedEntries(Map scoresMap)
	{
    	Long cgDropId = 0L;
    	AbstractGradeRecord dropRec, cgRec;
		if ((scoresMap == null) || (scoresMap.size() == 0)) return;

		Iterator it = scoresMap.entrySet().iterator();
		while (it.hasNext())
		{
			Map.Entry pairs = (Map.Entry) it.next();
			Map studentMap = (Map) pairs.getValue();
			if (studentMap != null && studentMap.size() > 0)
			{
				Iterator subIt = studentMap.entrySet().iterator();
				ArrayList<ItemPointsValue> pointsValueList = new ArrayList<ItemPointsValue>();
				boolean nonZeroEntry = false;

				while (subIt.hasNext())
				{
					Map.Entry subPairs = (Map.Entry) subIt.next();
					Long gradableObjectId = (Long) subPairs.getKey();
					AbstractGradeRecord agr = (AbstractGradeRecord) subPairs.getValue();
					if (agr.getPointsEarned() != null && agr.getPointsEarned().doubleValue() > 0.0) nonZeroEntry = true;
					if (agr.isCourseGradeRecord()) cgDropId = gradableObjectId;
					if (!agr.isCourseGradeRecord() && agr.getPointsEarned() != null && agr.getGradableObject().getPointsForDisplay() != null)
						pointsValueList.add(new ItemPointsValue(gradableObjectId, agr.getPointsEarned()
								/ agr.getGradableObject().getPointsForDisplay(), agr.getGradableObject().getPointsForDisplay()));
				}

				if (pointsValueList != null && pointsValueList.size() > 1 && nonZeroEntry)
				{
					Collections.sort(pointsValueList);
					Long dropId = pointsValueList.get(0).getId();
					dropRec = (AbstractGradeRecord) studentMap.get(dropId);
					if (dropRec != null)
					{
						dropRec.setDroppedEntry(true);
						cgRec = (AbstractGradeRecord) studentMap.get(cgDropId);
						if (cgRec != null)
						{
							cgRec.setPointsEarned(cgRec.getPointsEarned().doubleValue() - dropRec.getPointsEarned().doubleValue());
						}
					}
				}
			}
		}
	}
    
    /**
     * Iterates through a list, creates a list with points/score to drop the lowest points/score ratio
     * item. Flags the appropriate item
     * @param scoresList
     * @return list whose lowest grade is flagged
     */
    protected List flagDroppedEntries(List scoresList)
    {
    	if (scoresList == null || scoresList.size() == 0) return null;
    	Iterator subIt = scoresList.iterator();
		ArrayList<ItemPointsValue> pointsValueList = new ArrayList<ItemPointsValue>();
		List newScoreList = new ArrayList();
		boolean nonZeroEntry = false;
		
		while (subIt.hasNext())
		{
			AbstractGradeRecord agr = (AbstractGradeRecord) subIt.next();
			Long gradableObjectId = agr.getGradableObject().getId();
			if (agr.getPointsEarned() != null && agr.getPointsEarned().doubleValue() > 0.0) nonZeroEntry = true;
			if (!agr.isCourseGradeRecord() && agr.getPointsEarned() != null && agr.getGradableObject().getPointsForDisplay() != null)
				pointsValueList.add(new ItemPointsValue(gradableObjectId, agr.getPointsEarned()
						/ agr.getGradableObject().getPointsForDisplay(), agr.getGradableObject().getPointsForDisplay()));
		}

		if (pointsValueList != null && pointsValueList.size() > 1  && nonZeroEntry)
		{
			Collections.sort(pointsValueList);
			Long dropId = pointsValueList.get(0).getId();
			Iterator newIt = scoresList.iterator();
			while (newIt.hasNext())
			{
				AbstractGradeRecord agr = (AbstractGradeRecord) newIt.next();
				if (agr.getGradableObject().getId().equals(dropId)) 
				{
					agr.setDroppedEntry(true);
				}
				else
				{
					agr.setDroppedEntry(false);
				}
				newScoreList.add(agr);
			}
		}
		return newScoreList;
    }
    
	/**
	 * Convenience method, for use in calling locally implemented services
	 * that assume the gradebook ID is an integer.
	 */
	Long getGradebookId() {
		return getGradebookBean().getGradebookId();
	}

	/**
	 * Convenience method, for use in calling external facades
	 * that assume the gradebook ID is an string.
	 */
	String getGradebookUid() {
		return getGradebookManager().getGradebookUid(getGradebookId());
	}

	/**
	 * Convenience method to hide the Authn context object.
	 */
	public String getUserUid() {
		return getAuthnService().getUserUid();
	}

	/**
	 * Convenience method to load the current gradebook object.
	 */
	Gradebook getGradebook() {
		return getGradebookManager().getGradebook(getGradebookId());
	}

    /**
     * Gets a localized message string based on the locale determined by the
     * FacesContext.  Useful for adding localized FacesMessages from a backing bean.
     *
     * TODO Replace with direct calls to FacesUtil.
     *
     * @param key The key to look up the localized string
     */
    public String getLocalizedString(String key) {
    	return FacesUtil.getLocalizedString(key);
    }

    /**
     * Gets a localized message string based on the locale determined by the
     * FacesContext.  Useful for adding localized FacesMessages from a backing bean.
     *
     * TODO Replace with direct calls to FacesUtil.
     *
     * @param key The key to look up the localized string
     * @param params The array of strings to use in replacing the placeholders
     * in the localized string
     */
    public String getLocalizedString(String key, String[] params) {
    	return FacesUtil.getLocalizedString(key, params);
    }

    // Still more convenience methods, hiding the bean configuration details.

	public GradebookManager getGradebookManager() {
		return getGradebookBean().getGradebookManager();
	}

	public SectionAwareness getSectionAwareness() {
		return getGradebookBean().getSectionAwareness();
	}

	public UserDirectoryService getUserDirectoryService() {
		return getGradebookBean().getUserDirectoryService();
	}

	public Authn getAuthnService() {
		return getGradebookBean().getAuthnService();
	}

	public boolean isUserAbleToEditAssessments() {
		return getGradebookBean().getAuthzService().isUserAbleToEditAssessments(getGradebookUid());
	}
	public boolean isUserAbleToGradeAll() {
		return getGradebookBean().getAuthzService().isUserAbleToGradeAll(getGradebookUid());
	}
	public boolean isUserAbleToGradeSection(String sectionUid) {
		return getGradebookBean().getAuthzService().isUserAbleToGradeSection(sectionUid);
	}

	public List getAvailableEnrollments() {
		return getGradebookBean().getAuthzService().getAvailableEnrollments(getGradebookUid());
	}

	public List getAvailableSections() {
		return getGradebookBean().getAuthzService().getAvailableSections(getGradebookUid());
	}

	public List getSectionEnrollments(String sectionUid) {
		return getGradebookBean().getAuthzService().getSectionEnrollments(getGradebookUid(), sectionUid);
	}

	public List findMatchingEnrollments(String searchString, String optionalSectionUid) {
		return getGradebookBean().getAuthzService().findMatchingEnrollments(getGradebookUid(), searchString, optionalSectionUid);
	}

	/**
	 * Get the gradebook context.
	 */
	public GradebookBean getGradebookBean() {
		if (gradebookBean == null) {
			// This probably happened because gradebookBean is transient.
			// Just restore it from the session context.
			setGradebookBean((GradebookBean)FacesUtil.resolveVariable("gradebookBean"));
		}
		return gradebookBean;
	}

	/**
	 * Set the gradebook context.
	 */
	public void setGradebookBean(GradebookBean gradebookBean) {
		this.gradebookBean = gradebookBean;
	}

    /**
     * @return Returns the preferencesBean.
     */
    public PreferencesBean getPreferencesBean() {
        if (preferencesBean == null) {
            setPreferencesBean((PreferencesBean)FacesUtil.resolveVariable("preferencesBean"));
        }
        return preferencesBean;
    }
    /**
     * @param preferencesBean The preferencesBean to set.
     */
    public void setPreferencesBean(PreferencesBean preferencesBean) {
        this.preferencesBean = preferencesBean;
    }

    /**
     * Set up close relations with page and action names for easier control
     * of menus.
     */
    public String getPageName() {
    	return pageName;
    }
    public void setPageName(String pageName) {
    	this.pageName = pageName;
    }
   
}
