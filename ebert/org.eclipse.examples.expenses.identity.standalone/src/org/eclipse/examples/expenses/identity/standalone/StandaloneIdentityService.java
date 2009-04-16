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
package org.eclipse.examples.expenses.identity.standalone;

import org.eclipse.examples.expenses.context.IIdentityService;

public class StandaloneIdentityService implements IIdentityService {

	public String getUserId() {
		return "local-user";
	}

}
