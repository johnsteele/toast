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

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.examples.expenses.core.ExpenseType;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.examples.expenses.core.ObjectWithProperties;
import org.eclipse.examples.expenses.ui.fields.currency.MoneyField;
import org.eclipse.examples.expenses.views.databinding.DateFieldObserverableValue;
import org.eclipse.examples.expenses.views.databinding.MoneyFieldObserverableValue;
import org.eclipse.examples.expenses.views.databinding.ObjectWithPropertiesObservableValue;
import org.eclipse.examples.expenses.views.databinding.ObjectWithPropertiesObservableValue.PropertyGetterSetter;
import org.eclipse.examples.expenses.widgets.datefield.DateField;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.internal.databinding.provisional.swt.ControlUpdater;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;

import com.ibm.icu.util.CurrencyAmount;

public class LineItemView extends AbstractView {

	/**
	 * As a matter of convenience, the ID of the view is the same
	 * as the class name (with package).
	 */
	public static final String ID = LineItemView.class.getName();
	
	/**
	 * The domain model is represented as an {@link IObservableValue} so that we
	 * can use it as a &quot;master&quot; with JFace DataBinding. All the
	 * widgets on this part are bound to this observable though the databinding
	 * API. When the contents of this holder changes, all the bindings are
	 * updated from the contents of the new value.
	 */
	IObservableValue lineItem = new WritableValue();
		
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

