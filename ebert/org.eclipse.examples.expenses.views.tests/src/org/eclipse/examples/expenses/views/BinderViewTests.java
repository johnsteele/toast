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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BinderViewTests extends WorkbenchTests {

	private BinderView view;
	private ExpensesBinder binder;
	private ExpenseReport report;

	@Before
	public void setUp() throws Exception {
		view = (BinderView) getActivePage().showView(BinderView.ID);
		binder = new ExpensesBinder();
		report = new ExpenseReport("Trip to Hell");
		binder.addExpenseReport(report);
		view.setBinder(binder);
		
		processEvents();
	}

	@After
	public void shutdown() {
		getActivePage().hideView(view);
	}

	/**
	 * This test ensures that the {@link BinderView#contentProvider} gives us
	 * the right collection of objects to display when given valid input.
	 */
	@Test
	public void testThatContentProviderAnswersExpenseReportsForBinder() {
		assertArrayEquals(binder.getReports(), view.contentProvider.getElements(binder));
	}

	/**
	 * This test ensures that the {@link BinderView#contentProvider} gives us
	 * the right collection of objects to display when given invalid input. For
	 * this content provider, an empty array is expected if anything other than
	 * an instance of {@link ExpensesBinder} is given as input.
	 */
	@Test
	public void testThatContentProviderAnswersEmptyArrayForInvalidInput() {
		assertArrayEquals(new Object[0], view.contentProvider.getElements(new Date()));
	}	
	
	/**
	 * This test ensures that the required set of listeners are properly installed
	 * on the objects we're interested in. Since it is the {@link BinderView#contentProvider} that
	 * is responsible for installing the listeners, we work directly with it by telling
	 * it to invoke the {@link IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, Object, Object)}
	 * method. 
	 *
	 * @see BinderView#contentProvider
	 */
	@Test
	public void testThatContentProviderInstallsListenersOnBinderAndContainedReports() {	
		ExpensesBinder newBinder = new ExpensesBinder();
		ExpenseReport newReport = new ExpenseReport("A different Trip to Hell");
		newBinder.addExpenseReport(report);
		view.contentProvider.inputChanged(view.viewer, null, newBinder);
		
		assertSame(view.binderListener, newBinder.getPropertyChangeListeners()[0]);
		assertSame(view.expenseReportListener, newBinder.getReports()[0].getPropertyChangeListeners()[0]);
	}
	
	/**
	 * This test ensures that the required set of listeners are properly uninstalled
	 * from the objects we're interested in. 
	 *
	 * @see #testThatContentProviderInstallsListenersOnBinderAndContainedReports()
	 * @see BinderView#contentProvider
	 */
	@Test
	public void testThatContentProviderUnnstallsListenersOnBinderAndContainedReports() {		
		view.contentProvider.inputChanged(view.viewer, binder, null);
		
		assertEquals(0, binder.getPropertyChangeListeners().length);
		assertEquals(0, binder.getReports()[0].getPropertyChangeListeners().length);
	}
	
	/**
	 * This tests makes sure that the content provider's dispose() method does
	 * what it's supposed to do, which is to clear the listeners that had previously
	 * been installed on the {@link ExpensesBinder} instance.
	 * 
	 * @see BinderView#contentProvider
	 * @throws Exception
	 */
	@Test
	public void testThatContentProviderCleansUpWhenDisposed() throws Exception {
		view.contentProvider.dispose();
		
		assertEquals(0, binder.getPropertyChangeListeners().length);
		assertEquals(0, binder.getReports()[0].getPropertyChangeListeners().length);
	}
	
	/**
	 * This test ensures that the {@link BinderView#expenseReportListener} is installed
	 * on any {@link ExpenseReport} instance that is added to the binder after the
	 * binder has been set into the view. We further check that the {@link ListViewer}
	 * has been updated to include the new element.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testThatListenerIsInstalledOnAddedExpenseReports() throws Exception {
		ExpenseReport addedReport = new ExpenseReport("Another Expense Report");
		binder.addExpenseReport(addedReport);
		
		assertEquals(view.expenseReportListener, addedReport.getPropertyChangeListeners()[0]);
		assertSame(addedReport, view.viewer.getElementAt(1));
	}

	/**
	 * This test ensures that the {@link BinderView#expenseReportListener} is uninstalled
	 * from any {@link ExpenseReport} instance that is removed from the binder after the
	 * binder has been set into the view. We further check that the removed element
	 * has been removed from the {@link ListViewer}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testThatListenerIsUninstalledFromRemovedExpenseReports() throws Exception {
		binder.removeExpenseReport(report);
		
		assertEquals(0, report.getPropertyChangeListeners().length);
		assertNull(view.viewer.getElementAt(0));
	}
	 
	/**
	 * This test ensures that the {@link BinderView#labelProvider} is
	 * functioning properly. This test hammers directly on the label provider
	 * (rather than working through the {@link ListViewer}) to ensure that the
	 * label provider is properly generating labels and is correctly identifying
	 * those properties that are used to create the label.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLabelProvider() throws Exception {
		ExpenseReport report = new ExpenseReport("Trip");
		assertEquals("Trip", view.labelProvider.getText(report));
		assertTrue(view.labelProvider.isLabelProperty(report, ExpenseReport.TITLE_PROPERTY));
		assertFalse(view.labelProvider.isLabelProperty(report, ExpenseReport.LINEITEMS_PROPERTY));
	}
		
	/**
	 * In this test, we are making sure that the state of the "Remove" button is
	 * properly maintained. Specifically, we are ensuring that the "Remove"
	 * button is enabled when a valid selection is made in
	 * {@link BinderView#viewer} (an instance of {@link ListViewer}).
	 * 
	 * <p>
	 * Setting the selection in the ListViewer should invoke the
	 * {@link ISelectionChangedListener}; this listener invokes the
	 * {@link BinderView#updateButtons()} method which in turn sets the state of
	 * the "Remove" button.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRemoveButtonEnabledWhenExpenseReportSelected() throws Exception {
		view.viewer.setSelection(new StructuredSelection(binder.getReports()[0]));
		assertTrue(view.removeButton.isEnabled());
	}
	
	/**
	 * In this test, we are making sure that the state of the "Remove" button is
	 * properly maintained. 
	 * 
	 * @see #testRemoveButtonEnabledWhenExpenseReportSelected()
	 * @throws Exception
	 */
	@Test
	public void testRemoveButtonDisabledWithEmptySelection() throws Exception {
		view.viewer.setSelection(StructuredSelection.EMPTY);
		assertFalse(view.removeButton.isEnabled());
	}
}
