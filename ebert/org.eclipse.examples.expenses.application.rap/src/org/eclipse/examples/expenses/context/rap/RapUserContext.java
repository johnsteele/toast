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
package org.eclipse.examples.expenses.context.rap;

import org.eclipse.examples.expenses.context.UserContext;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.rwt.RWT;
import org.eclipse.rwt.service.SessionStoreEvent;
import org.eclipse.rwt.service.SessionStoreListener;

import com.ibm.icu.util.ULocale;

public class RapUserContext extends UserContext {

	public RapUserContext(ExpensesBinder binder) {
		super(binder);
	}

	protected void initialize() {		
		RWT.getSessionStore().addSessionStoreListener(new SessionStoreListener(){		
			public void beforeDestroy(SessionStoreEvent event) {
				RapUserContext.this.dispose();
			}
		});
	}
	
	public ULocale getUserLocale() {
		return ULocale.forLocale(RWT.getLocale());
	}
}
