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
package org.eclipse.examples.expenses.context.standalone;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.eclipse.examples.expenses.context.IUserContext;
import org.eclipse.examples.expenses.core.ExpensesBinder;
import org.eclipse.examples.expenses.views.model.ViewModel;
 
import com.ibm.icu.util.ULocale;

public class StandaloneUserContext implements IUserContext {

	private static final String DIRECTORY_NAME = ".ebert";
	private static final String FILE_NAME = "local-user";
	
	private ViewModel viewModel;
	private ExpensesBinder binder;

	public StandaloneUserContext() {
		viewModel = new ViewModel();
		binder = loadExpensesBinder();
		viewModel.setBinder(binder);
	}

	public void stop() {
		saveExpensesBinder(binder);
		viewModel.setBinder(null);
		viewModel = null;
		binder = null;
	}
	
	public ViewModel getViewModel() {
		return viewModel;
	}

	public ULocale getUserLocale() {
		return ULocale.getDefault();
	}
	
	/**
	 * Temporary method to provide a convenient mechanism for
	 * loading existing binders.
	 */
	public ExpensesBinder loadExpensesBinder() {
		ExpensesBinder binder = loadExpensesBinder(getBinderFile());
		if (binder == null) return new ExpensesBinder();
		return binder;
	}
	
	public void saveExpensesBinder(ExpensesBinder binder) {
		saveExpensesBinder(getBinderFile(), binder);
	}

	File getBinderFile() {
		return new File(getStateLocation(), FILE_NAME);
	}
	
	File getStateLocation() {
		return new File(getUserHomeDirectory(), DIRECTORY_NAME);
	}
	
	File getUserHomeDirectory() {
		return new File(System.getProperty("user.home"));
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
