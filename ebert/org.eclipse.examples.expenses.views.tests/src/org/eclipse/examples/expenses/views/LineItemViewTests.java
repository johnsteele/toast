package org.eclipse.examples.expenses.views;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.util.Date;

import org.eclipse.examples.expenses.core.ExpenseType;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class LineItemViewTests {

	private LineItemView view;
	private LineItem lineItem;

	@Before
	public void setUp() throws Exception {
		view = (LineItemView) getActivePage().showView(LineItemView.ID);
		lineItem = new LineItem();
		view.setLineItem(lineItem);
	}
	
	@After
	public void shutdown() {
		getActivePage().hideView(view);
	}

	private IWorkbenchPage getActivePage() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	/**
	 * We expect that, when the date in our date field is changed,
	 * the change is propagated to the lineItem.
	 */
	@Test
	public void testDateFieldChangeReflectedInModel() {
		Date today = new Date();
		view.dateField.setDate(today);
		assertSame(today, lineItem.getDate());
	}
	
	@Test
	public void testModelChangeReflectedInDateField() {
		Date today = new Date();
		lineItem.setDate(today);
		assertSame(today, view.dateField.getDate());
	}

	@Test
	public void testTypeFieldChangeReflectedInModel() {
		ExpenseType type = ExpensesBinder.getTypes()[0];
		view.typeDropdown.setSelection(new StructuredSelection(type));
		assertSame(type, lineItem.getType());
	}

	@Test
	public void testModelChangeReflectedinTypeField() {
		ExpenseType type = ExpensesBinder.getTypes()[0];
		lineItem.setType(type);
		assertSame(type, ((IStructuredSelection)view.typeDropdown.getSelection()).getFirstElement());
	}
	
	@Test
	public void testTypeFieldCorrectlyObtainsContentAndLabels() {
		view.typeDropdown.setInput(new ExpenseType[] {new ExpenseType("Expense", 1), new ExpenseType("Air fare", 2)});
		assertEquals("Expense", view.typeDropdown.getCombo().getItems()[0]);
		assertEquals("Air fare", view.typeDropdown.getCombo().getItems()[1]);
	}
	
	@Test
	public void testCreateAmountField() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateExchangeRateField() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateCommentField() {
		fail("Not yet implemented");
	}

	@Test
	public void testHandleSelectionISelection() {
		fail("Not yet implemented");
	}

	@Test
	public void testHandleSelectionIStructuredSelection() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetLineItem() {
		fail("Not yet implemented");
	}

}
