package com.teakdata.rpi.doorsensor;

import java.io.File;

/**
 * Generic interface for camera devices
 * @author smarcu
 */
public interface Camera {

	/**
	 * take a picture and return the file.
	 * @return the picture file
	 */
	File takePicture();

}