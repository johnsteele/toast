package org.eclipse.examples.expenses.ui.fields.currency;

import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import junit.framework.TestCase;

public class MoneyFieldTests extends TestCase {
	Shell shell;
	MoneyField moneyField;

	protected void setUp() throws Exception {
		shell = new Shell();
		moneyField = new MoneyField(shell, SWT.NONE);
	}
	
	public void testEachCurrencyOnlyAppearsOnce() {
		Set set = new HashSet();
		int index=0;
		while (true) {
			Object currency = moneyField.currencyViewer.getElementAt(index++);
			if (currency == null) break;
			if (set.contains(currency)) fail("Duplicate Currency found.");
			set.add(currency);
		}
	}
	
	public void testIsFrequentlyUsedCurrency() {
		moneyField.setFequentlyUsedCurrencies(new Currency[] {
				Currency.getInstance(Locale.GERMANY), 
				Currency.getInstance(Locale.CANADA)});
	
		assertTrue(moneyField.isFrequentlyUsedCurrency(Currency.getInstance(Locale.CANADA)));
		assertTrue(moneyField.isFrequentlyUsedCurrency(Currency.getInstance(Locale.GERMANY)));
		assertFalse(moneyField.isFrequentlyUsedCurrency(Currency.getInstance(Locale.US)));
		assertFalse(moneyField.isFrequentlyUsedCurrency(Currency.getInstance(Locale.CHINA)));
	}
	
	public void testSortFrequentlyUsedCurrenciesFirst1() {
		moneyField.setFequentlyUsedCurrencies(new Currency[] {
				Currency.getInstance(Locale.CANADA), 
				Currency.getInstance(Locale.GERMANY)});
	
		assertSame(Currency.getInstance(Locale.CANADA), moneyField.currencyViewer.getElementAt(0));
		assertSame(Currency.getInstance(Locale.GERMANY), moneyField.currencyViewer.getElementAt(1));
	}
	
	public void testSortFrequentlyUsedCurrenciesFirst2() {
		moneyField.setFequentlyUsedCurrencies(new Currency[] {
				Currency.getInstance(Locale.GERMANY), 
				Currency.getInstance(Locale.CANADA)});
	
		assertSame(Currency.getInstance(Locale.GERMANY), moneyField.currencyViewer.getElementAt(0));
		assertSame(Currency.getInstance(Locale.CANADA), moneyField.currencyViewer.getElementAt(1));
	}
	
	public void testConvertAmountTextToNumberWithNullCurrency() throws Exception {
		moneyField.setCurrency(null);
		assertEquals(123.45d, moneyField.convertAmountTextToNumber("123.45").doubleValue(), 0.001d);
	}

	public void testConvertAmountTextToNumberWithCanadianCurrency() throws Exception {
		moneyField.setCurrency(Currency.getInstance(Locale.CANADA));
		assertEquals(123.45d, moneyField.convertAmountTextToNumber("123.45").doubleValue(), 0.001d);
	}
}
