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
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.examples.expenses.core.ObjectWithProperties;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public class ObjectWithPropertiesObservableValue extends AbstractObservableValue {
	private final ObjectWithProperties object;
	private final String propertyName;
	private final PropertyGetterSetter getterSetter;

	private IPropertyChangeListener listener;

	public interface PropertyGetterSetter {
		Object getValue(Object source);
		void setValue(Object Source, Object value);
		Object getType();
	}
		
	public ObjectWithPropertiesObservableValue(ObjectWithProperties object, String propertyName, PropertyGetterSetter getterSetter) {
		this.object = object;
		this.propertyName = propertyName;
		this.getterSetter = getterSetter;
	}

	protected Object doGetValue() {
		return getterSetter.getValue(object);
	}

	protected void doSetValue(Object value) {
		getterSetter.setValue(object, value);
	}
	
	public Object getObserved() {
		return object;
	}

	public Object getValueType() {
		return getterSetter.getType();
	}

	protected void firstListenerAdded() {
		listener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (!event.getProperty().equals(propertyName)) return;
				final ValueDiff diff = Diffs.createValueDiff(event.getOldValue(), event.getNewValue());
				fireValueChange(diff);
			}
		};
		object.addPropertyChangeListener(listener);
		super.firstListenerAdded();
	}
	
	public synchronized void dispose() {
		if (listener == null) return;
		object.removePropertyChangeListener(listener);
		listener = null;
		super.dispose();
	}
}
