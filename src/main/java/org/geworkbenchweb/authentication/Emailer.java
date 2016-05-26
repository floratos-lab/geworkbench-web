package org.geworkbenchweb.authentication;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geworkbenchweb.GeworkbenchRoot;

/* The class to send email to the users. */
public class Emailer {
	private Log log = LogFactory.getLog(Emailer.class);

	private String auth = GeworkbenchRoot.getAppProperty("mail.smtp.auth");
	private String host = GeworkbenchRoot.getAppProperty("mail.smtp.host");
	private String port = GeworkbenchRoot.getAppProperty("mail.smtp.port");
	private String starttls = GeworkbenchRoot.getAppProperty("mail.smtp.starttls.enable");
	private String fromUserName = GeworkbenchRoot.getAppProperty("from.username");
	private String fromPassword = GeworkbenchRoot.getAppProperty("from.password");

	final private Properties props;

	// default from-email
	public Emailer() {
		props = new Properties() {
			private static final long serialVersionUID = -4425069165161665144L;
			{
				put("mail.smtp.auth", auth);
				put("mail.smtp.host", host);
				put("mail.smtp.port", port);
				put("mail.smtp.starttls.enable", starttls);
			}
		};
	}

	public void send(String toEmail, String title, String content) {
		Session mailSession = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(fromUserName, fromPassword);
			}
		});
		MimeMessage mailMessage = new MimeMessage(mailSession);
		String unicodeTitle = "";
		try {
			unicodeTitle = MimeUtility.encodeText(title, "utf-8", null);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		try {
			mailMessage.setSubject(unicodeTitle);
			mailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
			mailMessage.setContent(content, "text/html");
			Transport.send(mailMessage);
		} catch (MessagingException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}
}
