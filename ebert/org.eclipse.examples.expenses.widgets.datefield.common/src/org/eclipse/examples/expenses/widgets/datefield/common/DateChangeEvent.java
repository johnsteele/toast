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
package org.eclipse.examples.expenses.widgets.datefield.common;

import java.util.Date;
import java.util.EventObject;

public class DateChangeEvent extends EventObject {
	private static final long serialVersionUID = 3360451196508645054L;
	
	private final Date newValue;

	public DateChangeEvent(Object source, Date newValue) {
		super(source);
		this.newValue = newValue;
	}

	public Date getNewValue() {
		return newValue;
	}
}
