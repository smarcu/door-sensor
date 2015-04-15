package com.teakdata.rpi.doorsensor;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.util.UUID;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CameraImplTest {
	
	private CameraImpl fixture;
	private File testFolder;
	private File srcFile;
	private static String OS = System.getProperty("os.name").toLowerCase();

	@Before
	public void setUp() throws Exception {
		testFolder = Files.createTempDirectory("TEST-CAMERAIMPL-"+UUID.randomUUID()).toFile();
		srcFile = new File(testFolder, "source");
		srcFile.createNewFile();
		String cmd = isWindows() ? "copy" : "cp";
		fixture = new CameraImpl(cmd + " "+srcFile.getPath()+" %IMG%", testFolder.getPath());
	}

	@After
	public void tearDown() {
		if(!testFolder.delete())
			testFolder.deleteOnExit();
	}
	
	@Test
	public void test() {
		
		File[] files = testFolder.listFiles();
		assertEquals(1, files.length);

		fixture.takePicture();

		files = testFolder.listFiles();
		assertEquals(2, files.length);
		
		fixture.takePicture();

		files = testFolder.listFiles();
		assertEquals(3, files.length);
	}
	
	private boolean isWindows() {
		return (OS.indexOf("win") >= 0);
	}

}
