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

import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.examples.expenses.views.ExpenseReportViewProxy;
import org.eclipse.examples.expenses.views.IExpenseReportViewCustomizer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ExpenseReportViewCustomizer implements
		IExpenseReportViewCustomizer {

	private ExpenseReportViewProxy proxy;

	public void postCreateExpenseReportView(ExpenseReportViewProxy proxy) {
		this.proxy = proxy;
		Composite buttonArea = proxy.getButtonArea();

		createAddButton(buttonArea);
		createRemoveButton(buttonArea);
	}
	
	void createAddButton(Composite parent) {
		Button addButton = new Button(parent, SWT.PUSH);
		addButton.setText("Add");
		addButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				proxy.createLineItem();
			}
	
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}			
		});
	}
	
	void createRemoveButton(Composite parent) {
		final Button removeButton = new Button(parent, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				proxy.removeLineItems();
			}
	
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}			
		});
		
		removeButton.setEnabled(proxy.lineItemViewerHasSelection());
		
		/*
		 * Add a listener to the selection on the viewer. When the
		 * selection changes, update the state of the remove button.
		 */
		proxy.getLineItemViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				removeButton.setEnabled(!event.getSelection().isEmpty());
			}			
		});
	}
}
