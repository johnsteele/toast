/*******************************************************************************
 * Copyright (c) 2009 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.examples.expenses.context.standalone;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.ServiceRegistration;

import org.eclipse.examples.expenses.context.IUserContextService;

public class Activator implements BundleActivator {
	private ServiceRegistration serviceRegistration;
	private StandaloneUserContextService userContextService;

	public void start(BundleContext context) throws Exception {
		userContextService = new StandaloneUserContextService();
		serviceRegistration = context.registerService(IUserContextService.class.getName(), userContextService, null);
	}

	public void stop(BundleContext context) throws Exception {
		serviceRegistration.unregister();
		userContextService.stop();
	}
}
