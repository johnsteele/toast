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

/**
 * Instances of the ExpenseBinder class collect related instances of {@link ExpenseReport}
 * together.
 */
public class ExpensesBinder extends ObjectWithProperties implements Serializable {

	private static final long serialVersionUID = -4963643349830605681L;

	// TODO Make this more dynamic
	private static final ExpenseType[] EXPENSE_TYPES = new ExpenseType[] {
				new ExpenseType("Air Travel"),
				new ExpenseType("Other Travel"),
				new ExpenseType("Hotel"),
				new ExpenseType("Employee Meal"),
				new ExpenseType("Business Meal"),
				new ExpenseType("Hardware"),
				new ExpenseType("Software"),
				new ExpenseType("Other")
			};
	
	/**
	 * The REPORTS_PROPERTY constant holds the name of the property used to
	 * notify observers that a change has occurred in the &quot;reports&quot; property.
	 */
	public static final String REPORTS_PROPERTY = "reports";

	/**
	 * This method returns an array of all the {@link ExpenseType}s that the
	 * receiver knows about.
	 * 
	 * @return an array of {@link ExpenseType} instances.
	 */
	public static ExpenseType[] getExpenseTypes() {
		return EXPENSE_TYPES;
	}

	/**
	 * The reports field holds the collection of {@link ExpenseReport} instances
	 * maintained by the receiver. Note that the {@link List} used to hold these
	 * instances is never exposed to the consumer.
	 * 
	 * @see getReports
	 * @see addExpenseReport
	 * @see removeExpenseReport
	 */
	List reports = new ArrayList();

	/**
	 * This method answers an array of {@link ExpenseReport}s owned by the receiver.
	 * Note that this method does not guarantee that the returned value will
	 * be the same object on subsequent calls.
	 * 
	 * @return an array of {@link ExpenseReport} instances.
	 */
	public ExpenseReport[] getReports() {
		return (ExpenseReport[]) reports.toArray(new ExpenseReport[reports.size()]) ;
	}

	public synchronized void addExpenseReport(ExpenseReport report) {
		reports.add(report);
		
		fireCollectionAddEvent(REPORTS_PROPERTY, reports, report);
	}

	public synchronized void removeExpenseReport(ExpenseReport report) {
		if (!reports.remove(report)) return;
		
		fireCollectionRemoveEvent(REPORTS_PROPERTY, reports, report);
	}
}