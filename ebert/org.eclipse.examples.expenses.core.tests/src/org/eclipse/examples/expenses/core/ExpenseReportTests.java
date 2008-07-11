package org.eclipse.examples.expenses.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import junit.framework.TestCase;

/**
 * This class provides a handful of tests for the {@link ExpenseReport} class.
 * 
 * <p>Note that this class should be run as a &quot;JUnit Plug-in Test&quot; and
 * that it depends on the presence of an OSGi event service. It does attempt to
 * start the &quot;org.eclipse.equinox.event&quot; bundle, but will only do
 * so if the bundle has already been installed.
 */
public class ExpenseReportTests extends TestCase {

	ExpenseReport expenseReport;
	List events = new ArrayList();
	List queuedEvents = new ArrayList();
	private ServiceRegistration registration;

	/**
	 * Setup fixtures for the tests. Here, we create an instance of the
	 * ExpenseReport class, and attach an {@link IPropertyChangeListener} to it.
	 * The property change listener will simply add any received
	 * {@link PropertyChangeEvent}s to the {@link #events} List. Additionally,
	 * we create an {@link EventHandler} service to listen for events delivered
	 * through the OSGi Event service.
	 */
	public void setUp() throws Exception {
		expenseReport = new ExpenseReport("Expenses");
		
		/*
		 * The property change listener simply keeps track of the fact
		 * that the event has occurred.
		 */
		expenseReport.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				events.add(event);
			}			
		});
		
		registerEquinoxEventServiceHandler();
	}

	/**
	 * The Event handler simply records the fact that the event has been
	 * delivered and notifies anybody who happens to be waiting for an event.
	 * Since our implementation posts an event, processing of the event
	 * potentially occurs in a different thread.
	 */
	void registerEquinoxEventServiceHandler() throws BundleException {	
		ensureEventAdminServiceIsRunning();
		
		/*
		 * The EventHandler is notified whenever a matching event is posted.
		 */
		EventHandler service = new EventHandler() {
			public void handleEvent(Event event) {
				queuedEvents.add(event);
				synchronized (queuedEvents) {
					queuedEvents.notify();
				}
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
	private void ensureEventAdminServiceIsRunning() throws BundleException {
	
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
	private BundleContext getBundleContext() {
		return ExpensesCoreActivator.getDefault().getBundle().getBundleContext();
	}
	
	public void shutdown() {
		registration.unregister();
	}
	
	public void testSetTitle() throws Exception {
		expenseReport.setTitle("Wayne's Expenses");
		PropertyChangeEvent propertyChangeEvent = (PropertyChangeEvent)events.get(0);
		assertSame(expenseReport, propertyChangeEvent.getSource());
		assertEquals("Expenses", propertyChangeEvent.getOldValue());
		assertEquals("Wayne's Expenses", propertyChangeEvent.getNewValue());
		
		synchronized (queuedEvents) {
			queuedEvents.wait();
		}
		
		Event event = (Event) queuedEvents.get(0);
		assertEquals(expenseReport, event.getProperty(ObjectWithProperties.SOURCE));
	}

	public void testAddLineItem() {
		fail("Not yet implemented");
	}

	public void testRemoveLineItem() {
		fail("Not yet implemented");
	}

}
