/**********************************************************************************
*
* $Id: Pager.java 3 2008-10-20 18:44:42Z ggolden $
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A stopgap class to mimic the functionality of our eventual pager JSF
 * component. When such a component becomes available, we can take these
 * objects out of the backing beans, and refer to the backing bean methods
 * directly from the component tag.
 *
 * @deprecated Replaced by sakaix:pager component.
 */
public class Pager implements Serializable {
	private static final Log logger = LogFactory.getLog(Pager.class);

	private List displayedRowsSelectItems;
	private Paging pagingBean;

	public Pager() {
		if (logger.isDebugEnabled()) logger.debug("PagerBean()");
		displayedRowsSelectItems = new ArrayList();
		displayedRowsSelectItems.add(new SelectItem(new Integer(10), "Show 10"));
		displayedRowsSelectItems.add(new SelectItem(new Integer(20), "Show 20"));
		displayedRowsSelectItems.add(new SelectItem(new Integer(50), "Show 50"));
		displayedRowsSelectItems.add(new SelectItem(new Integer(0), "Show all"));
	}

	public void pagePrevious(ActionEvent event) {
		setFirstRow(Math.max(getFirstRow() - getInternalMaxDisplayedRows(), 0));
	}

	public void pageNext(ActionEvent event) {
		setFirstRow(Math.min(getFirstRow() + getInternalMaxDisplayedRows(), getDataRows() - 1));
	}

	public void pageFirst(ActionEvent event) {
		setFirstRow(0);
	}

	public void pageLast(ActionEvent event) {
		int displayed = getInternalMaxDisplayedRows();
		int lastPage = (getDataRows() - 1) / displayed;
		setFirstRow(lastPage * displayed);
	}

	public void pageChangeRange(ValueChangeEvent event) {
		if (logger.isDebugEnabled()) logger.debug("pageChangeRange");
	}

	public Paging getPagingBean() {
		return pagingBean;
	}
	public void setPagingBean(Paging pagingBean) {
		this.pagingBean = pagingBean;

		// Initialize the backing bean's paging position.
		// (The assumption here is that user preferences for the paging
		// components will be loaded outside the specific backing bean.)
		setFirstRow(0);
		setMaxDisplayedRows(new Integer(20));
	}

	public boolean isFirstPage() {
		return (getFirstRow() == 0);
	}
	public boolean isLastPage() {
		int maxDisplayedRows = getInternalMaxDisplayedRows();
		int dataRows = getDataRows();
		return ((maxDisplayedRows > dataRows) || ((getFirstRow() + maxDisplayedRows) >= dataRows));
	}

	public int getFirstRow() {
		return pagingBean.getFirstRow();
	}
	public void setFirstRow(int firstRow) {
		pagingBean.setFirstRow(firstRow);
	}

	int getInternalMaxDisplayedRows() {
		if (logger.isDebugEnabled()) logger.debug("getInternalMaxDisplayedRows " + pagingBean.getMaxDisplayedRows());
		return pagingBean.getMaxDisplayedRows();
	}

	public Integer getMaxDisplayedRows() {
		int returnInt = getInternalMaxDisplayedRows();
		if (returnInt == Integer.MAX_VALUE) {
			returnInt = 0;
		}
		if (logger.isDebugEnabled()) logger.debug("getMaxDisplayedRows " + returnInt);
		return new Integer(returnInt);
	}
	public void setMaxDisplayedRows(Integer iMaxDisplayedRows) {
		if (logger.isDebugEnabled()) logger.debug("setMaxDisplayedRows " + iMaxDisplayedRows);
		int maxDisplayedRows = getInternalMaxDisplayedRows();
		if (maxDisplayedRows != iMaxDisplayedRows.intValue()) {
			maxDisplayedRows = iMaxDisplayedRows.intValue();
			if (maxDisplayedRows == 0) {
				// "Show all" was selected.
				maxDisplayedRows = Integer.MAX_VALUE;
			}
			pagingBean.setMaxDisplayedRows(maxDisplayedRows);
			if (getFirstRow() < maxDisplayedRows) {
				setFirstRow(0);
			}
		}
	}

	public List getDisplayedRowsSelectItems() {
		return displayedRowsSelectItems;
	}
	public int getDataRows() {
		return pagingBean.getDataRows();
	}

	/**

	 * Since we don't know how the eventual paging control component
	 * will package this informational message, I'm implementing it
	 * in a very primitive way right now.
	 */
	public String getPageContext() {
		int dataRows = getDataRows();
		int lastDisplayedRow = getInternalMaxDisplayedRows();
		if (lastDisplayedRow != Integer.MAX_VALUE) {
			lastDisplayedRow += getFirstRow();
		}
		if (lastDisplayedRow > dataRows) {
			lastDisplayedRow = dataRows;
		}
		return "Viewing " + (getFirstRow() + 1) + " - " + lastDisplayedRow + " of " + dataRows + " students";
	}
}



