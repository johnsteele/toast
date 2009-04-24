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
package org.eclipse.examples.expenses.context.standalone;

import org.eclipse.examples.expenses.context.UserContext;
import org.eclipse.examples.expenses.core.ExpensesBinder;

import com.ibm.icu.util.ULocale;

public class StandaloneUserContext extends UserContext {
	public StandaloneUserContext(ExpensesBinder binder) {
		super(binder);
	}

	public ULocale getUserLocale() {
		return ULocale.getDefault();
	}
}
