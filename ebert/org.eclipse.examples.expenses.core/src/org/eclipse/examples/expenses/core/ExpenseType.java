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

/**
 * Instances of the ExpenseType class represent a type of expense (e.g. "Air Fare", or "Hotel").
 * Note that instances are immutable.
 */
public class ExpenseType implements Comparable, Serializable {

	private static final long serialVersionUID = -5617541680847621474L;
	
	/**
	 * The title of the instance is a short natural language string.
	 */
	final String title;
	
	/**
	 * The ordinality is used for ordering instances. 
	 * 
	 * TODO Remove this; consider sorting expense types based on frequency of use. See bug 259511.
	 */
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
