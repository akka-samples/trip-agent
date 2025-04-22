package com.lb.ai.models;

import io.micrometer.observation.ObservationRegistry;
import java.util.List;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.ai.tool.execution.DefaultToolExecutionExceptionProcessor;
import org.springframework.ai.tool.resolution.DelegatingToolCallbackResolver;

public class TripAgentChatModel extends AnthropicChatModel {
  public TripAgentChatModel(AnthropicApi anthropicApi, AnthropicChatOptions chatModelOptions) {
    super(
        anthropicApi,
        chatModelOptions,
        new DefaultToolCallingManager(
            ObservationRegistry.NOOP,
            new DelegatingToolCallbackResolver(List.of()),
            DefaultToolExecutionExceptionProcessor.builder().build()),
        RetryUtils.DEFAULT_RETRY_TEMPLATE,
        ObservationRegistry.NOOP);
  }
}
