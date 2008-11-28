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

import org.eclipse.examples.expenses.core.CollectionPropertyChangeEvent;
import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.examples.expenses.ui.ExpenseReportingUI;
import org.eclipse.examples.expenses.ui.ExpenseReportingUIModelAdapter;
import org.eclipse.examples.expenses.ui.IExpenseReportingUIModel;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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

/**
 * The BinderView is used to view instances of {@link ExpensesBinder}.
 * 
 * <p>This example does change notification the traditional/hard-way, making
 * extensive use of the Observer pattern by installing dozens of listeners
 * on the objects being displayed. When one of the objects changes, the
 * receiver is notified of the change through the listeners and updates
 * accordingly.
 *  
 * @see ExpensesBinder
 * @see AbstractView
 * @author wayne
 *
 */
public class BinderView extends AbstractView {

	public static final String ID = BinderView.class.getName();
	
	ListViewer viewer;
	Button removeButton;
	Button addButton;
	
	ExpensesBinder expensesBinder;
	
	/**
	 * When the list viewer is created, this instance is given as its content
	 * provider via the
	 * {@link ListViewer#setContentProvider(org.eclipse.jface.viewers.IContentProvider)}
	 * method.
	 * 
	 * <p>
	 * The contentProvider is responsible for providing content for the
	 * {@link ListViewer} that takes up most of the space in this view. When the
	 * list viewer's
	 * <em>input<em> is changed via the {@link ListViewer#setInput(Object)} method, this
	 * content provider is asked to provide reasonable content based on that input.
	 */
	IStructuredContentProvider contentProvider = new IStructuredContentProvider() {
		/**
		 * When asked to get the elements that are to be displayed by the viewer,
		 * this method returns an array of {@link ExpenseReport} instances owned
		 * by the input object (an instance of {@link ExpensesBinder}). If the
		 * input is anything other than an instance of {@link ExpensesBinder}, a
		 * generic empty array is returned.
		 */
		public Object[] getElements(Object input) {
			if (input instanceof ExpensesBinder) {
				return ((ExpensesBinder)input).getReports();
			}
			return new Object[0];
		}
		
		/**
		 * When the instance is disposed, any previously installed listeners
		 * are cleaned up.
		 */
		public void dispose() {
			unhookListeners(expensesBinder);
		}

		public void inputChanged(Viewer viewer, Object oldBinder, Object newBinder) {
			unhookListeners((ExpensesBinder)oldBinder);
			hookListeners((ExpensesBinder)newBinder);
		}			
	};

	LabelProvider labelProvider = new LabelProvider() {
		public String getText(Object report) {
			return ((ExpenseReport)report).getTitle();
		}
		
		public boolean isLabelProperty(Object report, String property) {
			return ExpenseReport.TITLE_PROPERTY.equals(property);
		}
	};

	/**
	 * The binderListener listens for property changes on an instance of
	 * {@link ExpensesBinder}. When a property change occurs, the viewer 
	 * (an instance of {@link ListViewer} is forced to refresh and listeners
	 * are updated.
	 */
	IPropertyChangeListener binderListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event instanceof CollectionPropertyChangeEvent) {
				handleCollectionChangeEvent((CollectionPropertyChangeEvent)event);
			} else {
				/*
				 * This implementation is a bit like using a sledgehammer to
				 * hammer in a finishing nail; refreshing the entire viewer and
				 * hooking listeners on objects that very likely already have
				 * listeners on them is... excessive. However, in the absense of
				 * more information, there really is listte more that we can do
				 * than to refresh the viewer
				 */ 
				viewer.refresh();
				hookListeners(getBinder());
			}
		}

		/**
		 * This method handles the case when {@link ExpenseReport} instances are
		 * either added or removed from the {@link ExpensesBinder}. Listeners
		 * are hooked (or unhooked) from the affected instances, and the viewer
		 * is updated appropriately.
		 * 
		 * @param event
		 *            An instance of {@link CollectionPropertyChangeEvent} that
		 *            describes what happened.
		 */
		private void handleCollectionChangeEvent(final CollectionPropertyChangeEvent event) {
			for(int index=0;index<event.added.length;index++) {
				hookListeners((ExpenseReport)event.added[index]);
			}
			for(int index=0;index<event.removed.length;index++) {
				unhookListeners((ExpenseReport)event.removed[index]);
			}
			syncExec(new Runnable() {
				public void run() {
					viewer.add(event.added);
					viewer.remove(event.removed);
				}				
			});
		}		
	};

	ExpenseReportingUIModelAdapter expenseReportingUIModelListener = new ExpenseReportingUIModelAdapter() {
		public void binderChanged(ExpensesBinder binder) {
			setBinder(binder);
		}
	};
	
	IPropertyChangeListener expenseReportListener = new IPropertyChangeListener() {
		public void propertyChange(final PropertyChangeEvent event) {
			/*
			 * Run this in the UI thread just in case the change comes
			 * from a different thread.
			 */
			syncExec(new Runnable() {
				public void run() {
					/*
					 * When we tell the viewer which properties have actually
					 * changed (second parameter), it can use some smarts to
					 * figure out if the table contents need to be resorted, or
					 * refiltered.
					 */
					viewer.update(event.getSource(), new String[] {event.getProperty()});
				}
			});
		}
	};
	
	public void createPartControl(final Composite parent) {
		parent.setLayout(new GridLayout(1, true));
		createReportListViewer(parent);
		
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
		//TODO Implement some behaviour for the Remove button
		
		updateButtons();
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}			
		});
		
		customizeView(parent);
		
		/*
		 * Add a listener to the UI Model; should the binder change, we'll update
		 * ourselves to reflect that change.
		 */
		getExpenseReportingUIModel().addListener(expenseReportingUIModelListener);
		setBinder(getExpenseReportingUIModel().getBinder());
		
		viewer.setInput(getBinder());
	}

	private void createReportListViewer(Composite parent) {
		viewer = new ListViewer(parent, SWT.BORDER);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(labelProvider);		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				getExpenseReportingUIModel().setReport((ExpenseReport) selection.getFirstElement());
			}			
		});
		getSite().setSelectionProvider(viewer);
		viewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	/**
	 * This method obtains the UI Model from the activator.
	 * 
	 * @see ExpenseReportingUI#getExpenseReportingUIModel()
	 * 
	 * @return An instance of a class that implements {@link IExpenseReportingUIModel} 
	 * that is appropriate for the current user.
	 */
	private IExpenseReportingUIModel getExpenseReportingUIModel() {
		return ExpenseReportingUI.getDefault().getExpenseReportingUIModel();
	}

	public void dispose() {
		getExpenseReportingUIModel().removeListener(expenseReportingUIModelListener);
		super.dispose();
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

	public void setBinder(final ExpensesBinder expensesBinder) {
		this.expensesBinder = expensesBinder;
		asyncExec(new Runnable() {
			public void run() {
				setEnabled(expensesBinder != null);
				viewer.setInput(expensesBinder);
			}			
		});
	}

	private void setEnabled(boolean enabled) {
		viewer.getControl().setEnabled(enabled);
		addButton.setEnabled(enabled);
		removeButton.setEnabled(enabled);
	}
}
