package com.edulify.modules.geolocation;

import play.cache.AsyncCacheApi;

import java.util.concurrent.CompletionStage;

public class CachedProvider implements GeolocationProvider
{
  private final AsyncCacheApi cache;

  private final int timeToLive;

  private final GeolocationProvider provider;

  public CachedProvider(AsyncCacheApi cache, int timeToLive, GeolocationProvider provider) {
    this.cache = cache;
    this.timeToLive = timeToLive;
    this.provider = provider;
  }

  @Override
  public CompletionStage<Geolocation> get(String ip) {
    return  cache.getOrElseUpdate(ip, () -> provider.get(ip), timeToLive);
  }
}
