package com.teakdata.rpi.doorsensor.gpio;

import java.util.Properties;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * An event trigger for GPIO pins. 
 * 
 * @author smarcu
 */
public class GpioTriggerImpl implements GpioTrigger {

	/**
	 * config property for pin. Values example: "GPIO_02" @see {@link RaspiPin}
	 */
	private static final String GPIO_TRIGGER_PIN = "gpio.trigger.pin";
	/**
	 * config property for pin resistance mode. Value example: "PULL_DOWN" @see {@link PinPullResistance}
	 */
	private static final String GPIO_TRIGGER_PIN_PULL_RESISTANCE = "gpio.trigger.pin_pull_resistance";
	
	private GpioPinDigitalInput gpioPin;
	
	/**
	 * Creates a trigger for the pin using the pinPullResistance mode..
	 * @param pin the pin to be monitored
	 * @param pinPullResistance the pin mode
	 */
	public GpioTriggerImpl(Pin pin, PinPullResistance pinPullResistance) {
		GpioController gpio = GpioFactory.getInstance();
		gpioPin = gpio.provisionDigitalInputPin(pin, pinPullResistance);
	}

	/**
	 * Add a new listener
	 * @param listener the listener to be invoked when an event is triggered
	 */
	public void addListener(GpioPinListenerDigital listener) {
		gpioPin.addListener(listener);
	}
	
	public static GpioTrigger createFromConfig(Properties config) {
		try {
			String pinName = config.getProperty(GPIO_TRIGGER_PIN, "GPIO_02");
			Pin pin = (Pin) RaspiPin.class.getField(pinName).get(null);
			
			String modeName = config.getProperty(GPIO_TRIGGER_PIN_PULL_RESISTANCE, "PULL_DOWN");
			PinPullResistance mode = PinPullResistance.valueOf(modeName);
			
			GpioTrigger trigger = new GpioTriggerImpl(pin, mode);
			return trigger;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
