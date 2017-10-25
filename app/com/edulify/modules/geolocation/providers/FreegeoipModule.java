package com.edulify.modules.geolocation.providers;

import com.edulify.modules.geolocation.GeolocationProvider;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import play.libs.ws.WSClient;

import java.util.concurrent.Executor;

public class FreegeoipModule extends AbstractModule {

  @Override
  protected void configure() {  }

  @Provides
  public GeolocationProvider geolocationProvider(WSClient wsClient, Executor wsExecutor) {
    return new FreegeoipProvider(wsClient, wsExecutor);
  }
}
