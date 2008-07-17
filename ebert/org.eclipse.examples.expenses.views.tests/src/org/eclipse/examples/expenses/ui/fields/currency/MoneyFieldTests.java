package org.eclipse.examples.expenses.ui.fields.currency;

import static org.junit.Assert.*;

import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

public class MoneyFieldTests {
	Shell shell;
	MoneyField moneyField;

	@Before
	public void setUp() throws Exception {
		shell = new Shell();
		moneyField = new MoneyField(shell, SWT.NONE);
	}
	
	@Test
	public void testEachCurrencyOnlyAppearsOnce() {
		Set<Currency> set = new HashSet<Currency>();
		int index=0;
		while (true) {
			Currency currency = (Currency)moneyField.currencyViewer.getElementAt(index++);
			if (currency == null) break;
			if (set.contains(currency)) fail("Duplicate Currency found.");
			set.add(currency);
		}
	}
	
	@Test
	public void testIsFrequentlyUsedCurrency() {
		moneyField.setFequentlyUsedCurrencies(new Currency[] {
				Currency.getInstance(Locale.GERMANY), 
				Currency.getInstance(Locale.CANADA)});
	
		assertTrue(moneyField.isFrequentlyUsedCurrency(Currency.getInstance(Locale.CANADA)));
		assertTrue(moneyField.isFrequentlyUsedCurrency(Currency.getInstance(Locale.GERMANY)));
		assertFalse(moneyField.isFrequentlyUsedCurrency(Currency.getInstance(Locale.US)));
		assertFalse(moneyField.isFrequentlyUsedCurrency(Currency.getInstance(Locale.CHINA)));
	}
	
	@Test
	public void testSortFrequentlyUsedCurrenciesFirst1() {
		moneyField.setFequentlyUsedCurrencies(new Currency[] {
				Currency.getInstance(Locale.CANADA), 
				Currency.getInstance(Locale.GERMANY)});
	
		assertSame(Currency.getInstance(Locale.CANADA), moneyField.currencyViewer.getElementAt(0));
		assertSame(Currency.getInstance(Locale.GERMANY), moneyField.currencyViewer.getElementAt(1));
	}
	
	@Test
	public void testSortFrequentlyUsedCurrenciesFirst2() {
		moneyField.setFequentlyUsedCurrencies(new Currency[] {
				Currency.getInstance(Locale.GERMANY), 
				Currency.getInstance(Locale.CANADA)});
	
		assertSame(Currency.getInstance(Locale.GERMANY), moneyField.currencyViewer.getElementAt(0));
		assertSame(Currency.getInstance(Locale.CANADA), moneyField.currencyViewer.getElementAt(1));
	}
	
	@Test
	public void testConvertAmountTextToNumberWithNullCurrency() throws Exception {
		moneyField.setCurrency(null);
		assertEquals(123.45d, moneyField.convertAmountTextToNumber("123.45").doubleValue(), 0.001d);
	}

	@Test
	public void testConvertAmountTextToNumberWithCanadianCurrency() throws Exception {
		moneyField.setCurrency(Currency.getInstance(Locale.CANADA));
		assertEquals(123.45d, moneyField.convertAmountTextToNumber("123.45").doubleValue(), 0.001d);
	}
}
