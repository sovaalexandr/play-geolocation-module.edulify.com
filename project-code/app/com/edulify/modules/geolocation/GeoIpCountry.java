package com.edulify.modules.geolocation;

import play.Configuration;
import play.Logger;
import play.Play;
import play.libs.ws.WSResponse;
import play.mvc.Http;

import static play.Logger.ALogger;
import static play.libs.F.Promise;

public class GeoIpCountry implements ClientInterface {
  private static final int    EXPECTED_BODY_LENGTH = 2;
  private static final String IP_NOT_FOUND         = "(null),IP_NOT_FOUND";

  private final GeolocationFactory factory;
  private       String             maxmindLicense;
  private       boolean            debug;
  private       ALogger            logger;
  private       RequestBuilder     builder;

  public GeoIpCountry() {
    this(new GeolocationFactory());
  }

  public GeoIpCountry(String maxmindLicense) {
    this();
    this.maxmindLicense = maxmindLicense;
  }

  public GeoIpCountry(GeolocationFactory factory) {
    this(factory, new RequestBuilder());
  }

  public GeoIpCountry(GeolocationFactory factory, RequestBuilder builder) {
    this(factory, builder, Logger.of("geolocation"));
  }

  public GeoIpCountry(GeolocationFactory factory, RequestBuilder builder, ALogger logger) {
    this.factory = factory;
    this.builder = builder;

    Configuration configuration = Play.application().configuration();
    maxmindLicense = configuration.getString("geolocation.maxmind_license", "");
    debug = configuration.getBoolean("geolocation.debug", false);
    this.logger = logger;
  }

  @Override
  public Promise<Geolocation> getGeolocation(String ip) {
    String url = String.format("https://geoip.maxmind.com/a?l=%s&i=%s", maxmindLicense, ip);

    if (debug) {
      logger.debug(String.format("requesting %s using geoip_country...", ip));
    }

    return builder.buildRequest(url).get().map(response -> mapResponse(response, ip));
  }

  private Geolocation mapResponse(WSResponse response, String ip) throws ServiceErrorException {
    if (Http.Status.OK != response.getStatus()) {
      throw new ServiceErrorException(String.format("Invalid service response: %s", response.getStatusText()));
    }

    String responseBody = response.getBody().trim();
    if (debug) {
      logger.debug(String.format("response: %s", responseBody));
    }

    if (IP_NOT_FOUND.equals(responseBody)) {
      throw new InvalidAddressException(String.format("Invalid address: %s", ip));
    }

    if (EXPECTED_BODY_LENGTH == responseBody.length()) {
      return factory.create(ip, responseBody);
    }
    throw new ServiceErrorException(String.format("Unknown service response: %s", responseBody));
  }
}