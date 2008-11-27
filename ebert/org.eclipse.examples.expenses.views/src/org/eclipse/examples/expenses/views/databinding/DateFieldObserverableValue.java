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
package org.eclipse.examples.expenses.views.databinding;

import java.util.Date;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.examples.expenses.widgets.datefield.DateField;
import org.eclipse.examples.expenses.widgets.datefield.common.DateChangeEvent;
import org.eclipse.examples.expenses.widgets.datefield.common.IDateChangeListener;

public class DateFieldObserverableValue extends AbstractObservableValue {
	IDateChangeListener listener = new IDateChangeListener() {
		public void dateChange(DateChangeEvent event) {
			fireValueChange(Diffs.createValueDiff(event.getOldValue(), event.getNewValue()));
		}
	};
	private final DateField field;

	public DateFieldObserverableValue(DateField field) {
		this.field = field;
		field.addDateListener(listener);
	}

	public synchronized void dispose() {
		field.removeDateListener(listener);
	}

	protected Object doGetValue() {
		return field.getDate();
	}

	public Object getValueType() {
		return Date.class;
	}

	protected void doSetValue(Object value) {
		field.setDate((Date) value);
	}
}