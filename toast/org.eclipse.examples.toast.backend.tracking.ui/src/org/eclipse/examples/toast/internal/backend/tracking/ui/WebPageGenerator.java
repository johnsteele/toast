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
package org.eclipse.examples.toast.internal.backend.tracking.ui;

import java.io.IOException;
import org.eclipse.examples.toast.core.ICoreConstants;
import org.eclipse.examples.toast.core.PropertyManager;
import org.eclipse.examples.toast.core.UrlBuilder;

public class WebPageGenerator {
	public static final String TITLE = "Portal for: "; //$NON-NLS-1$
	public static String servletAlias = PropertyManager.getProperty(ICoreConstants.BACK_END_URL_PROPERTY, ICoreConstants.BACK_END_URL_DEFAULT);

	public WebPageGenerator() {
		super();
	}

	public static void writeHeader(StringBuffer buffer, String title) {
		buffer.append("<html>\n");
		buffer.append("\t<head>\n");
		buffer.append("\t\t<title>" + title + "</title>\n");
		buffer.append("\t</head>\n");
		buffer.append("\t<body>\n");
		buffer.append("\t\t<h2>" + title + "</h2>\n");
	}

	public static void writeFooter(StringBuffer buffer) {
		buffer.append("\t</body>\n");
		buffer.append("</html>\n");
	}

	public static String createLink(String label, String action, String parameter, String value) throws IOException {
		String[] parameters = {parameter};
		String[] values = {value};
		return createLink(label, action, parameters, values);
	}

	public static String createLink(String label, String action, String[] parameters, String[] values) throws IOException {
		UrlBuilder urlBuilder = new UrlBuilder(servletAlias);
		if (action != null)
			urlBuilder.addParameter(IPortalConstants.ACTION_PARAMETER, action);
		if (parameters != null)
			for (int i = 0; i < parameters.length; i++)
				urlBuilder.addParameter(parameters[i], values[i]);
		StringBuffer buffer = new StringBuffer(100);
		buffer.append("<a href=\"" + urlBuilder + "\">" + label + "</a>");
		return buffer.toString();
	}
}
