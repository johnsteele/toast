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
package org.eclipse.examples.expenses.context.rap;

import org.eclipse.examples.expenses.context.IUserContext;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.examples.expenses.views.model.ViewModel;
import org.eclipse.rwt.RWT;

import com.ibm.icu.util.ULocale;

public class RapUserContext implements IUserContext {
	
	private ViewModel viewModel;
	private ExpensesBinder binder;

	public RapUserContext() {
		viewModel = new ViewModel();
		binder = new ExpensesBinder();
		viewModel.setBinder(binder);
	}
	
	public ViewModel getViewModel() {
		return viewModel;
	}

	public ULocale getUserLocale() {
		return ULocale.forLocale(RWT.getLocale());
	}
}
