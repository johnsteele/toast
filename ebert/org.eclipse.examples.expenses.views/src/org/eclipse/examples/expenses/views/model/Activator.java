package org.eclipse.examples.expenses.views.model;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	private static Activator instance;
	private BundleContext context;

	public void start(BundleContext context) throws Exception {
		instance = this;
		this.context = context;		
	}

	public void stop(BundleContext context) throws Exception {
		
	}

	public static Activator getDefault() {
		return instance;
	}

	public BundleContext getContext() {
		return context;
	}
}
