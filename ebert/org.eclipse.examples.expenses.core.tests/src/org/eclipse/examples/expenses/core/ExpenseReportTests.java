package org.eclipse.examples.expenses.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.jface.util.PropertyChangeEvent;
import org.junit.Test;
import org.osgi.service.event.Event;

/**
 * This class provides a handful of tests for the {@link ExpenseReport} class.
 * 
 * <p>Note that this class should be run as a &quot;JUnit Plug-in Test&quot; and
 * that it depends on the presence of an OSGi event service. It does attempt to
 * start the &quot;org.eclipse.equinox.event&quot; bundle, but will only do
 * so if the bundle has already been installed.
 * 
 * <p>Note also that this test uses some Java 5 syntax and libraries. The host
 * bundle requires only Java 1.4.
 */
public class ExpenseReportTests extends ObjectWithPropertiesTests {

	ExpenseReport expenseReport = new ExpenseReport("Expenses");
	
	@Test
	public void testSetTitle() throws Exception {
		expenseReport.setTitle("Wayne's Expenses");
		
		PropertyChangeEvent propertyChangeEvent = observerQueue.remove();
		assertSame(expenseReport, propertyChangeEvent.getSource());
		assertEquals("Expenses", propertyChangeEvent.getOldValue());
		assertEquals("Wayne's Expenses", propertyChangeEvent.getNewValue());
		
		Event event = eventQueue.take();
		assertSame(expenseReport, event.getProperty(ObjectWithProperties.SOURCE));
		assertSame("Expenses", event.getProperty(ObjectWithProperties.OLD_VALUE));
		assertSame("Wayne's Expenses", event.getProperty(ObjectWithProperties.NEW_VALUE));
	}

	@Test
	public void testAddLineItem() throws Exception {
		LineItem lineItem = new LineItem();
		expenseReport.addLineItem(lineItem);

		PropertyChangeEvent propertyChangeEvent = observerQueue.remove();
		assertSame(expenseReport, propertyChangeEvent.getSource());
		assertSame(lineItem, ((List<?>)propertyChangeEvent.getNewValue()).get(0));

		Event event = eventQueue.take();
		assertSame(expenseReport, event.getProperty(ObjectWithProperties.SOURCE));
		assertSame(lineItem, ((List<?>) event.getProperty(ObjectWithProperties.NEW_VALUE)).get(0));
		assertSame(lineItem, event.getProperty(ObjectWithProperties.OBJECT_ADDED));
		assertNull(event.getProperty(ObjectWithProperties.OBJECT_REMOVED));
	}

	@Test
	public void testRemoveLineItem() throws Exception {
		LineItem lineItem = new LineItem();
		expenseReport.addLineItem(lineItem);
		expenseReport.removeLineItem(lineItem);

		observerQueue.remove(); // Ignore the first one
		PropertyChangeEvent propertyChangeEvent = observerQueue.remove();
		assertSame(expenseReport, propertyChangeEvent.getSource());
		assertTrue(((List<?>)propertyChangeEvent.getNewValue()).isEmpty());
		
		eventQueue.take(); // Ignore the first one
		Event event = eventQueue.take();
		assertSame(expenseReport, event.getProperty(ObjectWithProperties.SOURCE));
		assertTrue(((List<?>) event.getProperty(ObjectWithProperties.NEW_VALUE)).isEmpty());
		assertSame(lineItem, event.getProperty(ObjectWithProperties.OBJECT_REMOVED));
		assertNull(event.getProperty(ObjectWithProperties.OBJECT_ADDED));
	}

	@Override
	public ObjectWithProperties getModelObject() {
		return expenseReport;
	}

}
