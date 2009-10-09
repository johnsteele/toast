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

import org.eclipse.examples.toast.crust.widgets.ImageButton;
import org.eclipse.examples.toast.dev.radio.IAbstractRadio;
import org.eclipse.examples.toast.dev.radio.IRadioListener;
import org.eclipse.swt.widgets.Label;

public abstract class AbstractRadioController implements IRadioListener {
	private ImageButton[] presetElements;
	private Label frequencyElement;
	private IAbstractRadio device;
	private boolean displayed;

	protected AbstractRadioController() {
		displayed = false;
	}

	// Abstract methods
	protected abstract String convertFrequency(int frequency);

	// IRadioListener implementation
	public void frequencyChanged(int frequency) {
		updateFrequency(frequency);
	}

	public void presetChanged(int presetIndex, int frequency) {
		updatePreset(presetIndex, frequency);
	}

	// API
	public void bindElements(Label frequencyElement, ImageButton[] presetElements) {
		this.frequencyElement = frequencyElement;
		this.presetElements = presetElements;
		device.addListener(this);
	}

	public void bindDevice(IAbstractRadio device) {
		this.device = device;
	}

	public void unbind() {
		frequencyElement = null;
		presetElements = null;
		device.removeListener(this);
		device = null;
	}

	public void setDisplayed(boolean displayed) {
		if (displayed && !this.displayed) {
			this.displayed = true;
			updateAllElements();
		} else {
			this.displayed = displayed;
		}
	}

	public void frequencyDown() {
		device.frequencyDown();
	}

	public void frequencyUp() {
		device.frequencyUp();
	}

	public void seekDown() {
		device.seekDown();
	}

	public void seekUp() {
		device.seekUp();
	}

	public void setPresetToCurrent(int presetIndex) {
		device.setPreset(presetIndex, device.getFrequency());
	}

	public void tuneToPreset(int presetIndex) {
		device.tuneToPreset(presetIndex);
	}

	// Private
	private void updatePreset(int presetIndex, int frequency) {
		if (displayed && (presetElements != null)) {
			ImageButton presetElement = presetElements[presetIndex];
			if (presetElement != null) {
				presetElement.setText(convertFrequency(frequency));
				presetElement.redraw();
			}
		}
	}

	private void updateFrequency(int frequency) {
		if (displayed) {
			if (frequencyElement != null) {
				frequencyElement.setText(convertFrequency(frequency));
				frequencyElement.redraw();
			}
		}
	}

	private void updateAllElements() {
		updateFrequency(device.getFrequency());
		for (int i = 0; i < presetElements.length; i++) {
			updatePreset(i, device.getPreset(i));
		}
	}
}
