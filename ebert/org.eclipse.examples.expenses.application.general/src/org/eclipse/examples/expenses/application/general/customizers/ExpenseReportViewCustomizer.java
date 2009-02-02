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
import org.eclipse.examples.expenses.views.AbstractView;
import org.eclipse.examples.expenses.views.ExpenseReportViewPrivilegedAccessor;
import org.eclipse.examples.expenses.views.IExpenseReportViewCustomizer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class ExpenseReportViewCustomizer implements
		IExpenseReportViewCustomizer {

	private ExpenseReportViewPrivilegedAccessor accessor;

	public void postCreateExpenseReportView(ExpenseReportViewPrivilegedAccessor accessor) {
		this.accessor = accessor;
		
		createButtons(accessor.getParent());
	}

	/**
	 * This method creates and populates an area for buttons.
	 * This method assumes that the parent {@link Composite} has
	 * a {@link GridLayout} with one column for a layout manager.
	 * 
	 * @see AbstractView#createButtonArea(Composite)
	 * 
	 * @param parent A composite into which the buttons will be created.
	 */
	void createButtons(Composite parent) {
		Composite buttonArea = createButtonArea(parent);

		createAddButton(buttonArea);
		createRemoveButton(buttonArea);

		buttonArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	}

	protected Composite createButtonArea(Composite parent) {
		Composite buttonArea = new Composite(parent, SWT.NONE);
		buttonArea.setLayout(new RowLayout());
		return buttonArea;
	}
	
	void createAddButton(Composite parent) {
		Button addButton = new Button(parent, SWT.PUSH);
		addButton.setText("Add");
		addButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				if (accessor.getExpenseReport() == null) return;
				accessor.getExpenseReport().addLineItem(new LineItem());
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
				if (accessor.getExpenseReport() == null) return;
				IStructuredSelection selection = (IStructuredSelection)accessor.getLineItemViewer().getSelection();
				Object[] objects = selection.toArray();
				for(int index=0;index<objects.length;index++){
					accessor.getExpenseReport().removeLineItem((LineItem)objects[index]);					
				}
			}
	
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}			
		});
		
		removeButton.setEnabled(accessor.lineItemViewerHasSelection());
		
		/*
		 * Add a listener to the selection on the viewer. When the
		 * selection changes, update the state of the remove button.
		 */
		accessor.getLineItemViewer().addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				removeButton.setEnabled(!event.getSelection().isEmpty());
			}			
		});
	}
}
