package com.teakdata.rpi.doorsensor;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A generic camera implementation using a shell command 
 * <p>(e.g. "sudo raspistill -t 1 -w 400 -h 300 -o %IMG%")
 * 
 * @author smarcu
 */
public class CameraImpl implements Camera {

	private static final String SHOT_CMD = "shot.cmd";
	private static final String SHOT_FOLDER = "shot.folder";

	
	private static final Logger log = Logger.getLogger(CameraImpl.class.getName());
	private String shotCmd;
	private String destinationFolder;

	/**
	 * The shot command should have a file output param, use %IMG% token for the
	 * file output name. The %IMG% token will be replaced when picture is taken
	 * with a generated image name, see {@link #generateImgName()}
	 * 
	 * @param shotCmd
	 */
	public CameraImpl(String shotCmd, String destinationFolder) {
		this.shotCmd = shotCmd;
		this.destinationFolder = destinationFolder;
	}

	public File takePicture() {
		try {
			File img = new File(destinationFolder, generateImgName());
			log.info("shot ..." + img.getPath());
			String cmd = shotCmd.replace("%IMG%", img.getPath());
			String[] cmdSplit = cmd.split(" ");
			for (String c : cmdSplit)
				log.info("[" + c + "]");
			ProcessBuilder pb = new ProcessBuilder(cmdSplit);
			Process p = pb.start();
			p.waitFor();
			log.info("shot done!");
			if (img.exists()) {
				log.info("img file: " + img);
				return img;
			} else {
				log.info("File does not exist: " + img);
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "Failed to take picture: "+e.getMessage(), e);
		}
		return null;
	}

	private String generateImgName() {
		DateFormat df = new SimpleDateFormat("yyMMdd-HHmmss-SSS");
		return df.format(new Date()) + ".jpg";
	}

	public static Camera createFromConfig(Properties config) {
		String cmd = config.getProperty(SHOT_CMD, "sudo raspistill -t 1 -w 400 -h 300 -o %IMG%");
		String folder = config.getProperty(SHOT_FOLDER, ".");
		
		return new CameraImpl(cmd, folder);
	}
}
