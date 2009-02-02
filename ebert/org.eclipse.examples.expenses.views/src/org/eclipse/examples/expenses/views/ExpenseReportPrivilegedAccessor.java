package org.eclipse.examples.expenses.views;

import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

public final class ExpenseReportPrivilegedAccessor {

	private final Composite parent;
	private final ExpenseReportView expenseReportView;

	ExpenseReportPrivilegedAccessor(Composite parent, ExpenseReportView expenseReportView) {
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
