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
import org.eclipse.examples.expenses.views.BinderView;
import org.eclipse.examples.expenses.views.IBinderViewCustomizer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class BinderViewCustomizer implements IBinderViewCustomizer {
	private BinderView binderView;
	
	private Button addButton;
	private Button removeButton;

	public void postCreateBinderView(final BinderView binderView, Composite parent) {		
		this.binderView = binderView;
		
		Composite buttons = createButtonArea(parent);
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		addButton = new Button(buttons, SWT.PUSH);
		addButton.setText("Add");
		addButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {				
			}

			public void widgetSelected(SelectionEvent e) {
				binderView.getBinder().addExpenseReport(new ExpenseReport("New Expense Report"));
			}
		});

		removeButton = new Button(buttons, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {				
			}

			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)binderView.getExpenseReportViewer().getSelection();
				Object[] objects = selection.toArray();
				for(int index=0;index<objects.length;index++){
					// TODO Review deadlock potential.
					binderView.getBinder().removeExpenseReport((ExpenseReport)objects[index]);					
				}
			}			
		});
		
		updateButtons();		

		binderView.getExpenseReportViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}			
		});
	}

	protected Composite createButtonArea(Composite parent) {
		Composite buttonArea = new Composite(parent, SWT.NONE);
		buttonArea.setLayout(new RowLayout());
		return buttonArea;
	}

	private void updateButtons() {
		boolean hasSelection = !((IStructuredSelection)binderView.getExpenseReportViewer().getSelection()).isEmpty();
		removeButton.setEnabled(hasSelection);
	}

}
