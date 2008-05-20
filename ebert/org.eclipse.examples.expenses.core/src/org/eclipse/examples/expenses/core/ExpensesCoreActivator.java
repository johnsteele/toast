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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class ExpensesCoreActivator extends Plugin {
	static ExpensesCoreActivator instance;

	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
	}
	
	File getBinderFile(String fileName) {
		return getStateLocation().append(fileName).toFile();
	}
	
	/**
	 * Temporary method to provide a convenient mechanism for
	 * loading existing binders.
	 */
	public ExpensesBinder loadExpensesBinder(String fileName) {
		return loadExpensesBinder(getBinderFile(fileName));
	}
	
	ExpensesBinder loadExpensesBinder(File source) {
		if (!source.exists()) return null;
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

	public void saveExpensesBinder(String fileName, ExpensesBinder binder) {
		saveExpensesBinder(getBinderFile(fileName), binder);
	}
	
	void saveExpensesBinder(File target, ExpensesBinder binder) {
		ObjectOutputStream out = null;
		try {
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

	public static ExpensesCoreActivator getDefault() {
		return instance;
	}
}
