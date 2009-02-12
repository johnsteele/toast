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
package org.eclipse.examples.expenses.application.rap;

import org.eclipse.examples.expenses.context.IUserContextService;
import org.eclipse.examples.expenses.context.rap.RapUserContextService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {
	private RapUserContextService userContextService;
	private ServiceRegistration serviceRegistration;
	
	public void start(BundleContext context) throws Exception {
		userContextService = new RapUserContextService();
		serviceRegistration = context.registerService(IUserContextService.class.getName(), userContextService, null);
	}

	public void stop(BundleContext context) throws Exception {
		serviceRegistration.unregister();
	}
}
