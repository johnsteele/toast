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
package org.eclipse.examples.expenses.core;

import java.io.Serializable;

public class ExpenseType implements Comparable, Serializable {

	final String title;
	final int ordinality;

	public ExpenseType(String title, int ordinality) {
		this.title = title;
		this.ordinality = ordinality;
	}

	public String getTitle() {
		return title;
	}

	public int compareTo(Object object) {
		if (object == null) return -1;
		if (!(object instanceof ExpenseType)) return -1;
		ExpenseType other = (ExpenseType)object;
		if (other.ordinality == this.ordinality) return 0;
		if (other.ordinality > this.ordinality) return -1;
		return 1;
	}
}
