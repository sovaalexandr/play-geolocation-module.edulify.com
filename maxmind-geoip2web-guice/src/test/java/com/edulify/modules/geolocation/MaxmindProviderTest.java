package com.edulify.modules.geolocation;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.ws.StandaloneWSClient;
import play.libs.ws.StandaloneWSRequest;
import play.libs.ws.WSClient;
import play.mvc.Http;
import play.routing.RoutingDsl;
import play.server.Server;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static play.mvc.Results.ok;

public class MaxmindProviderTest {

  private Server server;

  private StandaloneWSClient ws;

  private GeolocationProvider maxmindProvider;
  private ExecutorService executor;

  @Before
  public void setUp() throws Exception {
    server = Server.forRouter((components) -> RoutingDsl.fromComponents(components)
      .GET("/ip").routeTo(() -> {
        final Http.Context context = Http.Context.current.get();// Deprecated hack because Java can't do string interpolation.

        final String countryCode = ImmutableMap.of("ip1", "country1", "ip2", "country2").entrySet().stream()
          .filter(entry -> entry.getKey().equals(context.request().getQueryString("i")))
          .map(Map.Entry::getValue)
          .findAny().orElse("(null),IP_NOT_FOUND");

        return ok(countryCode);
      })
      .build());

    WSClient wsClient = play.test.WSTestClient.newClient(server.httpPort());
    ws = new StandaloneWSClient()
    {
      @Override
      public Object getUnderlying()
      {
        return wsClient.getUnderlying();
      }

      @Override
      public StandaloneWSRequest url(String url)
      {
        return wsClient.url(url);
      }

      @Override
      public void close() throws IOException
      {
        wsClient.close();
      }
    };

    executor = Executors.newSingleThreadExecutor();
    maxmindProvider = new MaxmindProvider(ws, "any test string", executor, "/ip?l=%s");
  }

  @After
  public void tearDown() throws Exception {
    try {
      ws.close();
    }
    finally {
      executor.shutdown();
      server.stop();
    }
  }

  @Test
  public void get() throws Exception {
    Geolocation geolocation = maxmindProvider.get("ip1").toCompletableFuture().get(10, TimeUnit.SECONDS);
    assertEquals("country1", geolocation.getCountryCode());

    geolocation = maxmindProvider.get("ip2").toCompletableFuture().get(10, TimeUnit.SECONDS);
    assertEquals("country2", geolocation.getCountryCode());

    geolocation = maxmindProvider.get("not at list").toCompletableFuture().get(10, TimeUnit.SECONDS);
    assertEquals("", geolocation.getCountryCode());
  }

}
