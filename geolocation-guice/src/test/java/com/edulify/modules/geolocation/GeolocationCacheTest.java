package com.edulify.modules.geolocation;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import play.Configuration;
import play.cache.CacheApi;

import static org.mockito.Mockito.*;

/**
 * @deprecated Deprecated as of 2.2.0. Source should be removed.
 */
@Deprecated
public class GeolocationCacheTest {

  private final String ipAddress = "192.30.252.129";
  private final String countryCode = "BR";

  @Test
  public void shouldAddGeolocationToCacheWhenCacheIsOn() {
    final CacheApi cacheApi = mock(CacheApi.class);
    final GeolocationCache geolocationCache = new GeolocationCache(
      new Configuration(ImmutableMap.of(
        "geolocation.cache.on", true,
        "geolocation.cache.ttl", 5000L
      )),
      cacheApi
    );

    final Geolocation geolocation = new Geolocation(ipAddress, countryCode);
    geolocationCache.set(geolocation);

    verify(cacheApi).set(anyString(), eq(geolocation), eq(5000));
  }

  @Test
  public void shouldNotAddGeolocationToCacheWhenCacheIsOff() {
    final CacheApi cacheApi = mock(CacheApi.class);
    final GeolocationCache geolocationCache = new GeolocationCache(
      new Configuration(ImmutableMap.of(
        "geolocation.cache.on", false,
        "geolocation.cache.ttl", 5000L
      )),
      cacheApi
    );

    final Geolocation geolocation = new Geolocation(ipAddress, countryCode);
    geolocationCache.set(geolocation);

    verify(cacheApi, never()).set(anyString(), eq(geolocation), eq(5000));
  }
}
