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
package org.eclipse.examples.expenses.context;

import org.eclipse.examples.expenses.views.model.ViewModel;

import com.ibm.icu.util.ULocale;

public interface IUserContext {
	ULocale getUserLocale();

	ViewModel getViewModel();
	
	void dispose();
}
