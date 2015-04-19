package com.teakdata.rpi.doorsensor;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.teakdata.rpi.doorsensor.gpio.GpioDoorOpenProcessorFactory;
import com.teakdata.rpi.doorsensor.gpio.GpioTrigger;
import com.teakdata.rpi.doorsensor.gpio.GpioTriggerImpl;
import com.teakdata.rpi.doorsensor.gpio.LedFeedbackImpl;

public class DoorMonitor {

	private Properties config;
	private static final String EMAIL_SUBJECT = "email.subject";

	private static final Logger log = Logger.getLogger(DoorMonitor.class.getName());
	private Mail mailService;
	private Camera cameraService;
	private GpioTrigger gpioTrigger;
	private LedFeedback ledFeedback;
	private ServerPulse serverPulse;
	private DoorOpenProcessorFactory processorFactory;
	
	
	public DoorMonitor(Mail mailService, Camera cameraService,
			GpioTrigger gpioTrigger, LedFeedback ledFeedback,
			ServerPulse serverPulse, DoorOpenProcessorFactory doorOpenProcessorFactory,
			Properties config) {
		super();
		this.mailService = mailService;
		this.cameraService = cameraService;
		this.gpioTrigger = gpioTrigger;
		this.ledFeedback = ledFeedback;
		this.serverPulse = serverPulse;
		this.config = config;
		this.processorFactory = doorOpenProcessorFactory;
	}

	public static DoorMonitor createFromConfig(Properties config) {
		
		Mail mailService = MailImpl.createFromConfig(config);
		Camera cameraService = CameraImpl.createFromConfig(config);
		ServerPulse serverPulse = ServerPulse.createFromConfig(config);
		LedFeedback ledFeedback = null;
		// hardware required
		try {
			ledFeedback = LedFeedbackImpl.createFromConfig(config);
		} catch (UnsatisfiedLinkError e) {
			log.log(Level.WARNING, "Cannot initialize GPIO trigger", e);
		}
		GpioTrigger gpioTrigger = null;
		// hardware required
		try {
			gpioTrigger = GpioTriggerImpl.createFromConfig(config);
		} catch (UnsatisfiedLinkError e) {
			log.log(Level.WARNING, "Cannot initialize GPIO trigger", e);
		}
		
		return new DoorMonitor(mailService, cameraService, gpioTrigger, ledFeedback, serverPulse, 
				new GpioDoorOpenProcessorFactory(cameraService, mailService, ledFeedback, config),
				config);
	}
	
	public void startMonitoring() throws InterruptedException {
		
		log.info("started.");
		
		// send email, app started or rebooted
		mailService.sendEmail(10000, config.getProperty(EMAIL_SUBJECT, "Door Security"), " - app started");

		if (serverPulse != null) {
			log.info("starting pulse");
			serverPulse.start();
		}

		if (gpioTrigger != null) {
			log.info("adding gpio trigger listener");
			gpioTrigger.addListener(processorFactory.createListener());
		}
			
	}
	
	public static void main(String s[]) {

		try {
			String propertiesFile = s.length == 1 ? s[0] : "door.properties";
			Properties config = new Properties();
			try {
			  config.load(new FileInputStream(propertiesFile));
			} catch (IOException e) {
			  log.log(Level.WARNING, "cannot find properties file: {0}", propertiesFile);
			  return;
			}
			
			DoorMonitor monitor = DoorMonitor.createFromConfig(config);
			monitor.startMonitoring();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	

}
