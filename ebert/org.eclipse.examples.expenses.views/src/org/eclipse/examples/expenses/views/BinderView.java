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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.examples.expenses.core.CollectionPropertyChangeEvent;
import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.examples.expenses.ui.ExpenseReportingUI;
import org.eclipse.examples.expenses.views.model.ExpenseReportingViewModel;
import org.eclipse.examples.expenses.views.model.ExpenseReportingViewModelListener;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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

	private static final String BINDER_VIEW_CUSTOMIZERS = "org.eclipse.examples.expenses.views.binderViewCustomizers";

	/**
	 * This value is the id of the extension that defines this view.
	 * The fully qualified name of this class just happens to share the same name,
	 * so&mdash;as a matter of convenience&mdash;we're exploiting that similarity here
	 * rather than hard-code the name as a string.
	 */
	public static final String ID = BinderView.class.getName();
	
	ListViewer expenseReportViewer;
	
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
				expenseReportViewer.refresh();
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
					expenseReportViewer.add(event.added);
					expenseReportViewer.remove(event.removed);
				}				
			});
		}		
	};

	ExpenseReportingViewModelListener expenseReportingUIModelListener = new ExpenseReportingViewModelListener() {
		public void binderChanged(ExpensesBinder binder) {
			setBinder(binder);
		}

		public void lineItemChanged(LineItem item) {}

		public void reportChanged(ExpenseReport report) {}
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
					expenseReportViewer.update(event.getSource(), new String[] {event.getProperty()});
				}
			});
		}
	};
	
	/**
	 * This method, while public is <em>not</em> part of the public API. This
	 * method is called as part of the part creation process by the framework.
	 */
	public void createPartControl(final Composite parent) {
		parent.setLayout(new GridLayout(1, true));
		createExpenseReportViewer(parent);

		customizeBinderView(parent);
		
		/*
		 * Add a listener to the UI Model; should the binder change, we'll update
		 * ourselves to reflect that change.
		 */
		getExpenseReportingViewModel().addListener(expenseReportingUIModelListener);
		setBinder(getExpenseReportingViewModel().getBinder());
		
		expenseReportViewer.setInput(getBinder());
	}

	private void customizeBinderView(final Composite parent) {
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(BINDER_VIEW_CUSTOMIZERS);
			for(int index=0;index<elements.length;index++) {
				try {
					IBinderViewCustomizer customizer = (IBinderViewCustomizer) elements[index].createExecutableExtension("class");
					customizer.postCreateBinderView(this, parent);
				} catch (CoreException e) {
					// TODO Need to log this.
				}
		}
	}

	private void createExpenseReportViewer(Composite parent) {
		expenseReportViewer = new ListViewer(parent, SWT.BORDER);
		expenseReportViewer.setContentProvider(contentProvider);
		expenseReportViewer.setLabelProvider(labelProvider);		
		expenseReportViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				getExpenseReportingViewModel().setReport((ExpenseReport) selection.getFirstElement());
			}			
		});
		getSite().setSelectionProvider(expenseReportViewer);
		expenseReportViewer.getList().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	/**
	 * This method obtains the UI Model from the activator.
	 * 
	 * @see ExpenseReportingUI#getExpenseReportingViewModel()
	 * 
	 * @return An instance of a class that implements {@link IExpenseReportingUIModel} 
	 * that is appropriate for the current user.
	 */
	private ExpenseReportingViewModel getExpenseReportingViewModel() {
		return ExpenseReportingUI.getDefault().getExpenseReportingViewModel();
	}

	/**
	 * This method is <em>not</em> part of the public API.
	 */
	public void dispose() {
		getExpenseReportingViewModel().removeListener(expenseReportingUIModelListener);
		super.dispose();
	}

	/**
	 * This method returns the binder that is currently being considered by
	 * the receiver. This method is part of the public API.
	 * @return
	 */
	public ExpensesBinder getBinder() {
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
		
	protected void updateViewerInput(ExpensesBinder expensesBinder) {
		expenseReportViewer.setInput(expensesBinder);
	}

	/**
	 * This method is <em>not</em> part of the public API.
	 */
	public void setFocus() {
		expenseReportViewer.getList().setFocus();
	}

	public Viewer getExpenseReportViewer() {
		return expenseReportViewer;
	}

	public void setBinder(final ExpensesBinder expensesBinder) {
		this.expensesBinder = expensesBinder;
		asyncExec(new Runnable() {
			public void run() {
				setEnabled(expensesBinder != null);
				expenseReportViewer.setInput(expensesBinder);
			}			
		});
	}

	private void setEnabled(boolean enabled) {
		expenseReportViewer.getControl().getParent().setEnabled(enabled);
	}
}
