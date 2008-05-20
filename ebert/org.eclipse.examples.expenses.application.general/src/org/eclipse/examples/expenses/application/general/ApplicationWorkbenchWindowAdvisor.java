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

import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.examples.expenses.views.BinderView;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * Configures the initial size and appearance of a workbench window.
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	private final ApplicationStateManager applicationStateManager;

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer, ApplicationStateManager applicationStateManager) {
        super(configurer);
		this.applicationStateManager = applicationStateManager;
    }

    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer, applicationStateManager);
    }
    
    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(640, 480));
        configurer.setShowCoolBar(false);
        configurer.setShowStatusLine(false);
        configurer.setTitle("Expense Reporting");
    }
	
	public void postWindowCreate() {
		try {
			/*
			 * Obtain a handle on the BinderView and explicitly set an
			 * instance of BinderView into it. This way, the views themselves
			 * will manage the application state.
			 */
			BinderView view = (BinderView) getWindowConfigurer().getWindow().getActivePage().showView(BinderView.ID);
			ExpensesBinder binder = applicationStateManager.getBinder();
			view.setBinder(binder);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
