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
package org.eclipse.examples.expenses.ui;

import org.eclipse.examples.expenses.views.model.ExpenseReportingViewModel;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class ExpenseReportingUI implements BundleActivator {
	static ExpenseReportingUI instance;
	private BundleContext context;
	private ExpenseReportingViewModel expenseReportingViewModel;

	public ExpenseReportingUI() {
		instance = this;
	}

	public static ExpenseReportingUI getDefault() {
		return instance;
	}
	
	public void start(BundleContext context) throws Exception {
		this.context = context;
		expenseReportingViewModel = new ExpenseReportingViewModel();
	}

	public void stop(BundleContext context) throws Exception {
		expenseReportingViewModel.dispose();
		expenseReportingViewModel = null;
		context = null;
	}
	
	public BundleContext getContext() {
		return context;
	}

	public ExpenseReportingViewModel getExpenseReportingViewModel() {
		return expenseReportingViewModel;
	}
}
