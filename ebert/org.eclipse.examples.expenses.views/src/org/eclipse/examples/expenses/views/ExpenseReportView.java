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

import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.examples.expenses.core.CollectionPropertyChangeEvent;
import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.ExpenseType;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.examples.expenses.ui.ExpenseReportingUI;
import org.eclipse.examples.expenses.views.model.ExpenseReportingViewModel;
import org.eclipse.examples.expenses.views.model.ExpenseReportingViewModelListener;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPartSite;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.NumberFormat;

/**
 * This class provides a view that lets the user modify an {@link ExpenseReport}
 * . The main focus of the view is a {@link TableViewer} that lists the
 * {@link LineItem}s maintained by the {@link ExpenseReport}. New
 * {@link LineItem}s can be added and existing ones removed using buttons.
 * <p>
 * The implementation is limited Java 1.3 syntax, the CDC 1.0/Foundation 1.0
 * library, and the subset of Eclipse Platform APIs common to RCP, RAP, and
 * eRCP. A customization hook is provided so that the view can be extended to
 * exploit features that are available on specific platforms.
 */
public class ExpenseReportView extends AbstractView {

	static final int DATE_COLUMN = 0;
	static final int TYPE_COLUMN = 1;
	static final int AMOUNT_COLUMN = 2;
	static final int COMMENT_COLUMN = 3;

	/**
	 * As a matter of convenience, the ID of this instance is the same
	 * as the fully-qualified class name. This is the same as the value
	 * given for the ID in the plugin.xml file.
	 */
	public static final String ID = ExpenseReportView.class.getName();
	
	private static final String EXPENSE_REPORT_VIEW_CUSTOMIZERS = "org.eclipse.examples.expenses.views.expenseReportViewCustomizers";

	TableViewer lineItemTableViewer;
	
	// TODO I'd rather these not be public
	public TableColumn dateColumn;
	public TableColumn commentColumn;
	
	ExpenseReport expenseReport;

	Text titleText;
	
	/**
	 * This field provides an {@link IContentProvider} that takes an
	 * {@link ExpenseReport} for input (indirectly via the
	 * {@link Viewer#setInput(Object)} method).
	 */
	IStructuredContentProvider contentProvider = new IStructuredContentProvider() {
		public Object[] getElements(Object input) {
			if (input instanceof ExpenseReport) {
				return ((ExpenseReport)input).getLineItems();
			}
			return new Object[0];
		}

		public void dispose() {				
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			unhookPropertyChangeListener((ExpenseReport)oldInput);
			hookPropertyChangeListener((ExpenseReport)newInput);
		}
	};	

	/**
	 * This {@link ITableLabelProvider} is used to determine how to present
	 * the information contained in the {@link LineItem} instances displayed
	 * in the table.
	 */
	ITableLabelProvider labelProvider = new ITableLabelProvider() {
		public Image getColumnImage(Object lineItem, int index) {
			return null; // No images (for now, at least)
		}

		/**
		 * This method is called to determine what needs to be displayed
		 * for a particular object in a particular column. For our table,
		 * the objects should all be instances of {@link LineItem}.
		 */
		public String getColumnText(Object object, int index) {
			LineItem lineItem = (LineItem)object;
			switch (index) {
			case DATE_COLUMN: 
				return getDateFormat().format(lineItem.getDate());			
			case TYPE_COLUMN: 
				ExpenseType type = lineItem.getType();
				if (type == null) return "<specify type>";
				return type.getTitle();
			case AMOUNT_COLUMN: 
				return getCurrencyFormat().format(lineItem.getAmount());
			case COMMENT_COLUMN: 
				return lineItem.getComment();
			default: return "";
			}
		}

		/**
		 * This method is called to determine if the row representing
		 * the <code>lineItem</code> parameter needs to be relabeled.
		 * This method returns <code>true</code> if the given property
		 * is one that is displayed by this label provider.
		 */
		public boolean isLabelProperty(Object lineItem, String property) {
			/* 
			 * This is probably excessive and wasteful. It might be better
			 * to just always answer true since we're displaying all of the
			 * properties of a LineItem anyway. 
			 */
			if (LineItem.DATE_PROPERTY.equals(property)) return true;
			if (LineItem.AMOUNT_PROPERTY.equals(property)) return true;
			if (LineItem.TYPE_PROPERTY.equals(property)) return true;
			if (LineItem.COMMENT_PROPERTY.equals(property)) return true;
			
			return false;
		}

		public void addListener(ILabelProviderListener listener) {			
		}

		public void removeListener(ILabelProviderListener arg0) {	
		}	
		
		public void dispose() {	
		}
	};
		
