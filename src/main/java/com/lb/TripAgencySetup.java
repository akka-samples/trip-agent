package com.lb;

import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Setup;
import com.lb.ai.models.TripAgentChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;

@Setup
public class TripAgencySetup implements ServiceSetup {

  public TripAgencySetup() {
    String anthropicApiKey = System.getenv("ANTHROPIC_API_KEY");
    if (anthropicApiKey == null) {
      throw new RuntimeException(
          "ANTHROPIC_API_KEY environment variable is not set. See https://docs.anthropic.com/en/api/getting-started");
    }
  }

  @Override
  public DependencyProvider createDependencyProvider() {
    var anthropicApi = new AnthropicApi(System.getenv("ANTHROPIC_API_KEY"));
    var chatModelOptions =
        AnthropicChatOptions.builder()
            .model("claude-3-7-sonnet-latest")
            // `Claude 3.5 Haiku` wasn't 'clever' enough. See https://www.anthropic.com/pricing#api
            .temperature(0.4) // Higher temperatures lead to more creative and diverse outputs.
            .maxTokens(3000) // To handle JSON output from the @tools ~ 9k-12k characters total.
            .build();
    final var tripAgentChatModel = new TripAgentChatModel(anthropicApi, chatModelOptions);

    return new DependencyProvider() {
      @Override
      public <T> T getDependency(Class<T> aClass) {

        if (aClass == TripAgentChatModel.class) {
          return aClass.cast(tripAgentChatModel);
        } else {
          throw new IllegalArgumentException("Unsupported dependency type: " + aClass);
        }
      }
    };
  }
}
