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

import org.eclipse.examples.expenses.views.BinderView;
import org.eclipse.examples.expenses.views.ExpenseReportView;
import org.eclipse.examples.expenses.views.LineItemView;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {
	
	public static final String ID = Perspective.class.getName();

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);

		layout.addStandaloneView(BinderView.ID,  false, IPageLayout.LEFT, 0.3f, editorArea);
		layout.addStandaloneView(ExpenseReportView.ID,  false, IPageLayout.TOP, 0.5f, editorArea);
		layout.addStandaloneView(LineItemView.ID,  false, IPageLayout.BOTTOM, 0.5f, editorArea);
	}

}
