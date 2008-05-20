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
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

/**
 * 
 * <p>Money is not comparable (i.e. it doesn't implement {@link Comparable}
 * on purpose. Comparing two moneys, especially when different currencies
 * are involved, is relatively hard and is out of scope for the problems 
 * being solved by this class.
 */
public class Money implements Serializable {	
	public static final Money ZERO = new Money(0.0, Currency.getInstance(Locale.CANADA));
	
	/**
	 * Using a double to represent money is just plain stupid.
	 * However, in the spirit of making this work, we'll allow
	 * it for now.
	 * TODO Stop using double to represent the amount.
	 */
	private final double amount;
	private final Currency currency;

	public Money(double amount, Currency currency) {
		this.amount = amount;
		this.currency = currency;
	}

	public Currency getCurrency() {
		return currency;
	}

	public double getAmount() {
		return amount;
	}
	
	public String toString() {
		return currency.getSymbol() + amount;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(amount);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((currency == null) ? 0 : currency.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Money other = (Money) obj;
		if (Double.doubleToLongBits(amount) != Double
				.doubleToLongBits(other.amount))
			return false;
		if (currency == null) {
			if (other.currency != null)
				return false;
		} else if (!currency.equals(other.currency))
			return false;
		return true;
	}
}
