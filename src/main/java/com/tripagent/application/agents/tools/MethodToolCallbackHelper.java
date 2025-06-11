package com.tripagent.application.agents.tools;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;

public class MethodToolCallbackHelper {

  private static final Logger log = LoggerFactory.getLogger(MethodToolCallbackHelper.class);

  private static final AkkaToolCallResultConverter converter = new AkkaToolCallResultConverter();

  public static MethodToolCallback getMethodToolCallback(Object tool, String methodName) {
    try {
      Method method = tool.getClass().getMethod(methodName);
      String description = method.getAnnotation(Tool.class).description();
      return MethodToolCallback.builder()
          .toolCallResultConverter(converter)
          .toolObject(tool)
          .toolDefinition(
              new DefaultToolDefinition(
                  tool.getClass().getSimpleName(),
                  description,
                  JsonSchemaGenerator.generateForMethodInput(method)))
          .toolMethod(method)
          .build();
    } catch (NoSuchMethodException e) {
      log.error("Failed to create tool for {}: ", methodName, e);
      throw new RuntimeException(e);
    }
  }
}
