package com.teakdata.rpi.doorsensor;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;

public class DoorOpenProcessorTest {

	private DoorOpenProcessor fixture;
	private Camera cameraService;
	private Mail mailService;
	private LedFeedback ledFeedback;
	private Properties config;
	
	@Before
	public void setUp() throws Exception {
		cameraService = mock(Camera.class);
		mailService = mock(Mail.class);
		ledFeedback = mock(LedFeedback.class);
		config = new Properties();
		fixture = new DoorOpenProcessor(cameraService, mailService, ledFeedback, config);
		
	}

	@Test
	public void test() throws InterruptedException {
		
		File[] images = new File[]{new File("a"), new File("b")};
		when(cameraService.takePicture()).thenReturn(images[0]).thenReturn(images[1]);
		config.setProperty(DoorOpenProcessor.SHOT_COUNT, "2");
		config.setProperty(DoorOpenProcessor.SHOT_SLEEP, "10");
		config.setProperty(DoorOpenProcessor.EMAIL_SUBJECT, "test_subject");

		fixture.handleGpioPinDigitalStateChangeEvent(event(PinState.LOW));
		Thread.currentThread().sleep(5);
		fixture.handleGpioPinDigitalStateChangeEvent(event(PinState.HIGH));
		Thread.currentThread().sleep(20);
		
		// handle door open
		
		InOrder inOrder = inOrder(cameraService, mailService);
		inOrder.verify(cameraService, times(2)).takePicture();
		inOrder.verify(mailService).sendEmail(30000, "test_subject", " - door open", images);
	}

	private GpioPinDigitalStateChangeEvent event(PinState state) {
		GpioPinDigitalStateChangeEvent event = new GpioPinDigitalStateChangeEvent("", null, state);
		return event;
	}

}
