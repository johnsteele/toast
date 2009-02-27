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
import org.eclipse.examples.expenses.views.IExpenseReportViewCustomizer;
import org.eclipse.examples.expenses.views.LineItemView;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.ui.PartInitException;

public class ExpenseReportViewCustomizer implements	IExpenseReportViewCustomizer {

	private ExpenseReportViewProxy proxy;
	private Command addCommand;
	private Command removeCommand;
	private Command editCommand;
	private Command backCommand;

	/**
	 * This method is called at the end of the {@link ExpenseReportView} 
	 * creation process.
	 * <p>
	 * Our implementation adds some ESWT-specific Commands.
	 * 
	 * @param proxy
	 *            instance of {@link ExpenseReportViewProxy} that represents the
	 *            {@link ExpenseReportView} we're customizing.
	 */
	public void postCreateExpenseReportView(ExpenseReportViewProxy proxy) {
		this.proxy = proxy;

		createAddCommand();
		createRemoveCommand();
		createEditCommand();
		createBackCommand();
		
		proxy.getLineItemViewer().getTable().addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
				editLineItem();
			}

			public void widgetSelected(SelectionEvent arg0) {
			}			
		});

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
	void createDisposeListener(ExpenseReportViewProxy proxy) {
		proxy.getLineItemViewer().getControl().getParent().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				addCommand.dispose();
				removeCommand.dispose();
				editCommand.dispose();
				backCommand.dispose();
			}
		});
	}

	void createAddCommand() {
		addCommand = new Command(proxy.getParent(), Command.GENERAL, 1);
		addCommand.setText("Add");
		addCommand.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {				
			}

			public void widgetSelected(SelectionEvent arg0) {
				proxy.createLineItem();
				editLineItem();
			}			
		});
	}

	void createRemoveCommand() {
		removeCommand = new Command(proxy.getParent(), Command.DELETE, 1);
		removeCommand.setText("Remove");
		removeCommand.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {				
			}

			public void widgetSelected(SelectionEvent arg0) {
				proxy.removeLineItems();
			}			
		});
	}

	void createEditCommand() {
		editCommand = new Command(proxy.getParent(), Command.OK, 1);
		editCommand.setText("Edit");
		editCommand.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {				
			}

			public void widgetSelected(SelectionEvent arg0) {
				editLineItem();
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
					proxy.getPage().showView(BinderView.ID);
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
		});
	}	
	void editLineItem() {		
		IStructuredSelection selection = (IStructuredSelection) proxy.getLineItemViewer().getSelection();
		if (selection.isEmpty()) return;
		LineItem lineItem = (LineItem) selection.getFirstElement();
		try {
			LineItemView lineItemView = (LineItemView) proxy.getPage().showView(LineItemView.ID);
			lineItemView.setLineItem(lineItem);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
