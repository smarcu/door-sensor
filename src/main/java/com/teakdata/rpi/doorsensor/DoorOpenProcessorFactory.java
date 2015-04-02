package com.teakdata.rpi.doorsensor;

import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * Factory for door open listeners
 * TODO create generic interface instead of {@link GpioPinListenerDigital}
 * @author smarcu
 */
public interface DoorOpenProcessorFactory {

	GpioPinListenerDigital createListener();
	
}
