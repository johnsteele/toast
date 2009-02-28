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

import java.text.ParseException;
import java.util.Date;

import org.eclipse.examples.expenses.widgets.datefield.common.AbstractDateField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;

/**
 * The SimpleDateField class implements a DateField in the simplest way
 * possible, using an SWT {@link Text} for user input. This implementation works
 * on RCP, RAP, and eRCP.
 * 
 * <p>
 * This class demonstrates how Equinox binds bundles together. With this class'
 * bundle being installed at runtime, the package dependency resolution process
 * makes it available to consumers. If an alternative bundle providing a
 * simliarly named package and class is installed, that package and class will
 * be resolved and bound instead. For an alternative implementation, see the
 * org.eclipse.examples.expenses.widgets.datefield.nebula bundle.
 * 
 * @see IDateFieldFactory
 * @see ListItemView#createDateField
 */
public class DateField extends AbstractDateField {

	Text dateText;
	DateFormat format;
	
	public DateField(final Composite parent) {
		super(parent);
		initializeFormat();
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

	void initializeFormat() {
		ULocale locale = getLocale();
		if (locale == null) locale = ULocale.getDefault();
		format = DateFormat.getDateInstance(Calendar.getInstance(locale), DateFormat.MEDIUM, locale);
	}

	protected void clientSetDate(Date date) {		
		String text = date == null ? "" : format.format(date);
		if (text.equals(dateText.getText())) return;
		dateText.setText(text);
	}

	protected Control getControl() {
		return dateText;
	}
	
	public void setLocale(ULocale locale) {
		super.setLocale(locale);
		initializeFormat();
	}
}
