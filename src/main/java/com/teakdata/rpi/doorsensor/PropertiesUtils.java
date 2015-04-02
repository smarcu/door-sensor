package com.teakdata.rpi.doorsensor;

import java.util.Properties;

public class PropertiesUtils {
	public static int getConfigInt(Properties config, String key, int def) {
		int vint = def;
		try {
			String val = config.getProperty(key);
			if (val != null) {
				def = Integer.parseInt(val);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return vint;
	}

}
