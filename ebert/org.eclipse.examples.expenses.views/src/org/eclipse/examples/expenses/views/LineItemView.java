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

import org.eclipse.examples.expenses.core.ExpenseType;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.examples.expenses.ui.fields.currency.IMoneyChangeListener;
import org.eclipse.examples.expenses.ui.fields.currency.MoneyChangeEvent;
import org.eclipse.examples.expenses.ui.fields.currency.MoneyField;
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

public class LineItemView extends AbstractView {

	/**
	 * As a matter of convenience, the ID of the view is the same
	 * as the class name (with package).
	 */
	public static final String ID = LineItemView.class.getName();
	
	LineItem lineItem;
	
	DateField dateField;

	ComboViewer typeDropdown;

	MoneyField amountField;

	Text commentText;	

	Text exchangeRateText;

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
