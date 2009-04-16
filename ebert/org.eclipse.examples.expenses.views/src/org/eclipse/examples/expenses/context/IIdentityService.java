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
 * An identity service provides identity information. The
 * {@link #getUserId()} method is the main entry point for clients.
 */
public interface IIdentityService {

	/**
	 * This method answers an identifier for the user
	 * associated with the current thread. Note that since
	 * this method is thread-specific, there is no need
	 * to do any synchronization.
	 * 
	 * @return a non-empty {@link String}.
	 */
	String getUserId();

}
