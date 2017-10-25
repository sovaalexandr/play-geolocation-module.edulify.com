package com.edulify.modules.geolocation.providers;

import com.edulify.modules.geolocation.GeolocationProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import play.libs.ws.WSClient;

import java.util.concurrent.Executor;

public class MaxmindModule extends AbstractModule {

  @Override
  protected void configure() { }

  @Provides
  public GeolocationProvider geolocationProvider(WSClient wsClient, Config config, Executor wsExecutor) {
    return new MaxmindProvider(wsClient, config.getString("geolocation.maxmind.license"), wsExecutor);
  }
}
