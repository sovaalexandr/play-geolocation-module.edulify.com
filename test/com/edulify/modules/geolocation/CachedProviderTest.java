package com.edulify.modules.geolocation;

import akka.Done;
import org.junit.Test;
import play.cache.AsyncCacheApi;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class CachedProviderTest {
  @Test
  public void get() throws Exception {

    GeolocationProvider provider = mock(GeolocationProvider.class);
    when(provider.get("any address")).thenReturn(CompletableFuture.completedFuture(Geolocation.empty()));

    AsyncCacheApi cacheApi = new StubCacheApi();

    GeolocationProvider target = new CachedProvider(cacheApi, 100, provider);

    target.get("any address").toCompletableFuture().get(1, TimeUnit.SECONDS);
    target.get("any address").toCompletableFuture().get(1, TimeUnit.SECONDS);
    verify(provider, times(1)).get("any address");
  }

  private class StubCacheApi implements AsyncCacheApi {

    private Map<String, String> cache = new HashMap<>(1);

    @SuppressWarnings("unchecked")
    @Override
    public <T> CompletionStage<T> getOrElseUpdate(String key, Callable<CompletionStage<T>> block, int expiration) {
      if (cache.containsKey(key)) {
        return CompletableFuture.completedFuture((T) Geolocation.empty()); // We test only method invocations, not method call results so we don't need any value here.
      }
      cache.put(key, key);
      // See Scala.asScalaWithFuture(block)
      try {
        return block.call();
      } catch (RuntimeException | Error e) {
        throw e;
      } catch (Throwable t) {
        throw new RuntimeException(t);
      }
    }

    // ---- Rest are stubs. Just to implement interface. ISP violation.

    @Override
    public <T> CompletionStage<T> get(String key) {
      return null; // Testing white box. No need to implement all methods.
    }

    @Override
    public <T> CompletionStage<T> getOrElseUpdate(
      String key, Callable<CompletionStage<T>> block
    ) {
      return null; // Testing white box. No need to implement all methods.
    }

    @Override
    public CompletionStage<Done> set(String key, Object value, int expiration) {
      return null; // Testing white box. No need to implement all methods.
    }

    @Override
    public CompletionStage<Done> set(String key, Object value) {
      return null; // Testing white box. No need to implement all methods.
    }

    @Override
    public CompletionStage<Done> remove(String key) {
      return null; // Testing white box. No need to implement all methods.
    }

    @Override
    public CompletionStage<Done> removeAll() {
      return null; // Testing white box. No need to implement all methods.
    }
  }
}
