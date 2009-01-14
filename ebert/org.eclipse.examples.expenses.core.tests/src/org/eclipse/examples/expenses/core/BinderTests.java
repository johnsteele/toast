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
package org.eclipse.examples.expenses.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
/**
 * This class provides a handful of tests for the {@link ExpenseBinder} class.
 * 
 * <p>Note that this class should be run as a &quot;JUnit Plug-in Test&quot; and
 * that it depends on the presence of an OSGi event service. It does attempt to
 * start the &quot;org.eclipse.equinox.event&quot; bundle, but will only do
 * so if the bundle has already been installed.
 * 
 * <p>Note also that this test uses some Java 5 syntax and libraries. The host
 * bundle requires only Java 1.4.
 */
public class BinderTests extends ObjectWithPropertiesTests {
	ExpensesBinder binder = new ExpensesBinder();
	
	@Test
	public void testAddExpenseReport() throws Exception {
		ExpenseReport report = new ExpenseReport("Trip somewhere");
		binder.addExpenseReport(report);

		CollectionPropertyChangeEvent propertyChangeEvent = (CollectionPropertyChangeEvent)observerQueue.remove();
		assertSame(binder, propertyChangeEvent.getSource());
		assertEquals(report, propertyChangeEvent.added[0]);
	}

	@Test
	public void testRemoveExpenseReport() throws Exception {
		ExpenseReport report = new ExpenseReport("Trip somewhere");
		binder.addExpenseReport(report);
		binder.removeExpenseReport(report);

		/* Skip the first one */
		CollectionPropertyChangeEvent propertyChangeEvent = (CollectionPropertyChangeEvent)observerQueue.remove();
		
		/* The second event contains the remove */
		propertyChangeEvent = (CollectionPropertyChangeEvent)observerQueue.remove();
		assertSame(binder, propertyChangeEvent.getSource());
		assertEquals(report, propertyChangeEvent.removed[0]);
	}

	@Test
	public void testRemoveMissingExpenseReport() throws Exception {
		ExpenseReport report = new ExpenseReport("Trip somewhere");
		binder.removeExpenseReport(report);
		
		assertTrue(observerQueue.isEmpty());
	}
	
	@Override
	public ObjectWithProperties getModelObject() {
		return binder;
	}

}
