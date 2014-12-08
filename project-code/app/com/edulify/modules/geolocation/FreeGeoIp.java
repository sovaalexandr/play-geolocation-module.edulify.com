package com.edulify.modules.geolocation;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.Play;
import play.libs.ws.WSResponse;
import play.mvc.Http;

import static play.Logger.ALogger;
import static play.libs.F.Promise;

public class FreeGeoIp implements ClientInterface {
  private static final String IP_NOT_FOUND = "Not Found";

  private final GeolocationFactory factory;
  private       RequestBuilder     builder;
  private       ALogger            logger;
  private       boolean            debug;

  public FreeGeoIp() {
    this(new GeolocationFactory());
  }

  public FreeGeoIp(GeolocationFactory factory) {
    this(factory, new RequestBuilder());
  }

  public FreeGeoIp(GeolocationFactory factory, RequestBuilder builder) {
    this(factory, builder, Logger.of("geolocation"));
  }

  public FreeGeoIp(GeolocationFactory factory, RequestBuilder builder, ALogger logger) {
    this.factory = factory;
    this.logger = logger;
    this.builder = builder;
    this.debug = Play.application().configuration().getBoolean("geolocation.debug", false);
  }

  public Promise<Geolocation> getGeolocation(String ip) throws InvalidAddressException {
    if (debug) {
      logger.debug(String.format("requesting %s using freegeoip...", ip));
    }

    return builder.buildRequest(String.format("http://freegeoip.net/json/%s", ip))
            .get()
            .map(response -> mapResponse(response, ip));
  }

  private Geolocation mapResponse(WSResponse response, String ip) {
    if (Http.Status.OK != response.getStatus()) {
      throw new ServiceErrorException(
              String.format("Invalid service response: %s", response.getStatusText())
      );
    }

    String responseBody = response.getBody().trim();

    if (debug) {
      logger.debug(String.format("response: %s", responseBody));
    }

    if (IP_NOT_FOUND.equals(responseBody)) {
      throw new InvalidAddressException(String.format("Invalid address: %s", ip));
    }

    JsonNode jsonResponse = response.asJson();

    JsonNode jsonIp = jsonResponse.get("ip");
    JsonNode jsonCountryCode = jsonResponse.get("country_code");
    JsonNode jsonCountryName = jsonResponse.get("country_name");
    JsonNode jsonRegionCode = jsonResponse.get("region_code");
    JsonNode jsonRegionName = jsonResponse.get("region_name");
    JsonNode jsonCity = jsonResponse.get("city");
    JsonNode jsonLatitude = jsonResponse.get("latitude");
    JsonNode jsonLongitude = jsonResponse.get("longitude");

    if (jsonIp == null ||
            jsonCountryCode == null ||
            jsonCountryName == null ||
            jsonRegionCode == null ||
            jsonRegionName == null ||
            jsonCity == null ||
            jsonLatitude == null ||
            jsonLongitude == null) {
      return null;
    }

    return factory.create(jsonIp.asText(),
            jsonCountryCode.asText(),
            jsonCountryName.asText(),
            jsonRegionCode.asText(),
            jsonRegionName.asText(),
            jsonCity.asText(),
            jsonLatitude.asDouble(),
            jsonLongitude.asDouble());
  }
}
