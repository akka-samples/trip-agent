package com.lb.api;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

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
          "Sends an email. Use this tool only ONCE per conversation. By default: 'from' is 'trip.agency@gmail.com', 'subject' is the 'requestId' in scope.")
  public boolean sendEmail(String from, String to, String subject, String content) {
    try {
      if(!sentEmail.get()){ //Avoiding spam in case LLM tries to use it more than once (which is common ATM)
        Session session = Session.getInstance(props);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(content + createLink(), "text/html;charset=utf-8");
        Transport.send(message);
        sentEmail.compareAndSet(false, true);
        log.info("Email sent");
      } else {
        log.info("Email already sent");
      }
    } catch (MessagingException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
    return false;
  }

  public String createLink(){
    return """

             (bear with me, the following option should be just a link but for simplicity we use curl)
             If you want to book any of these options click here: `curl http://localhost:9000/trip/book -H "Content-Type: application/json" \\
            -d '{ "flightRef": "[your-ref-here]" }'`""";
  }
}
