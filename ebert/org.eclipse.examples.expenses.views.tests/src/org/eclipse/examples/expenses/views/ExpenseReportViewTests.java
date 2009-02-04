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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.ExpenseType;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Button;
import org.junit.Before;
import org.junit.Test;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.util.Currency;
import com.ibm.icu.util.CurrencyAmount;

public class ExpenseReportViewTests extends WorkbenchTests {
	ExpenseReportView view;
	ExpenseReport report;
	LineItem lineItemWithType;
	LineItem lineItemWithoutType;

	@Before
	public void setup() throws Exception {
		view = (ExpenseReportView) getActivePage().showView(ExpenseReportView.ID);
		
		report = new ExpenseReport("My Expense Report");
		lineItemWithType = new LineItem();
		lineItemWithType.setType(new ExpenseType("Air fare", 1));
		lineItemWithType.setAmount(new CurrencyAmount(10.0, Currency.getInstance("CAD")));
		lineItemWithType.setComment("Comment");
		report.addLineItem(lineItemWithType);
		lineItemWithoutType = new LineItem();
		report.addLineItem(lineItemWithoutType);
		
		view.setReport(report);
		
		processEvents();
	}
	
	/**
	 * The view installs listeners on the {@link ExpenseReport} and 
	 * any {@link LineItem} instances contained in it. With this test,
	 * we confirm that those listeners have been properly installed.
	 * 
	 * @see ExpenseReportView#expenseReportPropertyChangeListener
	 * @see ExpenseReportView#lineItemPropertyChangeListener
	 * @see ExpenseReportView#hookPropertyChangeListener(ExpenseReport)
	 * @see ExpenseReportView#hookPropertyChangeListener(LineItem)
	 */
	@Test
	public void testListenersInstalled() {
		assertSame(view.expenseReportPropertyChangeListener, report.getPropertyChangeListeners()[0]);
		assertSame(view.lineItemPropertyChangeListener, lineItemWithType.getPropertyChangeListeners()[0]);
		assertSame(view.lineItemPropertyChangeListener, lineItemWithoutType.getPropertyChangeListeners()[0]);
	}
	
	/**
	 * Confirm that the listeners are uninstalled when the {@link ExpenseReport}
	 * is changed.
	 * 
	 * @see #testListenersInstalled()
	 * @see ExpenseReportView#unhookPropertyChangeListener(ExpenseReport)
	 * @see ExpenseReportView#unhookPropertyChangeListener(LineItem)
	 */
	@Test
	public void testListenersUninstalled() {
		view.setReport(null);
		
		processEvents();
		
		assertEquals(0, report.getPropertyChangeListeners().length);
		assertEquals(0, lineItemWithType.getPropertyChangeListeners().length);
		assertEquals(0, lineItemWithoutType.getPropertyChangeListeners().length);
	}
	
	/**
	 * This test confirms that any {@link LineItem} instances added
	 * to the report get a listener installed on them.
	 * 
	 * @see #testListenersInstalled()
	 * @see ExpenseReportView#expenseReportPropertyChangeListener
	 * @see ExpenseReportView#hookPropertyChangeListener(LineItem)
	 */
	@Test
	public void testListenersInstalledOnAddedLineItems() throws Exception {
		LineItem anotherLineItem = new LineItem();
		report.addLineItem(anotherLineItem);
		
		processEvents();
		
		assertSame(view.lineItemPropertyChangeListener, anotherLineItem.getPropertyChangeListeners()[0]);
	}
	
	/**
	 * This test confirms that any {@link LineItem} instances removed
	 * from the report have the listener removed from them.
	 * 
	 * @see #testListenersInstalled()
	 * @see ExpenseReportView#expenseReportPropertyChangeListener
	 * @see ExpenseReportView#unhookPropertyChangeListener(LineItem)
	 */
	@Test
	public void testListenersInstalledFromRemovedLineItems() throws Exception {
		report.removeLineItem(lineItemWithoutType);
		
		processEvents();
		
		assertEquals(0, lineItemWithoutType.getPropertyChangeListeners().length);
	}
	
	/**
	 * This test talks directly to the {@link IContentProvider} for the table
	 * that displays {@link LineItem} instances, confirming that the content
	 * provider knows how to obtain the list of line items from an
	 * {@link ExpenseReport}.
	 */
	@Test
	public void testContentProviderAnswersLineItemsForExpenseReport() {		
		Object[] elements = view.contentProvider.getElements(report);
		assertArrayEquals(report.getLineItems(), elements);
	}

	/**
	 * This test talks directly to the {@link IContentProvider} for the table
	 * that displays {@link LineItem} instances, confirming that the content
	 * provider answers an empty array when given an input that is <em>not</em>
	 * an instance of {@link ExpenseReport}. FWIW, this is a condition that
	 * should never actually happen, but since there's &quot;paranoia&quot; code
	 * to check for the condition, we should still test it.
	 */
	@Test
	public void testContentProviderAnswersEmptyArrayForInvalidInput() {
		Object[] elements = view.contentProvider.getElements(new LineItem[5]);
		assertArrayEquals(new Object[0], elements);
	}
	
