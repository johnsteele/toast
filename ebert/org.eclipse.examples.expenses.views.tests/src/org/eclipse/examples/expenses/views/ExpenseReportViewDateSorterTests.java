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

import static org.junit.Assert.assertEquals;

import org.eclipse.examples.expenses.core.ExpenseType;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.jface.viewers.ViewerSorter;
import org.junit.Before;
import org.junit.Test;

import com.ibm.icu.util.Calendar;

/**
 * Sorting is done though a {@link ViewerSorter} that compares the elements
 * in pairs (all sorting algorithms do this at some point). The possible combinations
 * that need to be considered by the sorter are:
 * <table border="1">
 * <tr><th colspan="2">First</th><th colspan="2">Second</th><th rowspan="2">Result</th></tr>
 * <tr><th>Date</th><th>Type</th><th>Date</th><th>Type</th></tr>
 * <tr><td>earlier</td><td>*</td><td>later</td><td>*</td><td>-1</td></tr>
 * <tr><td>later</td><td>*</td><td>earlier</td><td>*</td><td>+1</td></tr>
 * <tr><td>earlier</td><td>*</td><td><code>null</code></td><td>*</td><td>-1</td></tr>
 * <tr><td><code>null</code></td><td>*</td><td>earlier</td><td>*</td><td>+1</td></tr>
 * <tr><td>same</td><td>lower</td><td>same</td><td>higher</td><td>-1</td></tr>
 * <tr><td>same</td><td>higher</td><td>same</td><td>lower</td><td>+1</td></tr>
 * <tr><td>same</td><td>lower</td><td>same</td><td><code>null</code></td><td>-1</td></tr>
 * <tr><td>same</td><td><code>null</code></td><td>same</td><td>lower</td><td>+1</td></tr>
 * </table>
 * 
 * @see ExpenseReportView#dateSorter
 * @see ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, Object, Object)
 * @throws Exception
 */
public class ExpenseReportViewDateSorterTests extends WorkbenchTests {
	LineItem earlier;
	LineItem later;
	ExpenseReportView view;
	ViewerSorter sorter;
	LineItem lower;
	LineItem higher;
	
	@Before
	public void setUp() throws Exception {
		view = (ExpenseReportView) getActivePage().showView(ExpenseReportView.ID);
		sorter = view.dateSorter;
		
		Calendar calendar = Calendar.getInstance();
		earlier = new LineItem();
		earlier.setDate(calendar.getTime());
		
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		later = new LineItem();
		later.setDate(calendar.getTime());
		
		lower = new LineItem();
		lower.setDate(calendar.getTime());
		lower.setType(new ExpenseType("lower", 0));
		
		higher = new LineItem();
		higher.setDate(calendar.getTime());
		higher.setType(new ExpenseType("higher", 1));
	}
	
	@Test
	public void testCompareEarlierWithLaterDate() throws Exception {
		assertEquals(sorter.compare(null, earlier, later), -1);
	}

	@Test
	public void testCompareLaterWithEarlierDate() throws Exception {
		assertEquals(sorter.compare(null, later, earlier), 1);
	}
	
	@Test
	public void testCompareEarlierWithNullDate() throws Exception {
		assertEquals(sorter.compare(null, earlier, new LineItem()), -1);
	}

	@Test
	public void testCompareNullWithEarlierDate() throws Exception {
		assertEquals(sorter.compare(null, new LineItem(), earlier), 1);
	}

	@Test
	public void testCompareLowerWithHigherType() throws Exception {
		assertEquals(sorter.compare(null, lower, higher), -1);
	}
	
	@Test
	public void testCompareHigherWithLowerType() throws Exception {
		assertEquals(sorter.compare(null, higher, lower), 1);
	}
	
	@Test
	public void testCompareLowerWithNullType() throws Exception {
		assertEquals(sorter.compare(null, lower, new LineItem()), -1);
	}
	
	@Test
	public void testCompareNullWithLowerType() throws Exception {
		assertEquals(sorter.compare(null, new LineItem(), higher), 1);
	}

	@Test
	public void testCompareLowerWithHigherTypeAndNullDate() throws Exception {
		lower = new LineItem();
		lower.setType(new ExpenseType("lower", 0));
		
		higher = new LineItem();
		higher.setType(new ExpenseType("higher", 1));
		
		assertEquals(sorter.compare(null, lower, higher), -1);
	}
}
