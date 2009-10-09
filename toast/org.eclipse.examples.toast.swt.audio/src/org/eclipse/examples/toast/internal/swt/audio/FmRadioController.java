/*******************************************************************************
 * Copyright (c) 2009 Paul VanderLei, Simon Archer, Jeff McAffer and others. All 
 * rights reserved. This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 and Eclipse Distribution License
 * v1.0 which accompanies this distribution. The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Eclipse Distribution License 
 * is available at http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors: 
 *     Paul VanderLei, Simon Archer, Jeff McAffer - initial API and implementation
 *******************************************************************************/
package org.eclipse.examples.toast.internal.swt.audio;

public class FmRadioController extends AbstractRadioController {
	protected String convertFrequency(int frequency) {
		int whole = frequency / 10;
		int fraction = frequency % 10;
		StringBuffer buffer = new StringBuffer(6);
		buffer.append(whole);
		buffer.append('.');
		buffer.append(fraction);
		return buffer.toString();
	}
}
