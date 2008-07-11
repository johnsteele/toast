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
import java.util.Properties;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public abstract class ObjectWithProperties implements Serializable {
	public static final String PROPERTY_CHANGE_TOPIC = "PropertyChange";
	public static final String SOURCE = "source";
	public static final String PROPERTY_NAME = "propertyName";
	public static final String OLD_VALUE = "oldValue";
	public static final String NEW_VALUE = "newValue";

	public static final String OBJECT_ADDED = "added";
	public static final String OBJECT_REMOVED = "removed";
	public static final String SOURCE_TYPE = "sourceType";
	public static final String EVENT_TYPE = "eventType";
	
	transient ListenerList listenerList;

	/**
	 * This method notifies the world that a property has changed (or at least
	 * the parts of the world that care). Any registered bound property
	 * observers are notified. Additionally, an event is delivered via the OSGi
	 * event service on the {@link #PROPERTY_CHANGE_TOPIC} topic.
	 * 
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
		if (oldValue == null && newValue == null) return;
		if (oldValue != null && oldValue.equals(newValue)) return;
		
		postEvent(propertyName, oldValue, newValue);
		
		if (listenerList == null) return;
		if (listenerList.isEmpty()) return;
		
		PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
		Object[] listeners = listenerList.getListeners();
		for (int index=0;index<listeners.length;index++) {
			IPropertyChangeListener listener = (IPropertyChangeListener) listeners[index];
			listener.propertyChange(event);
		}
	}

	/**
	 * This method notifies the world that a collection property has changed (or
	 * at least the parts of the world that care). Any registered bound property
	 * observers are notified. Additionally, an event is delivered via the OSGi
	 * event service on the {@link #PROPERTY_CHANGE_TOPIC} topic.
	 * 
	 * <p>
	 * Note that this method is intended to be used directly by subclasses
	 * 
	 * @param propertyName
	 *            The name of the property that has changed.
	 * @param collection
	 *            The value of the property.
	 * @param eventType
	 *            The type of change, either {@link #OBJECT_ADDED}, or
	 *            {@link #OBJECT_REMOVED}.
	 * @param object
	 *            The object that has been added or removed.
	 */
	protected void fireCollectionEvent(String propertyName, Collection collection, String eventType, Object object) {
		postCollectionEvent(propertyName, collection, eventType, object);

		if (listenerList == null) return;
		if (listenerList.isEmpty()) return;
		
		PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, null, collection);
		Object[] listeners = listenerList.getListeners();
		for (int index=0;index<listeners.length;index++) {
			IPropertyChangeListener listener = (IPropertyChangeListener) listeners[index];
			listener.propertyChange(event);
		}
	}

	/**
	 * This method posts a change event onto the {@link EventAdmin} queue if one
	 * is available. Note that the event's properties include {@value
	 * #NEW_VALUE} and {@value #OLD_VALUE} properties only if the corresponding
	 * values are non-<code>null</code>. That is, if the entries for these keys
	 * are absent, then it is safe to assume that the value is <code>null</code>.
	 * 
	 * <p>
	 * Note that this method is not part of the API.
	 * 
	 * @param propertyName
	 *            The name of the property that has changed.
	 * @param oldValue
	 *            The value of the property prior to the change.
	 * @param newValue
	 *            The new value of the property.
	 */
	void postEvent(String propertyName, Object oldValue, Object newValue) {
		EventAdmin eventAdmin = getEventAdmin();
		if (eventAdmin == null) return;
		
		Properties properties = new Properties();
		properties.put(SOURCE, this);
		properties.put(SOURCE_TYPE, this.getClass().getName());
		properties.put(PROPERTY_NAME, propertyName);
		if (oldValue != null) properties.put(OLD_VALUE, oldValue);
		if (newValue != null) properties.put(NEW_VALUE, newValue);
		
		eventAdmin.postEvent(new Event(PROPERTY_CHANGE_TOPIC, properties));
	}
	
	/**
	 * This method posts an event via the OSGi event service on the
	 * {@link #PROPERTY_CHANGE_TOPIC} topic.
	 * 
	 * <p>
	 * Note that this method is not part of the API and is not intended to be
	 * directly used by subclasses.
	 * 
	 * @param propertyName
	 *            The name of the property that has changed.
	 * @param collection
	 *            The value of the property.
	 * @param eventType
	 *            The type of change, either {@link #OBJECT_ADDED}, or
	 *            {@link #OBJECT_REMOVED}.
	 * @param object
	 *            The object that has been added or removed.
	 */
	void postCollectionEvent(String propertyName, Collection collection, String eventType, Object object) {
		EventAdmin eventAdmin = getEventAdmin();
		if (eventAdmin == null) return;
		
		Properties properties = new Properties();
		properties.put(SOURCE, this);
		properties.put(SOURCE_TYPE, this.getClass().getName());
		properties.put(EVENT_TYPE, eventType);
		properties.put(PROPERTY_NAME, propertyName);
		properties.put(NEW_VALUE, collection);
		properties.put(eventType, object);
		
		eventAdmin.sendEvent(new Event(PROPERTY_CHANGE_TOPIC, properties));
	}
	
	protected EventAdmin getEventAdmin() {
		return ExpensesCoreActivator.getDefault().getEventAdmin();
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
