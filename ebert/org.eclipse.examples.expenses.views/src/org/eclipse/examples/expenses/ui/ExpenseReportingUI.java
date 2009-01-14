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
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * ExpenseReportingUI is an activator for the
 * org.eclipse.examples.expenses.views bundle. Its main role is to provide a
 * entry point for the view model, an instance of
 * {@link ExpenseReportingViewModel}.
 * 
 * <p>
 * At first glance, this would appear to be a singleton value that would
 * restrict its usefulness in the RAP (i.e. multiple user) version of this
 * application. However, multiple versions of this class are provided (one for
 * RAP, one for RCP/eRCP); at runtime, we link in the correct one. The RAP
 * version is aware of the multiple-user nature of the environment and reacts
 * accordingly. This implementation will be revisited with Bug 259516.
 */
public class ExpenseReportingUI extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.examples.expenses.views";
	
	static ExpenseReportingUI instance;
	private ExpenseReportingViewModel expenseReportingViewModel;

	public ExpenseReportingUI() {
		instance = this;
	}

	public static ExpenseReportingUI getDefault() {
		return instance;
	}
	
	public void start(BundleContext context) throws Exception {
		expenseReportingViewModel = new ExpenseReportingViewModel();
	}

	public void stop(BundleContext context) throws Exception {
		expenseReportingViewModel.dispose();
		expenseReportingViewModel = null;
	}
	
	public ExpenseReportingViewModel getExpenseReportingViewModel() {
		return expenseReportingViewModel;
	}
}
