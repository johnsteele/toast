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
import java.util.ArrayList;
import java.util.List;

public class ExpenseReport extends ObjectWithProperties implements Serializable {
	
	public static final String LINEITEMS_PROPERTY = "lineItems";
	public static final String TITLE_PROPERTY = "title";
		
	private String title;
	List lineItems = new ArrayList();

	public ExpenseReport(String title) {
		this.title = title;
	}

	public void addLineItem(LineItem lineItem) {
		lineItems.add(lineItem);
		firePropertyChanged(LINEITEMS_PROPERTY, null, getLineItems());
	}

	public LineItem[] getLineItems() {
		return (LineItem[]) lineItems.toArray(new LineItem[lineItems.size()]);
	}

	public void setTitle(String title) {
		String oldValue = this.title;
		this.title = title;
		firePropertyChanged(TITLE_PROPERTY, oldValue, title);
	}

	public String getTitle() {
		return title;
	}

	public void removeLineItem(LineItem lineItem) {
		lineItems.remove(lineItem);
		firePropertyChanged(LINEITEMS_PROPERTY, null, getLineItems());
	}
}
