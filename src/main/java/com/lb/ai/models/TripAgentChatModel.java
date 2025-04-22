package com.lb.ai.models;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.model.tool.DefaultToolCallingManager;
import org.springframework.ai.model.tool.LegacyToolCallingManager;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.retry.RetryUtils;

import java.util.List;

public class TripAgentChatModel extends AnthropicChatModel {
  public TripAgentChatModel(AnthropicApi anthropicApi, AnthropicChatOptions chatModelOptions) {
    // TODO use latest
    super(anthropicApi, chatModelOptions,  RetryUtils.DEFAULT_RETRY_TEMPLATE, null, List.of(), ObservationRegistry.NOOP);
  }
}
