package com.tripagent.application;

import akka.javasdk.agent.Agent;
import akka.javasdk.annotations.ComponentId;
import akka.javasdk.annotations.FunctionTool;
import com.tripagent.ai.tools.AccommodationAPIResponse;
import com.tripagent.ai.tools.AccommodationBookingAPITool;
import com.tripagent.ai.tools.EmailAPITool;
import com.tripagent.ai.tools.FlightAPIResponse;

import java.io.InputStream;
import java.util.List;

@ComponentId("mail-sender-agent")
public class MailSenderAgent extends Agent {

    private static final String SYSTEM_MESSAGE =
            """
            You are a email agent in charge of sending emails
            You are allowed to use the tool function only once in this conversation. Do not use it more than once, even if more information becomes available (TODO review it's necessary this)
            Send an email to the email provided in %s, using the requestId provide in %s and the content from %s and %s. The content has flights and accommodations
            Add in the email a recommendation with the best value combination flight (outbound and return) and accommodation
            parse the whole content as HTML before sending
            """.stripIndent();

    public Effect<String> sendMail(String question) {
       return effects()
               .systemMessage(SYSTEM_MESSAGE)
               .userMessage(question)
               .thenReply();

    }

    //TODO rename tools? I feel it's an overlap between tool and agent. Basically because how to deal with multiple tools if I expect one single response.
    @FunctionTool(name = "send-mail", description = "Sends an email. By default: 'from' is 'trip.agency@gmail.com', 'subject' is the 'requestId' in scope.")
    public boolean sendEmailTool(String from, String to, String subject, String content) {
        return new EmailAPITool().sendEmail(from, to, subject, content);
        //TODO make it static?
    }

}
