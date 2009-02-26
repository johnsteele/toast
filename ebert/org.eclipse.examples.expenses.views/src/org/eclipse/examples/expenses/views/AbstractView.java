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
package org.eclipse.examples.expenses.views;

import org.eclipse.examples.expenses.context.IUserContext;
import org.eclipse.examples.expenses.context.IUserContextService;
import org.eclipse.examples.expenses.ui.ExpenseReportingUI;
import org.eclipse.examples.expenses.views.model.ViewModel;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.ibm.icu.util.ULocale;

public abstract class AbstractView extends ViewPart {

	/**
	 * This convenience method can be used to execute a {@link Runnable}
	 * asynchronously in the UI thread (essentially, the Runnable is executed
	 * when the UI takes a breather from whatever it's doing). It is used to run
	 * code that must be executed in the UI thread, such as updates to UI
	 * components like buttons, text fields, and lists.
	 * 
	 * @see Display#asyncExec
	 * @param runnable
	 */
	protected void asyncExec(Runnable runnable) {
		getViewSite().getWorkbenchWindow().getShell().getDisplay().asyncExec(runnable);
	}

	/**
	 * This convenience method can be used to execute a {@link Runnable} in the
	 * UI thread. It is used to run code that must be executed in the UI thread,
	 * such as updates to UI components like buttons, text fields, and lists.
	 * 
	 * @see Display#syncExec
	 * @param runnable
	 */
	protected void syncExec(Runnable runnable) {
		getViewSite().getWorkbenchWindow().getShell().getDisplay().syncExec(runnable);
	}
	
	/**
	 * This method gets the current user's {@link ULocale}. For an RCP-based application,
	 * this would typically be the default value; for an RAP application, this value
	 * is determiend from the HTTP Session. In any case, this method looks for a service
	 * that provides the locale; if it does not find such a service, it assumes the
	 * default value.
	 * 
	 * @see ULocale#getDefault()
	 * @return
	 */
	protected ULocale getUserLocale() {
		if (userContext == null) return ULocale.getDefault();
		return userContext.getUserLocale();
	}

	IUserContext userContext;
	ServiceTracker userContextServiceTracker;
	
	protected ViewModel getViewModel() {
		if (userContext == null) return null;
		return userContext.getViewModel();
	}
	
	protected void startUserContextServiceTracker() {
		userContextServiceTracker = new ServiceTracker(ExpenseReportingUI.getDefault().getContext(), IUserContextService.class.getName(), null) {
			/**
			 * We keep track of the first service we find and ignore the
			 * rest. This is a great example of where declarative services
			 * would be helpful: you can declare that you want exactly one
			 * instance of a service and that's what you get. 
			 */
			protected IUserContextService userContextService;
			
			/**
			 * This method is called when a matching service is found or
			 * added. This finds both pre-existing and new instances of the
			 * service.
			 */
			public Object addingService(ServiceReference reference) {
				Object service = super.addingService(reference);
				if (userContextService == null) {
					userContextService = (IUserContextService)service;
					/*
					 * Do the part where we get the user context in a
					 * syncExec block. This will make sure that it runs
					 * in the user interface thread for the current user.
					 * This doesn't matter too much on RCP/eRCP, but the
					 * thread that we're running in is pretty critical
					 * in RAP.
					 */
					syncExec(new Runnable() {
						public void run() {
							userContext = userContextService.getUserContext();
							connectToUserContext(userContext);
						}
					});
				}
				return service;
			}
			
			/**
			 * This method is called when the service is being removed, or the 
			 * tracker is being closed.
			 */
			public void removedService(ServiceReference reference, Object service) {
				if (service == userContextService) {
					syncExec(new Runnable() {
						public void run() {
							disconnectFromUserContext(userContext);
						}
					});
					userContext = null;
					userContextService = null;
					// TODO Do we try to match up with a hypothetical second service in this case?
				}
				super.removedService(reference, service);
			};			
		};
		userContextServiceTracker.open();
	}
	
	protected void stopUserContextServiceTracker() {
		userContextServiceTracker.close();
	}
	
	/**
	 * An {@link IUserContext} has become available, connect to it. 
	 * This gives the instance an opportunity to configure itself for a user.
	 * Since the service which provides this context can potentially be started
	 * at any time, we cannot depend on it being available before we
	 * open the view.
	 * <p>
	 * This method is invoked in the UI thread. It should not be directly 
	 * invoked by clients.
	 * 
	 * @param userContext instance of {@link IUserContext}
	 */
	protected void connectToUserContext(IUserContext userContext) {};
	
	/**
	 * The instance of {@link IUserContext} that we depend upon is going
	 * away and we need to disconnect from it.
	 * <p>
	 * This method is invoked in the UI thread. It should not be directly 
	 * invoked by clients.
	 * 
	 * @see #connectToUserContext(IUserContext)
	 * @param userContext
	 */
	protected void disconnectFromUserContext(IUserContext userContext) {};
}