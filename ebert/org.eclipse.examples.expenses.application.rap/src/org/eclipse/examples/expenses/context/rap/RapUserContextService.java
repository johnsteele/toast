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
package org.eclipse.examples.expenses.context.rap;

import org.eclipse.examples.expenses.application.rap.Activator;
import org.eclipse.examples.expenses.context.IUserContext;
import org.eclipse.examples.expenses.context.IUserContextService;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.service.ISessionStore;
import org.osgi.framework.BundleContext;

/**
 * One instance of this class is created by the {@link Activator} on startup
 * and registered as an Equinox/OSGi service (and is subsequently deregistered
 * when the activator stops). This instance is responsible for providing
 * user state in the application. 
 * <p>
 * This class is specific to RAP and leverages the RWT notion of state
 * to manage an instance of {@link RapUserContext} for each user.
 * 
 * @see Activator#start(BundleContext)
 * @see Activator#stop(BundleContext)
 */
public class RapUserContextService implements IUserContextService {
	private static final String USER_CONTEXT_KEY = "user-context";

	public IUserContext getUserContext() {
		ISessionStore sessionStore = RWT.getSessionStore();
		RapUserContext context = (RapUserContext) sessionStore.getAttribute(USER_CONTEXT_KEY);
		if (context == null) {
			context = new RapUserContext();
			sessionStore.setAttribute(USER_CONTEXT_KEY, context);
		}
		return context;
	}
}
