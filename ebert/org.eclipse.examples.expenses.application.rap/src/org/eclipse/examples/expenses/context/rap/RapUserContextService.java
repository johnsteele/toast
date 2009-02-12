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

import org.eclipse.examples.expenses.context.IUserContext;
import org.eclipse.examples.expenses.context.IUserContextService;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.service.ISessionStore;

public class RapUserContextService implements IUserContextService {
	private static final String USER_CONTEXT_KEY = "user-context";

	public RapUserContextService() {
	}
	
	public IUserContext getUserContext() {
		ISessionStore sessionStore = RWT.getSessionStore();
		/*
		 * Synchronize on the session store since there is some possibility
		 * that multiple threads connected to the current user may try to do
		 * this simultaneously (it's unlikely, but possible). This could happen,
		 * for example, as a result of a call to Display#asyncExec(Runnable).
		 */
		//synchronized (sessionStore) {
			RapUserContext context = (RapUserContext) sessionStore.getAttribute(USER_CONTEXT_KEY);
			if (context == null) {
				context = new RapUserContext();
				sessionStore.setAttribute(USER_CONTEXT_KEY, context);
			}
			return context;
		//}
	}
}