	ISelectionListener selectionListener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			handleSelection(selection);
		}	
	};

	private DataBindingContext bindingContext;
	
	public void createPartControl(final Composite parent) {
		parent.setLayout(new GridLayout(2, false));

		bindingContext = new DataBindingContext();
		createDateLabel(parent);
		createDateField(parent, bindingContext);

		createTypeLabel(parent);
		createTypeField(parent);
				
		createAmountLabel(parent);
		createAmountField(parent);

		createExchangeRateLabel(parent);
		createExchangeRateField(parent);
		
		createCommentLabel(parent);
		createCommentField(parent);
		
		Composite buttons = createButtonArea(parent);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.horizontalSpan = 2;
		buttons.setLayoutData(layoutData);
				
		hookSelectionListener();
		
		customizeView(parent);		

		/*
		 * ControlUpdaters is provisional API. This means that--strictly speaking--it
		 * is not currently part of the API and use of this class is--strictly speaking--a
		 * violation of one of the values of the Examples project. However, in an effort
		 * to keep the code here as concise as possible, and in anticipation of this
		 * class becoming API, we're gonna allow it.
		 */
		new ControlUpdater(parent) {
			protected void updateControl() {
				parent.setEnabled(lineItem.getValue() != null);
			}
		};
	}

	public void dispose() {
		bindingContext.dispose();
		unhookSelectionListener();
	}
	
	void unhookSelectionListener() {
		ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
		if (selectionService == null) return;
		selectionService.removeSelectionListener(selectionListener);
	}

	void hookSelectionListener() {
		ISelectionService selectionService = getSite().getWorkbenchWindow().getSelectionService();
		if (selectionService == null) return;
		selectionService.addSelectionListener(selectionListener);
	}
	
	protected void createDateLabel(Composite parent) {
		Label dateLabel = new Label(parent, SWT.NONE);
		dateLabel.setText("Date:");			
	}
		
	/**
	 * This method creates a field for capturing the date from the user. The
	 * technology for capturing dates varies by target, so we lean heavily on
	 * the Equinox dependency matching.
	 *  
	 * @see #dateField
	 * TODO Comment contains references to datefield bundles. Keep up-to-date.
	 * @param bindingContext 
	 */
	protected void createDateField(Composite parent, DataBindingContext bindingContext) {
		dateField = new DateField(parent);

		dateField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		IObservableValue dateFieldObservable = new DateFieldObserverableValue(dateField);
		IObservableValue datePropertyObservable = observeLineItemProperty(LineItem.DATE_PROPERTY, new ObjectWithPropertiesObservableValue.PropertyGetterSetter() {

			public Object getValue(Object source) {
				return ((LineItem)source).getDate();
			}

			public void setValue(Object source, Object value) {
				((LineItem)source).setDate((Date)value);
			}
			
			public Object getType() {
				return Date.class;
			}
			
		});
		
		bindingContext.bindValue(dateFieldObservable, datePropertyObservable, null, null);
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
		typeDropdown.setInput(ExpensesBinder.getTypes());
		
		IObservableValue typeDropDownObservable = ViewersObservables.observeSingleSelection(typeDropdown);
		IObservableValue typePropertyObservable = observeLineItemProperty(LineItem.TYPE_PROPERTY, new ObjectWithPropertiesObservableValue.PropertyGetterSetter() {
			public Object getValue(Object source) {
				return ((LineItem)source).getType();
			}

			public void setValue(Object source, Object value) {
				((LineItem)source).setType((ExpenseType)value);
			}

			public Object getType() {
				return ExpenseType.class;
			}
		});
		bindingContext.bindValue(typeDropDownObservable, typePropertyObservable, null, null);
	}

	/**
	 * This method creates an {@link IObservableValue} for the property named <code>propertyName</code>
	 * of the current {@link LineItem}.
	 * 
	 * @param propertyName
	 * @param getterSetter
	 * @return
	 */
	private IObservableValue observeLineItemProperty(final String propertyName, final PropertyGetterSetter getterSetter) {
		IObservableFactory factory = new IObservableFactory() {
			public IObservable createObservable(Object target) {
				return new ObjectWithPropertiesObservableValue((ObjectWithProperties)target, propertyName, getterSetter);
			}			
		};
		return MasterDetailObservables.detailValue(lineItem, factory, getterSetter.getType());
	}

	protected void createAmountLabel(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Amount:");			
	}
	
	// TODO Consider making this a public class 

	
	protected void createAmountField(Composite parent) {
		amountField = new MoneyField(parent, SWT.NONE);
		amountField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		
		IObservableValue amountFieldObservable = new MoneyFieldObserverableValue(amountField);
		IObservableValue amountPropertyObservable = observeLineItemProperty(LineItem.AMOUNT_PROPERTY, new ObjectWithPropertiesObservableValue.PropertyGetterSetter() {

			public Object getValue(Object source) {
				return ((LineItem)source).getAmount();
			}

			public void setValue(Object source, Object value) {
				((LineItem)source).setAmount((CurrencyAmount)value);
			}
			
			public Object getType() {
				return CurrencyAmount.class;
			}			
		});
		bindingContext.bindValue(amountFieldObservable, amountPropertyObservable, null, null);
	}

	private void createExchangeRateLabel(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Exchange:");
	}
	
	protected void createExchangeRateField(Composite parent) {
		exchangeRateText = new Text(parent, SWT.BORDER);
		exchangeRateText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));		
		
		IObservableValue exchangeRateTextObservable = SWTObservables.observeText(exchangeRateText, SWT.Modify);
		IObservableValue exchangeRatePropertyObservable = observeLineItemProperty(LineItem.EXCHANGE_RATE_PROPERTY, new ObjectWithPropertiesObservableValue.PropertyGetterSetter() {

			public Object getValue(Object source) {
				return new Double(((LineItem)source).getExchangeRate());
			}

			public void setValue(Object source, Object value) {
				((LineItem)source).setExchangeRate(((Double)value).doubleValue());
			}
			
			public Object getType() {
				return double.class;
			}
			
		});
		bindingContext.bindValue(exchangeRateTextObservable, exchangeRatePropertyObservable, null, null);
	}
	
	private void createCommentLabel(Composite parent) {
		Label commentLabel = new Label(parent, SWT.NONE);
		commentLabel.setText("Comment:");
	}
	
	protected void createCommentField(Composite parent) {
		commentText = new Text(parent, SWT.MULTI | SWT.BORDER);
		commentText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		IObservableValue commentTextObservable = SWTObservables.observeText(commentText, SWT.Modify);
		IObservableValue commentPropertyObservable = observeLineItemProperty(LineItem.COMMENT_PROPERTY, new ObjectWithPropertiesObservableValue.PropertyGetterSetter() {

			public Object getValue(Object source) {
				return ((LineItem)source).getComment();
			}

			public void setValue(Object source, Object value) {
				((LineItem)source).setComment((String)value);
			}
			
			public Object getType() {
				return String.class;
			}
			
		});
		bindingContext.bindValue(commentTextObservable, commentPropertyObservable, null, null);
	}
	
	public void setFocus() {
		dateField.setFocus();
	}

	/**
	 * 
	 * <p>This view doesn't use a viewer that
	 * contributes any notion of selection.</p>
	 */
	protected Viewer getViewer() {
		return null;
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

	public void setLineItem(LineItem lineItem) {
		this.lineItem.setValue(lineItem);
	}
}
