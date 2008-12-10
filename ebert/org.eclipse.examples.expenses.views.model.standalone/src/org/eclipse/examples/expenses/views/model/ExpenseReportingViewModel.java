/*******************************************************************************
 * Copyright (c) 2008 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.examples.expenses.views.model;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.examples.expenses.core.LineItem;

public class ExpenseReportingViewModel {

	/**
	 * This field references a {@link ListenerList} which holds references to a
	 * collection of {@link ExpenseReportingUIModelListener} instances that are
	 * to be informed of changes to the current {@link ExpensesBinder},
	 * {@link ExpenseReport}, and {@link LineItem} as the notion of
	 * &quot;current&quot; is understood by this instance. Typically, an
	 * instance of this class keeps track of the &quot;current&quot; objects for
	 * an individual user.
	 * <p>
	 * Most places that this class is used, it is lazy-initialized. To keep
	 * things simple&mdash;and owing to the fact that if we're actually using
	 * this class, we're most likely adding listeners&mdash;we'll just
	 * initialize it here.
	 */
	private ListenerList listenerList = new ListenerList();
	
	private ExpensesBinder binder;
	private LineItem lineItem;
	private ExpenseReport report;

	public void addListener(ExpenseReportingViewModelListener listener) {
		listenerList.add(listener);
	}

	public void dispose() {
		listenerList.clear();
	}

	
	public ExpensesBinder getBinder() {
		return binder;
	}

	public LineItem getLineItem() {
		return lineItem;
	}

	
	public ExpenseReport getReport() {
		return report;
	}

	public void removeListener(ExpenseReportingViewModelListener listener) {
		listenerList.remove(listener);
	}

	public void setBinder(ExpensesBinder binder) {
		this.binder = binder;
		this.report = null;
		this.lineItem = null;

		Object[] listeners = listenerList.getListeners();
		for(int index=0;index<listeners.length;index++) {
			ExpenseReportingViewModelListener listener = (ExpenseReportingViewModelListener) listeners[index];
			listener.binderChanged(this.binder);
			listener.reportChanged(this.report);
			listener.lineItemChanged(this.lineItem);
		}
	}
	
	public void setLineItem(LineItem item) {
		lineItem = item;	

		Object[] listeners = listenerList.getListeners();
		for(int index=0;index<listeners.length;index++) {
			ExpenseReportingViewModelListener listener = (ExpenseReportingViewModelListener) listeners[index];
			listener.lineItemChanged(this.lineItem);
		}
	}

	public void setReport(ExpenseReport report) {
		this.report = report;
		this.lineItem = null;
		
		Object[] listeners = listenerList.getListeners();
		for(int index=0;index<listeners.length;index++) {
			ExpenseReportingViewModelListener listener = (ExpenseReportingViewModelListener) listeners[index];
			listener.reportChanged(this.report);
			listener.lineItemChanged(this.lineItem);
		}
	}

}
