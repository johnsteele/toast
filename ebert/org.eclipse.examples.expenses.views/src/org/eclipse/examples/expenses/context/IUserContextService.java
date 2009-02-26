/*******************************************************************************
 * Copyright (c) 2009 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.examples.expenses.context;

/**
 * Instances of this class are responsible for providing user state. This
 * application can potentially service multiple user simultaneously (in the RAP
 * case, for example), so storing state using traditional singletons simply
 * doesn't work.
 * <p>
 * Implementors of this class are responsible for providing an instance
 * of {@link IUserContext} for the user operating in the calling thread.
 * <p>
 * Implementors of this interface should register themselves as Equinox/OSGi
 * services. In general, it makes sense for there to be exactly one registered
 * service implementing this interface. However, clients should be prepared
 * to handle the case where an instance is not registered (or hasn't been
 * registered yet), or multiple instances are registered.
 */
public interface IUserContextService {
	
	/**
	 * This method answers the instance of {@link IUserContext} for
	 * the user operating in the current thread. To ensure that this
	 * method answers the right context object, it should be called
	 * from the UI thread.
	 * 
	 * @return an implementor of {@link IUserContext} containing
	 * the state for the current user.
	 */
	IUserContext getUserContext();
}
