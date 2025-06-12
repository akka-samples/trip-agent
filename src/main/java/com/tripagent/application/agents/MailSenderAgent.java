package com.tripagent.application.agents;

import akka.javasdk.agent.Agent;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.FunctionTool;
import com.tripagent.application.agents.tools.EmailAPIToolHelper;

@ComponentId("mail-sender-agent")
public class MailSenderAgent extends Agent {

  private static final String SYSTEM_MESSAGE =
      """
            You are a email agent in charge of sending emails
            You are allowed to use the tool function only once in this conversation. Do not use it more than once, even if more information becomes available
            Send an email to the email provided in user request. The content has flights and accommodations
            Add in the email a recommendation with the best value combination flight (outbound and return) and accommodation
            parse the whole content as HTML before sending
            """
          .stripIndent();

  public Effect<String> sendMail(String question) {
    return effects().systemMessage(SYSTEM_MESSAGE).userMessage(question).thenReply();
  }

  @FunctionTool(
      name = "send-mail",
      description =
          "Sends an email. By default: 'from' is 'trip.agency@gmail.com', 'subject' is the 'requestId' in scope.")
  public boolean sendEmailTool(String from, String to, String subject, String content) {
    return new EmailAPIToolHelper().sendEmail(from, to, subject, content);
  }
}
