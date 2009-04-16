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
package org.eclipse.examples.expenses.identity.simple;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LoginDialog {
	protected String userId;
	private Text userIdText;
	private final Display display;

	public LoginDialog(Display display) {
		this.display = display;
	}

	public String login() {
		final Shell shell = createDialog(display);		
		shell.setBounds(25, 25, 400, 200);
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) 
				display.sleep();
		}
		
		return userId;
	}

	private Shell createDialog(Display display) {
		final Shell shell = new Shell(display, SWT.DIALOG_TRIM - SWT.CLOSE);
		shell.setText("Login");
		shell.setLayout(new GridLayout(2, false));
		Label userIdLabel = new Label(shell, SWT.NONE);
		userIdLabel.setText("User id:");
		
		userIdText = new Text(shell, SWT.BORDER);
		userIdText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label filler = new Label(shell, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.horizontalSpan = 2;
		filler.setLayoutData(data);
		
		Composite buttons = new Composite(shell, SWT.NONE);
		GridData buttonsData = new GridData(SWT.RIGHT, SWT.FILL, true, false);
		buttonsData.horizontalSpan = 2;
		buttons.setLayoutData(buttonsData);
		
		buttons.setLayout(new RowLayout());
		
		final Button okayButton = new Button(buttons, SWT.PUSH);
		
		okayButton.setText("Login");
		okayButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {				
			}
			public void widgetSelected(SelectionEvent e) {
				userId = userIdText.getText().trim();
				shell.dispose();
			}
		});
		
		okayButton.setEnabled(false);
		userIdText.addVerifyListener(new VerifyListener() {		
			public void verifyText(VerifyEvent event) {
				okayButton.setEnabled(!userIdText.getText().trim().isEmpty());
			}
		});
		
		Button cancelButton = new Button(buttons, SWT.PUSH);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {				
			}
			public void widgetSelected(SelectionEvent e) {
				userId = null;
				shell.dispose();
			}
		});
		return shell;
	}

	public String getUserId() {
		return userId;
	}
}
