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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.examples.expenses.context.IUserContext;
import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.ExpenseType;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.examples.expenses.ui.ExpenseReportingUI;
import org.eclipse.examples.expenses.ui.fields.currency.IMoneyChangeListener;
import org.eclipse.examples.expenses.ui.fields.currency.MoneyChangeEvent;
import org.eclipse.examples.expenses.ui.fields.currency.MoneyField;
import org.eclipse.examples.expenses.views.model.IViewModelListener;
import org.eclipse.examples.expenses.views.model.ViewModel;
import org.eclipse.examples.expenses.widgets.datefield.DateField;
import org.eclipse.examples.expenses.widgets.datefield.common.DateChangeEvent;
import org.eclipse.examples.expenses.widgets.datefield.common.IDateChangeListener;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import com.ibm.icu.util.CurrencyAmount;

/**
 * This {@link ViewPart} provides a view onto a single instance of the
 * {@link LineItem} class.
 * <p>
 * Two custom widgets are used by this part: the {@link MoneyField} widget
 * displays and creates instances of {@link CurrencyAmount}; the
 * {@link DateField} widget displays and selects instances of {@link Date}.
 * 
 * @see ViewPart
 * @see ObjectWithPropertiesObservableValue
 * @see DateFieldObserverableValue
 * @see MoneyFieldObserverableValue
 */
public class LineItemView extends AbstractView {

	/**
	 * As a matter of convenience, the ID of the view is the same
	 * as the class name (with package).
	 */
	public static final String ID = LineItemView.class.getName();
	
	/**
	 * The lineItem field contains the instance of {@link LineItem}
	 * being considered by the view.
	 */
	LineItem lineItem;
			
	/**
	 * This field is an instance of {@link DateField} used to view and choose a
	 * date for the {@link LineItem}. The technology for capturing dates varies
	 * by target, so we lean heavily on the Equinox dependency matching.
	 * <p>
	 * More specifically, in the manifest for this bundle, we import the package
	 * that contains the definition of the DateField. The actual bundle that
	 * contributes this package is determined at runtime. At the time of this
	 * writing, we have two possible implementations. The
	 * org.eclipse.examples.expenses.widgets.datefield.basic bundle provides a
	 * very simple SWT-widgets-based implementation. This implementation is
	 * appropriate for the RAP and eRCP platforms which currently do not include
	 * rich date-capturing fields. The
	 * org.eclipse.examples.expenses.widgets.datefield.nebula bundle uses
	 * Nebula's CDateTime widget which is fully supported on RCP. Both of these
	 * bundles contribute the same class in the same package; at runtime, only
	 * one of them should be available.
	 * 
	 * @see #createDateField(Composite, DataBindingContext)
	 * 
	 * TODO Comment contains references to datefield bundles. Keep up-to-date.
	 */
	DateField dateField;
	
	/**
	 * This {@link ComboViewer} provides a dropdown that allows the user to
	 * select an {@link ExpenseType} for the {@link LineItem}.
	 * 
	 * @see #createTypeField(Composite)
	 */
	ComboViewer typeDropdown;
	/**
	 * {@link MoneyField} is a custom widget that is used to display and specify
	 * a monetary amount (instance of {@link CurrencyAmount}.
	 * 
	 * @see #createAmountField(Composite)
	 */
	MoneyField amountField;
	/**
	 * This {@link Text} is used to display and specify a comment for the
	 * {@link LineItem}.
	 * 
	 * @see #createCommentField(Composite)
	 */
	Text commentText;	

	/**
	 * This {@link Text} is used to display and specify an exchange rate for
	 * the {@link LineItem}.
	 * 
	 * @see #createExchangeRateField(Composite)
	 */
	Text exchangeRateText;

	/**
	 * The exchangeRateFormat takes care of parsing user input and formatting
	 * output for the exchangeRateText field.
	 * 
	 * @see #exchangeRateText
	 * @see #createExchangeRateField(Composite)
	 * @see #update()
	 * @see #propertyChangeListener
	 */
	NumberFormat exchangeRateFormat = NumberFormat.getInstance();	
	
	IViewModelListener viewModelListener = new IViewModelListener() {
		public void binderChanged(ExpensesBinder binder) {
		}

		public void lineItemChanged(final LineItem item) {
			syncExec(new Runnable() {
				public void run() {
					setLineItem(item);
				};
			});			
		}

		public void reportChanged(ExpenseReport report) {}
	};
	
