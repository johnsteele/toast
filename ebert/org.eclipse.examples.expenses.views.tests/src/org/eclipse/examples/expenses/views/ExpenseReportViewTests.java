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

import static org.junit.Assert.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.text.DateFormat;

import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.ExpenseType;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.junit.Before;
import org.junit.Test;


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
		report.addLineItem(lineItemWithType);
		lineItemWithoutType = new LineItem();
		report.addLineItem(lineItemWithoutType);
	}
	
	@Test
	public void testThatContentProviderAnswersLineItemsForExpenseReport() throws Exception {		
		Object[] elements = view.contentProvider.getElements(report);
		assertArrayEquals(report.getLineItems(), elements);
	}

	@Test
	public void testThatContentProviderAnswersEmptyArrayForInvalidInput() throws Exception {
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
	 */
	@Test
	public void testThatDateFormatUsesCurrentLocale() throws Exception {
		assertEquals(DateFormat.getDateInstance(DateFormat.SHORT), view.getDateFormat());
	}
	
	@Test
	public void testThatLabelProviderAnswersDate() throws Exception {
		String expected = DateFormat.getDateInstance(DateFormat.SHORT).format(lineItemWithType.getDate());
		String text = view.labelProvider.getColumnText(lineItemWithType, ExpenseReportView.DATE_COLUMN);
		assertEquals(expected, text);
	}
	
	@Test
	public void testThatLabelProviderAnswersTypeTitleWhenTypeIsSet() throws Exception {
		assertEquals("Air fare", view.labelProvider.getColumnText(lineItemWithType, ExpenseReportView.TYPE_COLUMN));
	}

	@Test
	public void testThatLabelProviderAnswersDefaulteWhenTypeIsNotSet() throws Exception {
		assertEquals("<specify type>", view.labelProvider.getColumnText(lineItemWithoutType, ExpenseReportView.TYPE_COLUMN));
	}

	@Test
	public void testThatLabelProviderAnswersCurrency() throws Exception {
		fail();
	}

	@Test
	public void testThatLabelProviderAnswersComment() throws Exception {
		fail();
	}
	
	@Test
	public void testThatLabelPropertiesAreCorrectlyIdentified() throws Exception {
		assertTrue(view.labelProvider.isLabelProperty(lineItemWithType, LineItem.DATE_PROPERTY));
		assertTrue(view.labelProvider.isLabelProperty(lineItemWithType, LineItem.AMOUNT_PROPERTY));
		assertTrue(view.labelProvider.isLabelProperty(lineItemWithType, LineItem.TYPE_PROPERTY));
		assertTrue(view.labelProvider.isLabelProperty(lineItemWithType, LineItem.COMMENT_PROPERTY));
	}
	
	/**
	 * We assume at this point that the workbench's selection service works
	 * as designed/documented. We're not testing that particular feature here.
	 * What we are testing is that our code that responds to the workbench
	 * selection service does what it's supposed to.
	 */
	@Test
	public void testWorkbenchSelectionOfExpenseReport() throws Exception {
		ExpenseReport newReport = new ExpenseReport("New Expense Report");
		LineItem newLineItem = new LineItem();
		newReport.addLineItem(newLineItem);
		
		view.selectionListener.selectionChanged(null, new StructuredSelection(newReport));
		
		processEvents();
		
		assertSame(newReport, view.expenseReport);
		assertEquals("New Expense Report", view.titleText.getText());
		assertTrue(view.viewer.getTable().isEnabled());
		assertSame(view.viewer.getElementAt(0), newLineItem);
	}
	

}
