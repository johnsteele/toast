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
package org.eclipse.examples.expenses.application.general.customizers;

import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.views.BinderViewProxy;
import org.eclipse.examples.expenses.views.IBinderViewCustomizer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class BinderViewCustomizer implements IBinderViewCustomizer {
	private Button addButton;
	private Button removeButton;

	private BinderViewProxy proxy;

	public void postCreateBinderView(BinderViewProxy proxy) {		
		this.proxy = proxy;
		
		createButtons(proxy);		
		updateButtons();		

		proxy.getExpenseReportViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}			
		});
	}

	private void createButtons(BinderViewProxy proxy) {
		Composite buttons = proxy.getButtonArea();		
		createAddButton(buttons);
		createRemoveButton(buttons);
	}

	private void createRemoveButton(Composite buttons) {
		removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {				
			}

			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) proxy.getExpenseReportViewer().getSelection();
				Object[] objects = selection.toArray();
				for(int index=0;index<objects.length;index++){
					proxy.getBinder().removeExpenseReport((ExpenseReport)objects[index]);					
				}
			}			
		});
	}

	private void createAddButton(Composite buttons) {
		addButton = new Button(buttons, SWT.PUSH);
		addButton.setText("Add");
		addButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {				
			}

			public void widgetSelected(SelectionEvent e) {
				proxy.getBinder().addExpenseReport(new ExpenseReport("New Expense Report"));
			}
		});
	}

	protected Composite createButtonArea(Composite parent) {
		Composite buttonArea = new Composite(parent, SWT.NONE);
		buttonArea.setLayout(new RowLayout());
		return buttonArea;
	}

	private void updateButtons() {
		boolean hasSelection = !((IStructuredSelection)proxy.getExpenseReportViewer().getSelection()).isEmpty();
		removeButton.setEnabled(hasSelection);
	}

}
