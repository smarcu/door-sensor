package com.teakdata.rpi.doorsensor;

/**
 * Provide feedback to user using a LED attached to the device.
 */
public interface LedFeedback {

	/**
	 * Turn the LED on/off
	 * @param on true to turn the led on and false to turn off
	 */
	void set(boolean on);
	
}