	/**
	 * This {@link DateFormat} instance is used to format dates displayed in the
	 * table.
	 * 
	 * TODO Need to find a way to use the locale of the user when in RAP (see Bug 237646)
	 */
	final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
		
	/**
	 * {@link LineItem}s are sorted in the viewer by two criteria. They are
	 * first sorted chronologically (earlier to later) by the date. If two line
	 * items have the same date, they are then sorted by their type (types are
	 * sorted according to their ordinality (lowest to highest).
	 * <p>
	 * It is possible that either or both of the date and type can be
	 * <code>null</code>, so we need to account for that.
	 * <ul>
	 * <li>Line items that have the date set are placed before those that are
	 * not.</li>
	 * <li>Those line items with the same date (including <code>null</code>) are
	 * sorted by their type</li>
	 * </ul>
	 * For those line items that share the same date,
	 * <ul>
	 * <li>Line items with the type set are placed before those that are not.</li>
	 * <li>Those line items with the same type (including <code>null</code>) are
	 * not explicitly sorted in any particular order.</li>
	 * </ul>
	 */
	ViewerSorter dateSorter = new ViewerSorter() {
		/**
		 * This method is called when a change occurs in the table and the table
		 * needs to know if it needs to be resorted. The table is only resorted
		 * if the "date" property of the {@link LineItem} is changed. This is a
		 * pretty cool feature of the {@link TableViewer}; when we tell it to
		 * {@link TableViewer#update(Object, String[])} an item, we give it a
		 * list of the properties that have changed. If one of those properties
		 * is a sorter property (as defined below), the table is resorted;
		 * otherwise, well-enough is left alone.
		 */
		public boolean isSorterProperty(Object element, String property) {
			if (property == LineItem.DATE_PROPERTY) return true;
			if (property == LineItem.TYPE_PROPERTY) return true;
			return false;
		}
		
		/**
		 * This compare method sorts on two criteria: first, items
		 * are sorted by date; then they are sorted by type.
		 */
		public int compare(Viewer viewer, Object arg1, Object arg2) {
			LineItem lineItem1 = (LineItem) arg1;
			LineItem lineItem2 = (LineItem) arg2;
			
			int result = compareDates(lineItem1.getDate(), lineItem2.getDate());
			
			if (result != 0) return result;
			
			return compareTypes(lineItem1.getType(), lineItem2.getType());
		}

		/**
		 * Convenience method for comparing two {@link Date}s, either of which
		 * could be null.
		 */
		int compareDates(Date date1, Date date2) {
			if (date1 == null && date2 == null) return 0;
			if (date1 == null) return 1;
			if (date2 == null) return -1;
			return date1.compareTo(date2);
		}

		/**
		 * Convenience method for comparing two {@link ExpenseType}s, either of
		 * which could be null.
		 */
		int compareTypes(ExpenseType type1, ExpenseType type2) {
			if (type1 == null && type2 == null) return 0;
			if (type1 == null) return 1;
			if (type2 == null) return -1;
			return type1.compareTo(type2);
		}
	};
	
	private Composite titleArea;
			

	ExpenseReportingViewModelListener expenseReportingUIModelListener = new ExpenseReportingViewModelListener() {
		public void reportChanged(ExpenseReport report) {
			setReport(report);
		}

		public void binderChanged(ExpensesBinder binder) {}

		public void lineItemChanged(LineItem item) {}
	};
	
