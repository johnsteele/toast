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

import static org.junit.Assert.*;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.junit.Before;
import org.junit.Test;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;

public class DateFieldTest {

	private DateField field;
	private DateFormat format;

	@Before
	public void setUp() {
		ULocale canada = ULocale.CANADA;
		
		Shell shell = new Shell();
		field = new DateField(shell);
		field.setLocale(canada);		

		format = DateFormat.getDateInstance(Calendar.getInstance(canada), DateFormat.MEDIUM, canada);
	}

	@Test
	public void testSetDate() {
		Date date = new Date();
		field.setDate(date);
		assertEquals(format.format(date), field.dateText.getText());
	}
	
	@Test
	public void testDateFormatChangesWhenLocaleChanged() {
		DateFormat oldFormat = field.format;
		field.setLocale(ULocale.GERMANY);
		assertNotSame(oldFormat, field.format);
		assertEquals(ULocale.GERMANY, field.format.getLocale(ULocale.ACTUAL_LOCALE));
	}
	
	@Test
	public void testDefaulLocaleUsedWhenNoLocaleSpecified() {
		field.setLocale(null);
		
		assertEquals(ULocale.getDefault(), field.format.getLocale(ULocale.ACTUAL_LOCALE));
	}

	@Test
	public void testNoErrorIndicatedWithEmptyInput() {
		field.dateText.setText(""); // Doesn't change anything.
		assertEquals(field.dateText.getDisplay().getSystemColor(SWT.COLOR_WHITE), field.dateText.getBackground());		
	}

	@Test
	public void testNoErrorIndicatedWithValidInput() {
		field.dateText.setText(format.format(new Date()));
		assertEquals(field.dateText.getDisplay().getSystemColor(SWT.COLOR_WHITE), field.dateText.getBackground());		
	}

	@Test
	public void testErrorIndicatedWithPartiallyValidInput() {
		field.dateText.setText(format.format(new Date()).substring(0, 4));
		assertEquals(field.dateText.getDisplay().getSystemColor(SWT.COLOR_RED), field.dateText.getBackground());		
	}
	
	@Test
	public void testErrorIndicatedWithInvalidInput() {
		assertEquals(field.dateText.getDisplay().getSystemColor(SWT.COLOR_WHITE), field.dateText.getBackground());		
		field.dateText.setText("blah");
		assertEquals(field.dateText.getDisplay().getSystemColor(SWT.COLOR_RED), field.dateText.getBackground());
	}
}
