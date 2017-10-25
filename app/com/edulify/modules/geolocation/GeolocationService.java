package com.edulify.modules.geolocation;

import play.libs.concurrent.HttpExecution;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

/**
 * Wrapper for {@link GeolocationProvider} to cache results.
 *
 * @deprecated Deprecated as of 2.2.0. Use {@link CachedProvider}.
 */
@Deprecated
public final class GeolocationService {

  private GeolocationProvider provider;
  private GeolocationCache cache;

  @Inject
  public GeolocationService(GeolocationProvider provider, GeolocationCache cache) {
    this.provider = provider;
    this.cache = cache;
  }

  public CompletionStage<Geolocation> getGeolocation(String ip) {
    final CompletionStage<Geolocation> stage = provider.get(ip);
    stage.thenAcceptAsync(cache::set, HttpExecution.defaultContext());

    return stage;
  }
}
