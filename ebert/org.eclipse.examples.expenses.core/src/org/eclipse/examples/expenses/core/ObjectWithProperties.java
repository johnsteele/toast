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
import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

/**
 * ObjectWithProperties is an abstract superclass for types whose instances need
 * to perform property change notification. Two types of notification are
 * supported simultaneously: observer-style, and queued.
 * 
 *<p>
 * To make use of the observer-style events, interested objects can add (and
 * remove) listeners to (from) an instance via the
 * {@link #addPropertyChangeListener(IPropertyChangeListener)} and
 *{@link #removePropertyChangeListener(IPropertyChangeListener)} methods. When
 * a property changes, registered listeners&mdash;instances of
 * {@link IPropertyChangeListener}&mdash; are sent the
 * {@link IPropertyChangeListener#propertyChange(PropertyChangeEvent)} message
 * loaded with an instance of {@link PropertyChangeEvent} that describes change.
 * If that property is a collection, and instance of
 * {@link CollectionPropertyChangeEvent} which contains specific information
 * about what has changed in the collection is provided instead.
 * 
 * <p>
 * Instances also leverage the OSGi/Equinox {@link EventAdmin} service to
 * deliver property change events via the event queuing service. Objects
 * interested in changes to instances can register their own
 * {@link EventHandler} OSGi service to be notified of change. For example:
 * 
 * <pre>
 * void startExpenseReportChangedHandlerService(BundleContext context) {
 * 	EventHandler handler = new EventHandler() {
 * 		public void handleEvent(Event event) {
 * 			if (event.getProperty(ObjectWithProperties.SOURCE) != expenseReport)
 * 				return;
 * 			final String property = (String) event
 * 					.getProperty(ObjectWithProperties.PROPERTY_NAME);
 * 			if (ExpenseReport.TITLE_PROPERTY.equals(property)) {
 * 				asyncExec(new Runnable() {
 * 					public void run() {
 * 						updateTitleField();
 * 					}
 * 				});
 * 			}
 * 		}
 * 	};
 * 	Properties properties = new Properties();
 * 	properties.put(EventConstants.EVENT_TOPIC,
 * 			ObjectWithProperties.PROPERTY_CHANGE_TOPIC);
 * 	properties.put(EventConstants.EVENT_FILTER, &quot;(&quot;
 * 			+ ExpenseReport.class.getName() + &quot;=true)&quot;);
 * 
 * 	expenseReportChangedEventHandlerService = context.registerService(
 * 			EventHandler.class.getName(), handler, properties);
 * }
 * </pre>
 * 
 * To make inheritance work for event filters, {@link Event} instances as part
 * of this notification are loaded with a property named for each class in the
 * inheritance chain for the receiver. The above example demonstrates how this
 * works by applying a filter that will include instances of
 * {@link ExpenseReport} or any of its subclasses.
 * <p>
 * By default, all events are delivered asynchronously (though subclasses can
 * override this behaviour).
 */
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
		if (oldValue == null && newValue == null)
			return;
		if (oldValue != null && oldValue.equals(newValue))
			return;

		postEvent(propertyName, oldValue, newValue);

		if (listenerList == null)
			return;
		if (listenerList.isEmpty())
			return;

		PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName,
				oldValue, newValue);
		Object[] listeners = listenerList.getListeners();
		for (int index = 0; index < listeners.length; index++) {
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
	protected void fireCollectionEvent(String propertyName,
			Collection collection, String eventType, Object object) {
		postCollectionEvent(propertyName, collection, eventType, object);

		if (listenerList == null)
			return;
		if (listenerList.isEmpty())
			return;

		Object[] added = eventType == OBJECT_ADDED ? new Object[] { object }
				: new Object[0];
		Object[] removed = eventType == OBJECT_REMOVED ? new Object[] { object }
				: new Object[0];

		PropertyChangeEvent event = new CollectionPropertyChangeEvent(this,
				propertyName, collection, added, removed);
		Object[] listeners = listenerList.getListeners();
		for (int index = 0; index < listeners.length; index++) {
			IPropertyChangeListener listener = (IPropertyChangeListener) listeners[index];
			listener.propertyChange(event);
		}
	}

	/**
	 * This method posts a change event onto the {@link EventAdmin} queue if one
	 * is available. Note that the event's properties include {@value
	 * #NEW_VALUE} and {@value #OLD_VALUE} properties only if the corresponding
	 * values are non-<code>null</code>. That is, if the entries for these keys
	 * are absent, then it is safe to assume that the value is <code>null</code>
	 * .
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
		if (eventAdmin == null)
			return;

		Properties properties = new Properties();
		properties.put(SOURCE, this);
		properties.put(SOURCE_TYPE, this.getClass().getName());
		populateWithTypeProperties(properties, this.getClass());
		properties.put(PROPERTY_NAME, propertyName);
		if (oldValue != null)
			properties.put(OLD_VALUE, oldValue);
		if (newValue != null)
			properties.put(NEW_VALUE, newValue);

		postEvent(eventAdmin, new Event(PROPERTY_CHANGE_TOPIC, properties));
	}

	/**
	 * This method populates the properties for an event with the names of the
	 * given provided class and it's inheritance chain. This is done to 'fake
	 * out' the OSGi Services filtering mechanism into supporting a notion of
	 * inheritance. The filter can ask if the property with the name of a class
	 * is set to 'true'. This way the filter will match an event if a subclass
	 * is the source type.
	 * 
	 * @param properties
	 * @param type
	 */
	protected void populateWithTypeProperties(Properties properties, Class type) {
		if (type == null)
			return;
		properties.put(type.getName(), "true");
		populateWithTypeProperties(properties, type.getSuperclass());
		// TODO Interfaces?
	}

	protected void postEvent(EventAdmin eventAdmin, Event event) {
		eventAdmin.postEvent(event);
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
	void postCollectionEvent(String propertyName, Collection collection,
			String eventType, Object object) {
		EventAdmin eventAdmin = getEventAdmin();
		if (eventAdmin == null)
			return;

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

	public synchronized void addPropertyChangeListener(
			IPropertyChangeListener listener) {
		if (listenerList == null)
			listenerList = new ListenerList();
		listenerList.add(listener);
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		if (listenerList == null)
			return;
		listenerList.remove(listener);
	}
}
