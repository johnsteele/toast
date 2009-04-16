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

import org.eclipse.examples.expenses.core.ExpensesBinder;

/**
 * Implementors of this interface provide a persistence service. The
 * persistence service is registered as an Equinox Service; implementors
 * should expect to be shared by multiple users.
 * <p>
 * Implementors are called upon to provide state management for
 * users. More specifically, at the beginning of a user session, the
 * persistence service is asked to provide an instance of {@link ExpensesBinder}
 * that is appropriate for the user; at the end of the user session, the
 * service is given an opportunity to save the state.
 * <p>
 * Implementors may opt to save state periodically, perhaps in response to
 * changes in the object state. An implementor might, for example, install
 * listeners on the domain model in order to receive notification of changes.
 * <p>
 * Implementors should  assume that {@link #saveBinder(IUserContext, ExpensesBinder)} 
 * may be called multiple times during a user's session (perhaps in
 * response to a user request to &quot;save&quot;.
 *
 * TODO This needs to moved to where it can be shared.
 */
public interface IPersistenceService {
	ExpensesBinder loadBinder(String userId);
}