	/**
	 * Instances of the {@link FieldStateHandler} class are used to manage the state
	 * of the fields. One instance of this class is created for each field. One of
	 * the main ideas behind this implementation is to make it so that we can put
	 * as much information as possible about each field and how it is managed in
	 * a single place. More specifically, most of the information and behaviour
	 * for a field is found in the corresponding &quot;create&quot; method.
	 * 
	 * @see #propertyChangeListener
	 * @see #update()
	 * @see #createAmountField(Composite)
	 * @see #createCommentField(Composite)
	 * @see #createDateField(Composite)
	 * @see #createExchangeRateField(Composite)
	 * @see #createCommentField(Composite)
	 */
	abstract class FieldStateHandler {
		/**
		 * This method is invoked when a property changes in the model.
		 * When the handler is registered, it is registered with the name of
		 * the property; property change events are filtered so that only
		 * events for the property with that name will be sent to this
		 * handler.
		 * <p>
		 * Implementors should assume that the method is being invoked
		 * in the UI Thread.
		 *  
		 * @param oldValue the value of the property before the change
		 * @param newValue the new value.
		 */
		public abstract void propertyChanged(Object oldValue, Object newValue);
		
		/**
		 * This method is invoked when the view needs to refresh or update
		 * it's contents. This method is invoked at creation and any time
		 * that the {@link LineItem} tracked by the view is changed.
		 * <p>
		 * Implementors should assume that the method is being invoked
		 * in the UI Thread.
		 */
		public abstract void update();
	}
	
	/**
	 * @see FieldStateHandler
	 */
	Map fieldHandlers = new HashMap();
	
	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {

		/**
		 * When we get a property change event, we deindex the property name
		 * into the #fieldHandlers {@link Map} to find the appropriate instance
		 * of {@link FieldStateHandler} which we invoke via the
		 * {@link FieldStateHandler#propertyChanged(Object, Object)} method to react
		 * to the change. The alternative here is a huge list of nested ifs;
		 * this is more object-oriented.
		 */
		public void propertyChange(PropertyChangeEvent event) {
			FieldStateHandler handler = (FieldStateHandler)fieldHandlers.get(event.getProperty());
			if (handler == null) return;
			handler.propertyChanged(event.getOldValue(), event.getNewValue());
		}			
	};

	private Composite parent;
	
	
	public void createPartControl(Composite parent) {
		this.parent = parent;
		parent.setLayout(new GridLayout(2, false));
				
		createDateLabel(parent);
		createDateField(parent);

		createTypeLabel(parent);
		createTypeField(parent);
				
		createAmountLabel(parent);
		createAmountField(parent);

		createExchangeRateLabel(parent);
		createExchangeRateField(parent);
		
		createCommentLabel(parent);
		createCommentField(parent);
				
		customizeLineItemView();
		
		update();
				
		startUserContextServiceTracker();		
	}


	/**
	 * The receiver needs to be disposed. This is typically a result
	 * of the receiver having been closed. We use this opportunity
	 * to clean up some of the resources we've allocated.
	 */
	public void dispose() {
		unhookListeners(lineItem);
		stopUserContextServiceTracker();
	}
	
	/**
	 * This method finds and invokes <code>lineItemViewCustomizers</code> extensions.
	 * These so-called &quot;customizers&quot; are given an opportunity to customize the
	 * receiver by adding buttons and other such things.
	 * <p>
	 * An instance of {@link LineItemViewProxy}, wrapping the receiver
	 * is passed to the customizers. This object includes method that provide the
	 * customizer with access to restricted parts of the receiver.
	 * 
	 * @see LineItemViewProxy
	 * @see ILineItemViewCustomizer
	 */
	void customizeLineItemView() {
		LineItemViewProxy proxy = new LineItemViewProxy(this);
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor(ILineItemViewCustomizer.EXTENSION_POINT_ID);
			for(int index=0;index<elements.length;index++) {
				try {
					ILineItemViewCustomizer customizer = (ILineItemViewCustomizer) elements[index].createExecutableExtension("class");
					customizer.postCreateLineItemView(proxy);
				} catch (CoreException e) {
					ExpenseReportingUI.getDefault().getLog().log(e.getStatus());
				}
		}
	}
	protected void connectToUserContext(IUserContext userContext) {
		ViewModel viewModel = userContext.getViewModel();
		viewModel.addListener(viewModelListener);
		setLineItem(viewModel.getLineItem());
	}
	
	protected void disconnectFromUserContext(IUserContext userContext) {
		userContext.getViewModel().removeListener(viewModelListener);
		setLineItem(null);
	}
		
	protected void createDateLabel(Composite parent) {
		Label dateLabel = new Label(parent, SWT.NONE);
		dateLabel.setText("Date:");			
	}
	
