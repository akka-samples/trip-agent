package com.lb.application.models;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;

public class FlightAgentChatModel extends AnthropicChatModel {
  public FlightAgentChatModel(AnthropicApi anthropicApi, AnthropicChatOptions chatModelOptions) {
    super(anthropicApi, chatModelOptions);
  }
}
