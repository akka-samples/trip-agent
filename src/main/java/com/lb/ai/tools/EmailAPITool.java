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

  static {
    props.put("mail.smtp.host", smtpHost);
    props.put("mail.smtp.port", smtpPort);
    props.put("mail.smtp.auth", "false");
    props.put("mail.smtp.starttls.enable", "false");
  }

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
        Session session = Session.getInstance(props);
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
