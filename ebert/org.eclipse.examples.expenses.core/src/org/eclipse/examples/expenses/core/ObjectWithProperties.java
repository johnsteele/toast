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

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

public abstract class ObjectWithProperties implements Serializable {
	transient ListenerList listenerList;

	protected void firePropertyChanged(String propertyName, Object oldValue, Object newValue) {
		if (listenerList == null) return;
		if (listenerList.isEmpty()) return;
		if (oldValue == null && newValue == null) return;
		if (oldValue != null && oldValue.equals(newValue)) return;
		
		PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
		Object[] listeners = listenerList.getListeners();
		for (int index=0;index<listeners.length;index++) {
			IPropertyChangeListener listener = (IPropertyChangeListener) listeners[index];
			listener.propertyChange(event);
		}
	}

	public synchronized void addPropertyChangeListener(IPropertyChangeListener listener) {
		if (listenerList == null) listenerList = new ListenerList();
		listenerList.add(listener);
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		if (listenerList == null) return;
		listenerList.remove(listener);
	}
}
