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
package org.eclipse.examples.expenses.ui.fields.currency;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.examples.expenses.core.Money;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class MoneyField extends Composite {
	Text amountText;
	ComboViewer currencyViewer;
	Money money;
	Currency currency;
	double amount;
	ListenerList valueListeners;

	Currency[] currencies;
	boolean ignoreWidgetEvents;
	
	public MoneyField(Composite parent, int style) {
		super (parent, style);
		
		initialize();
		
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		this.setLayout(layout);
		
		amountText = new Text(this, SWT.BORDER);
		amountText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		amountText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (ignoreWidgetEvents) return;
				try {
					amount = convertAmountTextToDouble();
					updateMoneyAndNotify();
				} catch (ParseException e1) {
					fireValidationFailureEvent();
				}
			}
		});
		amountText.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {				
			}

			public void focusLost(FocusEvent e) {
				updateWidgets();
			}			
		});
		
		currencyViewer = new ComboViewer(this, SWT.DROP_DOWN | SWT.READ_ONLY);
		currencyViewer.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		
		currencyViewer.setLabelProvider(new LabelProvider() {
			public String getText(Object element) {
				return ((Currency)element).getCurrencyCode();
			}
		});
		currencyViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object input) {
				if (input instanceof Currency[]) {
					return (Currency[])input;
				}
				return new Object[0];
			}
			public void dispose() {				
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {				
			}
		});
		/*
		 * Set a sorter on the currency viewer. This sorter will ensure that the
		 * frequently used currencies appear at the top of the list. Moreover,
		 * these frequently used currencies will appear in the order that they
		 * are used (i.e. the most frequently used one is at the top.
		 */
		currencyViewer.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				Currency first = (Currency)e1;
				Currency second = (Currency)e2;
				
				if (isFrequentlyUsedCurrency(first)) {
					if (isFrequentlyUsedCurrency(second)) {
						return compareFrequentCurrencies(first, second);
					} else {
						return -1;
					}
				} else if (isFrequentlyUsedCurrency(second)) {
					return 1;
				}
				
				return first.getCurrencyCode().compareTo(second.getCurrencyCode());
			}
		});
		currencyViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (ignoreWidgetEvents) return; // Avoid needless looping
				setCurrency(getSelectedCurrency());
				updateMoneyAndNotify();
			}			
		});
		currencyViewer.setInput(getCurrencies());
	}

	protected int compareFrequentCurrencies(Currency first, Currency second) {
		int index1 = getFrequentlyUsedCurrencies().indexOf(first);
		int index2 = getFrequentlyUsedCurrencies().indexOf(second);
		
		if (index1 < index2) return -1;
		if (index1 > index2) return 1;
		return 0;
	}

	protected boolean isFrequentlyUsedCurrency(Currency currency) {
		return getFrequentlyUsedCurrencies().contains(currency);
	}

	private List getFrequentlyUsedCurrencies() {
		List common = new ArrayList();
		common.add(Currency.getInstance(Locale.CANADA));
		common.add(Currency.getInstance(Locale.US));
		common.add(Currency.getInstance(Locale.GERMANY));
		return common;
	}

	/**
	 * This method returns the currency currently selected in the combo box.
	 */
	Currency getSelectedCurrency() {
		return (Currency)((IStructuredSelection)currencyViewer.getSelection()).getFirstElement();
	}

	/**
	 * This method creates and returns an array of all available
	 * currencies. Each currency is represented once.
	 */
	private Currency[] getCurrencies() {
		return currencies;
	}
	 
	/**
	 * This method initializes the list of available currencies. This
	 * list is created from the list of available countries. For each
	 * country, a {@link Locale} with the country and the language
	 * of the user is created. From that local, a {@link Currency}
	 * is determined. By using the user's language, we avoid the case
	 * where multiple {@link Locale}s for the same country are created
	 * (e.g. Canada and Canada French).
	 */
	void initialize() {
		Map currencyToCountryMap = new HashMap();
		String[] countryCodes = Locale.getISOCountries();
		// TODO Localize to the user, not the workstation.
		String language = Locale.getDefault().getLanguage();
		for(int index=0;index<countryCodes.length;index++) {
			Locale locale = new Locale(language, countryCodes[index]);
			Currency currency = Currency.getInstance(locale);
			if (currency == null) continue;
			currencyToCountryMap.put(currency, locale);
		}
		Set currencies = currencyToCountryMap.keySet();
		this.currencies = (Currency[]) currencies.toArray(new Currency[currencies.size()]);
	}

	/**
	 * This method sets the value currently displayed by the
	 * receiver. To be consistent with other SWT &quot;set&quot; methods,
	 * this method fires a MoneyChangeEvent to notify any listeners
	 * of the change.
	 * 
	 * @param money
	 */
	public void setMoney(Money money) {
		if (money == this.money) return;
		this.money = money;
		amount = money == null ? 0.0 : money.getAmount();
		currency = money == null ? null : money.getCurrency();
		
		updateWidgets();
		updateMoneyAndNotify();
	}

	void updateMoneyAndNotify() {
		money = new Money(amount, currency);
		if (valueListeners == null) return;
		Object[] listeners = valueListeners.getListeners();
		if (listeners.length == 0) return;
		MoneyChangeEvent event = new MoneyChangeEvent(this, money);
		for(int index=0;index<listeners.length;index++) {
			((IMoneyChangeListener)listeners[index]).moneyChange(event);
		}
	}

	protected void fireValidationFailureEvent() {
		// TODO Auto-generated method stub
		
	}
	
	void updateWidgets() {
		ignoreWidgetEvents = true;
		if (money == null) {
			amountText.setText("");
			currencyViewer.setSelection(StructuredSelection.EMPTY);
		} else {
			amountText.setText(getAmountFormat().format(amount));
			currencyViewer.setSelection(new StructuredSelection(currency), true);
		}
		ignoreWidgetEvents = false;
	}
		
	void setCurrency(Currency currency) {
		if (this.currency == currency) return;
		this.currency = currency;
		updateWidgets();
	}
	
	NumberFormat getAmountFormat() {
		NumberFormat format = NumberFormat.getInstance(getUserLocale());
		int fractionDigits = currency.getDefaultFractionDigits();
		format.setMaximumFractionDigits(fractionDigits);
		format.setMinimumFractionDigits(fractionDigits);
		return format;
	}

	/**
	 * This method answers the user's locale.
	 * 
	 * TODO Get the user's locale in RAP.
	 */
	Locale getUserLocale() {
		return Locale.getDefault();
	}	

	double convertAmountTextToDouble() throws ParseException {
		return convertAmountTextToNumber(amountText.getText()).doubleValue();		
	}	
	
	/**
	 * This method attempts to convert the given text into a number. It makes
	 * several attempts at it using several different number formats. If
	 * ultimately unsuccessful, this method throws the {@link ParseException}
	 * thrown by the last attempt.
	 * 
	 * @return A {@link Number} parsed from the given <code>text</code>.
	 * @throws ParseException
	 *             if the value cannot be parsed.
	 */
	Number convertAmountTextToNumber(String text) throws ParseException {
		
		/*
		 * First, try to convert using the currency format.
		 */
		try {
			return getAmountFormat().parse(text);
		} catch (ParseException e) {
			// Eat
		}
		
		/*
		 * Next, try to convert using the currency's locale's
		 * number format.
		 */
		try {
			return NumberFormat.getNumberInstance(getUserLocale()).parse(text);
		} catch (ParseException e) {
			// Eat
		}

		/*
		 * Next, try to convert using the users's locale's number format.
		 */
		// TODO Need an attempt in the user's locale's number format.
		
		/*
		 * Finally, try to convert using the workstation's default locale's
		 * number format.
		 */
		return NumberFormat.getNumberInstance().parse(text);
		
	}

	public void addMoneyChangeListener(IMoneyChangeListener moneyChangeListener) {
		checkWidget();
		if (valueListeners == null) valueListeners = new ListenerList();
		valueListeners.add(moneyChangeListener);
	}
	
	public void removeMoneyChangeListener(IMoneyChangeListener moneyChangeListener) {
		checkWidget();
		if (valueListeners == null) return;
		valueListeners.remove(moneyChangeListener);
	}				
}
