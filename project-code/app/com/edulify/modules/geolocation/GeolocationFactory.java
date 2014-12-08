package com.edulify.modules.geolocation;

/**
 * Created by sovaalexandr
 */
public class GeolocationFactory {
  public Geolocation create(String ip, String countryCode) {
    return create(ip, countryCode, null, null, null, null, 0.0, 0.0);
  }

  public Geolocation create(String ip,
                            String countryCode,
                            String countryName,
                            String regionCode,
                            String regionName,
                            String city,
                            double latitude,
                            double longitude) {
    return new Geolocation(ip, countryCode, countryName, regionCode, regionName, city, latitude, longitude);
  }
}
