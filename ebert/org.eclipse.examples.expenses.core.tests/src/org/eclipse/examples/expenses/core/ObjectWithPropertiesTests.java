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

import java.util.LinkedList;
import java.util.Queue;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.junit.Before;

public abstract class ObjectWithPropertiesTests {

	protected Queue<PropertyChangeEvent> observerQueue = new LinkedList<PropertyChangeEvent>();

	/**
	 * Setup fixtures for the tests. Here, we attach an
	 * {@link IPropertyChangeListener} to the object being tested. The property
	 * change listener will simply add any received {@link PropertyChangeEvent}s
	 * to the {@link #observerQueue} {@link Queue}.
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
}