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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.eclipse.examples.expenses.core.ExpenseType;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.examples.expenses.ui.ExpenseReportingUI;
import org.eclipse.examples.expenses.ui.fields.currency.IMoneyChangeListener;
import org.eclipse.examples.expenses.ui.fields.currency.MoneyChangeEvent;
import org.eclipse.examples.expenses.ui.fields.currency.MoneyField;
import org.eclipse.examples.expenses.ui.fields.date.DateChangeEvent;
import org.eclipse.examples.expenses.ui.fields.date.DateField;
import org.eclipse.examples.expenses.ui.fields.date.IDateChangeListener;
import org.eclipse.examples.expenses.ui.fields.date.IDateFieldFactory;
import org.eclipse.examples.expenses.ui.fields.date.SimpleDateField;
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
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class LineItemView extends AbstractView {

	/**
	 * As a matter of convenience, the ID of the view is the same
	 * as the class name (with package).
	 */
	public static final String ID = LineItemView.class.getName();
	
	private LineItem lineItem;
	
	private DateField dateField;

	private ComboViewer typeDropdown;

	private MoneyField amountField;

	private Text commentText;	

	/**
	 * This colour is used to indicate an error on a field.
	 */
	private Color error;

	ISelectionListener selectionListener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			handleSelection(selection);
		}	
	};

	private IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			update();
		}			
	};

	private Text exchangeRateText;
	
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		
		allocateResources(parent.getDisplay());
		
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
		
		Composite buttons = createButtonArea(parent);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		layoutData.horizontalSpan = 2;
		buttons.setLayoutData(layoutData);
		
		update();
		
		hookSelectionListener();
		
		customizeView(parent);
	}


	public void dispose() {
		unhookListeners(lineItem);
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
	
	void allocateResources(Display display) {
		error = display.getSystemColor(SWT.COLOR_RED);
	}

	protected void createDateLabel(Composite parent) {
		Label dateLabel = new Label(parent, SWT.NONE);
		dateLabel.setText("Date:");			
	}
	
	/**
	 * This method creates a field for capturing the date from the user. The
	 * technology for capturing dates varies by target. First, we attempt to
	 * create a {@link DateField} using a factory. Factories can optionally
	 * be created as Equinox services. If a factory exists, we use it to create
	 * the date field. If no factory exists, we create a SimpleDateField
	 * instead. This allows platforms to optionally override the default
	 * behaviour.
	 */
	protected void createDateField(Composite parent) {
		dateField = createDateFieldUsingFactory(parent);
		if (dateField == null) {
			dateField = new SimpleDateField(parent);
		} 

		dateField.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		dateField.addDateListener(new IDateChangeListener() {
			public void dateChange(DateChangeEvent event) {
				if (lineItem == null) return;
				lineItem.setDate(event.getNewValue());
			}					
		});
	}

	/**
	 * This method attempts to find an {@link IDateFieldFactory}. If
	 * one exists, it is used to create a {@link DateField}.
	 */
	DateField createDateFieldUsingFactory(Composite parent) {
		/*
		 * First, we must obtain a service reference. If a service reference
		 * is found, we get the service that it references.
		 */
		BundleContext context = ExpenseReportingUI.getDefault().getContext();
		ServiceReference reference = context.getServiceReference(IDateFieldFactory.class.getName());
		if (reference == null) return null;
		IDateFieldFactory factory = (IDateFieldFactory) context.getService(reference);
		
		/*
		 * Use the service to create a DateField.
		 */
		DateField field = factory.createDateField(parent);
		
		/*
		 * Unget the service. It's important to balance 'get' service calls
		 * with a corresponding 'unget'.
		 */
		context.ungetService(reference);
		
		return field;
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
		typeDropdown.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (lineItem == null) return;
				IStructuredSelection selection = (IStructuredSelection)event.getSelection();
				if (selection == null) return;
				lineItem.setType((ExpenseType)selection.getFirstElement());
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
		
//		amountText.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent event) {
//				if (lineItem == null) return;
//				try {
//					Money money = MoneyConverter.convertStringToMoney(amountText.getText());
//					lineItem.setAmount(money);
//					amountText.setBackground(null);
//				} catch (MoneyConversionException e) {
//					amountText.setBackground(error);
//				}
//			}			
//		});
	}

	private void createExchangeRateLabel(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Exchange:");
	}
	
	protected void createExchangeRateField(Composite parent) {
		exchangeRateText = new Text(parent, SWT.BORDER);
		exchangeRateText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		exchangeRateText.addModifyListener(new ModifyListener() {
			private NumberFormat exchangeRateFormat = NumberFormat.getInstance();

			public void modifyText(ModifyEvent event) {
				if (lineItem == null) return;
				try {					
					lineItem.setExchangeRate(exchangeRateFormat.parse(exchangeRateText.getText()).doubleValue());
				} catch (ParseException e) {
					// Eat exception
				}
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
	}

	/**
	 * This might be a little excessive... The MoneyConversionException
	 * class is used by the {@link LineItemView#convertStringToMoney(String)} method
	 * to pass error messages back to the user.
	 */

	
	void update() {
		if (lineItem == null) {
			dateField.setEnabled(false);
			dateField.setDate(null);
			
			typeDropdown.getCombo().setEnabled(false);
			typeDropdown.setSelection(null);
			
			amountField.setEnabled(false);
			amountField.setMoney(null);
			
			commentText.setEnabled(false);
			commentText.setText("");
		} else {
			dateField.setEnabled(true);
			dateField.setDate(lineItem.getDate());
			
			typeDropdown.getCombo().setEnabled(true);
			typeDropdown.setSelection(lineItem.getType() == null ? StructuredSelection.EMPTY : new StructuredSelection(lineItem.getType()));
			
			amountField.setEnabled(true);
			amountField.setMoney(lineItem.getAmount());
			
			commentText.setEnabled(true);
			commentText.setText(lineItem.getComment() == null ? "" : lineItem.getComment());	
		}
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
		unhookListeners(this.lineItem);
		hookListeners(lineItem);
		this.lineItem = lineItem;
		update();
	}


	void hookListeners(LineItem lineItem) {
		if (lineItem == null) return;
		lineItem.addPropertyChangeListener(propertyChangeListener);
	}
	
	void unhookListeners(LineItem lineItem) {
		if (lineItem == null) return;
		lineItem.removePropertyChangeListener(propertyChangeListener);
	}
}
