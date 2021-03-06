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

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * ExpenseReportingUI is an activator for the
 * org.eclipse.examples.expenses.views bundle. 
 */
public class ExpenseReportingUI extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.examples.expenses.views";
	
	static ExpenseReportingUI instance;
	private BundleContext context;

	public ExpenseReportingUI() {
		instance = this;
	}

	public static ExpenseReportingUI getDefault() {
		return instance;
	}
	
	public void start(BundleContext context) throws Exception {
		this.context = context;	}

	public void stop(BundleContext context) throws Exception {
	}
	
	public BundleContext getContext() {
		return context;
	}
}
