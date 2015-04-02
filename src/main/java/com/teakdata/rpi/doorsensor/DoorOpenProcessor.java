package com.teakdata.rpi.doorsensor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * Processor for door open / door close event.
 * <p>
 * This class holds the main logic for taking pictures, sending email.
 * @author smarcu
 */
public class DoorOpenProcessor implements GpioPinListenerDigital {
	
	private final Logger log = Logger.getLogger(DoorOpenProcessor.class.getName());
	
	private static final String SHOT_SLEEP = "shot.sleep";
	private static final String SHOT_COUNT = "shot.count";
	private static final String EMAIL_SUBJECT = "email.subject";
	
	private static AtomicBoolean doorOpenFlag = new AtomicBoolean(false);
	private Camera cameraService;
	private Mail mailService;
	private LedFeedback ledFeedback;
	private static final ExecutorService executorServiceDoorOpen = Executors.newFixedThreadPool(1);
	private Properties config;


	public DoorOpenProcessor(Camera cameraService, Mail mailService, LedFeedback ledFeedback, Properties config) {
		this.cameraService = cameraService;
		this.mailService = mailService;
		this.ledFeedback = ledFeedback;
		this.config = config;
	}
	
	/**
	 * handle the gpio door open/closed event.
	 */
	public void handleGpioPinDigitalStateChangeEvent(
			GpioPinDigitalStateChangeEvent event) {

		log.info(" --> PIN STATE CHANGE: " + event.getPin() + " = "
				+ event.getState());

		if (event.getState() == PinState.HIGH) {
			doorOpenFlag.set(false);
			log.info(" --> DOOR CLOSED " );
			ledFeedback.set(false);
			// door closed
		} else {
			doorOpenFlag.set(true);
			log.info(" --> DOOR OPENED " );
			ledFeedback.set(true);
			// door open
			executorServiceDoorOpen.submit(new Runnable(){
				public void run() {
					try {
						doorOpen();
					} catch (Exception e) {
						log.info("ERROR: " + e.getMessage());
					}
				}
			});

		}
	}

	private void doorOpen() throws InterruptedException {

		while (doorOpenFlag.get()) {
			
			log.info("DOOR STILL OPEN, processing ...");
			
			int totalPics = PropertiesUtils.getConfigInt(config, SHOT_COUNT, 5);
	
			List<File> imgs = new ArrayList<File>();
			for (int i = 0; i < totalPics; i++) {
				File img = cameraService.takePicture();
				if (img != null) {
					imgs.add(img);
				}
				Thread.sleep(PropertiesUtils.getConfigInt(config, SHOT_SLEEP, 700));
			}
			mailService.sendEmail(30000, config.getProperty(EMAIL_SUBJECT, "Door Security"), " - door open", imgs.toArray(new File[] {}));
			
		}
	}

	
}
