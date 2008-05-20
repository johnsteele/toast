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
package org.eclipse.examples.expenses.ui.fields.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

/**
 * The SimpleDateField class implements a DateField in the simplest
 * way possible, using an SWT {@link Text} for user input. This
 * implementation works on RCP, RAP, and eRCP. 
 * 
 * @see IDateFieldFactory
 * @see ListItemView#createDateField
 */
public class SimpleDateField extends DateField {

	Text dateText;
	static DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM);
	
	public SimpleDateField(final Composite parent) {
		super(parent);
		dateText = new Text(parent, SWT.BORDER);
		dateText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				try {
					if (dateText.getText().trim().length() > 0)
						setDateAndNotify(format.parse(dateText.getText().trim()));
					dateText.setBackground(null);
				} catch (ParseException e) {
					dateText.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
				}
			}			
		});
	}

	protected void clientSetDate(Date date) {		
		String text = date == null ? "" : format.format(date);
		if (text.equals(dateText.getText())) return;
		dateText.setText(text);
	}

	protected Control getControl() {
		return dateText;
	}

}
