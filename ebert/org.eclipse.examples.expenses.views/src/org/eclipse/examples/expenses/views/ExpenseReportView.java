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

import java.text.DateFormat;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.ExpenseType;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.examples.expenses.core.ObjectWithProperties;
import org.eclipse.examples.expenses.ui.ExpenseReportingUI;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
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
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

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
 * <p>
 * An interesting feature of this view is that it uses the OSGi Event Service to
 * keep itself up-to-date. When an instance is created, it registers an
 * {@link EventHandler} service that is notified of any changes to the
 * underlying objects. The event service is essentially a queue (similar to JMS)
 * that delivers events on a topic. Instances of this view listen to events that
 * are put on the {@value ObjectWithProperties#PROPERTY_CHANGE_TOPIC} topic and
 * update themselves accordingly.
 * <p>
 * Use of the event service works quite well for RCP and eRCP since the number
 * of objects that we are concerned with is quite small. In a RAP-based
 * application, the number of objects is potentially huge (owing to the fact
 * that the application will potentially be serving hundreds or thousands of
 * clients) which could make the use of the event service for this sort of
 * notification prohibitively expensive.
 */
public class ExpenseReportView extends AbstractView {

	private static final int DATE_COLUMN = 0;
	private static final int TYPE_COLUMN = 1;
	private static final int AMOUNT_COLUMN = 2;
	private static final int COMMENT_COLUMN = 3;

	/**
	 * As a matter of convenience, the ID of this instance is the same
	 * as the fully-qualified class name. This is the same as the value
	 * given for the ID in the plugin.xml file.
	 */
	public static final String ID = ExpenseReportView.class.getName();
	
	TableViewer viewer;
	public TableColumn dateColumn;
	public TableColumn commentColumn;
	
	private ExpenseReport expenseReport;

	private Text titleText;
	
	/**
	 * This field provides an {@link IContentProvider} that takes an
	 * {@link ExpenseReport} for input (indirectly via the
	 * {@link Viewer#setInput(Object)} method).
	 */
	IContentProvider contentProvider = new IStructuredContentProvider() {
		public Object[] getElements(Object input) {
			if (input instanceof ExpenseReport) {
				return ((ExpenseReport)input).getLineItems();
			}
			return new Object[0];
		}

		public void dispose() {				
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
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
				// TODO Need currency formatter
				return String.valueOf(lineItem.getAmount()); 
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
	 * This field contains the listener that is installed on the workbench's
	 * selection service. 
	 * 
	 * @see IWorkbenchWindow#getSelectionService()
	 */
	ISelectionListener selectionListener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			handleSelection(selection);
		}	
	};

	/**
	 * This {@link DateFormat} instance is used to format dates displayed in the
	 * table.
	 * 
	 * TODO Need to find a way to use the local of the user when in RAP
	 */
	final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
		
	/**
	 * The sorter determines how the table is sorted. This sorter
	 * sorts by date and type. That is, items are first sorted by
	 * date; if two items have the same date, then they are sorted by type.
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
			if (date1 == null) return -1;
			return date1.compareTo(date2);
		}

		/**
		 * Convenience method for comparing two {@link ExpenseType}s, either of
		 * which could be null.
		 */
		int compareTypes(ExpenseType type1, ExpenseType type2) {
			if (type1 == null && type2 == null) return 0;
			if (type1 == null) return -1;
			return type1.compareTo(type2);
		}
	};
	
	/**
	 * This field holds the {@link ServiceRegistration} object for the
	 * {@link EventHandler} service that listens for changes to the basic
	 * properties of an {@link ExpenseReport}. This object is used to keep track
	 * of the service that we've registered and provide us with a convenient
	 * mechanism for unregistering the service when we're done.
	 * 
	 * @see #startExpenseReportChangedHandlerService(BundleContext)
	 * @see #dispose()
	 */
	ServiceRegistration expenseReportChangedEventHandlerService;
	
	/**
	 * This field holds the {@link ServiceRegistration} object for the
	 * {@link EventHandler} service that listens for additions to the
	 * {@link LineItem}s tracked by an {@link ExpenseReport}. This object is
	 * used to keep track of the service that we've registered and provide us
	 * with a convenient mechanism for unregistering the service when we're
	 * done.
	 * 
	 * @see #startLineItemAddedHandlerService(BundleContext)
	 * @see #dispose()
	 */
	ServiceRegistration lineItemAddedHandlerService;

	/**
	 * This field holds the {@link ServiceRegistration} object for the
	 * {@link EventHandler} service that listens for removals from the
	 * {@link LineItem}s tracked by an {@link ExpenseReport}. This object is
	 * used to keep track of the service that we've registered and provide us
	 * with a convenient mechanism for unregistering the service when we're
	 * done.
	 * 
	 * @see #startLineItemRemovedHandlerService(BundleContext)
	 * @see #dispose()
	 */
	ServiceRegistration lineItemRemovedHandlerService;
	
	/**
	 * This field holds the {@link ServiceRegistration} object for the
	 * {@link EventHandler} service that listens for changes to the basic
	 * properties of a {@link LineItem}. This object is used to keep track of
	 * the service that we've registered and provide us with a convenient
	 * mechanism for unregistering the service when we're done.
	 * 
	 * @see #startLineItemChangedHandlerService(BundleContext)
	 * @see #dispose()
	 */
	ServiceRegistration lineItemChangedHandlerService;
			
	/**
	 * This is where it all happens. This method is called to actually create
	 * the view. As part of the creation process, user interface component
	 * are assembled, listeners are hooked up, and services are created.
	 */
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		Composite title = createTitleArea(parent);
		title.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		viewer = createTableViewer(parent);
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		getSite().setSelectionProvider(viewer);
		
		Composite buttons = createButtonArea(parent);	
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		createAddButton(buttons);
		createRemoveButton(buttons);
				
		customizeView(parent);
		
		hookSelectionListener();
		
		BundleContext context = ExpenseReportingUI.getDefault().getContext();
		startLineItemAddedHandlerService(context);
		startLineItemRemovedHandlerService(context);
		startLineItemChangedHandlerService(context);
	}

	/**
	 * This method return an instance of {@link DateFormat} that is appropriate
	 * for formatting the output of dates for the current user.
	 * <p>
	 * At present, this implementation isn't smart enough to know what the current
	 * user needs to see. Bug 239865 addresses this issue.
	 * @return
	 */
	protected DateFormat getDateFormat() {
		return dateFormat;
	}

	/**
	 * This method starts an {@link EventHandler} service that listens for
	 * property changes to {@link LineItem} instances.
	 * 
	 * <p>
	 * This service listens on the
	 * {@link ObjectWithProperties#PROPERTY_CHANGE_TOPIC} topic, with an
	 * inclusion filter (via the {@link EventConstants#EVENT_FILTER} property)
	 * that only accepts events where the
	 * {@link ObjectWithProperties#SOURCE_TYPE} is {@link LineItem}.
	 * 
	 * @see ObjectWithProperties#postEvent(String, Object, Object)
	 * @param context
	 *            an instance of BundleContext to use to register the
	 *            EventListener.
	 */
	void startLineItemChangedHandlerService(BundleContext context) {
		/*
		 * Create the event handler. This is the object that will
		 * be notified when a matching event is delivered to the
		 * event service.
		 */
		EventHandler handler = new EventHandler() {
			public void handleEvent(Event event) {
				final Object source = event.getProperty(ObjectWithProperties.SOURCE);
				final String property = (String)event.getProperty(ObjectWithProperties.PROPERTY_NAME);
				/*
				 * The view must be updated from within the UI thread, so
				 * use an asyncExec block to do the actual update.
				 */
				asyncExec(new Runnable() {
					public void run() {
						viewer.update(source, new String[] {property});
					}
				});
				
			}			
		};
		
		Properties properties = new Properties();
		properties.put(EventConstants.EVENT_TOPIC, ObjectWithProperties.PROPERTY_CHANGE_TOPIC);
		properties.put(EventConstants.EVENT_FILTER, "(" + ObjectWithProperties.SOURCE_TYPE + "=" + LineItem.class.getName() +")");
		
		lineItemChangedHandlerService = context.registerService(EventHandler.class.getName(), handler, properties);
	}
	
	void startLineItemAddedHandlerService(BundleContext context) {
		EventHandler handler = new EventHandler() {
			public void handleEvent(final Event event) {
				if (event.getProperty(ObjectWithProperties.SOURCE) != expenseReport) return;
				asyncExec(new Runnable() {
					public void run() {
						viewer.add(event.getProperty(ObjectWithProperties.OBJECT_ADDED));
					}
				});
			}			
		};
		Properties properties = new Properties();
		properties.put(EventConstants.EVENT_TOPIC, ObjectWithProperties.PROPERTY_CHANGE_TOPIC);
		properties.put(EventConstants.EVENT_FILTER, "(&(" + ObjectWithProperties.SOURCE_TYPE + "=" + ExpenseReport.class.getName() +")(eventType=" + ObjectWithProperties.OBJECT_ADDED + "))");
		
		lineItemAddedHandlerService = context.registerService(EventHandler.class.getName(), handler, properties);
	}
	
	void startLineItemRemovedHandlerService(BundleContext context) {
		EventHandler handler = new EventHandler() {
			public void handleEvent(final Event event) {
				if (event.getProperty(ObjectWithProperties.SOURCE) != expenseReport) return;
				asyncExec(new Runnable() {
					public void run() {
						viewer.remove(event.getProperty(ObjectWithProperties.OBJECT_REMOVED));
					}
				});
			}			
		};
		Properties properties = new Properties();
		properties.put(EventConstants.EVENT_TOPIC, ObjectWithProperties.PROPERTY_CHANGE_TOPIC);
		properties.put(EventConstants.EVENT_FILTER, "(&(" + ObjectWithProperties.SOURCE_TYPE + "=" + ExpenseReport.class.getName() +")(eventType=" + ObjectWithProperties.OBJECT_REMOVED + "))");
		
		lineItemRemovedHandlerService = context.registerService(EventHandler.class.getName(), handler, properties);
	}
	
	void startExpenseReportChangedHandlerService(BundleContext context) {
		EventHandler handler = new EventHandler() {
			public void handleEvent(Event event) {
				if (event.getProperty(ObjectWithProperties.SOURCE) != expenseReport) return;
				final String property = (String)event.getProperty(ObjectWithProperties.PROPERTY_NAME);
				if (ExpenseReport.TITLE_PROPERTY.equals(property)) {
					updateTitleField();
				}				
			}			
		};
		Properties properties = new Properties();
		properties.put(EventConstants.EVENT_TOPIC, ObjectWithProperties.PROPERTY_CHANGE_TOPIC);
		properties.put(EventConstants.EVENT_FILTER, "(" + ObjectWithProperties.SOURCE_TYPE + "=" + LineItem.class.getName() +")");
		
		expenseReportChangedEventHandlerService = context.registerService(EventHandler.class.getName(), handler, properties);
	}
	
	protected void updateTitleField() {
		asyncExec(new Runnable() {
			public void run() {
				titleText.setText(expenseReport == null ? "" : expenseReport.getTitle());
			}
		});
	}

	public void dispose() {
		lineItemAddedHandlerService.unregister();
		lineItemRemovedHandlerService.unregister();
		lineItemChangedHandlerService.unregister();
		expenseReportChangedEventHandlerService.unregister();
		
		unhookSelectionListener();
		super.dispose();
	}

	void hookSelectionListener() {
		ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
		if (selectionService == null) return;
		selectionService.addSelectionListener(selectionListener);
	}
	
	void unhookSelectionListener() {
		ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
		if (selectionService == null) return;
		selectionService.removeSelectionListener(selectionListener);
	}

	private Composite createTitleArea(Composite parent) {
		Composite titleArea = new Composite(parent, SWT.NONE);
		titleArea.setLayout(new GridLayout(2, false));
		Label titleLabel = new Label(titleArea, SWT.NONE);
		titleLabel.setText("Title:");
		
		titleText = new Text(titleArea, SWT.BORDER);
		titleText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		titleText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				expenseReport.setTitle(titleText.getText());
			}			
		});
		return titleArea;
	}

	private TableViewer createTableViewer(Composite parent) {
		TableViewer viewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.getTable().setHeaderVisible(true);	
		
		createDateColumn(viewer);
		createTypeColumn(viewer);
		createAmountColumn(viewer);
		createCommentColumn(viewer);
		
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(labelProvider);
		viewer.setSorter(dateSorter);

		getSite().setSelectionProvider(viewer);
		
		return viewer;
	}
	
	private void createAddButton(Composite parent) {
		Button addButton = new Button(parent, SWT.PUSH);
		addButton.setText("Add");
		addButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				if (expenseReport == null) return;
				expenseReport.addLineItem(new LineItem());
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {

			}			
		});
	}

	private void createRemoveButton(Composite parent) {
		final Button removeButton = new Button(parent, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				if (expenseReport == null) return;
				LineItem lineItem = (LineItem) ((IStructuredSelection)viewer.getSelection()).getFirstElement();
				if (lineItem == null) return;
				expenseReport.removeLineItem(lineItem);
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {

			}			
		});
		/*
		 * Add a listener to the selection on the viewer. When the
		 * selection changes, update the state of the remove button.
		 */
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateRemoveButton(removeButton);
			}			
		});
		updateRemoveButton(removeButton);
	}
	
	void updateRemoveButton(Button removeButton) {
		removeButton.setEnabled(viewerHasSelection());
	}

	/**
	 * This method returns true if the viewer has a valid
	 * selection; it returns false otherwise.
	 */
	boolean viewerHasSelection() {
		return !((IStructuredSelection)viewer.getSelection()).isEmpty();
	}

	protected void handleSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			handleSelection((IStructuredSelection)selection);
		}
	}

	private void handleSelection(IStructuredSelection selection) {
		Object object = selection.getFirstElement();
		if (object instanceof ExpenseReport) {
			setReport((ExpenseReport)object);
		}
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
				titleText.setText(expenseReport == null ? "" : expenseReport.getTitle());
				viewer.setInput(expenseReport);
				viewer.getTable().setEnabled(expenseReport != null);
			}			
		});
	}

	final void createCommentColumn(TableViewer viewer) {
		commentColumn = new TableColumn(viewer.getTable(), SWT.LEFT);
		commentColumn.setText("Comment");
		commentColumn.setWidth(200);
	}
	
	final void createAmountColumn(TableViewer viewer) {
		TableColumn amountColumn = new TableColumn(viewer.getTable(), SWT.LEFT);
		amountColumn.setText("Amount");
		amountColumn.setWidth(120);
	}
	
	final void createTypeColumn(TableViewer viewer) {
		TableColumn typeColumn = new TableColumn(viewer.getTable(), SWT.LEFT);
		typeColumn.setText("Type");
		typeColumn.setWidth(180);
	}
	
	void createDateColumn(TableViewer viewer) {
		dateColumn = new TableColumn(viewer.getTable(), SWT.LEFT);
		dateColumn.setText("Date");
		dateColumn.setWidth(120);		
	}
	
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public Viewer getViewer() {
		return viewer;
	}
}