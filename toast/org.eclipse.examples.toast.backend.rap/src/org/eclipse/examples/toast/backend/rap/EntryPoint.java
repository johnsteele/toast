/*******************************************************************************
 * Copyright (c) 2009 EclipseSource Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v1.0 
 * which accompanies this distribution. The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Eclipse Distribution License 
 * is available at http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors: 
 *     EclipseSource Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.examples.toast.backend.rap;

import org.eclipse.rwt.lifecycle.IEntryPoint;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;

public class EntryPoint implements IEntryPoint {

	public int createUI() {
		Display display = PlatformUI.createDisplay();
		WorkbenchAdvisor worbenchAdvisor = new ApplicationWorkbenchAdvisor();
		return PlatformUI.createAndRunWorkbench(display, worbenchAdvisor);
	}
}
