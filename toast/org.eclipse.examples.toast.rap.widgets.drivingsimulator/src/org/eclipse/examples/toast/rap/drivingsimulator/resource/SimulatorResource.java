/*******************************************************************************
 * Copyright (c) 2009 EclipseSource Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the 
 * terms of the Eclipse Public License v1.0 and Eclipse Distribution License v1.0 
 * which accompanies this distribution. The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Eclipse Distribution License 
 * is available at http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors: 
 *     EclipseSource Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.examples.toast.rap.drivingsimulator.resource;

import org.eclipse.rwt.resources.IResource;
import org.eclipse.rwt.resources.IResourceManager.RegisterOptions;

public class SimulatorResource implements IResource {

	public String getCharset() {
		return DrivingSimulatorAPIResource.CHARSET_NAME_ISO_8859_1;
	}

	public ClassLoader getLoader() {
		return this.getClass().getClassLoader();
	}

	public String getLocation() {
		return "org/eclipse/rap/drivingsimulator/js/Simulator.js";
	}

	public RegisterOptions getOptions() {
		return RegisterOptions.VERSION;
	}

	public boolean isExternal() {
		return false;
	}

	public boolean isJSLibrary() {
		return true;
	}
}
