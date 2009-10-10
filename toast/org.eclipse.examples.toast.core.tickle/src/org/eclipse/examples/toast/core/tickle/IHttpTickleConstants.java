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
package org.eclipse.examples.toast.core.tickle;

public interface IHttpTickleConstants {
	public static final String ACTION_PARAMETER = "action"; //$NON-NLS-1$
	public static final String TICKLE_ACTION = "tickle"; //$NON-NLS-1$
	public static final String CONTENT_TYPE_PLAIN = "text/plain"; //$NON-NLS-1$
	public static final String SMS_FUNCTION = "sms"; //$NON-NLS-1$
	public static final char TICKLE_ACK_REPLY = '1';

	public static final String CLIENT_SMS_URL_DEFAULT = "http://localhost:8081/sms"; //$NON-NLS-1$
	public static final String CLIENT_SMS_URL_PROPERTY = "toast.client.sms.url"; //$NON-NLS-1$

}