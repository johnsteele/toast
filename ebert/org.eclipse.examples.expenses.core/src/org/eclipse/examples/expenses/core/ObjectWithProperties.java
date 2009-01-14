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
import java.util.Collection;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 * ObjectWithProperties is an abstract superclass for types whose instances need
 * to perform property change notification. Two types of notification are
 * supported simultaneously: observer-style, and queued.
 * 
 *<p>
 * To make use of the observer-style events, interested objects can add (and
 * remove) listeners to (from) an instance via the
 * {@link #addPropertyChangeListener(IPropertyChangeListener)} and
 * {@link #removePropertyChangeListener(IPropertyChangeListener)} methods. When
 * a property changes, registered listeners&mdash;instances of
 * {@link IPropertyChangeListener}&mdash; are sent the
 * {@link IPropertyChangeListener#propertyChange(PropertyChangeEvent)} message
 * loaded with an instance of {@link PropertyChangeEvent} that describes change.
 * If that property is a collection, and instance of
 * {@link CollectionPropertyChangeEvent} which contains specific information
 * about what has changed in the collection is provided instead.
 */
public abstract class ObjectWithProperties implements Serializable {
	private static final long serialVersionUID = 5776294072021059051L;
	
	transient ListenerList listenerList;

	/**
	 * This method notifies the world that a property has changed (or at least
	 * the parts of the world that care). Any registered bound property
	 * observers are notified. 
	 * <p>
	 * Note that this method is intended to be used directly by subclasses
	 * 
	 * @param propertyName
	 *            The name of the property that's changed.
	 * @param oldValue
	 *            The previous value of the property.
	 * @param newValue
	 *            The new value of the property.
	 */
	protected void firePropertyChanged(String propertyName, Object oldValue, Object newValue) {
		if (oldValue == null && newValue == null)
			return;
		if (oldValue != null && oldValue.equals(newValue))
			return;

		if (listenerList == null)
			return;
		if (listenerList.isEmpty())
			return;

		fireEvent(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
	}

	/**
	 * This method notifies the world that a collection property has changed (or
	 * at least the parts of the world that care). Any registered bound property
	 * observers are notified.
	 * 
	 * <p>
	 * Note that this method is intended to be used directly by subclasses
	 * 
	 * @param propertyName
	 *            The name of the property that has changed. Must not be
	 *            <code>null</code>
	 * @param collection
	 *            The value of the property. Must not be <code>null</code>.
	 * @param object
	 *            The object that has been added or removed.
	 */
	protected void fireCollectionAddEvent(String propertyName, Collection collection, Object object) {
		if (listenerList == null) return;
		if (listenerList.isEmpty()) return;

		PropertyChangeEvent event = new CollectionPropertyChangeEvent(this,
				propertyName, collection, new Object[] { object }, new Object[0]);
		fireEvent(event);
	}

	
	/**
	 * This method notifies registered property change listeners that an object
	 * has been removed from a collection property. *
	 * <p>
	 * Note that this method is intended to be used directly by subclasses
	 * 
	 * @param propertyName
	 *            The name of the property that has changed. Must not be
	 *            <code>null</code>
	 * @param collection
	 *            The value of the property. Must not be <code>null</code>.
	 * @param object
	 *            The object that has been added or removed. Must not be
	 *            <code>null</code>.
	 */
	protected void fireCollectionRemoveEvent(String propertyName, Collection collection, Object object) {
		if (listenerList == null) return;
		if (listenerList.isEmpty()) return;

		PropertyChangeEvent event = new CollectionPropertyChangeEvent(this,
				propertyName, collection, new Object[0], new Object[] { object });
		fireEvent(event);
	}

	private void fireEvent(PropertyChangeEvent event) {
		Object[] listeners = listenerList.getListeners();
		for (int index = 0; index < listeners.length; index++) {
			IPropertyChangeListener listener = (IPropertyChangeListener) listeners[index];
			listener.propertyChange(event);
		}
	}
	
	/**
	 * This method returns an array containing those
	 * {@link IPropertyChangeListener} instances that have been added to the
	 * receiver.
	 * 
	 * <p>
	 * This method is really only intended for testing purposes; as such, the
	 * implementation is as complex as it needs to be (more specifically, it's
	 * not all that complex).
	 * 
	 * @return an array of {@link IPropertyChangeListener} instances that have
	 *         been added to the receiver.
	 */
	public Object[] getPropertyChangeListeners() {
		return listenerList.getListeners();
	}

	/**
	 * This method adds an {@link IPropertyChangeListener} to the receiver. This
	 * listener will be notified whenever a property changes in the receiver.

	 * @param listener an instance of {@link IPropertyChangeListener}. Must not be <code>null</code>.
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		/*
		 * Since the listenerList field is marked as transient, we need
		 * to lazy initialize. If, for example, the instance has been
		 * deserialized, this field will not have been initialized.
		 * Ideally--and once we get away from serialization--we'll
		 * just initialize the instance in the constructor and be
		 * done with it.
		 * 
		 * FWIW, I hate double-check locking.
		 */
		if (listenerList == null) {
			synchronized (this) {
				if (listenerList == null) listenerList = new ListenerList();
			}
		}
		listenerList.add(listener);
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		if (listenerList == null) return;
		listenerList.remove(listener);
	}
}
