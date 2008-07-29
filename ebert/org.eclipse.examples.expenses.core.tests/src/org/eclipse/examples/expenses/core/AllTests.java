package org.eclipse.examples.expenses.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses( { 
	BinderTests.class,
	ExpenseReportTests.class,
	LineItemTests.class,
	ExpenseTypeTests.class
})

public class AllTests {

}
