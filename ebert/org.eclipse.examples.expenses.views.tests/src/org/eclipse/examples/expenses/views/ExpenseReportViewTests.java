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
import static org.junit.Assert.fail;

import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.ExpenseType;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;

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
		
		/*
		 * Here, we create a subclass of ExpenseReport that changes the way that
		 * the EventAdmin service is notified of a change event. See the
		 * comments on the testTitleFieldUpdate method for more information.
		 */
		report = new ExpenseReport("My Expense Report") {
			@Override
			protected void postEvent(EventAdmin eventAdmin, Event event) {
				eventAdmin.sendEvent(event);
			}
		};
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
	 * 
	 * @see ExpenseReportView#getDateFormat()
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
		assertEquals("$10.00", view.labelProvider.getColumnText(lineItemWithType, ExpenseReportView.AMOUNT_COLUMN));
	}

	@Test
	public void testThatLabelProviderAnswersComment() throws Exception {
		assertEquals("Comment", view.labelProvider.getColumnText(lineItemWithType, ExpenseReportView.COMMENT_COLUMN));
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
		assertTrue(view.lineItemTableViewer.getTable().isEnabled());
		assertSame(view.lineItemTableViewer.getElementAt(0), newLineItem);
	}
	
	/**
	 * This test confirms that the service that listens for 
	 * changes to a {@link LineItem} has been started. This service
	 * should have been started as part of the process of creating the
	 * view.
	 * 
	 * @see ExpenseReportView#startLineItemChangedHandlerService(BundleContext)
	 * @throws Exception
	 */
	@Test
	public void testLineItemChangedHandlerServiceStarted() throws Exception {
		// If the service has not been registered, this should throw an exception.
		view.lineItemChangedHandlerService.getReference();		
	}
	
	/**
	 * @see ExpenseReportView#dispose()
	 * @throws Exception
	 */
	@Test
	public void testLineItemChangedHandlerServiceStopped() throws Exception {
		getActivePage().hideView(view);
		try {
			view.lineItemChangedHandlerService.getReference();
			fail("Service is still registered.");
		} catch (IllegalStateException e) {
			/*
			 * If the service has been unregistered as we expect, then
			 * an IllegalStateException will be thrown. This is expected
			 * behaviour.
			 */
		}
	}
	
	/**
	 * @see ExpenseReportView#startLineItemRemovedHandlerService(BundleContext)
	 * @throws Exception
	 */
	@Test
	public void testLineItemAddedHandlerServiceStarted() throws Exception {
		// If the service has not been registered, this should throw an exception.
		view.lineItemAddedHandlerService.getReference();		
	}
	
	/**
	 * @see ExpenseReportView#dispose()
	 * @throws Exception
	 */
	@Test
	public void testLineItemAddedHandlerServiceStopped() throws Exception {
		getActivePage().hideView(view);
		try {
			view.lineItemAddedHandlerService.getReference();
			fail("Service is still registered.");
		} catch (IllegalStateException e) {
			/*
			 * If the service has been unregistered as we expect, then
			 * an IllegalStateException will be thrown. This is expected
			 * behaviour.
			 */
		}
	}
	
	/**
	 * @see ExpenseReportView#startLineItemRemovedHandlerService(BundleContext)
	 * @throws Exception
	 */
	@Test
	public void testLineItemRemovedHandlerServiceStarted() throws Exception {
		// If the service has not been registered, this should throw an exception.
		view.lineItemRemovedHandlerService.getReference();		
	}
	
	/**
	 * @see ExpenseReportView#dispose()
	 * @throws Exception
	 */
	@Test
	public void testLineItemRemovedHandlerServiceStopped() throws Exception {
		getActivePage().hideView(view);
		try {
			view.lineItemRemovedHandlerService.getReference();
			fail("Service is still registered.");
		} catch (IllegalStateException e) {
			/*
			 * If the service has been unregistered as we expect, then
			 * an IllegalStateException will be thrown. This is expected
			 * behaviour.
			 */
		}
	}
	
	/**
	 * I am not aware of any mechanism to check to see if a particular
	 * handler is registered with the selections service. For now, this
	 * test will answer success.
	 * 
	 * TODO Confirm that the handler is indeed registered.
	 */
	@Test
	public void testSelectionHandlerRegistered() {
		
	}
	
	/**
	 * Likewise, I am not aware of any mechanism to check to see if a particular
	 * handler is deregistered with the selections service.
	 * 
	 * TODO Confirm that the handler is indeed deregistered.
	 */
	@Test
	public void testSelectionHandlerDeregistered() {
		
	}
	
	@Test
	public void testRemoveButtonEnabledWhenLineItemSelected() throws Exception {
		view.lineItemTableViewer.setSelection(new StructuredSelection(lineItemWithType));
		processEvents();
		assertTrue(view.removeButton.isEnabled());
	}
	
	@Test
	public void testRemoveButtonDisabledWhenNoLineItemSelected() throws Exception {
		view.lineItemTableViewer.setSelection(StructuredSelection.EMPTY);
		processEvents();
		assertFalse(view.removeButton.isEnabled());
	}
	
	/**
	 * This test confirms that the {@link ExpenseReportView#titleText} field is
	 * appropriately updated when the title property changes in the
	 * {@link ExpenseReport}. Note that this test works with the complete event
	 * lifecycle: The change in the report queues a change {@link Event} on the
	 * {@link EventAdmin} service which is picked up by the handler installed in
	 * the
	 * {@link ExpenseReportView#startExpenseReportChangedHandlerService(BundleContext)}
	 * method; That method then effects the update via the
	 * {@link ExpenseReportView#handleExpenseReportPropertyChangedEvent(Event)}
	 * method.
	 * 
	 * <p>
	 * By default, the expense report enqueues an event asynchronously. This
	 * creates a condition that is difficult to test without messing around with
	 * threads. Since our intent is to test how the ExpenseReportView handles
	 * updates, and not to test the EventAdmin service ability to deliver events
	 * asynchronously, the {@link ExpenseReport} under observation has been
	 * changed to deliver events synchronously (see comments in the
	 * initialization of the {@link #report} field in the {@link #setup()}
	 * method.
	 * 
	 * <p>
	 * It could probably be argued that we don't need to involve the EventAdmin
	 * service at all since it's not what we need to test. We could, for
	 * example, just invoke the
	 * {@link ExpenseReportView#handleExpenseReportPropertyChangedEvent(Event)}
	 * method directly and still have a perfectly valid test.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTitleFieldUpdated() throws Exception {
		report.setTitle("New Title");
		
		processEvents();
		
		assertEquals("New Title", view.titleText.getText());
	}
	
	/**
	 * The {@link EventHandler} registered by in the
	 * {@link ExpenseReportView#startExpenseReportChangedHandlerService(BundleContext)}
	 * method will receive update events for changes to all
	 * {@link ExpenseReport}s. We need to make sure that
	 * {@link ExpenseReportView#titleText} is only updated when the report under
	 * observation changes.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTitleFieldUpdatedInADifferentInstance() throws Exception {
		ExpenseReport newReport = new ExpenseReport("Some other Expense Report");
		newReport.setTitle("New Title");
		
		processEvents();
		
		assertEquals("My Expense Report", view.titleText.getText());
	}
}
