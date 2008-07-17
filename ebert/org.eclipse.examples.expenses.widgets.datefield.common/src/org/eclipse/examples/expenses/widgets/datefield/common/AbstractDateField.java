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
package org.eclipse.examples.expenses.widgets.datefield.common;

import java.util.Date;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * This is an abstract superclass for user interface components that
 * capture a date. This class provides basic information for adding,
 * removing, and notifying listeners of changes to the date. It also
 * provides some basic lifecycle management.
 *
 * TODO This class should probably subclass {@link Composite}.
 */
public abstract class AbstractDateField {
	Date date;
	ListenerList dateListeners;

	public AbstractDateField(Composite parent) {
	}

	public void setLayoutData(Object layoutData) {
		getControl().setLayoutData(layoutData);
	}
	
	protected abstract Control getControl();

	public void addDateListener(IDateChangeListener listener) {
		if (dateListeners == null) dateListeners = new ListenerList();
		dateListeners.add(listener);
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		clientSetDate(date);
		setDateAndNotify(date);
	}

	/**
	 * This method sets the contents of the widget based
	 * on the provided date. This method is invoked in response
	 * to a client's request to set the date. i.e. the date
	 * has been set by an external source.
	 * 
	 * @param date
	 */
	protected abstract void clientSetDate(Date date);

	protected void setDateAndNotify(Date date) {
		if (date == null && this.date == null) return;
		if (date != null && date.equals(this.date)) return;
		this.date = date;
		
		if (dateListeners == null) return;
		Object[] listeners = dateListeners.getListeners();
		if (listeners.length == 0) return;
		DateChangeEvent event = new DateChangeEvent(this, date);
		for (int index=0;index<listeners.length;index++) {
			((IDateChangeListener)listeners[index]).dateChange(event);
		}
	}

	public void setEnabled(boolean enabled) {
		getControl().setEnabled(enabled);
	}

	public void setFocus() {
		getControl().setFocus();
	}
}
