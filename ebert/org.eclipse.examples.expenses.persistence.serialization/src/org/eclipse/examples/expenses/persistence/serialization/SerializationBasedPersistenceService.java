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
import org.eclipse.examples.expenses.core.CollectionPropertyChangeEvent;
import org.eclipse.examples.expenses.core.ExpenseReport;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.examples.expenses.core.ObjectWithProperties;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

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
public class SerializationBasedPersistenceService implements IPersistenceService {

	/**
	 * Name of the subdirectory of the user's home directory where the state is
	 * kept.
	 */
	private static final String DIRECTORY_NAME = ".ebert";

	public ExpensesBinder loadBinder(String userId) {
		ExpensesBinder binder = loadExpensesBinder(getBinderFile(userId));
		if (binder == null) binder = new ExpensesBinder();
		addListeners(userId, binder);
		return binder;
	}

	protected void addListeners(final String userId, final ExpensesBinder binder) {
		final IPropertyChangeListener[] listeners = new IPropertyChangeListener[1];
		IPropertyChangeListener listener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				saveBinder(userId, binder);
				if (event instanceof CollectionPropertyChangeEvent) {
					addAndRemoveListeners((CollectionPropertyChangeEvent)event, listeners[0]);
				}
			}			
		};
		listeners[0] = listener;
		addListenerToBinder(binder, listener);
	}

	private void addListenerToBinder(ExpensesBinder binder, IPropertyChangeListener listener) {
		binder.addPropertyChangeListener(listener);
		ExpenseReport[] expenseReports = binder.getReports();
		for(int index=0;index<expenseReports.length;index++) {
			addListenerToExpenseReport(listener, expenseReports[index]);
		}
	}

	private void addListenerToExpenseReport(IPropertyChangeListener listener, ExpenseReport expenseReport) {
		expenseReport.addPropertyChangeListener(listener);
		LineItem[] lineItems = expenseReport.getLineItems();
		for(int index=0;index<lineItems.length;index++) {
			addListenerToLineItem(listener, lineItems[index]);
		}
	}

	private void addListenerToLineItem(IPropertyChangeListener listener, LineItem lineItem) {
		lineItem.addPropertyChangeListener(listener);
	}

	protected void addAndRemoveListeners(CollectionPropertyChangeEvent event, IPropertyChangeListener listener) {
		for (int index=0;index<event.added.length;index++) {
			((ObjectWithProperties)event.added[index]).addPropertyChangeListener(listener);
		}
		
		for (int index=0;index<event.removed.length;index++) {
			((ObjectWithProperties)event.removed[index]).removePropertyChangeListener(listener);
		}
		
	}

	public void saveBinder(String userId, ExpensesBinder binder) {
		saveExpensesBinder(getBinderFile(userId), binder);
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
