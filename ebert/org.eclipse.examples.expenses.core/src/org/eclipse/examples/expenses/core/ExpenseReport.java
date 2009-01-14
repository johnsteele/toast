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

/**
 * An ExpenseReport represents a collection of expenses made together, perhaps
 * the result of a business trip or the like. Pragmatically, an ExpenseReport has
 * a title (description) and a collection of {@link LineItem}s, each representing
 * an individual expense.
 */
public class ExpenseReport extends ObjectWithProperties implements Serializable {
	private static final long serialVersionUID = -7372269354627281476L;
		
	/**
	 * The LINEITEMS_PROPERTY constant holds the name of the property used
	 * to notify observers that a change has occurred in the &quot;lineItems&quot; property.
	 */
	public static final String LINEITEMS_PROPERTY = "lineItems";
	
	/**
	 * The TITLE_PROPERTY constant holds the name of the property used to
	 * notify observers that a change has occurred in the &quot;title&quot; property.
	 */
	public static final String TITLE_PROPERTY = "title";
	
	/**
	 * The title field contains a one-line title (or description) that helps the
	 * user to identify the instance.
	 * 
	 * @see setTitle
	 * @see getTitle
	 */
	String title;
	
	/**
	 * The lineItems field contains the list of {@link LineItem} instances referenced
	 * by the receiver. Note that the {@link List} that's used to hold the {@link LineItem}s
	 * is never exposed directly to consumers.
	 * 
	 * @see getLineItems
	 * @see addLineItem
	 * @see removeLineItem
	 */
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

	/**
	 * This method adds a {@link LineItem} to the receiver.
	 * <p>
	 * Note that this method is synchronized so that clients can lock on the
	 * receiver and be assured that no line items will be added by other 
	 * threads as long as they hold that lock.
	 * 
	 * @param lineItem an instance of {@link LineItem}. Must not be <code>null</code>.
	 */
	public synchronized void addLineItem(LineItem lineItem) {
		lineItems.add(lineItem);
		fireCollectionAddEvent(LINEITEMS_PROPERTY, lineItems, lineItem);
	}
	
	/**
	 * This method removes a {@link LineItem} from the receiver.
	 * <p>
	 * Note that this method is synchronized so that clients can lock on the
	 * receiver and be assured that no line items will be removed by other 
	 * threads as long as they hold that lock.
	 * 
	 * @param lineItem an instance of {@link LineItem}. Must not be <code>null</code>.
	 */
	public synchronized void removeLineItem(LineItem lineItem) {
		lineItems.remove(lineItem);		
		fireCollectionRemoveEvent(LINEITEMS_PROPERTY, lineItems, lineItem);
	}	
}
