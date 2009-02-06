package org.eclipse.examples.expenses.application.ercp.customizers;

import org.eclipse.ercp.swt.mobile.Command;
import org.eclipse.examples.expenses.views.BinderViewProxy;
import org.eclipse.examples.expenses.views.IBinderViewCustomizer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class BinderViewCustomizer implements IBinderViewCustomizer {

	private BinderViewProxy proxy;
	private Command addCommand;
	private Command removeCommand;

	public void postCreateBinderView(BinderViewProxy proxy) {
		this.proxy = proxy;
		
		createAddCommand();
		createRemoveCommand();
		
		createDisposeListener(proxy);
	}

	void createDisposeListener(BinderViewProxy proxy) {
		proxy.getExpenseReportViewer().getControl().getParent().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent arg0) {
				addCommand.dispose();
				removeCommand.dispose();
			}			
		});
	}

	void createAddCommand() {
		addCommand = new Command(getParent(), Command.GENERAL, 1);
		addCommand.setText("Add");
		addCommand.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {				
			}

			public void widgetSelected(SelectionEvent arg0) {
				proxy.createExpenseReport();
			}			
		});
	}

	void createRemoveCommand() {
		removeCommand = new Command(getParent(), Command.DELETE, 1);
		removeCommand.setText("Remove");
		removeCommand.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {				
			}

			public void widgetSelected(SelectionEvent arg0) {
				proxy.removeExpenseReports();
			}			
		});
	}

	private Control getParent() {
		return proxy.getExpenseReportViewer().getControl().getParent();
	}
}
