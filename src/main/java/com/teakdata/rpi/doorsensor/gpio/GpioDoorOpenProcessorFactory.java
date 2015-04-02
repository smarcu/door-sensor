package com.teakdata.rpi.doorsensor.gpio;

import java.util.Properties;

import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.teakdata.rpi.doorsensor.Camera;
import com.teakdata.rpi.doorsensor.DoorOpenProcessor;
import com.teakdata.rpi.doorsensor.DoorOpenProcessorFactory;
import com.teakdata.rpi.doorsensor.LedFeedback;
import com.teakdata.rpi.doorsensor.Mail;

/**
 * Factory creating gpio implementation
 * @author smarcu
 *
 */
public class GpioDoorOpenProcessorFactory implements DoorOpenProcessorFactory{

	private Camera cameraService;
	private Mail mailService;
	private LedFeedback ledFeedback;
	private Properties config;
	
	
	public GpioDoorOpenProcessorFactory(Camera cameraService, Mail mailService,
			LedFeedback ledFeedback, Properties config) {
		super();
		this.cameraService = cameraService;
		this.mailService = mailService;
		this.ledFeedback = ledFeedback;
		this.config = config;
	}



	@Override
	public GpioPinListenerDigital createListener() {
		return new DoorOpenProcessor(cameraService, mailService, ledFeedback, config);
	}

}
