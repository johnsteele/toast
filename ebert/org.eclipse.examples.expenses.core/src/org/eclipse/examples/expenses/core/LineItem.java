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

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class LineItem extends ObjectWithProperties implements Serializable {
	public static final String DATE_PROPERTY = "date";
	public static final String TYPE_PROPERTY = "type";
	public static final String AMOUNT_PROPERTY = "amount";
	private static final String EXCHANGE_RATE_PROPERTY = "exchangeRate";
	public static final String COMMENT_PROPERTY = "comment";
	public static final String[] PROPERTIES = new String[] {
		DATE_PROPERTY, TYPE_PROPERTY, AMOUNT_PROPERTY, EXCHANGE_RATE_PROPERTY, COMMENT_PROPERTY};
		
	Date date;
	ExpenseType type;
	Money amount = Money.ZERO;
	String comment;
	double exchangeRate = 0.0;

	public LineItem() {
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		this.date = calendar.getTime();
	}

	public Date getDate() {
		return date;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		String oldValue = this.comment;
		this.comment = comment;
		firePropertyChanged(COMMENT_PROPERTY, oldValue, comment);
	}

	public void setDate(Date date) {
		Date oldValue = this.date;
		this.date = date;
		firePropertyChanged(DATE_PROPERTY, oldValue, date);
	}

	public void setType(ExpenseType type) {
		ExpenseType oldValue = this.type;
		this.type = type;
		firePropertyChanged(TYPE_PROPERTY, oldValue, type);
	}

	public ExpenseType getType() {
		return type;
	}

	public void setAmount(Money amount) {
		Money oldValue = this.amount;
		this.amount = amount;
		firePropertyChanged(AMOUNT_PROPERTY, oldValue, amount);
	}

	public Money getAmount() {
		return amount;
	}

	public void setExchangeRate(double exchangeRate) {

		double oldValue = this.exchangeRate;
		this.exchangeRate = exchangeRate;
		firePropertyChanged(EXCHANGE_RATE_PROPERTY, new Double(oldValue), new Double(exchangeRate));
	} 
	
	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
		stream.writeObject(date);
		stream.writeInt(type == null ? -1 : type.ordinality);
		stream.writeObject(amount);
		stream.writeDouble(exchangeRate);
		stream.writeObject(comment);
	}

	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
		date = (Date) stream.readObject();
		int ordinality = stream.readInt();
		if (ordinality == -1) {
			type = null;
		} else {
			type = ExpensesBinder.getTypes()[ordinality];
		}
		amount = (Money) stream.readObject();
		exchangeRate = stream.readDouble();
		comment = (String) stream.readObject();
	}

}
