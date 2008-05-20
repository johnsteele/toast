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
package org.eclipse.examples.expenses.application.general;

import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.examples.expenses.core.ExpensesCoreActivator;

public class ApplicationStateManager {

	private final String name;
	private ExpensesBinder expensesBinder;

	public ApplicationStateManager(String name) {
		this.name = name;
		expensesBinder = ExpensesCoreActivator.getDefault().loadExpensesBinder(name);
		if (expensesBinder == null) {
			expensesBinder = new ExpensesBinder();
		}
	}

	public ExpensesBinder getBinder() {
		return expensesBinder;
	}

	public void save() {
		ExpensesCoreActivator.getDefault().saveExpensesBinder(name, expensesBinder);
	}

}
