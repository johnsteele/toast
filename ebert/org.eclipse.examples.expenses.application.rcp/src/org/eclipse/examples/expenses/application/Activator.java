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
package org.eclipse.examples.expenses.application;

import org.eclipse.examples.expenses.ui.fields.date.DateField;
import org.eclipse.examples.expenses.ui.fields.date.IDateFieldFactory;
import org.eclipse.examples.expenses.ui.fields.date.nebula.NebulaDateField;
import org.eclipse.examples.expenses.views.LineItemView;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.examples.expenses.application";

	// The shared instance
	private static Activator plugin;

	private ServiceRegistration dateFieldFactoryService;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		createServices(context);
	}

	void createServices(BundleContext context) {
		createDateFieldFactoryService(context);
	}

	/**
	 * This method creates the {@link IDateFieldFactory} service
	 * that is used to override the default {@link DateField} implementation.
	 * 
	 * @see DateField
	 * @see IDateFieldFactory
	 * @see LineItemView#createDateField
	 */
	void createDateFieldFactoryService(BundleContext context) {
		IDateFieldFactory factory = new IDateFieldFactory() {
			@Override
			public DateField createDateField(Composite parent) {
				return new NebulaDateField(parent);
			}			
		};
		dateFieldFactoryService = context.registerService(IDateFieldFactory.class.getName(), factory, null);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		dateFieldFactoryService.unregister();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
