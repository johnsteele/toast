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
package org.eclipse.examples.expenses.application.ercp.customizers;

import org.eclipse.ercp.swt.mobile.Command;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.examples.expenses.views.BinderView;
import org.eclipse.examples.expenses.views.BinderViewProxy;
import org.eclipse.examples.expenses.views.ExpenseReportView;
import org.eclipse.examples.expenses.views.ExpenseReportViewProxy;
import org.eclipse.examples.expenses.views.ILineItemViewCustomizer;
import org.eclipse.examples.expenses.views.LineItemView;
import org.eclipse.examples.expenses.views.LineItemViewProxy;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PartInitException;

public class LineItemViewCustomizer implements ILineItemViewCustomizer {

	private LineItemViewProxy proxy;
	private Command backCommand;

	/**
	 * This method is called at the end of the {@link LineItemView} 
	 * creation process.
	 * <p>
	 * Our implementation adds some ESWT-specific Commands.
	 * 
	 * @param proxy
	 *            instance of {@link LineItemViewProxy} that represents the
	 *            {@link LineItemView} we're customizing.
	 */
	public void postCreateLineItemView(LineItemViewProxy proxy) {
		this.proxy = proxy;

		createBackCommand();

		createDisposeListener(proxy);
	}

	/**
	 * This method adds a {@link DisposeListener} on the view. When the view is
	 * disposed, we clean up the commands we created.
	 * 
	 * @param proxy
	 *            instance of {@link BinderViewProxy} that represents the
	 *            BinderView we're customizing.
	 */
	void createDisposeListener(LineItemViewProxy proxy) {
		proxy.getParent().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				backCommand.dispose();
			}
		});
	}

	void createBackCommand() {
		backCommand = new Command(proxy.getParent(), Command.BACK, 1);
		backCommand.setText("Back");
		backCommand.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {				
			}

			public void widgetSelected(SelectionEvent arg0) {
				try {
					proxy.getPage().showView(ExpenseReportView.ID);
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
		});
	}	
}
