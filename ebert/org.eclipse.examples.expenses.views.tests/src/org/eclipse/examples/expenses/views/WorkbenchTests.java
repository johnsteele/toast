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

import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public abstract class WorkbenchTests {

	/**
	 * This method makes sure that the UI has had an opportunity to process any
	 * outstanding events. This includes any asynchronous blocks introduced to
	 * the display via calls to {@link Display#asyncExec(Runnable)} (see
	 * {@link BinderView#setBinder(ExpensesBinder)} as an example). 
	 */
	protected void processEvents() {
		// The following line forces those queued up asynchronous tasks to run. 
		while (getWorkbench().getDisplay().readAndDispatch());
	}

	protected IWorkbenchPage getActivePage() {
		return getWorkbenchWindow().getActivePage();
	}

	protected IWorkbenchWindow getWorkbenchWindow() {
		return getWorkbench().getActiveWorkbenchWindow();
	}

	protected IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}
}