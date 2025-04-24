package com.tripagent.ai.tools;

import java.lang.reflect.Type;

import akka.javasdk.JsonSupport;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.execution.ToolCallResultConverter;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.lang.Nullable;

/** Leverages akka.javasdk.JsonSupport; Among others to allow ZonedDateTime deSer. */
public final class AkkaToolCallResultConverter implements ToolCallResultConverter {

  private static final Logger logger = LoggerFactory.getLogger(MethodToolCallback.class);

  @Override
  public String convert(@Nullable Object result, @Nullable Type returnType) {
    if (returnType == Void.TYPE) {
      logger.debug("The tool has no return type. Converting to conventional response.");
      return "Done";
    } else {
      try {
        logger.debug("Converting tool result to JSON.");
        return JsonSupport.getObjectMapper().writeValueAsString(result);
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
