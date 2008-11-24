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
package org.eclipse.examples.expenses.ui.fields.currency;

import java.util.EventObject;

import com.ibm.icu.util.CurrencyAmount;

public class MoneyChangeEvent extends EventObject {
	private static final long serialVersionUID = -7815430026338867768L;
	
	private final CurrencyAmount newValue;

	public MoneyChangeEvent(Object source, CurrencyAmount money) {
		super(source);
		this.newValue = money;
	}

	public CurrencyAmount getNewValue() {
		return newValue;
	}
}
