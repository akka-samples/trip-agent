package com.tripagent;

import java.time.Duration;

import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Setup;
import com.tripagent.ai.models.TripAgentChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Setup
public class TripAgentSetup implements ServiceSetup {

  public TripAgentSetup() {
    String anthropicApiKey = System.getenv("ANTHROPIC_API_KEY");
    if (anthropicApiKey == null) {
      throw new RuntimeException(
          "ANTHROPIC_API_KEY environment variable is not set. See https://docs.anthropic.com/en/api/getting-started");
    }
  }

  @Override
  public DependencyProvider createDependencyProvider() {
    // Extending the read time to allow LLM to process request
    SimpleClientHttpRequestFactory simpleClientHttpRequestFactory =
        new SimpleClientHttpRequestFactory();
    simpleClientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(120));
    RestClient.Builder clientBuilder =
        RestClient.builder().requestFactory(simpleClientHttpRequestFactory);
    var anthropicApi =
        new AnthropicApi(
            AnthropicApi.DEFAULT_BASE_URL,
            System.getenv("ANTHROPIC_API_KEY"),
            AnthropicApi.DEFAULT_ANTHROPIC_VERSION,
            clientBuilder,
            WebClient.builder(),
            RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER);
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
