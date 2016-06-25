package com.teakdata.rpi.doorsensor;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
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

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Mail sender implementation using javax.mail
 * @author smarcu
 */
public class MailImpl implements Mail {
	
	private static final String EMAIL_TO = "email.to";
	private static final String EMAIL_FROM = "email.from";
	private static final String EMAIL_SMTP_HOST = "email.smtp.host";
	private static final String EMAIL_SMTP_PORT = "email.smtp.port";
	private static final String EMAIL_SMTP_USER = "email.smtp.user";
	private static final String EMAIL_SMTP_PASS = "email.smtp.passwd";

	private static final Logger log = Logger.getLogger(MailImpl.class.getName());
	private static final ExecutorService executorService = Executors.newFixedThreadPool(1);
	private String smtpHost;
	private int smtpPort;
	private String smtpUser;
	private String smtpPass;
	private String emailTo;
	private String emailFrom;
	
	/**
	 * Create a new mail instance.
	 * @param smtpHost the smtp server name (e.g. smtp.gmail.com)
	 * @param smtpUser the smtp server user
	 * @param smtpPass the smtp server password
	 * @param emailTo the destination email
	 * @param emailFrom the source email (that works on the smpt server)
	 */
	public MailImpl(String smtpHost, int smtpPort, String smtpUser, String smtpPass,
			String emailTo, String emailFrom) {
		super();
		this.smtpHost = smtpHost;
		this.smtpPort = smtpPort;
		this.smtpUser = smtpUser;
		this.smtpPass = smtpPass;
		this.emailTo = emailTo;
		this.emailFrom = emailFrom;
	}

	/**
	 * {@inheritDoc}
	 */
	public void sendEmail(final int timeoutMillisec, final String subjectText, final String textMessage, 
			final File...images) {
		long currentTimeMillis = System.currentTimeMillis();
		try {
			
			List<Future<Void>> tasks = executorService.invokeAll(Arrays.asList(new Callable<Void>() {
				public Void call() throws Exception {
					sendEmail(subjectText, textMessage, images);
					return null;
				}
			}), timeoutMillisec, TimeUnit.MILLISECONDS);
			tasks.get(0).get();
			
		} catch (InterruptedException e) {
			log.log(Level.WARNING, "SEND EMAIL FAILED (INTERRUPTED) "+e.getMessage(), e);
		} catch (CancellationException e) {
			log.log(Level.WARNING, "SEND EMAIL FAILED (TIMEOUT) "+e.getMessage(),e );
		} catch (ExecutionException e) {
			log.log(Level.WARNING, "SEND EMAIL FAILED (EXECUTION) "+e.getMessage(),e );
		} finally {
			log.info("send email total time : " + (System.currentTimeMillis() - currentTimeMillis)+" ms");
		}
	}
	
	/**
	 * Send the email, sync method.
	 */
	private void sendEmail(String subjectText, String text_message, File... images) {
		Properties properties = System.getProperties();
		properties.setProperty("mail.smtp.host", smtpHost);
		properties.put("mail.smtp.port", smtpPort);
		properties.put("mail.smtp.user", emailFrom);
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
		
		Authenticator auth = new Authenticator() {
			@Override
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(smtpUser, smtpPass);
			}
		};
		Session session = Session.getInstance(properties, auth);

		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(emailFrom));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					emailTo));
			message.setSubject(subjectText);
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText("" + new Date() + text_message);
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);

			if (images != null) {
				for (File img : images) {
					MimeBodyPart mbpart = new MimeBodyPart();
					log.info("add image to email: " + img.getPath());
					DataSource source = new FileDataSource(img);
					mbpart.setDataHandler(new DataHandler(source));
					mbpart.setFileName(img.getName());
					multipart.addBodyPart(mbpart);
				}
			}

			message.setContent(multipart);
			
			Transport.send(message);

			log.info("Sent message successfully....");
			
			if (images != null) {
				for (File f:images) {
					f.delete();
					log.info("delete " + f);
				}
			}
			
		} catch (MessagingException mex) {
			log.log(Level.WARNING, "Failed to send message: "+mex.getMessage(), mex);
		}

	}
	
	public static Mail createFromConfig(Properties config) {
		final String smtpHost = config.getProperty(EMAIL_SMTP_HOST);
		final int smtpPort = PropertiesUtils.getConfigInt(config, EMAIL_SMTP_PORT, 25);
		final String smtpUser = config.getProperty(EMAIL_SMTP_USER);
		final String smtpPass = config.getProperty(EMAIL_SMTP_PASS);
		String to = config.getProperty(EMAIL_TO);
		String from = config.getProperty(EMAIL_FROM);
		return new MailImpl(smtpHost, smtpPort, smtpUser, smtpPass, to, from);
	}

}
