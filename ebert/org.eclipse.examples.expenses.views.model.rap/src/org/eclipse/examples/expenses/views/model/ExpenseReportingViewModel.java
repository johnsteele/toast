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

import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.rwt.RWT;

import com.ibm.icu.util.ULocale;

public class ExpenseReportingViewModel {
	
	public void addListener(ExpenseReportingViewModelListener listener) {
		getModelForCurrentUser().addListener(listener);
	}

	protected ExpenseReportingViewModelImpl getModelForCurrentUser() {
		ExpenseReportingViewModelImpl model = (ExpenseReportingViewModelImpl) RWT.getSessionStore().getAttribute("ExpenseReportingViewModel");
		if (model == null) {
			model = new ExpenseReportingViewModelImpl();
			RWT.getSessionStore().setAttribute("ExpenseReportingViewModel", model);
		}
		return model;
	}

	public void dispose() {
		getModelForCurrentUser().dispose();
	}
	
	public ExpensesBinder getBinder() {
		return getModelForCurrentUser().getBinder();
	}

	public LineItem getLineItem() {
		return getModelForCurrentUser().getLineItem();
	}

	
	public ExpenseReport getReport() {
		return getModelForCurrentUser().getReport();
	}

	public void removeListener(ExpenseReportingViewModelListener listener) {
		getModelForCurrentUser().removeListener(listener);
	}

	public void setBinder(ExpensesBinder binder) {
		getModelForCurrentUser().setBinder(binder);
	}
	
	public void setLineItem(LineItem item) {
		getModelForCurrentUser().setLineItem(item);
	}

	public void setReport(ExpenseReport report) {
		getModelForCurrentUser().setReport(report);
	}
	
	public ULocale getUserLocale() {
		return ULocale.forLocale(RWT.getLocale());
	}

}
