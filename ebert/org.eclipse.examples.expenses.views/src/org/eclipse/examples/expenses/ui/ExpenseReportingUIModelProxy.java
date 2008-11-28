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
package org.eclipse.examples.expenses.ui;

import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.examples.expenses.core.LineItem;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

public class ExpenseReportingUIModelProxy implements IExpenseReportingUIModel {
	/**
	 * This field provides a default implementor of
	 * {@link IExpenseReportingUIModel} in the event that a service that
	 * provides an instance has not been created.
	 */
	private static final IExpenseReportingUIModel DefaultExpenseReportingUIModel = new ExpenseReportingUIModel();
	
	private ServiceTracker serviceTracker;	
	private IExpenseReportingUIModel service;
		
	public ExpenseReportingUIModelProxy(BundleContext context) {
		serviceTracker = new ServiceTracker(context, IExpenseReportingUIModel.class.getName(), null) {
			public Object addingService(ServiceReference reference) {
				IExpenseReportingUIModel newService = (IExpenseReportingUIModel) super.addingService(reference);
				
				if (service == null) {
					service = newService;
				}
				
				return newService;
			}
			
			/**
			 * When a matching service is removed, we check to see if it is the one
			 * that we're following. If it is, then we <code>null</code>-out our
			 * reference to it. The next time that we need a service, we'll ask
			 * the tracker for it.
			 * 
			 * @see ExpenseReportingUIModelProxy#getService
			 */
			public void removedService(ServiceReference reference, Object oldService) {
				super.removedService(reference, service);
				if (oldService == service) {
					service = null;
				}
			}
		};
		serviceTracker.open();
	}

	public void addListener(ExpenseReportingUIModelListener listener) {
		getService().addListener(listener);
	}

	public void removeListener(ExpenseReportingUIModelListener listener) {
		getService().removeListener(listener);
	}
	
	public void dispose() {
		serviceTracker.close();
	}

	public ExpensesBinder getBinder() {
		return getService().getBinder();
	}
	public LineItem getLineItem() {
		return getService().getLineItem();
	}

	public ExpenseReport getReport() {
		return getService().getReport();
	}

	protected synchronized IExpenseReportingUIModel getService() {
		if (service == null) service = (IExpenseReportingUIModel) serviceTracker.getService();
		if (service == null) return DefaultExpenseReportingUIModel;
		return service;
	}

	public void setBinder(ExpensesBinder binder) {
		getService().setBinder(binder);
	}

	public void setLineItem(LineItem item) {
		getService().setLineItem(item);
	}

	public void setReport(ExpenseReport report) {
		getService().setReport(report);
	}

}
