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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public abstract class AbstractView extends ViewPart {
	private Composite buttonArea;
	
	protected Composite createButtonArea(Composite parent) {
		buttonArea = new Composite(parent, SWT.NONE);
		buttonArea.setLayout(new RowLayout());
		return buttonArea;
	}
	
	public Composite getButtonArea() {
		return buttonArea;
	}

	protected void customizeView(Composite parent) {
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.examples.expenses.views.viewCustomizers");
		for(int index=0;index<elements.length;index++) {
			try {
				customizeView(parent, elements[index]);
			} catch (CoreException e) {
				// TODO Need to log this.
			}
		}
	}

	void customizeView(Composite parent, IConfigurationElement element) throws CoreException {
		IViewCustomizer customizer = (IViewCustomizer) element.createExecutableExtension("class");
		customizer.customizeView(parent, this);
	}

	protected void asyncExec(Runnable runnable) {
		getViewSite().getWorkbenchWindow().getShell().getDisplay().asyncExec(runnable);
	}

	public abstract Viewer getViewer();
}