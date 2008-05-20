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
package org.eclipse.examples.expenses.application.general;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	BundleContext context;
	static Activator instance;

	public void start(BundleContext context) throws Exception {
		instance = this;
		this.context = context;		
	}

	public void stop(BundleContext context) throws Exception {
		this.context = null;
		instance = null;
	}

	public static Activator getDefault() {
		return instance;
	}

	public BundleContext getContext() {
		return context;
	}

	
}
