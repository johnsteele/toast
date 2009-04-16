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

import org.eclipse.examples.expenses.application.general.ApplicationWorkbenchAdvisor;
import org.eclipse.examples.expenses.application.general.ApplicationWorkbenchWindowAdvisor;
import org.eclipse.rwt.lifecycle.IEntryPoint;
import org.eclipse.rwt.lifecycle.UICallBack;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * This class controls all aspects of the application's execution and is
 * contributed through the plugin.xml.
 */
public class ExpenseReporting implements IEntryPoint {

	private static final String USER_ID = "userId";

	public int createUI() {
		/*
		 * This call sets us up to let background threads update the UI.
		 * Our user interface registers several listeners on the various models.
		 * When those objects are changed as a result of something occuring in the
		 * UI thread, and the corresponding listeners are invoked in response
		 * resulting in updates to user interface components, everything works as
		 * expected without this call. However, if something happens in a thread
		 * other than the UI thread (like, for example, another RAP UI thread, those
		 * changes--resulting in the invocation of listeners--do not result in
		 * immediate changes in the UI unless this feature is activated.
		 */
		UICallBack.activate(getClass().getName());

		Display display = PlatformUI.createDisplay();
		
		WorkbenchAdvisor advisor = createApplicationWorkbenchAdvisor();
		return PlatformUI.createAndRunWorkbench(display, advisor);
	}

	ApplicationWorkbenchAdvisor createApplicationWorkbenchAdvisor() {
		return new ApplicationWorkbenchAdvisor() {
			@Override
			public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
				return new ApplicationWorkbenchWindowAdvisor(configurer) {
					public void preWindowOpen() {
						super.preWindowOpen();
						IWorkbenchWindowConfigurer windowConfigurer = getWindowConfigurer();
						windowConfigurer.setShellStyle(SWT.NO_TRIM);
						//windowConfigurer.setShowMenuBar(false);
						//windowConfigurer.setShowCoolBar(true);
					}

					public void postWindowCreate() {
						super.postWindowCreate();
						Shell shell = getWindowConfigurer().getWindow().getShell();
						shell.setMaximized(true);
					}
				};
			}
		};
	}
}
