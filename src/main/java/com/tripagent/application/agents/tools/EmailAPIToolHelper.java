package com.tripagent.application.agents.tools;

import java.util.Properties;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmailAPIToolHelper {

  private static final String smtpHost = "localhost";
  private static final String smtpPort = "1025";
  private static final Logger log = LoggerFactory.getLogger(EmailAPIToolHelper.class);

  private static final Properties props = new Properties();
  private static final String smtpHostEnv = System.getenv("SMTP_HOST");
  private static final String smtpPortEnv = System.getenv("SMTP_PORT");
  private static final String smtpAuthEnv = System.getenv("SMTP_AUTH");
  private static final String smtpStartTLSEnv = System.getenv("SMTP_START_TLS");

  static {
    props.put("mail.smtp.host", smtpHostEnv != null ? smtpHostEnv : smtpHost);
    props.put("mail.smtp.port", smtpPortEnv != null ? smtpPortEnv : smtpPort);
    props.put("mail.smtp.auth", smtpAuthEnv != null ? smtpAuthEnv : "false");
    props.put("mail.smtp.starttls.enable", smtpStartTLSEnv != null ? smtpStartTLSEnv : "false");
  }

  private static final Session session = Session.getInstance(props);

  public boolean sendEmail(String from, String to, String subject, String content) {
    if (content == null || content.isEmpty()) {
      log.info("Email content is null or empty. Not sending email.");
      return false;
    }
    try {
      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(from));
      message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(to));
      message.setSubject(subject);
      message.setContent(content, "text/html;charset=utf-8");
      Transport.send(message);
      log.info("Email sent");
      return true;
    } catch (MessagingException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
