package com.axiomsl.email;

import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public final class SendEmail {

	@SuppressWarnings("null")
	public static void sendEmail(String emailFrom, String[] emailTo, String subject, String bodyText, String... dirs) {

		if (emailTo.length < 1) {
			System.err.println("You must pass Recipient's email ID.");
			System.exit(0);
		}

		// Sender's email ID needs to be mentioned
		String from = emailFrom;

		// Assuming you are sending email from localhost
		String host = "10.250.2.203";

		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.host", host);

		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties);

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);
			MimeMultipart messageMultiPart = new MimeMultipart();

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			for (String email : emailTo) {
				// Set To: header field of the header.
				String to = email;

				// Recipient's email ID needs to be mentioned.
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			}

			// Set Subject: header field
			message.setSubject(subject);

			// Add attachments
			for (String s : dirs) {

				for (String t : bodyText.split("-------------PDFs that have passed validation-------------")[0]
						.split("\r\n")) {

					File attch = new File(new File(s), t);

					if (attch.isFile()) {

						MimeBodyPart messageBodyPart = new MimeBodyPart();

						DataSource source = new FileDataSource(attch.getAbsolutePath());
						messageBodyPart.setDataHandler(new DataHandler(source));
						// Name of parent folder_Attachment Name
						messageBodyPart.setFileName(attch.getParentFile().getName() + "_" + attch.getName());
						messageMultiPart.addBodyPart(messageBodyPart);

					}

				}

			}
			final MimeBodyPart textPart = new MimeBodyPart();

			textPart.setContent(bodyText.trim(), "text/plain");

			messageMultiPart.addBodyPart(textPart);

			message.setContent(messageMultiPart);
			// Now set the actual message
			// message.setText(bodyText);

			// Send message
			try {
				Transport.send(message);
			} catch (Exception e) {
				messageMultiPart = null;
				messageMultiPart.addBodyPart(textPart);
				message.setContent(messageMultiPart);
				
				Transport.send(message);
				
			}
			System.out.println("Sent message successfully....");
//			System.out.println(textPart.getContent().toString());
		} catch (MessagingException mex) {
			mex.printStackTrace();
		}
	}

}
