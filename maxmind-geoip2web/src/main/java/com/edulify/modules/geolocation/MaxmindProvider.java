package com.edulify.modules.geolocation;

import play.libs.ws.StandaloneWSClient;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

public class MaxmindProvider implements GeolocationProvider {

  private final Executor threadToRunOn;

  private final StandaloneWSClient ws;

  private final String license;

  private final String baseUrl;

  MaxmindProvider(StandaloneWSClient ws, String license, Executor threadToRunOn, String urlFormat) {
    this.ws = ws;
    this.license = license;
    this.threadToRunOn = threadToRunOn;
    this.baseUrl = urlFormat;
  }

  public MaxmindProvider(StandaloneWSClient ws, String license, Executor threadToRunOn) {
    this(ws, license, threadToRunOn, "https://geoip.maxmind.com/a?l=%s");
  }

  @Override
  public CompletionStage<Geolocation> get(final String ip) {
    return ws.url(baseUrl) // WSRequest is not a value-object. Adding query parameters will modify it's state instead of
        .addQueryParameter("l", license) // creating a new instance. That is why we FORCED to store all request
        .addQueryParameter("i", ip) // parameters and re-creating request each time rather re-using same instance.
        .get()
        .thenApplyAsync(response -> {
          if (response.getStatus() != 200) return null;

          String body = response.getBody();
          if ("(null),IP_NOT_FOUND".equals(body)) return null;
          return body;
        }, threadToRunOn)
        .thenApplyAsync(body -> {
          if (body == null) return Geolocation.empty();
          return new Geolocation(ip, body);
        }, threadToRunOn);
  }
}
