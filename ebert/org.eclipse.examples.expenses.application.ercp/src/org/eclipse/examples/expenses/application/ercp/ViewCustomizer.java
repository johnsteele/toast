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
package org.eclipse.examples.expenses.application.ercp;

import org.eclipse.ercp.swt.mobile.Command;
import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.examples.expenses.views.AbstractView;
import org.eclipse.examples.expenses.views.BinderView;
import org.eclipse.examples.expenses.views.ExpenseReportView;
import org.eclipse.examples.expenses.views.IViewCustomizer;
import org.eclipse.examples.expenses.views.LineItemView;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;

public class ViewCustomizer implements IViewCustomizer {
	private Button editButton;

	public void customizeView(Composite parent, IViewPart view) {
		if (view instanceof BinderView) customizeBinderView(parent, (BinderView)view);
		if (view instanceof ExpenseReportView) customizeExpenseReportView(parent, (AbstractView)view);
		if (view instanceof LineItemView) customizeLineItemView(parent, (LineItemView)view);
	}

	void customizeBinderView(Composite parent, final BinderView binderView) {
		addEditButton(binderView);
		
		updateButtons(binderView.getExpenseReportViewer());
		
		binderView.getExpenseReportViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons(binderView.getExpenseReportViewer());
			}			
		});
		
		final Command selectCommand = new Command(binderView.getExpenseReportViewer().getControl(), Command.SELECT, 1);
		selectCommand.setText("Open");
		selectCommand.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {				
			}

			public void widgetSelected(SelectionEvent arg0) {
				editExpenseReport(binderView);
			}			
		});
		
		binderView.getExpenseReportViewer().getControl().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				selectCommand.dispose();
			}			
		});
	}
	
	void customizeExpenseReportView(Composite parent, final AbstractView view) {
		addEditButton(view);
		addBackButton(view.getButtonArea(), parent, view);

		final Command selectCommand = new Command(view.getExpenseReportViewer().getControl(), Command.SELECT, 1);
		selectCommand.setText("Edit");
		selectCommand.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {				
			}

			public void widgetSelected(SelectionEvent arg0) {
				editLineItem(view);
			}			
		});
		
		view.getExpenseReportViewer().getControl().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				selectCommand.dispose();
			}			
		});
	}

	void customizeLineItemView(Composite parent, LineItemView view) {
		addBackButton(view.getButtonArea(), parent, view);
	}

	private void addBackButton(Composite buttonArea, Composite parent, final IViewPart view) {
		Button backButton = new Button(buttonArea, SWT.PUSH);
		backButton.setText("Back");
		SelectionListener listener = new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {				
			}

			public void widgetSelected(SelectionEvent arg0) {
				view.getSite().getPage().hideView(view);
			}			
		};
		backButton.addSelectionListener(listener);
		
		final Command backCommand = new Command(parent, Command.EXIT, 1);
		backCommand.setText("Back");
		backCommand.addSelectionListener(listener);
		
		parent.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				backCommand.dispose();
			}			
		});
	}

	void updateButtons(Viewer viewer) {
		boolean hasSelection = !((IStructuredSelection)viewer.getSelection()).isEmpty();
		
		editButton.setEnabled(hasSelection);
	}

	void addEditButton(final BinderView view) {
		editButton = new Button(view.getButtonArea(), SWT.PUSH);
		editButton.setText("Edit");
		editButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent event) {				
			}

			public void widgetSelected(SelectionEvent event) {
				editExpenseReport(view);
			}		
		});
	}

	void editExpenseReport(final BinderView view) {
		IStructuredSelection selection = (IStructuredSelection) view.getExpenseReportViewer().getSelection();
		if (selection.isEmpty()) return;
		ExpenseReport report = (ExpenseReport) selection.getFirstElement();
		try {
			ExpenseReportView expenseReportView = (ExpenseReportView) view.getSite().getPage().showView(ExpenseReportView.ID);
			expenseReportView.setReport(report);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	void addEditButton(final AbstractView view) {
		editButton = new Button(view.getButtonArea(), SWT.PUSH);
		editButton.setText("Edit");
		editButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent event) {				
			}

			public void widgetSelected(SelectionEvent event) {
				editLineItem(view);
			}			
		});
	}

	private void editLineItem(final AbstractView view) {
		IStructuredSelection selection = (IStructuredSelection) view.getExpenseReportViewer().getSelection();
		if (selection.isEmpty()) return;
		LineItem lineItem = (LineItem) selection.getFirstElement();
		try {
			LineItemView lineItemView = (LineItemView) view.getSite().getPage().showView(LineItemView.ID);
			lineItemView.setLineItem(lineItem);
		} catch (PartInitException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
