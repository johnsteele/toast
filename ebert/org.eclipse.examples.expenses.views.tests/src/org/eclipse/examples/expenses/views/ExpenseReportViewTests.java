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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.ExpenseType;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.jface.viewers.IContentProvider;
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
		 * comments on the #testTitleFieldUpdated method for more information.
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
	 * This test confirms that the service that listens for changes to a
	 * {@link LineItem} has been started. This service should have been started
	 * as part of the process of creating the view.
	 * 
	 * @see ExpenseReportView#lineItemChangedHandlerService
	 * @see ExpenseReportView#startLineItemChangedHandlerService(BundleContext)
	 */
	@Test
	public void testLineItemChangedHandlerServiceStarted() {
		// If the service has not been registered, this should throw an exception.
		view.lineItemChangedHandlerService.getReference();		
	}
	
	/**
	 * This test confirms that the service that listens for changes to {@link LineItem}
	 * instances is shut down when the instance is disposed.
	 * 
	 * @see ExpenseReportView#lineItemChangedHandlerService
	 * @see ExpenseReportView#dispose()
	 */
	@Test
	public void testLineItemChangedHandlerServiceStopped() {
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
	 * This method confirms that the {@link ExpenseReportView#lineItemAddedHandlerService}
	 * service has been started.
	 * 
	 * @see ExpenseReportView#lineItemAddedHandlerService
	 * @see ExpenseReportView#startLineItemAddedHandlerService(BundleContext)
	 * @throws Exception
	 */
	@Test
	public void testLineItemAddedHandlerServiceStarted() throws Exception {
		// If the service has not been registered, this should throw an exception.
		view.lineItemAddedHandlerService.getReference();		
	}
	
	/**
	 * This method confirms that the {@link ExpenseReportView#lineItemAddedHandlerService}
	 * service has been stopped.
	 * 
	 * @see ExpenseReportView#lineItemAddedHandlerService
	 * @see ExpenseReportView#startLineItemAddedHandlerService(BundleContext)
	 * @see ExpenseReportView#dispose()
	 */
	@Test
	public void testLineItemAddedHandlerServiceStopped() {
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
	 * This method confirms that the {@link ExpenseReportView#lineItemRemovedHandlerService}
	 * service has been started.
	 * 
	 * @see ExpenseReportView#lineItemRemovedHandlerService
	 * @see ExpenseReportView#startLineItemRemovedHandlerService(BundleContext)
	 */
	@Test
	public void testLineItemRemovedHandlerServiceStarted() {
		// If the service has not been registered, this should throw an exception.
		view.lineItemRemovedHandlerService.getReference();		
	}
	
	/**
	 * This method confirms that the {@link ExpenseReportView#lineItemRemovedHandlerService}
	 * service has been stopped.
	 * 
	 * @see ExpenseReportView#lineItemRemovedHandlerService
	 * @see ExpenseReportView#startLineItemRemovedHandlerService(BundleContext)
	 * @see ExpenseReportView#dispose()
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
		assertTrue(view.removeButton.isEnabled());
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
