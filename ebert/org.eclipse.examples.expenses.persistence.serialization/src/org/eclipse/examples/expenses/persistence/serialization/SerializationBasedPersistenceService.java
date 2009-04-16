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
package org.eclipse.examples.expenses.persistence.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.examples.expenses.context.IPersistenceService;
import org.eclipse.examples.expenses.context.IUserContext;
import org.eclipse.examples.expenses.core.ExpensesBinder;

/**
 * This class implements are pretty unsophisticated persistence service based on
 * standard Java serialization. Instances store data in the user's home
 * directory, in a directory named {@value #DIRECTORY_NAME}.
 * <p>
 * An instance of this class gets registered as an OSGi/Equinox service by the
 * bundle {@link Activator}. Zero or one {@link IPersistenceService} services
 * should be registered at any time as consumers expect there to be one
 * implementor at most.
 */
public class SerializationBasedPersistenceService implements
		IPersistenceService {

	/**
	 * Name of the subdirectory of the user's home directory where the state is
	 * kept.
	 */
	private static final String DIRECTORY_NAME = ".ebert";

	public ExpensesBinder loadBinder(String userId) {
		ExpensesBinder binder = loadExpensesBinder(getBinderFile(userId));
		if (binder == null) binder = new ExpensesBinder();  
		return binder;
	}

	public void saveBinder(IUserContext userContext, ExpensesBinder binder) {
		saveExpensesBinder(getBinderFile(userContext.getUserId()), binder);
	}

	File getBinderFile(String id) {
		return new File(getStateLocation(), id);
	}

	File getStateLocation() {
		return new File(getUserHomeDirectory(), DIRECTORY_NAME);
	}

	File getUserHomeDirectory() {
		return new File(System.getProperty("user.home"));
	}

	ExpensesBinder loadExpensesBinder(File source) {
		if (!source.exists())
			return null;
		ExpensesBinder binder = null;
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(new FileInputStream(source));
			binder = (ExpensesBinder) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return binder;
	}

	void saveExpensesBinder(File target, ExpensesBinder binder) {
		ObjectOutputStream out = null;
		try {
			target.getParentFile().mkdirs();
			out = new ObjectOutputStream(new FileOutputStream(target));
			out.writeObject(binder);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
