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

import org.eclipse.swt.widgets.Composite;

public interface IExpenseReportViewCustomizer {
	public static final String EXTENSION_POINT_ID = "org.eclipse.examples.expenses.views.expenseReportViewCustomizers";

	/**
	 * This method is called as the last act of the
	 * {@link ExpenseReportView#createPartControl(Composite)} method. Here, the
	 * extender is given an opportunity to customize the view. Note that this is
	 * the only opportunity that the extender gets: no message is sent when the
	 * view is disposed. If your implementation needs to clean up after itself,
	 * add a dispose listener to the parent.
	 * <p>
	 * The customizer is not passed the {@link ExpenseReportView} directly, but
	 * rather is passed a proxy (instance of {@link ExpenseReportViewProxy} which
	 * can be used to indirectly access the view.
	 * 
	 * @see ExpenseReportView#customizeExpenseReportView()
	 * @see ExpenseReportViewProxy
	 * 
	 * @param proxy
	 *            an instance of {@link ExpenseReportViewProxy} that acts as a
	 *            proxy for the {@link ExpenseReportView} being customized.
	 */
	void postCreateExpenseReportView(ExpenseReportViewProxy proxy);
}
