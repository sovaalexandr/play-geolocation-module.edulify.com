package com.edulify.modules.geolocation;

import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.libs.ws.StandaloneWSClient;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

public class FreegeoipProvider implements GeolocationProvider {

  private StandaloneWSClient ws;

  private final String urlFormat;

  private final Executor threadToRunOn;

  public FreegeoipProvider(StandaloneWSClient ws, Executor threadToRunOn, String urlFormat) {
    this.ws = ws;
    this.urlFormat = urlFormat;
    this.threadToRunOn = threadToRunOn;
  }

  FreegeoipProvider(StandaloneWSClient ws, Executor threadToRunOn) {
    this(ws, threadToRunOn, "http://freegeoip.net/json/%s");
  }

  @Override
  public CompletionStage<Geolocation> get(String ip) {
    String url = String.format(urlFormat, ip);
    return ws.url(url)
        .get()
        .thenApplyAsync(response -> {
          if (response.getStatus() != 200) return null;
          if (response.getBody().contains("not found")) return null;
          return Json.parse(response.getBodyAsBytes().toArray());
        }, threadToRunOn)
        .thenApplyAsync(json -> {
          if (json == null) return Geolocation.empty();
          return asGeolocation(json);
        }, threadToRunOn);
  }

  private Geolocation asGeolocation(JsonNode json) {
    JsonNode jsonIp          = json.get("ip");
    JsonNode jsonCountryCode = json.get("country_code");
    JsonNode jsonCountryName = json.get("country_name");
    JsonNode jsonRegionCode  = json.get("region_code");
    JsonNode jsonRegionName  = json.get("region_name");
    JsonNode jsonCity        = json.get("city");
    JsonNode jsonLatitude    = json.get("latitude");
    JsonNode jsonLongitude   = json.get("longitude");
    JsonNode jsonTimeZone    = json.get("time_zone");

    if (jsonIp          == null ||
        jsonCountryCode == null ||
        jsonCountryName == null ||
        jsonRegionCode  == null ||
        jsonRegionName  == null ||
        jsonCity        == null ||
        jsonLatitude    == null ||
        jsonLongitude   == null ||
        jsonTimeZone    == null) {
      return Geolocation.empty();
    }

    return new Geolocation(
        jsonIp.asText(),
        jsonCountryCode.asText(),
        jsonCountryName.asText(),
        jsonRegionCode.asText(),
        jsonRegionName.asText(),
        jsonCity.asText(),
        jsonLatitude.asDouble(),
        jsonLongitude.asDouble(),
        jsonTimeZone.asText()
    );
  }
}