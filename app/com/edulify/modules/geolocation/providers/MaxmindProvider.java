package com.edulify.modules.geolocation.providers;

import com.edulify.modules.geolocation.Geolocation;
import com.edulify.modules.geolocation.GeolocationProvider;
import play.Configuration;
import play.libs.concurrent.HttpExecution;
import play.libs.ws.WSClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletionStage;

@Singleton
public class MaxmindProvider implements GeolocationProvider {

  private String license;

  private WSClient ws;

  @Inject
  public MaxmindProvider(WSClient ws, Configuration configuration) {
    this.ws = ws;
    this.license = configuration.getString("geolocation.maxmind.license");
  }

  @Override
  public CompletionStage<Geolocation> get(final String ip) {
    String url = String.format("https://geoip.maxmind.com/a?l=%s&i=%s", license, ip);
    return ws.url(url)
        .get()
        .thenApplyAsync(response -> {
          if (response.getStatus() != 200) return null;

          String body = response.getBody();
          if ("(null),IP_NOT_FOUND".equals(body)) return null;
          return body;
        }, HttpExecution.defaultContext())
        .thenApplyAsync(body -> {
          if (body == null) return Geolocation.empty();
          return new Geolocation(ip, body);
        }, HttpExecution.defaultContext());
  }
}
