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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.examples.expenses.context.IIdentityService;
import org.eclipse.examples.expenses.context.IPersistenceService;
import org.eclipse.examples.expenses.context.IUserContext;
import org.eclipse.examples.expenses.context.IUserContextService;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.service.ISessionStore;
import org.eclipse.rwt.service.SessionStoreEvent;
import org.eclipse.rwt.service.SessionStoreListener;

/**
 * One instance of this class is created by the declarative services
 * framework. See META-INF/RapUserContextService.xml.
 * <p>
 * This class is specific to RAP and leverages the RWT notion of state
 * to manage an instance of {@link RapUserContext} for each user.
 * <p>
 * TODO There is some duplication with StandaloneUserContextService. Refactor.
 */
public class RapUserContextService implements IUserContextService {
	//private static final String USER_CONTEXT_KEY = "user-context";

	private IPersistenceService persistenceService;
	private IIdentityService identityService;
	
	Map<String, RapUserContext> contextsMap = new HashMap<String, RapUserContext>();

	/**
	 * This method answers the {@link IUserContext} for the user
	 * running in the current thread. By virtue of the thread-specific
	 * nature of this method, we don't have to worry about synchronizing
	 * anything.
	 * <p>
	 * Note that we probably should be storing our value in the RWT
	 * session store. We aren't storing it there because, we want to make
	 * a clean break if this service is shut down. More specifically, when
	 * the service stopped (for whatever reason), we want to remove
	 * any {@link IUserContext} instances that were created by the service.
	 * As far as I know, there is no API available for hunting down and
	 * removing entries from all current sessions.
	 */
	public IUserContext getUserContext() {
		final String key = RWT.getSessionStore().getId();
		RapUserContext context = contextsMap.get(key);
		if (context == null) {
			context = createUserContext();
			contextsMap.put(key, context);
			
			RWT.getSessionStore().addSessionStoreListener(new SessionStoreListener() {
				/**
				 * If the session is destroyed, remove the context from our
				 * list. Also give the context a chance to clean up anything
				 * that needs cleanin' up.
				 */
				public void beforeDestroy(SessionStoreEvent event) {
					contextsMap.remove(key).dispose();
				}
			});
		}
		return context;
		
//		ISessionStore sessionStore = RWT.getSessionStore();
//		RapUserContext context = (RapUserContext) sessionStore.getAttribute(USER_CONTEXT_KEY);
//		if (context == null) {
//			context = createUserContext();
//			sessionStore.setAttribute(USER_CONTEXT_KEY, context);
//		}
//		return context;
	}

	private RapUserContext createUserContext() {
		/*
		 * Thanks to the dependency injection provided by declarative
		 * services, #identityService and #persistenceService should
		 * never be null.
		 */
		String userId = identityService.getUserId();
		ExpensesBinder binder = persistenceService.loadBinder(userId);
		return new RapUserContext(binder);
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
