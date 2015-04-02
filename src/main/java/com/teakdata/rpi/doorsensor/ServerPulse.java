package com.teakdata.rpi.doorsensor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A thread that monitors connectivity and restarts the system if not connected for 
 * a specific interval.
 * <p>
 * In some scenarios, the raspberry pi was in a state with wifi disconnected
 * and not being able to auto reconnect. 
 * 
 * @author smarcu
 */
public class ServerPulse extends Thread {

	private static final String SERVER_PULSE_INTERVAL = "server.pulse.interval";
	private static final String SERVER_PULSE_URL = "server.pulse.url";
	private static final String SERVER_PULSE__BACKUP_URL = "server.pulse.backupUrl";
	private static final String SERVER_PULSE_RETRY = "server.pulse.retry";
	private static final String SERVER_PULSE_REBOOT_CMD = "server.pulse.rebootcmd";

	private static final Logger log = Logger.getLogger(ServerPulse.class.getName());
	private int pulseInterval;
	private String pulseServerUrl;
	private String pulseServerBackupUrl;
	private int retry;
	private int retryCount=0;
	private ExecutorService executorService = Executors.newFixedThreadPool(1);
	private int timeoutOperation = 60000;
	private String rebootCmd;

	public ServerPulse(int pulseInterval, String serverUrl, String serverBackupUrl, int retry, String rebootCmd) {
		super();
		
		this.pulseInterval = pulseInterval;
		this.pulseServerUrl = serverUrl;
		this.pulseServerBackupUrl = serverBackupUrl;
		this.retry = retry;
		this.rebootCmd = rebootCmd;
	}

	@Override
	public void run() {
		while (true) {
			log.info("SENDING PULSE ...");
			if (sendGet(pulseServerUrl, timeoutOperation)) {
				log.info("PULSE OK");
				retryCount = 0;					
			} else {
				log.info("PULSE FAILED RETRY "+retryCount+"/"+retry);
				if (retryCount++ > retry) {
					boolean reboot = true;
					// if backup defined, check backup first
					if (isBackupDefined()) {
						log.info("PULSE TRY BACKUP ");
						if(sendGet(pulseServerBackupUrl, timeoutOperation)) {
							log.info("PULSE BACKUP OK");
							// successfully connected to the backup, don't reboot
							retryCount = 0;
							reboot = false;
						} else {
							log.info("PULSE BACKUP FAILED");
						}
					}
					
					if (reboot) {
						reboot();
					}
				}
			}

			try {
				sleep(pulseInterval);
			} catch (InterruptedException e) {
				log.info("sleep interrupted");
				return;
			}
		}
	}

	private void reboot() {
		try {
			log.info("running command: "+rebootCmd);
			String[] cmdSplit = rebootCmd.split(" ");
			for (String c : cmdSplit)
				log.info("[" + c + "]");
			ProcessBuilder pb = new ProcessBuilder(cmdSplit);
			Process p = pb.start();
			p.waitFor();
		} catch (Exception e) {
			log.log(Level.WARNING, "failed to reboot: " + e.getMessage(), e);
		}
	}
	
	private boolean isBackupDefined() {
		return pulseServerBackupUrl !=null && !pulseServerBackupUrl.trim().isEmpty();
	}
	
	private boolean sendGet(final String url, int timeoutMillisec) {
		
		try {
			
			List<Future<Boolean>> tasks = executorService.invokeAll(Arrays.asList(new Callable<Boolean>() {
				public Boolean call() throws Exception {
					return sendGet(url);
				}
			}), timeoutMillisec, TimeUnit.MILLISECONDS);
			return tasks.get(0).get();
			
		} catch (InterruptedException e) {
			log.log(Level.WARNING, "PULSE FAILED (INTERRUPTED) "+e.getMessage(), e);
			return false;
		} catch (CancellationException e) {
			log.log(Level.WARNING, "PULSE FAILED (TIMEOUT) "+e.getMessage(), e);
			return false;
		} catch (ExecutionException e) {
			log.log(Level.WARNING, "PULSE FAILED (EXECUTION) "+e.getMessage(), e);
			return false;
		}
		
	}
	
	/**
	 * Send a get HTTP/HTTPS command
	 * @param url
	 * @return
	 * @throws Exception
	 */
	private boolean sendGet(String url) {

		try {
			
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent", "RPI");
	
			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + url);
			System.out.println("Response Code : " + responseCode);
	
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
	
			return true;
			
		} catch (Exception e) {
			log.log(Level.WARNING, "PULSE FAILED "+e.getMessage(), e);
			//e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * creates a new ServerPulse object from configuration. If pulseUrl not defined,
	 * it will return null.
	 * @param config configuration 
	 * @return a new ServerPulse or null
	 */
	public static ServerPulse createFromConfig(Properties config) {
		int pulseInterval = PropertiesUtils.getConfigInt(config, SERVER_PULSE_INTERVAL, 60000);
		String pulseUrl = config.getProperty(SERVER_PULSE_URL, null);
		String pulseBackupUrl = config.getProperty(SERVER_PULSE__BACKUP_URL, null);
		int pulseRetry = PropertiesUtils.getConfigInt(config, SERVER_PULSE_RETRY, 20);
		String rebootCmd = config.getProperty(SERVER_PULSE_REBOOT_CMD, "reboot");
		
		boolean serverPulseDefined = pulseUrl != null && !pulseUrl.trim().isEmpty();
		
		return serverPulseDefined ? new ServerPulse(pulseInterval, pulseUrl, 
				pulseBackupUrl, pulseRetry, rebootCmd) : null;
	}

	/**
	 * Test only 
	 */
	public static void main(String s[]) throws Exception {
		ServerPulse pulse = new ServerPulse(3000, "http://google.ca", 
				"http://google.com", 3, "echo rebooting");
		pulse.setDaemon(false);
		pulse.start();
	}
}