	/**
	 * This method creates a field for capturing the date from the user. The
	 * technology for capturing dates varies by target, so we lean heavily on
	 * the Equinox dependency matching.
	 * <p>
	 * More specifically, in the manifest for this bundle, we import the package
	 * that contains the definition of the DateField. The actual bundle that
	 * contributes this package is determined at runtime. At the time of this
	 * writing, we have two possible implementations. The
	 * org.eclipse.examples.expenses.widgets.datefield.basic bundle provides a
	 * very simple SWT-widgets-based implementation. This implementation is
	 * appropriate for the RAP and eRCP platforms which currently do not include
	 * rich date-capturing fields. The
	 * org.eclipse.examples.expenses.widgets.datefield.nebula bundle uses
	 * Nebula's CDateTime widget which is fully supported on RCP. Both of these
	 * bundles contribute the same class in the same package; at runtime, only
	 * one of them should be available.
	 * 
	 * TODO Comment contains references to datefield bundles. Keep up-to-date.
	 */
	protected void createDateField(Composite parent) {
		dateField = new DateField(parent);

		dateField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		dateField.addDateListener(new IDateChangeListener() {
			public void dateChange(DateChangeEvent event) {
				if (lineItem == null) return;
				lineItem.setDate(event.getNewValue());
			}					
		});
		fieldHandlers.put(LineItem.DATE_PROPERTY, new FieldStateHandler() {
			public void propertyChanged(Object oldValue, Object newValue) {
				if (dateField.isDisposed()) return;
				dateField.setDate((Date)newValue);
			}

			public void update() {
				if (dateField.isDisposed()) return;
				if (lineItem == null) {
					dateField.setEnabled(false);
					dateField.setDate(null);
				} else {
					dateField.setEnabled(true);
					dateField.setDate(lineItem.getDate());
				}
			}
		});
	}

	private void createTypeLabel(Composite parent) {
		Label typeLabel = new Label(parent, SWT.NONE);
		typeLabel.setText("Type:");
	}
	
