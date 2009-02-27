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
import org.eclipse.ui.IWorkbenchPage;

public class LineItemViewProxy {

	private final LineItemView lineItemView;

	public LineItemViewProxy(LineItemView lineItemView) {
		this.lineItemView = lineItemView;
	}

	public Composite getParent() {
		return lineItemView.getParent();
	}

	public IWorkbenchPage getPage() {
		return lineItemView.getSite().getPage();
	}

}
