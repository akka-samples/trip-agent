package com.lb.ai.tools;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;

public class EmailAPITool {

  private static final String smtpHost = "localhost";
  private static final String smtpPort = "1025";
  private static final Logger log = LoggerFactory.getLogger(EmailAPITool.class);
  private static final AtomicBoolean sentEmail = new AtomicBoolean(false);

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

  @Tool(
      description =
          "Sends an email. By default: 'from' is 'trip.agency@gmail.com', 'subject' is the 'requestId' in scope.")
  public boolean sendEmail(String from, String to, String subject, String content) {
    if (content == null || content.isEmpty()) {
      log.info("Email content is null or empty. Not sending email.");
      return false;
    }
    try {
      if (!sentEmail
          .get()) { // Avoiding spam in case LLM tries to use it more than once (which is common
        // ATM)
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(content, "text/html;charset=utf-8");
        Transport.send(message);
        log.info("Email sent");
        return sentEmail.compareAndSet(false, true);
      } else {
        log.info("Email already sent");
        return false;
      }
    } catch (MessagingException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
  }
}
