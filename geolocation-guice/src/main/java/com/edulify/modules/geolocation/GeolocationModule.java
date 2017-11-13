package com.edulify.modules.geolocation;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.typesafe.config.Config;
import play.Environment;
import play.cache.AsyncCacheApi;

import javax.inject.Named;

public class GeolocationModule extends AbstractModule {

  private final Environment environment;
  private final Config config;

  public GeolocationModule(Environment environment, Config config)
  {
    this.environment = environment;
    this.config = config;
  }

  @Override
  protected void configure() { }

  @Named("CachedGeolocationProvider")
  @Provides
  public GeolocationProvider cachedGeolocationProvider(GeolocationProvider provider, AsyncCacheApi cacheApi)
  {
    return new CachedProvider(cacheApi, config.getInt("geolocation.cache.ttl"), provider);
  }
}
