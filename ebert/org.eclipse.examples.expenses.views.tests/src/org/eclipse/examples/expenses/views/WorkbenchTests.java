package org.eclipse.examples.expenses.views;

import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.examples.expenses.core.ObjectWithProperties;
import org.eclipse.examples.expenses.ui.ExpenseReportingUI;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public abstract class WorkbenchTests {

	private static BlockingQueue<Event> eventQueue;
	private static ServiceRegistration registration;

	@BeforeClass
	public static void setupPropertyChangeEventHandler() throws Exception {
		ensureEventAdminServiceIsRunning();
		eventQueue = new ArrayBlockingQueue<Event>(100);
		
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
	
	private static void ensureEventAdminServiceIsRunning() throws BundleException {
		Bundle[] bundles = ExpenseReportingUI.getDefault().getContext().getBundles();
		for(Bundle bundle : bundles) {
			if ("org.eclipse.equinox.event".equals(bundle.getSymbolicName())) {
				if (bundle.getState() == Bundle.ACTIVE) return;
				if (bundle.getState() == Bundle.STARTING) return;
				bundle.start();
				return;
			}
		}
	}

	@AfterClass
	public static void tearDownPropertyChangeEventHandler() {
		registration.unregister();
	}
	
	/**
	 * This method makes sure that the UI has had an opportunity to process any
	 * outstanding events. This includes any asynchronous blocks introduced to
	 * the display via calls to {@link Display#asyncExec(Runnable)} (see
	 * {@link BinderView#setBinder(ExpensesBinder)} as an example). The
	 * following line forces those queued up asynchronous tasks to run.
	 */
	protected void processEvents() {
		while (getWorkbench().getDisplay().readAndDispatch());
	}

	protected IWorkbenchPage getActivePage() {
		return getWorkbenchWindow().getActivePage();
	}

	protected IWorkbenchWindow getWorkbenchWindow() {
		return getWorkbench().getActiveWorkbenchWindow();
	}

	protected IWorkbench getWorkbench() {
		return PlatformUI.getWorkbench();
	}

	protected void waitForAPropertyChangeEvent(ExpenseReport source, String property) throws Exception {
		while (true) {
			Event event = eventQueue.take();
			if (!event.getTopic().equals(ObjectWithProperties.PROPERTY_CHANGE_TOPIC)) continue;
			if (event.getProperty(ObjectWithProperties.SOURCE) != source) continue;
			if (!property.equals(event.getProperty(ObjectWithProperties.PROPERTY_NAME))) continue;
			break;
		}
	}

	protected static BundleContext getBundleContext() {
		return ExpenseReportingUI.getDefault().getContext();
	}

}