	/**
	 * This is where it all happens. This method is called to actually create
	 * the view. As part of the creation process, user interface component
	 * are assembled, listeners are hooked up, and services are created.
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		createTitleArea(parent);		
		createLineItemTableViewer(parent);		
				
		customizeExpenseReportView(parent);

		/*
		 * Add a listener to the UI Model; should the binder change, we'll update
		 * ourselves to reflect that change.
		 */
		getExpenseReportingViewModel().addListener(expenseReportingUIModelListener);
		setReport(getExpenseReportingViewModel().getReport());
	}

	private void customizeExpenseReportView(final Composite parent) {
		ExpenseReportPrivilegedAccessor proxy = new ExpenseReportPrivilegedAccessor(parent, this);
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXPENSE_REPORT_VIEW_CUSTOMIZERS);
			for(int index=0;index<elements.length;index++) {
				try {
					IExpenseReportViewCustomizer customizer = (IExpenseReportViewCustomizer) elements[index].createExecutableExtension("class");
					customizer.postCreateExpenseReportView(proxy);
				} catch (CoreException e) {
					// TODO Need to log this.
				}
		}
	}
	
	IPropertyChangeListener expenseReportPropertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getSource() != expenseReport) {
				/*
				 * This check has been included to confirm that we don't have a leak
				 * The only ExpenseReport that we should be getting notifications
				 * from is the one that we're referencing.
				 */
				ExpenseReportingUI.getDefault().getLog().log(new Status(Status.ERROR, ExpenseReportingUI.PLUGIN_ID, "Unexpected ExpenseReport providing notification to ExpenseReportView."));
			}
			if (ExpenseReport.TITLE_PROPERTY.equals(event.getProperty())) {
				handleTitlePropertyChanged(event);
			} else if (ExpenseReport.LINEITEMS_PROPERTY.equals(event.getProperty())) {
				if (event instanceof CollectionPropertyChangeEvent) {
					handleLineItemsAddedOrRemoved((CollectionPropertyChangeEvent)event);
				}
			}
		}

		
	};
	
	IPropertyChangeListener lineItemPropertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			lineItemTableViewer.update(event.getSource(), new String[] {event.getProperty()});
		}
	};
	
	/**
	 * This method adds {@link IPropertyChangeListener}s to an
	 * {@link ExpenseReport} instance and any {@link LineItem}s it contains.
	 * These listeners will inform the receiver of changes to their properties,
	 * so that the view can be updated. A change, for example to a LineItem's
	 * {@link LineItem#DATE_PROPERTY} would require that we update the
	 * corresponding entry in the table.
	 * <p>
	 * This method synchronizes on the ExpenseReport because there is some
	 * potential that LineItem instances can be added and removed by a different
	 * thread while we are adding our listeners.
	 * 
	 * @param report an instance of {@link ExpenseReport} or <code>null</code>.
	 */
	protected void hookPropertyChangeListener(ExpenseReport report) {
		if (report == null) return;
		
		synchronized (report) {
			report.addPropertyChangeListener(expenseReportPropertyChangeListener);
			LineItem[] items = report.getLineItems();
			for(int index=0;index<items.length;index++) {
				hookPropertyChangeListener(items[index]);
			}
		}
	}

	protected void hookPropertyChangeListener(LineItem item) {
		item.addPropertyChangeListener(lineItemPropertyChangeListener);
	}

	/**
	 * This method removes any listeners that have been previously installed
	 * on the {@link ExpenseReport} instance and all {@link LineItem} instances
	 * it contains.
	 * 
	 * @see #hookPropertyChangeListener(ExpenseReport)
	 * 
	 * @param report an instance of {@link ExpenseReport} or <code>null</code>.
	 */
	protected void unhookPropertyChangeListener(ExpenseReport report) {
		if (report == null) return;
		
		synchronized (report) {
			report.removePropertyChangeListener(expenseReportPropertyChangeListener);
			LineItem[] items = report.getLineItems();
			for(int index=0;index<items.length;index++) {
				unhookPropertyChangeListener(items[index]);
			}
		}
	}

	protected void unhookPropertyChangeListener(LineItem item) {
		item.removePropertyChangeListener(lineItemPropertyChangeListener);
	}
	

	/**
	 * {@link LineItem} instances have either been added to or removed from the
	 * {@link ExpenseReport}. In response, we add listeners to the affected LineItem
	 * instances and then update the table. We use the {@link #syncExec(Runnable)} method to ensure
	 * that the update occurs in the UI thread as required.
	 * 
	 * @param event an instance of {@link CollectionPropertyChangeEvent}. Must not be <code>null</code>.
	 */
	private void handleLineItemsAddedOrRemoved(final CollectionPropertyChangeEvent event) {
		for(int index=0;index<event.added.length;index++) {
			hookPropertyChangeListener((LineItem)event.added[index]);
		}
		for(int index=0;index<event.removed.length;index++) {
			unhookPropertyChangeListener((LineItem)event.removed[index]);
		}
		syncExec(new Runnable() {
			public void run() {
				lineItemTableViewer.add(event.added);
				lineItemTableViewer.remove(event.removed);
			}
			
		});
	}

	/**
	 * The {@link ExpenseReport#TITLE_PROPERTY} has changed and the corresponding field
	 * needs to be updated. We use the {@link #syncExec(Runnable)} method to ensure
	 * that the update occurs in the UI thread as required.
	 * 
	 * @param event an instance of {@link PropertyChangeEvent}. Must not be <code>null</code>.
	 */
	protected void handleTitlePropertyChanged(final PropertyChangeEvent event) {
		syncExec(new Runnable() {
			public void run() {
				/*
				 * We check to make sure that the value currently held by the
				 * text field is not the same as the value we're trying to assign into
				 * it. This avoids triggering any unnecessary events and prevents
				 * us from starting/continuing a notification loop.
				 */
				if (titleText.getText().equals(event.getNewValue())) return;
				titleText.setText((String)event.getNewValue());
			}			
		});
	}
	
	private ExpenseReportingViewModel getExpenseReportingViewModel() {
		return ExpenseReportingUI.getDefault().getExpenseReportingViewModel();
	}

	protected NumberFormat getCurrencyFormat() {
		return NumberFormat.getCurrencyInstance(getUserLocale());
	}

	/**
	 * This method creates an area at the top of the view for title information.
	 * It populates that area with a {@link Label} and a {@link Text}. A
	 * {@link ModifyListener} is added to the text which notifies the object
	 * model of changes as the user types them. This method assumes that the
	 * parent {@link Composite} has a {@link GridLayout} with one column for a
	 * layout manager.
	 * 
	 * @param parent A composite into which the title area will be created.
	 */
	void createTitleArea(Composite parent) {
		titleArea = new Composite(parent, SWT.NONE);		
		titleArea.setLayout(new GridLayout(2, false));
		Label titleLabel = new Label(titleArea, SWT.NONE);
		titleLabel.setText("Title:");
		
		titleText = new Text(titleArea, SWT.BORDER);
		titleText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		titleText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (expenseReport == null) return;
				expenseReport.setTitle(titleText.getText());
			}			
		});
	
		titleArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	}

	/**
	 * This method creates a {@link TableViewer} for displaying {@link LineItem}
	 * instances. The table has four columns. Further, this table is given to
	 * the view's {@link IWorkbenchPartSite} as the selection provider for the
	 * view; selections in the {@link #lineItemTableViewer} will be directed to
	 * the workbench {@link ISelectionService}.This method assumes that the
	 * parent {@link Composite} has a {@link GridLayout} with one column for a
	 * layout manager.
	 * 
	 * @param parent A composite into which the {@link TableViewer} will be created.
	 */
	void createLineItemTableViewer(Composite parent) {
		lineItemTableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		lineItemTableViewer.getTable().setHeaderVisible(true);	
		
		createDateColumn(lineItemTableViewer);
		createTypeColumn(lineItemTableViewer);
		createAmountColumn(lineItemTableViewer);
		createCommentColumn(lineItemTableViewer);
		
		lineItemTableViewer.setContentProvider(contentProvider);
		lineItemTableViewer.setLabelProvider(labelProvider);
		lineItemTableViewer.setSorter(dateSorter);
	
		lineItemTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				getExpenseReportingViewModel().setLineItem((LineItem) selection.getFirstElement());
			}			
		});
		
		getSite().setSelectionProvider(lineItemTableViewer);
		
		lineItemTableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	void createDateColumn(TableViewer viewer) {
		dateColumn = new TableColumn(viewer.getTable(), SWT.LEFT);
		dateColumn.setText("Date");
		dateColumn.setWidth(120);		
	}

	void createTypeColumn(TableViewer viewer) {
		TableColumn typeColumn = new TableColumn(viewer.getTable(), SWT.LEFT);
		typeColumn.setText("Type");
		typeColumn.setWidth(180);
	}

	void createAmountColumn(TableViewer viewer) {
		TableColumn amountColumn = new TableColumn(viewer.getTable(), SWT.LEFT);
		amountColumn.setText("Amount");
		amountColumn.setWidth(120);
	}

	void createCommentColumn(TableViewer viewer) {
		commentColumn = new TableColumn(viewer.getTable(), SWT.LEFT);
		commentColumn.setText("Comment");
		commentColumn.setWidth(200);
	}

	/**
	 * This method return an instance of {@link DateFormat} that is appropriate
	 * for formatting the output of dates for the current user.
	 * <p>
	 * At present, this implementation isn't smart enough to know what the current
	 * user needs to see. Bug 239865 addresses this issue.
	 * @return
	 */
	DateFormat getDateFormat() {
		return dateFormat;
	}

	public void dispose() {		
		getExpenseReportingViewModel().removeListener(expenseReportingUIModelListener);
		
		super.dispose();
	}

	/**
	 * This method returns true if the viewer has a valid
	 * selection; it returns false otherwise.
	 */
	boolean viewerHasSelection() {
		return !((IStructuredSelection)lineItemTableViewer.getSelection()).isEmpty();
	}

	/**
	 * This method sets the instance of {@link ExpenseReport} being viewed
	 * by the receiver. Note that this method can be called by any thread
	 * (user interface updates are co-ordinated with the UI Thread).
	 * 
	 * @param expenseReport an instance of {@link ExpenseReport} or <code>null</code>.
	 */
	public void setReport(final ExpenseReport expenseReport) {
		this.expenseReport = expenseReport;
		/*
		 * Update the user interface using an asyncExec block, as
		 * it is possible that the request is coming from somewhere
		 * other than the UI Thread.
		 */
		asyncExec(new Runnable() {
			public void run() {
				handleTitlePropertyChanged();
				updateLineItemsTable();
			}			
		});
	}

	public void setFocus() {
		lineItemTableViewer.getControl().setFocus();
	}

	/**
	 * This method, curiously enough, updates the title field to reflect the
	 * current state of the title property of the current {@link ExpenseReport}.
	 * This method must be run in the UI thread.
	 */
	void handleTitlePropertyChanged() {
		titleText.setText(expenseReport == null ? "" : expenseReport.getTitle());
	}
	
	/**
	 * This method, oddly enough, updates the table of {@link LineItem}s from
	 * the current {@link ExpenseReport} using a bit of a sledgehammer approach.
	 * As a result of running this method, the entire table is refreshed and
	 * it's contents entirely re-populated. This method must be run in the UI
	 * thread.
	 * 
	 * @param expenseReport
	 */
	void updateLineItemsTable() {
		lineItemTableViewer.setInput(expenseReport);
		lineItemTableViewer.getTable().setEnabled(expenseReport != null);
	}

	TableViewer getLineItemViewer() {
		return lineItemTableViewer;
	}
}