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
import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.views.BinderView;
import org.eclipse.examples.expenses.views.BinderViewProxy;
import org.eclipse.examples.expenses.views.ExpenseReportView;
import org.eclipse.examples.expenses.views.IBinderViewCustomizer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.ui.PartInitException;

/**
 * Instances of this class customize a {@link BinderView}. In the ERCP
 * case, {@link Command}s are added. The notion of Command varies from
 * phone to phone, but tends to manifest as buttons around the display.
 * We define buttons to add, remove, and edit {@link ExpenseReport} 
 * instances displayed by the view. In the case of the edit command, we
 * provide navigation which brings the {@link ExpenseReportView} to
 * the top (in ERCP, we can only show one view at a time). 
 * 
 * @see IBinderViewCustomizer
 */
public class BinderViewCustomizer implements IBinderViewCustomizer {

	private BinderViewProxy proxy;
	private Command addCommand;
	private Command removeCommand;
	private Command editCommand;
	
	/**
	 * This method is called at the end of the {@link BinderView} 
	 * creation process.
	 * <p>
	 * Our implementation adds some ESWT-specific Commands.
	 * 
	 * @param proxy
	 *            instance of {@link BinderViewProxy} that represents the
	 *            BinderView we're customizing.
	 */
	public void postCreateBinderView(BinderViewProxy proxy) {
		this.proxy = proxy;
		
		createAddCommand();
		createRemoveCommand();
		createEditCommand();
		
		proxy.getExpenseReportViewer().getList().addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
				editExpenseReport();
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
	void createDisposeListener(BinderViewProxy proxy) {
		proxy.getExpenseReportViewer().getControl().getParent().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				addCommand.dispose();
				removeCommand.dispose();
				editCommand.dispose();
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
				proxy.createExpenseReport();
				editExpenseReport();
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
				proxy.removeExpenseReports();
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
				editExpenseReport();
			}			
		});
	}
	
	void editExpenseReport() {		
		IStructuredSelection selection = (IStructuredSelection) proxy.getExpenseReportViewer().getSelection();
		if (selection.isEmpty()) return;
		ExpenseReport report = (ExpenseReport) selection.getFirstElement();
		try {
			ExpenseReportView expenseReportView = (ExpenseReportView) proxy.getPage().showView(ExpenseReportView.ID);
			expenseReportView.setReport(report);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}