	protected void createTypeField(Composite parent) {
		typeDropdown = new ComboViewer(parent, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		typeDropdown.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		typeDropdown.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return (ExpenseType[])inputElement;
			}

			public void dispose() {				
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {				
			}			
		});
		typeDropdown.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return ((ExpenseType)element).getTitle();
			}
		});
		typeDropdown.setInput(ExpensesBinder.getExpenseTypes());
		typeDropdown.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (lineItem == null) return;
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				if (selection == null) return;
				lineItem.setType((ExpenseType)selection.getFirstElement());
			}			
		});
		fieldHandlers.put(LineItem.TYPE_PROPERTY, new FieldStateHandler() {
			public void propertyChanged(Object oldValue, Object newValue) {
				if (typeDropdown.getCombo().isDisposed()) return;
				setSelection(newValue);
			}

			public void update() {
				if (typeDropdown.getCombo().isDisposed()) return;
				if (lineItem == null) {
					typeDropdown.getCombo().setEnabled(false);
					typeDropdown.setSelection(StructuredSelection.EMPTY);
				} else {
					typeDropdown.getCombo().setEnabled(true);
					setSelection(lineItem.getType());
				}
			}
			
			private void setSelection(Object selection) {
				typeDropdown.setSelection(selection == null ? StructuredSelection.EMPTY : new StructuredSelection(selection));
			}
		});
	}

	protected void createAmountLabel(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Amount:");			
	}
	
	protected void createAmountField(Composite parent) {
		amountField = new MoneyField(parent, SWT.NONE);
		amountField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		amountField.addMoneyChangeListener(new IMoneyChangeListener() {
			public void moneyChange(MoneyChangeEvent event) {
				if (lineItem == null) return;
				lineItem.setAmount(event.getNewValue());
			}			
		});
		
		fieldHandlers.put(LineItem.AMOUNT_PROPERTY, new FieldStateHandler() {
			public void propertyChanged(Object oldValue, Object newValue) {
				if (amountField.isDisposed()) return;
				amountField.setMoney((CurrencyAmount)newValue);
			}

			public void update() {
				if (amountField.isDisposed()) return;
				if (lineItem == null) {
					amountField.setEnabled(false);
					amountField.setMoney(null);
				} else {
					amountField.setEnabled(true);
					amountField.setMoney((CurrencyAmount)lineItem.getAmount());
				}
			}
		});
	}

	private void createExchangeRateLabel(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Exchange:");
	}
	
	protected void createExchangeRateField(Composite parent) {
		exchangeRateText = new Text(parent, SWT.BORDER);
		exchangeRateText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		exchangeRateText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent event) {
				if (lineItem == null) return;
				try {					
					lineItem.setExchangeRate(exchangeRateFormat.parse(exchangeRateText.getText()).doubleValue());
				} catch (ParseException e) {
					// Eat exception
				}
			}			
		});
		fieldHandlers.put(LineItem.EXCHANGE_RATE_PROPERTY, new FieldStateHandler() {
			public void propertyChanged(Object oldValue, Object newValue) {
				setValue(((Number)newValue).doubleValue());
			}

			public void update() {
				if (exchangeRateText.isDisposed()) return;
				if (lineItem == null) {
					exchangeRateText.setEnabled(false);
					exchangeRateText.setText("");
				} else {
					exchangeRateText.setEnabled(true);
					setValue(lineItem.getExchangeRate());
				}
			}

			private void setValue(double exchangeRate) {
				if (exchangeRateText.isDisposed()) return;
				exchangeRateText.setText(exchangeRateFormat.format(exchangeRate));
			}
		});
	}
	
	private void createCommentLabel(Composite parent) {
		Label commentLabel = new Label(parent, SWT.NONE);
		commentLabel.setText("Comment:");
	}
	
	protected void createCommentField(Composite parent) {
		commentText = new Text(parent, SWT.MULTI | SWT.BORDER);
		commentText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		commentText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				if (lineItem == null) return;
				lineItem.setComment(commentText.getText());
			}			
		});		
		fieldHandlers.put(LineItem.COMMENT_PROPERTY, new FieldStateHandler() {
			public void propertyChanged(Object oldValue, Object newValue) {
				setValue((String)newValue);
			}

			public void update() {
				if (commentText.isDisposed()) return;
				if (lineItem == null) {
					commentText.setEnabled(false);
					commentText.setText("");
				} else {
					commentText.setEnabled(true);
					setValue(lineItem.getComment());
				}
			}

			private void setValue(String comment) {
				if (commentText.isDisposed()) return;
				if (commentText.getText().equals(comment)) return;
				commentText.setText(comment == null ? "" : comment);
			}
		});
	}
	
	/**
	 * Update all the fields on the view. This iterates through
	 * all the field handlers and sends the {@link FieldStateHandler#update()}
	 * message to each. This method must be run in the UI Thread. 
	 */
	void update() {
		Iterator handlers = fieldHandlers.values().iterator();
		while (handlers.hasNext()) {
			FieldStateHandler handler = (FieldStateHandler)handlers.next();
			handler.update();
		}
	}
	
	public void setFocus() {
		dateField.setFocus();
	}

	protected void handleSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			handleSelection((IStructuredSelection)selection);
		}
	}

	protected void handleSelection(IStructuredSelection selection) {
		Object object = selection.getFirstElement();
		if (object instanceof LineItem) {
			setLineItem((LineItem)object);
		}
	}

	/**
	 * Use this method to set the {@link LineItem} instance tracked by the
	 * receiver. Invoking this method will force the receiver to update itself
	 * to display, and provide editing support, for the provided instance.
	 * 
	 * @param lineItem
	 *            an instance of {@link LineItem}. Can be <code>null</code>
	 */
	public void setLineItem(final LineItem lineItem) {
		unhookListeners(this.lineItem);
		hookListeners(lineItem);
		syncExec(new Runnable() {
			public void run() {
				LineItemView.this.lineItem = lineItem;
				update();
			}			
		});
	}

	/**
	 * This method hooks listeners to a {@link LineItem} instance
	 * so that property changes in the instance will be reported
	 * to the receiver.
	 * 
	 * @see #unhookListeners(LineItem)
	 * @see #setLineItem(LineItem)
	 * 
	 * @param lineItem instance of {@link LineItem} or <code>null</code>.
	 */
	void hookListeners(LineItem lineItem) {
		if (lineItem == null) return;
		lineItem.addPropertyChangeListener(propertyChangeListener);
	}
	
	/**
	 * This method removes any previously installed listeners from
	 * the {@link LineItem}. Once called, the the receiver will no
	 * longer be notified of changes to the instance. This method
	 * must be called whenever we are no longer interested in the 
	 * instance; it must be called when we (a) change the instance
	 * of LineItem, or dispose the receiver.
	 * 
	 * @see #hookListeners(LineItem)
	 * @see #setLineItem(LineItem)
	 * @see #dispose()
	 * @param lineItem instance of {@link LineItem} or <code>null</code>.
	 */
	void unhookListeners(LineItem lineItem) {
		if (lineItem == null) return;
		lineItem.removePropertyChangeListener(propertyChangeListener);
	}


	Composite getParent() {
		return parent;
	}
}
