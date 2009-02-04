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
package org.eclipse.examples.expenses.views;

import org.eclipse.examples.expenses.ui.ExpenseReportingUI;
import org.eclipse.examples.expenses.views.model.ExpenseReportingViewModel;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import com.ibm.icu.util.ULocale;

public abstract class AbstractView extends ViewPart {

	/**
	 * This convenience method can be used to execute a {@link Runnable}
	 * asynchronously in the UI thread (essentially, the Runnable is executed
	 * when the UI takes a breather from whatever it's doing). It is used to run
	 * code that must be executed in the UI thread, such as updates to UI
	 * components like buttons, text fields, and lists.
	 * 
	 * @see Display#asyncExec
	 * @param runnable
	 */
	protected void asyncExec(Runnable runnable) {
		getViewSite().getWorkbenchWindow().getShell().getDisplay().asyncExec(runnable);
	}

	/**
	 * This convenience method can be used to execute a {@link Runnable} in the
	 * UI thread. It is used to run code that must be executed in the UI thread,
	 * such as updates to UI components like buttons, text fields, and lists.
	 * 
	 * @see Display#syncExec
	 * @param runnable
	 */
	protected void syncExec(Runnable runnable) {
		getViewSite().getWorkbenchWindow().getShell().getDisplay().syncExec(runnable);
	}

	/**
	 * This method obtains the UI Model from the activator.
	 * 
	 * @see ExpenseReportingUI#getExpenseReportingViewModel()
	 * 
	 * @return An instance of a class that implements {@link IExpenseReportingUIModel} 
	 * that is appropriate for the current user.
	 */
	protected ExpenseReportingViewModel getExpenseReportingViewModel() {
		return ExpenseReportingUI.getDefault().getExpenseReportingViewModel();
	}
	
	/**
	 * This method gets the current user's {@link ULocale}. For an RCP-based application,
	 * this would typically be the default value; for an RAP application, this value
	 * is determiend from the HTTP Session. In any case, this method looks for a service
	 * that provides the locale; if it does not find such a service, it assumes the
	 * default value.
	 * 
	 * @see ULocale#getDefault()
	 * @return
	 */
	ULocale getUserLocale() {
		return getExpenseReportingViewModel().getUserLocale();
	}
}