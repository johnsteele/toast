package org.eclipse.examples.expenses.core;

import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.junit.Before;
import org.junit.BeforeClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public abstract class ObjectWithPropertiesTests {

	protected Queue<PropertyChangeEvent> observerQueue = new LinkedList<PropertyChangeEvent>();
	protected BlockingQueue<Event> eventQueue = new ArrayBlockingQueue<Event>(20);
	protected ServiceRegistration registration;

	/**
	 * Setup fixtures for the tests. Here, we attach an {@link IPropertyChangeListener} to 
	 * the object being tested.
	 * The property change listener will simply add any received
	 * {@link PropertyChangeEvent}s to the {@link #observerQueue} {@link Queue}. 
	 */
	@Before
	public void installPropertyChangeListener() throws Exception {		
		/*
		 * The property change listener simply keeps track of the fact
		 * that the event has occurred.
		 */
		getModelObject().addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				observerQueue.add(event);
			}			
		});
	}
	
	public abstract ObjectWithProperties getModelObject();

	/**
	 * Before each test is run, we make sure that a service handler is
	 * registered to listen for the events that result from property changes.
	 * The Event handler simply records the fact that the event has been
	 * delivered and notifies anybody who happens to be waiting for an event.
	 * Since our implementation posts an event, processing of the event
	 * potentially occurs in a different thread.
	 */
	@Before
	public void registerEquinoxEventServiceHandler() throws BundleException {
		
		/*
		 * The EventHandler is notified whenever a matching event is posted.
		 */
		EventHandler service = new EventHandler() {
			public void handleEvent(Event event) {
				eventQueue.add(event);
			}			
		};
		
		/*
		 * We only need on property: the name of the topic that we're listening for.
		 * The topic (along with other properties) are passed in as part of the 
		 * registration process.
		 */
		Properties properties = new Properties();
		properties.put(EventConstants.EVENT_TOPIC, ObjectWithProperties.PROPERTY_CHANGE_TOPIC);
	
		registration = getBundleContext().registerService(EventHandler.class.getName(), service, properties);
	}

	/**
	 * This method ensures that the {@link EventAdmin} service is running (i.e.
	 * it has been started). This method assumes that the
	 * &quot;org.eclipse.equinox.event&quot; bundle has been installed. If the
	 * bundle has not been installed, an {@link IllegalStateException} is
	 * thrown.
	 */
	@BeforeClass
	public static void ensureEventAdminServiceIsRunning() throws BundleException {
	
		Bundle[] bundles = getBundleContext().getBundles();
		for (int index=0;index<bundles.length;index++) {
			if ("org.eclipse.equinox.event".equals(bundles[index].getSymbolicName())) {
				bundles[index].start();
				return;
			}
		}
		throw new IllegalStateException("The org.eclipse.equinox.event bundle must be installed in order to run this test. This bundle is available at http://www.eclipse.org/equinox/bundles");
	}

	/**
	 * This convenience method obtains the bundle context from the activator.
	 * @return The {@link BundleContext}.
	 */
	private static BundleContext getBundleContext() {
		return ExpensesCoreActivator.getDefault().getBundle().getBundleContext();
	}

}