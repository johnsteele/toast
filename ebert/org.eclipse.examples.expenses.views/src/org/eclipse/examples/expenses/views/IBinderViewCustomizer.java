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

/**
 * Instances of classes that implement this interface are given an opportunity
 * to customize an instance of {@link BinderView} after it is created. The
 * instance must be registered with an extension to the
 * <code>org.eclipse.examples.expenses.views.binderViewCustomizers</code>
 * extension-point.
 * 
 * @see BinderView#createPartControl(Composite)
 */
public interface IBinderViewCustomizer {
	public static final String EXTENSION_POINT_ID = "org.eclipse.examples.expenses.views.binderViewCustomizers";

	/**
	 * This method is called as the last act of the
	 * {@link BinderView#createPartControl(Composite)}
	 * method. Here, the extender is given an opportunity to customize the
	 * binder view. Note that this is the only opportunity that the extender
	 * gets: no message is sent when the view is disposed. If your
	 * implementation needs to clean up after itself, add a dispose listener to
	 * the parent.
	 * 
	 * @param proxy
	 *            instance of {@link BinderViewProxy} that represents the
	 *            BinderView we're customizing.
	 */
	public void postCreateBinderView(BinderViewProxy proxy);
}
