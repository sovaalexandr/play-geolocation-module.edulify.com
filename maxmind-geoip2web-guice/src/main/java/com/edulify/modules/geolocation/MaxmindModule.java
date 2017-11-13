package com.edulify.modules.geolocation;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import play.Environment;
import play.libs.ws.StandaloneWSClient;

import java.util.concurrent.Executor;

public class MaxmindModule extends AbstractModule {

  private final Environment environment;
  private final Config config;

  public MaxmindModule(Environment environment, Config config)
  {
    this.environment = environment;
    this.config = config;
  }

  @Override
  protected void configure() {
    install(new GeolocationModule(environment, config));
  }

  @Provides
  public GeolocationProvider geolocationProvider(StandaloneWSClient wsClient, Executor wsExecutor) {
    return new MaxmindProvider(wsClient, config.getString("geolocation.maxmind.license"), wsExecutor);
  }
}
