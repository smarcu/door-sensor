package com.teakdata.rpi.doorsensor;

import java.io.File;

/**
 * Email sending utility
 * @author smarcu
 */
public interface Mail {
	/**
	 * Send email in a limited amount of time, if timeout reached will return (no exception thrown)
	 * @param timeoutMillisec timeout before the call is interrupted and returns
	 * @param subjectText the subject line
	 * @param textMessage the message to be sent
	 * @param images the files to be attached
	 */
	void sendEmail(final int timeoutMillisec, final String subjectText, final String textMessage, final File...images);
}
