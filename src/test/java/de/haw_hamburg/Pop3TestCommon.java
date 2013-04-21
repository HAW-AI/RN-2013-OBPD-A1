package de.haw_hamburg;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Pop3TestCommon {
	public static MimeMessage construct(String to, String from) {
		// Assuming you are sending email from localhost
		// String host = "localhost";

		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		// properties.setProperty("mail.smtp.host", host);

		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties);

		MimeMessage message = new MimeMessage(session);
		try {
			message.setFrom(new InternetAddress(from));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					to));
			message.setSubject("This is the Subject Line!");
			message.setContent("This is actual message", "text/plain");
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}

		return message;
	}
}
