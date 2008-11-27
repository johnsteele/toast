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

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.examples.expenses.ui.fields.currency.IMoneyChangeListener;
import org.eclipse.examples.expenses.ui.fields.currency.MoneyChangeEvent;
import org.eclipse.examples.expenses.ui.fields.currency.MoneyField;

import com.ibm.icu.util.CurrencyAmount;

public class MoneyFieldObserverableValue extends AbstractObservableValue {
	IMoneyChangeListener listener = new IMoneyChangeListener() {
		public void moneyChange(MoneyChangeEvent event) {
			fireValueChange(Diffs.createValueDiff(event.getOldValue(), event.getNewValue()));
		}
	};
	private final MoneyField field;

	public MoneyFieldObserverableValue(MoneyField field) {
		this.field = field;
		field.addMoneyChangeListener(listener);
	}

	public synchronized void dispose() {
		field.removeMoneyChangeListener(listener);
	}

	protected Object doGetValue() {
		return field.getMoney();
	}

	public Object getValueType() {
		return CurrencyAmount.class;
	}

	protected void doSetValue(Object value) {
		field.setMoney((CurrencyAmount) value);
	}
}