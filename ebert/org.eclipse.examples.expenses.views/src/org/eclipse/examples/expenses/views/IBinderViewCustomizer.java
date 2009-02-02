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

public interface IBinderViewCustomizer {
	/**
	 * This method is called as the last act of the
	 * {@link BinderView#createPartControl(org.eclipse.swt.widgets.Composite)}
	 * method. Here, the extender is given an opportunity to customize the
	 * binder view. Note that this is the only opportunity that the extender
	 * gets: no message is sent when the view is disposed. If your
	 * implementation needs to clean up after itself, add a dispose listener to
	 * the parent.
	 * 
	 * @param binderView
	 *            an instance of {@link BinderView}
	 * @param parent
	 *            the instance of {@link Composite} that the BinderView is
	 *            created in.
	 */
	public void postCreateBinderView(BinderView binderView, Composite parent);
}
