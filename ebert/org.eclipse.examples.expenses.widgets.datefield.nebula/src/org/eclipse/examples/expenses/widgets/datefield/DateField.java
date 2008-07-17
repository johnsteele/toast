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
package org.eclipse.examples.expenses.widgets.datefield;

import java.util.Date;

import org.eclipse.examples.expenses.widgets.datefield.common.AbstractDateField;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DateField extends AbstractDateField {
	private CDateTime dateTime;
	
	public DateField(Composite parent) {
		super(parent);
		dateTime = new CDateTime(parent, SWT.BORDER | CDT.DROP_DOWN);
		dateTime.setFormat(CDT.DATE_MEDIUM);
		dateTime.addSelectionListener(new SelectionListener() {
			/*
			 * Selection by changing the date in the text editor
			 * results in a "selection".
			 */
			public void widgetSelected(SelectionEvent e) {
				setDateAndNotify(dateTime.getSelection());
			}

			/*
			 * Selection by clicking on a date in the popup calendar
			 * results in a "defaultSelection".
			 */
			public void widgetDefaultSelected(SelectionEvent e) {
				setDateAndNotify(dateTime.getSelection());
			}			
		});
	}

	protected void clientSetDate(Date date) {
		dateTime.setSelection(date);
	}

	@Override
	protected Control getControl() {
		return dateTime;
	}
}
