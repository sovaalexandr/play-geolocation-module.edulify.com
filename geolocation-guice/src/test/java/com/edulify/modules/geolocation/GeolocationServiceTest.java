package com.edulify.modules.geolocation;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

/**
 * @deprecated Deprecated as of 2.2.0. Source should be removed.
 */
@Deprecated
public class GeolocationServiceTest {

  @Test
  public void shouldGetAGeolocationForAGivenIp() throws Exception {

    final GeolocationProvider provider = mock(GeolocationProvider.class);

    String ipAddress = "192.30.252.129";
    String countryCode = "BR";
    final Geolocation geolocation = new Geolocation(ipAddress, countryCode);
    when(provider.get(ipAddress)).thenReturn(CompletableFuture.completedFuture(geolocation));
    final GeolocationCache cache = mock(GeolocationCache.class);

    GeolocationService service = new GeolocationService(provider, cache);
    Geolocation target = service.getGeolocation(ipAddress).toCompletableFuture().get(1, TimeUnit.SECONDS);

    verify(cache, timeout(100).times(1)).set(geolocation);

    Assert.assertSame(target, geolocation);
  }
}
