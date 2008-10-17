package org.eclipse.examples.expenses.views;

import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public abstract class WorkbenchTests {

	/**
	 * This method makes sure that the UI has had an opportunity to process any
	 * outstanding events. This includes any asynchronous blocks introduced to
	 * the display via calls to {@link Display#asyncExec(Runnable)} (see
	 * {@link BinderView#setBinder(ExpensesBinder)} as an example). The
	 * following line forces those queued up asynchronous tasks to run.
	 */
	protected void processEvents() {
		while (getWorkbench().getDisplay().readAndDispatch());
	}

	protected IWorkbenchPage getActivePage() {
		return getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	protected IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

}