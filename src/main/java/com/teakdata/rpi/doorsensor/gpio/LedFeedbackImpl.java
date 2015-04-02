package com.teakdata.rpi.doorsensor.gpio;

import java.util.Properties;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.teakdata.rpi.doorsensor.LedFeedback;

public class LedFeedbackImpl implements LedFeedback {

	private GpioPinDigitalOutput ledLightPin;
	
	/**
	 * config property for pin. Values example: "GPIO_02" @see {@link RaspiPin}
	 */
	private static final String GPIO_LED_PIN = "gpio.led.pin";
	
	/**
	 * config property for pin initial state. Value example: "HIGH" @see {@link PinState}
	 */
	private static final String GPIO_LED_PIN_STATE = "gpio.led.state";
	
	public LedFeedbackImpl(Pin pin, PinState initialState) {
		GpioController gpio = GpioFactory.getInstance();

		ledLightPin = gpio.provisionDigitalOutputPin(pin, "MyLED", initialState);
	}
	
	@Override
	public synchronized void set(boolean on) {
		ledLightPin.setState(on);
	}

	public static LedFeedback createFromConfig(Properties config) {
		try {
			String pinName = config.getProperty(GPIO_LED_PIN, "GPIO_01");
			Pin pin = (Pin) RaspiPin.class.getField(pinName).get(null);
			
			String modeName = config.getProperty(GPIO_LED_PIN_STATE, "HIGH");
			PinState state = PinState.valueOf(modeName);
			
			LedFeedbackImpl led = new LedFeedbackImpl(pin, state);
			return led;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
