package com.teakdata.rpi.doorsensor;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Iterator;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

public class MailImplTest {

	private MailImpl fixture;
	private SimpleSmtpServer dumbsterSmtpServer;
	private static File testFile;

	@BeforeClass
	public static void setUpClass() throws Exception {
		testFile = new File(MailImplTest.class.getClassLoader()
				.getResource("com/teakdata/rpi/doorsensor/img1.png").getFile());
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}
	
	@Before
	public void setUp() throws Exception {
		dumbsterSmtpServer = SimpleSmtpServer.start(3333);
		fixture = new MailImpl("localhost", 3333, "test", "pass", "destEmail",
				"srcEmail");
	}

	@After
	public void tearDown() throws Exception {
		dumbsterSmtpServer.stop();
	}

	@Test
	public void testSendEmail_noAttachement() {

		fixture.sendEmail(1000, "test subject", "test message");

		@SuppressWarnings("unchecked")
		Iterator<SmtpMessage> receivedEmailIterator = dumbsterSmtpServer
				.getReceivedEmail();

		SmtpMessage email = receivedEmailIterator.next();

		assertEquals("test subject", email.getHeaderValue("Subject"));
	}

	@Test
	public void testSendEmail_withAttachement() {

		fixture.sendEmail(1000, "test subject", "test message", testFile,
				testFile);

		@SuppressWarnings("unchecked")
		Iterator<SmtpMessage> receivedEmailIterator = dumbsterSmtpServer
				.getReceivedEmail();

		SmtpMessage email = receivedEmailIterator.next();

		assertEquals("test subject", email.getHeaderValue("Subject"));
	}
}
