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

import java.util.Date;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.junit.Test;

import com.ibm.icu.util.Currency;
import com.ibm.icu.util.CurrencyAmount;
import com.ibm.icu.util.ULocale;

/**
 * This class provides a handful of tests for the {@link LineItem} class.
 * 
 * <p>Note that this class should be run as a &quot;JUnit Plug-in Test&quot; and
 * that it depends on the presence of an OSGi event service. It does attempt to
 * start the &quot;org.eclipse.equinox.event&quot; bundle, but will only do
 * so if the bundle has already been installed.
 * 
 * <p>Note also that this test uses some Java 5 syntax and libraries. The host
 * bundle requires only Java 1.4.
 */
public class LineItemTests extends ObjectWithPropertiesTests {

	LineItem lineItem = new LineItem();
	
	@Test
	public void testSetAmount() throws Exception {
		CurrencyAmount amount = new CurrencyAmount(1000.0, Currency.getInstance(ULocale.CANADA));
		lineItem.setAmount(amount);
		
		PropertyChangeEvent propertyChangeEvent = observerQueue.remove();
		assertSame(lineItem, propertyChangeEvent.getSource());
		assertEquals(amount, propertyChangeEvent.getNewValue());
	}

	@Test
	public void testSetComment() throws Exception {
		lineItem.setComment("Comment");
		
		PropertyChangeEvent propertyChangeEvent = observerQueue.remove();
		assertSame(lineItem, propertyChangeEvent.getSource());
		assertEquals("Comment", propertyChangeEvent.getNewValue());
	}

	@Test
	public void testSetDate() throws Exception {
		Date date = new Date();
		lineItem.setDate(date);
		
		PropertyChangeEvent propertyChangeEvent = observerQueue.remove();
		assertSame(lineItem, propertyChangeEvent.getSource());
		assertEquals(date, propertyChangeEvent.getNewValue());
	}

	@Test
	public void testSetExchangeRate() throws Exception {
		lineItem.setExchangeRate(1.6);
		
		PropertyChangeEvent propertyChangeEvent = observerQueue.remove();
		assertSame(lineItem, propertyChangeEvent.getSource());
		assertEquals(Double.valueOf(1.6), propertyChangeEvent.getNewValue());
	}
	

	@Test
	public void testSetType() throws Exception {
		ExpenseType type = new ExpenseType("Air fare", 0);
		lineItem.setType(type);
		
		PropertyChangeEvent propertyChangeEvent = observerQueue.remove();
		assertSame(lineItem, propertyChangeEvent.getSource());
		assertSame(type, propertyChangeEvent.getNewValue());
	}
	
	@Override
	public ObjectWithProperties getModelObject() {
		return lineItem;
	}

}
