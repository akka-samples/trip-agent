package com.tripagent.ai.models;

import java.util.List;

import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.resolution.DelegatingToolCallbackResolver;

public class TripAgentChatModel extends AnthropicChatModel {

  private static final Logger logger = LoggerFactory.getLogger(TripAgentChatModel.class);

  public TripAgentChatModel(AnthropicApi anthropicApi, AnthropicChatOptions chatModelOptions) {
    super(
        anthropicApi,
        chatModelOptions,
        new DefaultToolCallingManager(
            ObservationRegistry.NOOP,
            new DelegatingToolCallbackResolver(List.of()),
            new DefaultToolExecutionExceptionProcessor(true)),
        RetryUtils.DEFAULT_RETRY_TEMPLATE,
        ObservationRegistry.NOOP);
  }
}
