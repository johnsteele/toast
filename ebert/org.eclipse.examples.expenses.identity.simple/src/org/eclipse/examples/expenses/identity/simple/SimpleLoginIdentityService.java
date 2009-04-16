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
package org.eclipse.examples.expenses.identity.simple;

import org.eclipse.examples.expenses.context.IIdentityService;
import org.eclipse.swt.widgets.Display;

public class SimpleLoginIdentityService implements IIdentityService {
	
	public String getUserId() {
		return login(Display.getCurrent());
	}

	String login(Display display) {
		LoginDialog dialog = new LoginDialog(display);
		dialog.login();
		
		//if (dialog.isCancelled()) throw new IdentityAssertionRefusedException();
		
		return dialog.getUserId();
	}
}
