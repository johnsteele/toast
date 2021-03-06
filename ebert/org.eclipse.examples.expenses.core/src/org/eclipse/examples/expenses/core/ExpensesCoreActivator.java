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
package org.eclipse.examples.expenses.core;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class ExpensesCoreActivator extends Plugin {
	static ExpensesCoreActivator instance;

	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
	}
		
	public void stop(BundleContext context) throws Exception {
		instance = null;
		super.stop(context);
	}

	public static ExpensesCoreActivator getDefault() {
		return instance;
	}
}
