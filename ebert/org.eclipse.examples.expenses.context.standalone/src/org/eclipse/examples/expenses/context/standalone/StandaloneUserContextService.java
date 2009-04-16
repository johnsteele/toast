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

import org.eclipse.examples.expenses.context.IIdentityService;
import org.eclipse.examples.expenses.context.IPersistenceService;
import org.eclipse.examples.expenses.context.IUserContext;
import org.eclipse.examples.expenses.context.IUserContextService;
import org.eclipse.examples.expenses.core.ExpensesBinder;

public class StandaloneUserContextService implements IUserContextService {

	private StandaloneUserContext userContext;
	private IPersistenceService persistenceService;
	private IIdentityService identityService;

	public StandaloneUserContextService() {
	}
	
	/**
	 * Obtain the user context for the current thread. This method
	 * assumes that the {@link #identityService} and {@link #persistenceService}
	 * fields have been set. This relatively safe assumption depends on
	 * declarative services are doing their job correctly.
	 * <p>
	 * Note that this method does not require synchronization as
	 * it is thread-specific.
	 */
	public IUserContext getUserContext() {
		if (userContext == null) {
			createUserContext();
		}
		return userContext;
	}

	private void createUserContext() {
		String userId = identityService.getUserId();
		ExpensesBinder binder = persistenceService.loadBinder(userId);
		userContext = new StandaloneUserContext(binder);
	}

	public void stop() {
		userContext.dispose();
	}

	public void setPersistenceService(IPersistenceService persistenceService) {
		this.persistenceService = persistenceService;		
	}
	
	public void unsetPersistenceService(IPersistenceService persistenceService) {
		this.persistenceService = null;
	}
	
	public void setIdentityService(IIdentityService identityService) {
		this.identityService = identityService;
	}
	
	public void unsetIdentityService(IIdentityService identityService) {
		this.identityService = null;
	}
}
