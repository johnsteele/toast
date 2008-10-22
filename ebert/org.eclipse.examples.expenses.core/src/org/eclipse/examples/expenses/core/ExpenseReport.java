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
import java.util.Properties;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class ExpenseReport extends ObjectWithProperties implements Serializable {
	
	public static final String LINEITEMS_PROPERTY = "lineItems";
	public static final String TITLE_PROPERTY = "title";
		
	private String title;
	List lineItems = new ArrayList();

	public ExpenseReport(String title) {
		this.title = title;
	}

	/**
	 * This method answers an array of {@link LineItem}s owned by the receiver.
	 * Note that this method does not guarantee that the returned value will
	 * be the same object on subsequent calls.
	 * 
	 * @return an array of {@link LineItem} instances.
	 */
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

	public void addLineItem(LineItem lineItem) {
		lineItems.add(lineItem);

		Properties properties = new Properties();
		properties.put(OBJECT_ADDED, lineItem);
		
		fireCollectionEvent(LINEITEMS_PROPERTY, lineItems, OBJECT_ADDED, lineItem);
	}
	
	public void removeLineItem(LineItem lineItem) {
		lineItems.remove(lineItem);
		fireCollectionEvent(LINEITEMS_PROPERTY, lineItems, OBJECT_REMOVED, lineItem);
	}	
}
