package com.edulify.modules.geolocation;

import static java.util.Optional.ofNullable;

public class GeolocationBuilder {
  private final String ip;

  private String countryCode;
  private String countryName;

  private String regionCode;
  private String regionName;

  private String city;

  private String timeZone;
  private double latitude;
  private double longitude;

  public GeolocationBuilder(String ip) {
    this.ip = ip;
  }

  public GeolocationBuilder withIsoCode(String isoCode) {
    countryCode = isoCode;
    return this;
  }

  public GeolocationBuilder withCountryName(String name) {
    countryName = name;
    return this;
  }

  public GeolocationBuilder withCityName(String name) {
    city = name;
    return this;
  }

  public GeolocationBuilder withRegionCode(String isoCode) {
    regionCode = isoCode;
    return this;
  }

  public GeolocationBuilder withRegionName(String name) {
    regionName = name;
    return this;
  }

  public GeolocationBuilder withLatitude(Double latitude) {
    this.latitude = ofNullable(latitude).orElse(0.);
    return this;
  }

  public GeolocationBuilder withLongitude(Double longitude) {
    this.longitude = ofNullable(longitude).orElse(0.);
    return this;
  }

  public GeolocationBuilder withTimeZone(String timeZone) {
    this.timeZone = timeZone;
    return this;
  }

  public Geolocation build() {
    return new Geolocation(ip, countryCode, countryName, regionCode, regionName, city, latitude, longitude, timeZone);
  }
}
