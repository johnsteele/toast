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
package org.eclipse.examples.expenses.views;

import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * This class provides privileged access to an instance of {@link BinderView}. It
 * was created to solve a problem: in providing the ability to customize an
 * instance of BinderView, it becomes necessary to provide access too much
 * of the inner workings; in short it forces us to expose too much as API.
 * Instead of making the innards of the view accessible to just anybody, an
 * instance of this class is created to provide access only to consumers we want
 * access granted.
 * <p>
 * The constructor for this class has default visibility meaning that instances
 * can only be created from within the package.
 * <p>
 * Instances of this class are passed to <code>binderViewCustomizers</code>
 * extensions via an instance of {@link IBinderViewCustomizer}.
 * 
 * @see IBinderViewCustomizer
 * @see BinderView#customizeBinderView(org.eclipse.swt.widgets.Composite)
 */
public class BinderViewProxy {

	private final BinderView binderView;

	public BinderViewProxy(BinderView binderView) {
		this.binderView = binderView;
	}

	public ListViewer getExpenseReportViewer() {
		return binderView.expenseReportViewer;
	}

	public Composite getButtonArea() {
		return binderView.getButtonArea();
	}

	public ExpensesBinder getBinder() {
		return binderView.getBinder();
	}
}
