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
import java.util.Properties;

import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.ExpenseType;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.examples.expenses.core.ObjectWithProperties;
import org.eclipse.examples.expenses.ui.ExpenseReportingUI;
import org.eclipse.examples.expenses.views.model.ExpenseReportingViewModel;
import org.eclipse.examples.expenses.views.model.ExpenseReportingViewModelListener;
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
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPartSite;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

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
	
	TableViewer lineItemTableViewer;
	
	// TODO I'd rather these not be public
	public TableColumn dateColumn;
	public TableColumn commentColumn;
	
	ExpenseReport expenseReport;

	Text titleText;
	Button removeButton;
	
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
	private Button addButton;
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
		createButtons(parent);	
				
		customizeView(parent);

		/*
		 * Add a listener to the UI Model; should the binder change, we'll update
		 * ourselves to reflect that change.
		 */
		getExpenseReportingViewModel().addListener(expenseReportingUIModelListener);
		setReport(getExpenseReportingViewModel().getReport());
		
		startEventHandlers();

		updateRemoveButton();
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
	
	void createAddButton(Composite parent) {
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

	void createRemoveButton(Composite parent) {
		removeButton = new Button(parent, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent arg0) {
				if (expenseReport == null) return;
				LineItem lineItem = (LineItem) ((IStructuredSelection)lineItemTableViewer.getSelection()).getFirstElement();
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
		lineItemTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateRemoveButton();
			}			
		});
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

	/**
	 * This method starts the various services that handle events
	 * generated by the domain model objects.
	 */
	void startEventHandlers() {
		BundleContext context = ExpenseReportingUI.getDefault().getContext();
		startExpenseReportChangedHandlerService(context);
		startLineItemAddedHandlerService(context);
		startLineItemRemovedHandlerService(context);
		startLineItemChangedHandlerService(context);
	}
	
	/**
	 * This method starts an {@link EventHandler} service that listens for
	 * property changes to {@link LineItem} instances. This event handler
	 * is used by the OSGi {@link EventAdmin} service to handle matching
	 * events as they occur. All subclasses of {@link ObjectWithProperties}
	 * dispatch events through the {@link EventAdmin} service.
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
						lineItemTableViewer.update(source, new String[] {property});
					}
				});
				
			}			
		};
		
		Properties properties = new Properties();
		properties.put(EventConstants.EVENT_TOPIC, ObjectWithProperties.PROPERTY_CHANGE_TOPIC);
		properties.put(EventConstants.EVENT_FILTER, "(" + ObjectWithProperties.SOURCE_TYPE + "=" + LineItem.class.getName() +")");
		
		lineItemChangedHandlerService = context.registerService(EventHandler.class.getName(), handler, properties);
	}
	
	/**
	 * This method starts the {@link #lineItemAddedHandlerService} service. This service
	 * is notified (via the {@link EventHandler#handleEvent(Event)} method) whenever
	 * a {@link LineItem} instance is added to an {@link ExpenseReport}.
	 * 
	 * @see #lineItemAddedHandlerService
	 * @param context
	 */
	void startLineItemAddedHandlerService(BundleContext context) {
		EventHandler handler = new EventHandler() {
			public void handleEvent(final Event event) {
				if (event.getProperty(ObjectWithProperties.SOURCE) != expenseReport) return;
				asyncExec(new Runnable() {
					public void run() {
						lineItemTableViewer.add(event.getProperty(ObjectWithProperties.OBJECT_ADDED));
					}
				});
			}			
		};
		Properties properties = new Properties();
		properties.put(EventConstants.EVENT_TOPIC, ObjectWithProperties.PROPERTY_CHANGE_TOPIC);
		properties.put(EventConstants.EVENT_FILTER, "(&(" + ObjectWithProperties.SOURCE_TYPE + "=" + ExpenseReport.class.getName() +")(eventType=" + ObjectWithProperties.OBJECT_ADDED + "))");
		
		lineItemAddedHandlerService = context.registerService(EventHandler.class.getName(), handler, properties);
	}
	
	/**
	 * This method starts the {@link #lineItemRemovedHandlerService} service. This service
	 * is notified (via the {@link EventHandler#handleEvent(Event)} method) whenever
	 * a {@link LineItem} instance is removed from an {@link ExpenseReport}.
	 * 
	 * @see #lineItemRemovedHandlerService
	 * @param context
	 */
	void startLineItemRemovedHandlerService(BundleContext context) {
		EventHandler handler = new EventHandler() {
			public void handleEvent(final Event event) {
				if (event.getProperty(ObjectWithProperties.SOURCE) != expenseReport) return;
				asyncExec(new Runnable() {
					public void run() {
						lineItemTableViewer.remove(event.getProperty(ObjectWithProperties.OBJECT_REMOVED));
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
				handleExpenseReportPropertyChangedEvent(event);				
			}			
		};
		Properties properties = new Properties();
		properties.put(EventConstants.EVENT_TOPIC, ObjectWithProperties.PROPERTY_CHANGE_TOPIC);
		properties.put(EventConstants.EVENT_FILTER, "(" + ExpenseReport.class.getName() +"=true)");
		
		expenseReportChangedEventHandlerService = context.registerService(EventHandler.class.getName(), handler, properties);
	}
	
	public void dispose() {
		lineItemAddedHandlerService.unregister();
		lineItemRemovedHandlerService.unregister();
		lineItemChangedHandlerService.unregister();
		expenseReportChangedEventHandlerService.unregister();
		
		getExpenseReportingViewModel().removeListener(expenseReportingUIModelListener);
		
		super.dispose();
	}

	void updateRemoveButton() {
		removeButton.setEnabled(viewerHasSelection());
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
				updateTitleField();
				updateLineItemsTable();
			}			
		});
	}

	public void setFocus() {
		lineItemTableViewer.getControl().setFocus();
	}

	public Viewer getViewer() {
		return lineItemTableViewer;
	}

	/**
	 * This method, curiously enough, updates the title field to reflect the
	 * current state of the title property of the current {@link ExpenseReport}.
	 * This method must be run in the UI thread.
	 */
	void updateTitleField() {
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

	/**
	 * This method handles property change events reported by any instance of
	 * {@link ExpenseReport}. For our purposes, we only care about changes made
	 * to the {@link #expenseReport} under observation (events generated for any
	 * other instance are discarded). For now, the only simple property for an
	 * ExpenseReport that we display is the title; if the title property
	 * changes, we update the {@link #titleText} field to reflect the change.
	 * 
	 * <p>
	 * This method can be run from any thread.
	 * 
	 * @param event
	 *            an instance of {@link Event} detailing the property change.
	 */
	void handleExpenseReportPropertyChangedEvent(Event event) {
		if (event.getProperty(ObjectWithProperties.SOURCE) != expenseReport) return;
		
		final String property = (String)event.getProperty(ObjectWithProperties.PROPERTY_NAME);
		if (ExpenseReport.TITLE_PROPERTY.equals(property)) {
			asyncExec(new Runnable() {
				public void run() {
					updateTitleField();
				}
			});
		}
	}
}