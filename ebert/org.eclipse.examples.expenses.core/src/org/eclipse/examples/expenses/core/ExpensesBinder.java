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
package org.eclipse.examples.expenses.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExpensesBinder extends ObjectWithProperties implements Serializable {

	// TODO Make this more dynamic
	private static final ExpenseType[] EXPENSE_TYPES = new ExpenseType[] {
				new ExpenseType("Air Travel", 0),
				new ExpenseType("Other Travel", 1),
				new ExpenseType("Hotel",2),
				new ExpenseType("Employee Meal",3),
				new ExpenseType("Business Meal",4),
				new ExpenseType("Hardware",5),
				new ExpenseType("Software",6),
				new ExpenseType("Other", 7)
			};
	
	public static final String REPORTS_PROPERTY = "reports";

	public static ExpenseType[] getTypes() {
		return EXPENSE_TYPES;
	}

	List reports = new ArrayList();
	
	public ExpenseReport[] getReports() {
		return (ExpenseReport[]) reports.toArray(new ExpenseReport[reports.size()]) ;
	}

	public void addExpenseReport(ExpenseReport report) {
		reports.add(report);
		fireCollectionEvent(REPORTS_PROPERTY, reports, OBJECT_ADDED, report);
	}

	public void removeExpenseReport(ExpenseReport report) {
		if (!reports.remove(report)) return;
		fireCollectionEvent(REPORTS_PROPERTY, reports, OBJECT_REMOVED, report);
	}
	
	
}