/**
 * 	MADMaker - Genera e invia tramite PEC moduli di MAD per supplenze nelle scuole itailane
 *  (No chance you need this program and do not speak Italian!)
 *  
 *   Copyright (C) 2016  Davide Gatto
 *   
 *   @author Davide Gatto
 *   @mail davgatto@gmail.com
 *   
 *   This file is part of MADMaker
 *   MADMaker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gmail.davgatto.MADKing.Sender;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.json.JsonObject;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class PECSender {

	private int port;
	private String host;
//	private String from;
	//private boolean auth;
	private String username;
	private String password;
	//private Protocol protocol;
	private boolean debug;

	private String to;
	private String subject;
	private String body;
	private String attachment;

	private int getPort() {
		return port;
	}

	private void setPort(int port) {
		this.port = port;
	}

	private String getHost() {
		return host;
	}

	private void setHost(String host) {
		this.host = host;
	}

//	private String getFrom() {
//		return from;
//	}

//	private void setFrom(String from) {
//		this.from = from;
//	}

//	private boolean isAuth() {
//		return auth;
//	}
//
//	private void setAuth(boolean auth) {
//		this.auth = auth;
//	}

	private String getUsername() {
		return username;
	}

	private void setUsername(String username) {
		this.username = username;
	}

	private String getPassword() {
		return password;
	}

	private void setPassword(String password) {
		this.password = password;
	}

//	private Protocol getProtocol() {
//		return protocol;
//	}
//
//	private void setProtocol(Protocol protocol) {
//		this.protocol = protocol;
//	}

	private boolean isDebug() {
		return debug;
	}

	private void setDebug(boolean debug) {
		this.debug = debug;
	}

	private String getTo() {
		return to;
	}

	private void setTo(String to) {
		this.to = to;
	}

	private String getSubject() {
		return subject;
	}

	private void setSubject(String subject) {
		this.subject = subject;
	}

	private String getBody() {
		return body;
	}

	private void setBody(String body) {
		this.body = body;
	}

	private String getAttachment() {
		return attachment;
	}

	private void setAttachment(String attachment) {
		this.attachment = attachment;
	}

	public PECSender(JsonObject jsoMail, JsonObject jsoTeach, String folderPath, boolean debug) {

		setUsername(jsoMail.getString("username"));

		setPassword(jsoMail.getString("password"));

		setPort(Integer.parseInt(jsoMail.getString("port")));

		setHost(jsoMail.getString("host"));

		// setAuth(true);

//		if ("SMTP".equals(jsoMail.getString("protocol"))) {
//			setProtocol(Protocol.SMTP);
//		} else if ("TLS".equals(jsoMail.getString("protocol"))) {
//			setProtocol(Protocol.TLS);
//		} else {
//			setProtocol(Protocol.SMTPS);
//		}

		setDebug(debug);

		setSubject(jsoMail.getString("subject"));
		setBody(jsoMail.getString("body"));

		setAttachment(folderPath + "/" + jsoTeach.getString("name").replaceAll("\\s|\'", "") + "_");

	}

	public void sendMail(JsonObject jsoSchool, String pecIstruzione, boolean simulating)
			throws AddressException, MessagingException, IOException {

		String body = getBody();
		if (!simulating) {
			setTo(jsoSchool.getString("codMec") + pecIstruzione);
		} else {
			setTo(pecIstruzione);
			body += "\nDEBUG: Questa mail sarebbe stata inviata alla scuola " + jsoSchool.getString("codMec") + "\n";
		}
//		setFrom(getUsername());

		//setAuth(true);
		setDebug(isDebug());

		String basicAttachmentName = getAttachment();

		setAttachment(basicAttachmentName + jsoSchool.getString("nome").replaceAll("\\s|\"|\'", "") + "-"
				+ jsoSchool.getString("codMec") + "_MAD.pdf");

		String[] attachments = new String[1];
		attachments[0] = getAttachment();

		sendEmailWithAttachments(getHost(), getPort() + "", getUsername(), getPassword(), getTo(),
				getSubject() + "[" + jsoSchool.getString("codMec") + pecIstruzione + "]", body, attachments);

		setAttachment(basicAttachmentName);

	}

	private static void sendEmailWithAttachments(String host, String port, final String userName, final String password,
			String toAddress, String subject, String message, String[] attachFiles)
			throws AddressException, MessagingException {
		// sets SMTP server properties
		Properties properties = new Properties();
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", port);
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.user", userName);
		properties.put("mail.password", password);

		// creates a new session with an authenticator
		Authenticator auth = new Authenticator() {
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userName, password);
			}
		};
		Session session = Session.getInstance(properties, auth);

		// creates a new e-mail message
		Message msg = new MimeMessage(session);

		msg.setFrom(new InternetAddress(userName));
		InternetAddress[] toAddresses = { new InternetAddress(toAddress) };
		msg.setRecipients(Message.RecipientType.TO, toAddresses);
		msg.setSubject(subject);
		msg.setSentDate(new Date());

		// creates message part
		MimeBodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setContent(message, "text/html");

		// creates multi-part
		Multipart multipart = new MimeMultipart();
		multipart.addBodyPart(messageBodyPart);

		// adds attachments
		if (attachFiles != null && attachFiles.length > 0) {
			for (String filePath : attachFiles) {
				MimeBodyPart attachPart = new MimeBodyPart();

				try {
					attachPart.attachFile(filePath);
				} catch (IOException ex) {
					ex.printStackTrace();
				}

				multipart.addBodyPart(attachPart);
			}
		}

		// sets the multi-part as e-mail's content
		msg.setContent(multipart);

		// sends the e-mail
		Transport.send(msg);

	}

}
