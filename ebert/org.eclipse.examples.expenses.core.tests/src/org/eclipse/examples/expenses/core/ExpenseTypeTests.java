package org.eclipse.examples.expenses.core;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

/**
 * This class provides a handful of tests for the {@link ExpenseType} class.
 * 
 * Note also that this test uses some Java 5 syntax and libraries. The host
 * bundle requires only Java 1.4.
 */
public class ExpenseTypeTests {

	@Test
	public void testCompare() throws Exception {
		ExpenseType type1 = new ExpenseType("Air fare", 1);
		ExpenseType type2 = new ExpenseType("Other Travel", 2);
		
		assertEquals(1, type2.compareTo(type1));
		assertEquals(-1, type1.compareTo(type2));
		assertEquals(-1, type1.compareTo(new Date()));
		assertEquals(-1, type1.compareTo(null));
		assertEquals(0, type1.compareTo(type1));
	}
}
