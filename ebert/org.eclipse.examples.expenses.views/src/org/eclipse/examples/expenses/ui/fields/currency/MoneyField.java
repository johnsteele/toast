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

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.util.Currency;
import com.ibm.icu.util.CurrencyAmount;
import com.ibm.icu.util.ULocale;

/**
 * MoneyField is a custom widget for displaying and capturing a
 * {@link CurrencyAmount} instance from the user. The current implementation
 * uses a combination of a {@link Text} field and a {@link Combo}/
 * {@link ComboViewer} to capture a {@link Number} and a {@link Currency}. ICU4J
 * is used. *
 * <p>
 * Bug 259517 suggests changing this to use auto completion instead.
 */
public class MoneyField extends Composite {
	Text amountText;
	ComboViewer currencyViewer;
	CurrencyAmount money;
	Currency currency;
	Number amount;
	ListenerList valueListeners;

	Currency[] currencies;
	boolean ignoreWidgetEvents;
	Currency[] frequentlyUsedCurrencies;
	
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
					amount = convertAmountTextToNumber(amountText.getText());
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


	/**
	 * This method compares the two frequently used {@link Currency} instances
	 * and returns -1 if they are &quot;in order&quot;, 1 if they are &quot;in
	 * reverse order&quot; or 0 if they are the same. Note that this method
	 * assumes that both currencies are actually in the
	 * {@link #frequentlyUsedCurrencies} array (and that the field has actually
	 * been set).
	 * 
	 * @param first
	 *            an instance of {@link Currency}.
	 * @param second
	 *            an instance of {@link Currency}.
	 * @return -1, 0, or 1 as described above.
	 */
	int compareFrequentCurrencies(Currency first, Currency second) {
		if (first == second) return 0;
		for(int index=0;index<frequentlyUsedCurrencies.length;index++) {
			if (first == frequentlyUsedCurrencies[index]) return -1;
			if (second == frequentlyUsedCurrencies[index]) return 1;
		}
		return 0;
	}

	/**
	 * This method answers whether or not the given {@link Currency} is a
	 * frequently used currency.
	 * 
	 * @param currency
	 *            an instance of Currency.
	 * @return <code>true</code> if the parameter occurs in the
	 *         {@link #frequentlyUsedCurrencies} field; false otherwise.
	 */
	boolean isFrequentlyUsedCurrency(Currency currency) {
		if (frequentlyUsedCurrencies == null) return false;
		for(int index=0;index<frequentlyUsedCurrencies.length;index++) {
			if (currency.equals(frequentlyUsedCurrencies[index])) return true;
		}
		return false;
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
	 * country, a {@link ULocale} with the country and the language
	 * of the user is created. From that local, a {@link Currency}
	 * is determined. By using the user's language, we avoid the case
	 * where multiple {@link ULocale}s for the same country are created
	 * (e.g. Canada and Canada French).
	 */
	void initialize() {
		Map currencyToCountryMap = new HashMap();
		String[] countryCodes = ULocale.getISOCountries();
		// TODO Localize to the user, not the workstation.
		String language = ULocale.getDefault().getLanguage();
		for(int index=0;index<countryCodes.length;index++) {
			ULocale locale = new ULocale(language, countryCodes[index]);
			try {
				Currency currency = Currency.getInstance(locale);
				if (currency == null) continue;
				currencyToCountryMap.put(currency, locale);
			} catch (IllegalArgumentException e) {
				/*
				 * Am seeing invalid country entry errors when using
				 * J2SE 1.4 (they don't seem to occur on other JVM versions).
				 * When one occurs, catch it, ignore it, and move on.
				 */
			}
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
	public void setMoney(CurrencyAmount money) {
		if (money == this.money) return;
		this.money = money;
		amount = money == null ? new Integer(0) : money.getNumber();
		currency = money == null ? null : money.getCurrency();
		
		updateWidgets();
		updateMoneyAndNotify();
	}

	public CurrencyAmount getMoney() {
		return money;
	}
	
	void updateMoneyAndNotify() {
		CurrencyAmount oldMoney = money;
		if (currency != null) {
			money = new CurrencyAmount(amount, currency);
		} else {
			money = null;
		}
		if (valueListeners == null) return;
		Object[] listeners = valueListeners.getListeners();
		if (listeners.length == 0) return;
		MoneyChangeEvent event = new MoneyChangeEvent(this, oldMoney, money);
		for(int index=0;index<listeners.length;index++) {
			((IMoneyChangeListener)listeners[index]).moneyChange(event);
		}
	}

	protected void fireValidationFailureEvent() {
		// TODO Auto-generated method stub
		
	}
	
	void updateWidgets() {
		ignoreWidgetEvents = true;
		try {
			if (money == null) {
				amountText.setText("");
				currencyViewer.setSelection(StructuredSelection.EMPTY);
			} else {
				amountText.setText(getCurrencyParser().format(amount));
				currencyViewer.setSelection(new StructuredSelection(currency), true);
			}
		} finally {
			ignoreWidgetEvents = false;
		}
	}
		
	void setCurrency(Currency currency) {
		if (this.currency == currency) return;
		this.currency = currency;
		updateWidgets();
	}
	
	NumberFormat getCurrencyParser() {
		NumberFormat format = NumberFormat.getCurrencyInstance(getUserLocale());
		if (currency != null) format.setCurrency(currency);
		return format;
	}

	NumberFormat getAmountParser() {
		return NumberFormat.getInstance(getUserLocale());
	}
	
	NumberFormat getAmountFormat() {
		NumberFormat format = NumberFormat.getInstance(getUserLocale());
		int fractionDigits = currency == null ? 2 : currency.getDefaultFractionDigits();
		format.setMaximumFractionDigits(fractionDigits);
		format.setMinimumFractionDigits(fractionDigits);
		return format;
	}

	/**
	 * This method answers the user's locale.
	 * 
	 * TODO Get the user's locale in RAP.
	 */
	ULocale getUserLocale() {
		return ULocale.getDefault();
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
			return getCurrencyParser().parse(text);
		} catch (ParseException e) {
			// Eat
		}
		
		/*
		 * Next, try to convert using the currency's locale's
		 * number format.
		 */
		try {
			return getAmountParser().parse(text);
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

	public Currency[] getFrequentlyUsedCurrencies() {
		return frequentlyUsedCurrencies;
	}
	
	public void setFequentlyUsedCurrencies(Currency[] frequentlyUsedCurrencies) {
		this.frequentlyUsedCurrencies = frequentlyUsedCurrencies;
		currencyViewer.refresh();
	}				
}
