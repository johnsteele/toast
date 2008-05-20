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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;

/**
 * Creates, adds and disposes actions for the menus and action bars of
 * each workbench window.
 */
public class ApplicationActionBarAdvisor extends ActionBarAdvisor {

	private final ApplicationStateManager applicationStateManager;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer, ApplicationStateManager applicationStateManager) {
		super(configurer);
		this.applicationStateManager = applicationStateManager;
	}
	
	// Actions - important to allocate these only in makeActions, and then use
	// them in the fill methods. This ensures that the actions aren't recreated
	// in the fill methods. 
	private IWorkbenchAction exitAction;
	private SaveAction saveAction;

	protected void makeActions(IWorkbenchWindow window) {
		// Creates the actions and registers them. Registering also 
		// provides automatic disposal of the actions when the window is closed.
		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);
		
		saveAction = new SaveAction();
		register(saveAction);
	}

	protected void fillMenuBar(IMenuManager menuBar) {
		final MenuManager fileMenu = new MenuManager("&File",IWorkbenchActionConstants.M_FILE);
		menuBar.add(fileMenu);
		fileMenu.add(saveAction);
		fileMenu.add(exitAction);
	}

	class SaveAction extends Action implements IWorkbenchAction {
		public SaveAction() {
			setId("saveBinder");
			setText("Save Binder");
			setToolTipText("Save the expenses binder");
		}
		
		@Override
		public void run() {
			applicationStateManager.save();
		}
		
		public void dispose() {		
		}
		
	}
}
