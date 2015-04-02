package com.teakdata.rpi.doorsensor.gpio;

import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * An event trigger for GPIO pins. 
 * @author smarcu
 *
 */
public interface GpioTrigger {
	/**
	 * Add a listener for gpio events
	 * @param listener
	 */
	void addListener(GpioPinListenerDigital listener);
}
