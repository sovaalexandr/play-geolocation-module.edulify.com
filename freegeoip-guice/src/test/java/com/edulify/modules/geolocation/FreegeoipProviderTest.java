package com.edulify.modules.geolocation;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.ws.StandaloneWSClient;
import play.libs.ws.StandaloneWSRequest;
import play.libs.ws.WSClient;
import play.routing.RoutingDsl;
import play.server.Server;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static play.mvc.Results.ok;

public class FreegeoipProviderTest {

  private Server server;

  private StandaloneWSClient ws;

  private GeolocationProvider freeGeoIpProvider;
  private ExecutorService executor;

  @Before
  public void setUp() throws Exception {
    server = Server.forRouter((components) -> RoutingDsl.fromComponents(components)
      .GET("/ip/:ipAddress").routeTo((String ipAddress) -> {

        final String countryCode = ImmutableMap.of(
          "185.129.62.62", "{\"ip\":\"185.129.62.62\",\"country_code\":\"DK\",\"country_name\":\"Denmark\",\"region_code\":\"\",\"region_name\":\"\",\"city\":\"\",\"zip_code\":\"\",\"time_zone\":\"Europe/Copenhagen\",\"latitude\":55.7123,\"longitude\":12.0564,\"metro_code\":0}",
          "88.85.80.69", "{\"ip\":\"88.85.80.69\",\"country_code\":\"NL\",\"country_name\":\"Netherlands\",\"region_code\":\"\",\"region_name\":\"\",\"city\":\"\",\"zip_code\":\"\",\"time_zone\":\"Europe/Amsterdam\",\"latitude\":52.3824,\"longitude\":4.8995,\"metro_code\":0}")
          .entrySet().stream()
          .filter(entry -> entry.getKey().equals(ipAddress))
          .map(Map.Entry::getValue)
          .findAny().orElse("{\"ip\":\""+ipAddress+"\",\"country_code\":\"\",\"country_name\":\"\",\"region_code\":\"\",\"region_name\":\"\",\"city\":\"\",\"zip_code\":\"\",\"time_zone\":\"\",\"latitude\":0,\"longitude\":0,\"metro_code\":0}");

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
    freeGeoIpProvider = new FreegeoipProvider(ws, executor, "/ip/%s");
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
    Geolocation geolocation = freeGeoIpProvider.get("185.129.62.62").toCompletableFuture().get(10, TimeUnit.SECONDS);
    assertEquals("DK", geolocation.getCountryCode());

    geolocation = freeGeoIpProvider.get("88.85.80.69").toCompletableFuture().get(10, TimeUnit.SECONDS);
    assertEquals("NL", geolocation.getCountryCode());

    geolocation = freeGeoIpProvider.get("172.16.1.5").toCompletableFuture().get(10, TimeUnit.SECONDS);
    assertEquals("", geolocation.getCountryCode());
  }
}
