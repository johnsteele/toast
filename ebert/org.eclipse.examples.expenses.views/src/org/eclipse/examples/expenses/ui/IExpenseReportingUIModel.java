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

public interface IExpenseReportingUIModel {

	void setBinder(ExpensesBinder binder);
	ExpensesBinder getBinder();
	
	void setReport(ExpenseReport report);
	ExpenseReport getReport();

	void setLineItem(LineItem item);
	LineItem getLineItem();

	void addListener(ExpenseReportingUIModelListener listener);

	void removeListener(ExpenseReportingUIModelListener listener);

	void dispose();

}