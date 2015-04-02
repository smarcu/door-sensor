package com.teakdata.rpi.doorsensor;

import static org.junit.Assert.*;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;

import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.teakdata.rpi.doorsensor.gpio.GpioTrigger;

public class DoorMonitorTest {

	private DoorMonitor subject;
	private Mail mailService;
	private Camera cameraService;
	private GpioTrigger gpioTrigger;
	private LedFeedback ledFeedback;
	private ServerPulse serverPulse;
	private DoorOpenProcessorFactory processorFactory;
	private GpioPinListenerDigital processor;
	private Properties config;

	@Before
	public void setUp() throws Exception {

		mailService = mock(Mail.class);
		cameraService = mock(Camera.class);
		gpioTrigger = mock(GpioTrigger.class);
		ledFeedback = mock(LedFeedback.class);
		serverPulse = mock(ServerPulse.class);
		processorFactory = mock(DoorOpenProcessorFactory.class);
		processor = mock(GpioPinListenerDigital.class);
		config = mock(Properties.class);

		subject = new DoorMonitor(mailService, cameraService, gpioTrigger,
				ledFeedback, serverPulse, processorFactory, config);

	}

	@Test
	public void test() throws InterruptedException {
		when(processorFactory.createListener()).thenReturn(processor);

		subject.startMonitoring();

		verify(gpioTrigger).addListener(processor);

	}

}
