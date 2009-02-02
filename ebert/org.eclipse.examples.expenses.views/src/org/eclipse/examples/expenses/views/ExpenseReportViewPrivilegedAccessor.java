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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

/**
 * This class provides privileged access to an instance of ExpenseReportView. It
 * was created to solve a problem: in providing the ability to customize an
 * instance of ExpenseReportView, it becomes necessary to provide access to much
 * of the inner workings; in short it forces to to expose too much as API.
 * Instead of making the inards of the view accessible to just anybody, an
 * instance of this class is created to provide access only to consumers we want
 * access granted.
 * <p>
 * The constructor for this class has default visibility meaning that instances
 * can only be created from within the package.
 * <p>
 * Instances of this class are passed to <code>expenseReportView</code>
 * extensions via an instance of {@link IExpenseReportViewCustomizer}.
 */
public final class ExpenseReportViewPrivilegedAccessor {

	private final Composite parent;
	private final ExpenseReportView expenseReportView;

	ExpenseReportViewPrivilegedAccessor(Composite parent, ExpenseReportView expenseReportView) {
		this.parent = parent;
		this.expenseReportView = expenseReportView;
	}

	public Composite getParent() {
		return parent;
	}

	public ExpenseReport getExpenseReport() {
		return expenseReportView.expenseReport;
	}

	public TableViewer getLineItemViewer() {
		return expenseReportView.getLineItemViewer();
	}

	public boolean lineItemViewerHasSelection() {
		return !((IStructuredSelection)getLineItemViewer().getSelection()).isEmpty();
	}

	public TableColumn getDateColumn() {
		return expenseReportView.dateColumn;
	}

	public TableColumn getCommentColumn() {
		return expenseReportView.commentColumn;
	}
}
