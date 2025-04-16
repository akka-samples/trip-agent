package com.lb.ai.models;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;

public class TripAgentChatModel extends AnthropicChatModel {
  public TripAgentChatModel(AnthropicApi anthropicApi, AnthropicChatOptions chatModelOptions) {
    super(anthropicApi, chatModelOptions);
  }
}
