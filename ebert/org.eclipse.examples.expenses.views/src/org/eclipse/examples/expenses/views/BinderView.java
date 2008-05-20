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

import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class BinderView extends AbstractView {

	public static final String ID = BinderView.class.getName();
	
	private ListViewer viewer;

	private IStructuredContentProvider contentProvider = new IStructuredContentProvider() {
		public Object[] getElements(Object input) {
			if (input instanceof ExpensesBinder) {
				return ((ExpensesBinder)input).getReports();
			}
			return new Object[0];
		}
		
		public void dispose() {
			
		}

		public void inputChanged(Viewer viewer, Object oldBinder, Object newBinder) {
			unhookListeners((ExpensesBinder)oldBinder);
			hookListeners((ExpensesBinder)newBinder);
		}			
	};

	private LabelProvider labelProvider = new LabelProvider() {
		public String getText(Object report) {
			return ((ExpenseReport)report).getTitle();
		}
		
		public boolean isLabelProperty(Object report, String property) {
			return ExpenseReport.TITLE_PROPERTY.equals(property);
		}
	};

	private Button removeButton;

	private Button addButton;

	/*
	 * TODO This listener is a dirty hack. Clean it up.
	 */
	IPropertyChangeListener binderListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			viewer.refresh();
			hookListeners(getBinder());
		}		
	};
	
	IPropertyChangeListener expenseReportListener = new IPropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent event) {
			/*
			 * Run this in the UI thread just in case the change comes
			 * from a different thread.
			 */
			viewer.getControl().getDisplay().syncExec(new Runnable() {
				public void run() {
					/*
					 * When we tell the viewer which properties have actually changed,
					 * it can use some smarts to figure out if the table contents need
					 * to be resorted, or refiltered.
					 */
					viewer.update(event.getSource(), new String[] {event.getProperty()});
				}
			});
		}
	};

	private ExpensesBinder expensesBinder;

	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, true));
		viewer = new ListViewer(parent, SWT.BORDER);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(labelProvider);		
		getSite().setSelectionProvider(viewer);
		viewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite buttons = createButtonArea(parent);
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		addButton = new Button(getButtonArea(), SWT.PUSH);
		addButton.setText("Add");
		addButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {				
			}

			public void widgetSelected(SelectionEvent e) {
				getBinder().addExpenseReport(new ExpenseReport("New Expense Report"));
			}
			
		});

		removeButton = new Button(getButtonArea(), SWT.PUSH);
		removeButton.setText("Remove");
		
		updateButtons();
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}			
		});
		
		customizeView(parent);
		
		// TODO Need a listener for new ExpenseReports
		viewer.setInput(getBinder());
	}

	ExpensesBinder getBinder() {
		return expensesBinder;
	}

	protected void hookListeners(ExpensesBinder binder) {
		if (binder == null) return;
		binder.addPropertyChangeListener(binderListener);
		ExpenseReport[] reports = binder.getReports();
		for(int index=0;index<reports.length;index++) {
			hookListeners(reports[index]);
		}
	}

	protected void unhookListeners(ExpensesBinder binder) {
		if (binder == null) return;
		binder.removePropertyChangeListener(binderListener);
		ExpenseReport[] reports = binder.getReports();
		for(int index=0;index<reports.length;index++) {
			unhookListeners(reports[index]);
		}	
	}

	void hookListeners(ExpenseReport expenseReport) {
		expenseReport.addPropertyChangeListener(expenseReportListener);
	}
	
	void unhookListeners(ExpenseReport expenseReport) {
		expenseReport.removePropertyChangeListener(expenseReportListener);
	}

	private void updateButtons() {
		boolean hasSelection = !((IStructuredSelection)viewer.getSelection()).isEmpty();
		removeButton.setEnabled(hasSelection);
	}
		
	protected void updateViewerInput(ExpensesBinder expensesBinder) {
		viewer.setInput(expensesBinder);
	}

	public void setFocus() {
		viewer.getList().setFocus();
	}

	public Viewer getViewer() {
		return viewer;
	}

	protected void handleSelection(ISelection selection) {
	}

	public void setBinder(final ExpensesBinder expensesBinder) {
		this.expensesBinder = expensesBinder;
		asyncExec(new Runnable() {
			public void run() {
				viewer.setInput(expensesBinder);
			}			
		});
	}

}
