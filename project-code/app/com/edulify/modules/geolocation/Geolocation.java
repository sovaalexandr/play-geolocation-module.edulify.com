package com.edulify.modules.geolocation;

import static org.apache.commons.lang3.builder.ToStringBuilder.reflectionToString;

public class Geolocation {

  private String ip;
  private String countryCode;
  private String countryName;
  private String regionCode;
  private String regionName;
  private String city;
  private double latitude;
  private double longitude;
  private String timeZone;

  public Geolocation(String ip,
                     String countryCode,
                     String countryName,
                     String regionCode,
                     String regionName,
                     String city,
                     double latitude,
                     double longitude,
                     String timeZone) {
    this.ip = ip;
    this.countryCode = countryCode;
    this.countryName = countryName;
    this.regionCode  = regionCode;
    this.regionName  = regionName;
    this.city        = city;
    this.latitude    = latitude;
    this.longitude   = longitude;
    this.timeZone    = timeZone;
  }

  public String getIp() {
    return this.ip;
  }

  public String getCountryCode() {
    return this.countryCode;
  }

  public String getCountryName() {
    return this.countryName;
  }

  public String getRegionCode() {
    return this.regionCode;
  }

  public String getRegionName() {
    return this.regionName;
  }

  public String getCity() {
    return this.city;
  }

  public double getLatitude() {
    return this.latitude;
  }

  public double getLongitude() {
    return this.longitude;
  }

  public String getTimeZone() {
    return this.timeZone;
  }

  @Override
  public String toString() {
    return reflectionToString(this);
  }
}