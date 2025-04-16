package com.lb;

import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Setup;
import com.lb.application.models.TripAgentChatModel;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;

@Setup
public class TripAgencySetup implements ServiceSetup {

  // TODO find out how can I insert plain POJOs/Beans not being @Service or @Component
  //  more like @Autowired
  @Override
  public DependencyProvider createDependencyProvider() {
    String anthropicApiKey = System.getenv("ANTHROPIC_API_KEY");
    if (anthropicApiKey == null) {
      throw new RuntimeException("ANTHROPIC_API_KEY environment variable is not set");
    }
    var anthropicApi = new AnthropicApi(System.getenv("ANTHROPIC_API_KEY"));
    // TODO use the new API https://docs.spring.io/spring-ai/reference/api/tools-migration.html
    var chatModelOptions =
        AnthropicChatOptions.builder()
            .model("claude-3-7-sonnet-latest") // haiku wasn't 'clever' enough
            .temperature(0.4)
            .maxTokens(2000)
            .toolCallbacks()
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
