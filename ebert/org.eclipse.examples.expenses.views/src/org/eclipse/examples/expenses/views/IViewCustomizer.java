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
package org.eclipse.examples.expenses.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;

/**
 * Instances of classes implementing this interface are called by the view
 * classes after the construction of the view is complete. The implementing
 * class is provided to the view via the
 * <code>org.eclipse.examples.expenses.views.viewCustomizers</code>
 * extension-point.
 */
public interface IViewCustomizer {
	void customizeView(Composite parent, IViewPart view);
}
