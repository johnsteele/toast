/*******************************************************************************
 * Copyright (c) 2009 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.examples.expenses.views;

import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

/**
 * This class provides privileged access to an instance of
 * {@link ExpenseReportView}. It was created to solve a problem: in providing
 * the ability to customize an instance of ExpenseReportView, it becomes
 * necessary to provide access too much of the inner workings; in short it
 * forces us to expose too much as API. Instead of making the innards of the
 * view accessible to just anybody, an instance of this class is created to
 * provide access only to consumers we want access granted.
 * <p>
 * The constructor for this class has default visibility meaning that instances
 * can only be created from within the package.
 * <p>
 * Instances of this class are passed to <code>expenseReportView</code>
 * extensions via an instance of {@link IExpenseReportViewCustomizer}.
 * 
 * @see IExpenseReportViewCustomizer
 * @see ExpenseReportView#customizeExpenseReportView()
 */
public final class ExpenseReportViewProxy {
	private final ExpenseReportView expenseReportView;

	ExpenseReportViewProxy(ExpenseReportView expenseReportView) {
		this.expenseReportView = expenseReportView;
	}

	public ExpenseReport getExpenseReport() {
		return expenseReportView.expenseReport;
	}

	public TableViewer getLineItemViewer() {
		return expenseReportView.getLineItemViewer();
	}

	public boolean lineItemViewerHasSelection() {
		return !((IStructuredSelection) getLineItemViewer().getSelection())
				.isEmpty();
	}

	public TableColumn getDateColumn() {
		return expenseReportView.dateColumn;
	}

	public TableColumn getAmountColumn() {
		return expenseReportView.amountColumn;
	}

	public TableColumn getTypeColumn() {
		return expenseReportView.typeColumn;
	}

	public TableColumn getCommentColumn() {
		return expenseReportView.commentColumn;
	}

	/**
	 * This method returns the area of the view where buttons can be added. The
	 * button area stretches across the bottom of the view; it uses a
	 * {@link RowLayout} to, curiously enough, assemble widgets placed into it
	 * in a tidy row.
	 * <p>
	 * WARNING: This method must be run in the UI Thread.
	 * 
	 * @return a {@link Composite}.
	 */
	public Composite getButtonArea() {
		return expenseReportView.getButtonArea();
	}

	public void createLineItem() {
		if (getExpenseReport() == null) return;
		getExpenseReport().addLineItem(new LineItem());
	}

	public void removeLineItems() {
		if (getExpenseReport() == null) return;
		IStructuredSelection selection = (IStructuredSelection)getLineItemViewer().getSelection();
		Object[] objects = selection.toArray();
		for(int index=0;index<objects.length;index++){
			getExpenseReport().removeLineItem((LineItem)objects[index]);					
		}
	}
}
