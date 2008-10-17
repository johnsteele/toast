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

import java.util.Collection;

import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * This class provides a little more information about property change events
 * that result from a change in a contained collection. More specifically,
 * instances of this class tell us the source of the change, the changed form of
 * the collection, and what objects have been added or removed from the
 * collection.
 * 
 * <p>
 * For example, when an {@link ExpenseReport} is added to an
 * {@link ExpensesBinder}, an instance of this class is created and dispatched
 * to any listeners (the instance is only created if there is at least one
 * listener). The instance's &quot;source&quot; property would be the
 * ExpenseBinder; the &quot;property&quot; would be
 * {@value ExpensesBinder#REPORTS_PROPERTY}, the &quot;oldValue&quot; would be
 * <code>null</code>, the &quot;newValue&quot; would be the collection of
 * reports contained in the ExpenseseBinder, {@link #added} would be an array
 * containing the new ExpenseReport, and {@link #removed} would be an empty
 * collection.
 * 
 * <p>
 * The {@link #added} and {@link #removed} fields are never <code>null</code>.
 * 
 * @author wayne
 * 
 */
public class CollectionPropertyChangeEvent extends PropertyChangeEvent {

	private static final long serialVersionUID = 5718269324650795376L;
	
	public final Object[] added;
	public final Object[] removed;

	public CollectionPropertyChangeEvent(Object source, String property, Collection collection, Object[] added, Object[] removed) {
		super(source, property, null, collection);
		this.added = added != null ? added : new Object[0];
		this.removed = removed != null ? removed : new Object[0];
	}

}
