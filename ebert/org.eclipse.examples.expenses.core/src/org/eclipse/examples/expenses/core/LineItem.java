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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.ibm.icu.util.Currency;
import com.ibm.icu.util.CurrencyAmount;

public class LineItem extends ObjectWithProperties implements Serializable {
	private static final long serialVersionUID = -1018224652196877185L;

	/**
	 * The DATE_PROPERTY constant holds the name of the property used to
	 * notify observers that a change has occurred in the &quot;date&quot; property.
	 */
	public static final String DATE_PROPERTY = "date";	
	
	/**
	 * The type_PROPERTY constant holds the name of the property used to
	 * notify observers that a change has occurred in the &quot;type&quot; property.
	 */
	public static final String TYPE_PROPERTY = "type";
	
	/**
	 * The AMOUNT_PROPERTY constant holds the name of the property used to
	 * notify observers that a change has occurred in the &quot;amount&quot; property.
	 */
	public static final String AMOUNT_PROPERTY = "amount";
	
	/**
	 * The EXCHANGE_RATE_PROPERTY constant holds the name of the property used to
	 * notify observers that a change has occurred in the &quot;exchangeRate&quot; property.
	 */
	public static final String EXCHANGE_RATE_PROPERTY = "exchangeRate";
	
	/**
	 * The COMMENT_PROPERTY constant holds the name of the property used to
	 * notify observers that a change has occurred in the &quot;comment&quot; property.
	 */
	public static final String COMMENT_PROPERTY = "comment";
	
	/**
	 * The PROPERTIES constant holds an array containing the names of all LineItem properties.
	 */
	public static final String[] PROPERTIES = new String[] {
		DATE_PROPERTY, TYPE_PROPERTY, AMOUNT_PROPERTY, EXCHANGE_RATE_PROPERTY, COMMENT_PROPERTY};
		
	/**
	 * The date that this expense was incurred. The default value, which is created by the
	 * constructor is the current date.
	 * 
	 * @see getDate
	 * @see setDate
	 */
	Date date;
	
	/**
	 * The type of the expense. This value should generally be an instance of {@link ExpenseType}
	 * taken from the {@link ExpensesBinder#getTypes()}. The default value is <code>null</code>.
	 * 
	 * @see getType
	 * @see setType
	 */
	ExpenseType type;
	
	/**
	 * The amount of the expense. We use the {@link CurrencyAmount} type from ICU4J.
	 * The default value is CAD$0.
	 * 
	 * @see getAmount
	 * @see setAmount
	 * 
	 * TODO This value should be specified in the binder's default currency (Bug 239512).
	 */
	CurrencyAmount amount = new CurrencyAmount(new Integer(0), Currency.getInstance(Locale.CANADA));
	
	/**
	 * The comment is arbitrary text describing the receiver. The default value is <code>null</code>.
	 * 
	 * @see getComment
	 * @see setComment
	 */
	String comment;
	
	/**
	 * The exchangeRate is used to override any exchange rate value that's
	 * automatically determined. In computing the value of the expense report,
	 * for example, an external exchange rate web service might report a rate
	 * that's different from the one actually incurred. This gives us an
	 * opportunity to override that value. A value of 0.0 (the default)
	 * indicates that the default value should be used.
	 * 
	 * @see getExchangeRate
	 * @see setExchangeRate
	 */
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

	public void setAmount(CurrencyAmount amount) {
		CurrencyAmount oldValue = this.amount;
		this.amount = amount;
		firePropertyChanged(AMOUNT_PROPERTY, oldValue, amount);
	}

	public CurrencyAmount getAmount() {
		return amount;
	}

	public void setExchangeRate(double exchangeRate) {

		double oldValue = this.exchangeRate;
		this.exchangeRate = exchangeRate;
		firePropertyChanged(EXCHANGE_RATE_PROPERTY, new Double(oldValue), new Double(exchangeRate));
	} 

	public double getExchangeRate() {
		return exchangeRate;
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
		amount = (CurrencyAmount) stream.readObject();
		exchangeRate = stream.readDouble();
		comment = (String) stream.readObject();
	}
}
