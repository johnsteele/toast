package org.eclipse.examples.expenses.core;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.osgi.service.event.Event;
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
		
		Event event = eventQueue.take();
		assertSame(binder, event.getProperty(ObjectWithProperties.SOURCE));
		assertEquals(ExpensesBinder.REPORTS_PROPERTY, event.getProperty(ObjectWithProperties.PROPERTY_NAME));
		assertSame(report, ((List<?>) event.getProperty(ObjectWithProperties.NEW_VALUE)).get(0));
		assertSame(report, event.getProperty(ObjectWithProperties.OBJECT_ADDED));
		assertNull(event.getProperty(ObjectWithProperties.OBJECT_REMOVED));
	}

	@Test
	public void testRemoveExpenseReport() throws Exception {
		ExpenseReport report = new ExpenseReport("Trip somewhere");
		binder.addExpenseReport(report);
		binder.removeExpenseReport(report);
		
		eventQueue.take(); // Ignore add
		Event event = eventQueue.take(); 		
		assertSame(binder, event.getProperty(ObjectWithProperties.SOURCE));
		assertEquals(ExpensesBinder.REPORTS_PROPERTY, event.getProperty(ObjectWithProperties.PROPERTY_NAME));
		assertSame(report, event.getProperty(ObjectWithProperties.OBJECT_REMOVED));
		assertNull(event.getProperty(ObjectWithProperties.OBJECT_ADDED));
	}

	@Test
	public void testRemoveMissingExpenseReport() throws Exception {
		ExpenseReport report = new ExpenseReport("Trip somewhere");
		
		/*
		 * Switch up the ordering of the remove and add. We expect
		 * that there will be no event for the remove (since we are
		 * attempting to remove something that hasn't yet been added), 
		 * but that there will be one for the add.
		 */
		binder.removeExpenseReport(report);
		binder.addExpenseReport(report);
		
		Event event = eventQueue.take();
		assertSame(ObjectWithProperties.OBJECT_ADDED, event.getProperty(ObjectWithProperties.EVENT_TYPE));
		assertSame(report, event.getProperty(ObjectWithProperties.OBJECT_ADDED));
		assertNull(event.getProperty(ObjectWithProperties.OBJECT_REMOVED));
	}
	
	@Override
	public ObjectWithProperties getModelObject() {
		return binder;
	}

}