	/**
	 * This test confirms that the {@link DateFormat} used to provide dates for
	 * the {@link ExpenseReportView#DATE_COLUMN} column is appropriate for the
	 * current locale.
	 * <p>
	 * On the RCP/eRCP, the locale should be the environment's default Locale.
	 * On RAP, the locale is determined from the request header. Since we're
	 * running these tests on RCP, the following should do it. We'll sort out
	 * how to test the RAP case in other tests.
	 * 
	 * @see ExpenseReportView#getDateFormat()
	 */
	@Test
	public void testDateFormatUsesCurrentLocale() {
		assertEquals(DateFormat.getDateInstance(DateFormat.SHORT), view.getDateFormat());
	}
	
	/**
	 * This test works directly with the label provider for the table that displays
	 * {@link LineItem} instances. Here, we're making sure that the value created
	 * for the &quot;Date&quot; column is what we expect.
	 */
	@Test
	public void testLabelProviderAnswersDate() {
		String expected = DateFormat.getDateInstance(DateFormat.SHORT).format(lineItemWithType.getDate());
		String text = view.labelProvider.getColumnText(lineItemWithType, ExpenseReportView.DATE_COLUMN);
		assertEquals(expected, text);
	}
	
	/**
	 * This test works directly with the label provider for the table that
	 * displays {@link LineItem} instances. Here, we're making sure that the
	 * value created for the &quot;Type&quot; column is what we expect when the
	 * line item has a valid instance of {@link ExpenseType}.
	 */
	@Test
	public void testLabelProviderAnswersTypeTitleWhenTypeIsSet() {
		assertEquals("Air fare", view.labelProvider.getColumnText(lineItemWithType, ExpenseReportView.TYPE_COLUMN));
	}
	
	/**
	 * This test works directly with the label provider for the table that
	 * displays {@link LineItem} instances. Here, we're making sure that the
	 * value created for the &quot;Type&quot; column is what we expect when the
	 * line item does <em>not</em> have a type (i.e. the type is
	 * <code>null</code>).
	 */
	@Test
	public void testLabelProviderAnswersDefaulteWhenTypeIsNotSet() {
		assertEquals("<specify type>", view.labelProvider.getColumnText(lineItemWithoutType, ExpenseReportView.TYPE_COLUMN));
	}
	
	/**
	 * This test works directly with the label provider for the table that displays
	 * {@link LineItem} instances. Here, we're making sure that the value created
	 * for the &quot;Amount&quot; column is what we expect.
	 */
	@Test
	public void testLabelProviderAnswersCurrency() {
		assertEquals("$10.00", view.labelProvider.getColumnText(lineItemWithType, ExpenseReportView.AMOUNT_COLUMN));
	}
	
	/**
	 * This test works directly with the label provider for the table that displays
	 * {@link LineItem} instances. Here, we're making sure that the value created
	 * for the &quot;Comment&quot; column is what we expect.
	 */
	@Test
	public void testLabelProviderAnswersComment() throws Exception {
		assertEquals("Comment", view.labelProvider.getColumnText(lineItemWithType, ExpenseReportView.COMMENT_COLUMN));
	}
	
	/**
	 * When an update occurs on one of the {@link LineItem}s, the table may need
	 * to be updated. An optimization in JFace updates the table only when one
	 * of the properties that are actually being displayed changes. Our label
	 * provider knows which columns to look for. This method tests to make sure
	 * that the label provider knows which properties to watch for changes.
	 */
	@Test
	public void testLabelPropertiesAreCorrectlyIdentified() {
		assertTrue(view.labelProvider.isLabelProperty(lineItemWithType, LineItem.DATE_PROPERTY));
		assertTrue(view.labelProvider.isLabelProperty(lineItemWithType, LineItem.AMOUNT_PROPERTY));
		assertTrue(view.labelProvider.isLabelProperty(lineItemWithType, LineItem.TYPE_PROPERTY));
		assertTrue(view.labelProvider.isLabelProperty(lineItemWithType, LineItem.COMMENT_PROPERTY));
		assertTrue(view.labelProvider.isLabelProperty(lineItemWithType, LineItem.TYPE_PROPERTY));
		
		assertFalse(view.labelProvider.isLabelProperty(lineItemWithType, LineItem.EXCHANGE_RATE_PROPERTY));
	}
					
	/**
	 * This method tests that the &quot;Remove&quot; button is properly updated
	 * in response to a change in selection in the
	 * {@link ExpenseReportView#lineItemTableViewer}; we force a selection into
	 * the view and confirm that the button has become enabled as a result of
	 * that change.
	 */
	@Test
	public void testRemoveButtonEnabledWhenLineItemSelected() {
		view.lineItemTableViewer.setSelection(new StructuredSelection(lineItemWithType));
		processEvents();
		assertTrue(((Button)view.getButtonArea().getChildren()[1]).isEnabled());
	}

	/**
	 * This method tests that the &quot;Remove&quot; button is properly updated
	 * in response to a change in selection in the
	 * {@link ExpenseReportView#lineItemTableViewer}; we force an empty
	 * selection into the view and confirm that the button has become disabled
	 * as a result of that change.
	 */
	@Test
	public void testRemoveButtonDisabledWhenNoLineItemSelected() throws Exception {
		view.lineItemTableViewer.setSelection(StructuredSelection.EMPTY);
		processEvents();
		assertFalse(((Button)view.getButtonArea().getChildren()[1]).isEnabled());
	}
	
	/**
	 * This test confirms that the {@link ExpenseReportView#titleText} field is
	 * appropriately updated when the title property changes in the
	 * {@link ExpenseReport}. 
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTitleFieldUpdated() throws Exception {
		report.setTitle("New Title");
		
		processEvents();
		
		assertEquals("New Title", view.titleText.getText());
	}
}
