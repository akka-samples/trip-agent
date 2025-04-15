package com.lb;

import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Setup;
import com.lb.application.models.FlightAgentChatModel;
import com.lb.application.models.TripAgentChatModel;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.springframework.ai.anthropic.AnthropicChatOptions;
import org.springframework.ai.anthropic.api.AnthropicApi;

@Setup
public class KojakSetup implements ServiceSetup {

  private final Config config = ConfigFactory.load();

  // TODO find out how can I insert plain POJOs/Beans not being @Service or @Component
  //  more like @Autowired
  @Override
  public DependencyProvider createDependencyProvider() {
    var anthropicApi = new AnthropicApi(System.getenv("ANTHROPIC_API_KEY"));
    // TODO use the new API https://docs.spring.io/spring-ai/reference/api/tools-migration.html
    var chatModelOptions =
        AnthropicChatOptions.builder()
            .model("claude-3-opus-20240229")
            .temperature(0.4)
            .maxTokens(2000)
            .toolCallbacks()
            .build();
    final var tripAgentChatModel = new TripAgentChatModel(anthropicApi, chatModelOptions);
    final var flightAgentChatModel = new FlightAgentChatModel(anthropicApi, chatModelOptions);
    // TODO add accomodation chat model

    return new DependencyProvider() {
      @Override
      public <T> T getDependency(Class<T> aClass) {
        if (aClass == TripAgentChatModel.class) {
          return aClass.cast(tripAgentChatModel);
        }
        if (aClass == FlightAgentChatModel.class) {
          return aClass.cast(flightAgentChatModel);
        } else {
          throw new IllegalArgumentException("Unsupported dependency type: " + aClass);
        }
      }
    };
  }
}
