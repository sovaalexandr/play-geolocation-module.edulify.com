package com.edulify.modules.geolocation.providers;

import com.edulify.modules.geolocation.Geolocation;
import com.edulify.modules.geolocation.GeolocationProvider;
import com.edulify.modules.geolocation.infrastructure.maxmind.geolite.GeoLite2;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Location;
import com.maxmind.geoip2.record.Subdivision;
import play.libs.concurrent.HttpExecution;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.CompletionStage;

@Singleton
public class GeoLiteProvider implements GeolocationProvider {
  private final GeoLite2 geoLite2;

  @Inject
  public GeoLiteProvider(@Named("geo-lite-2") GeoLite2 geoLite2) {
    this.geoLite2 = geoLite2;
  }

  @Override
  public CompletionStage<Geolocation> get(String ip) {

    return geoLite2.locateCityByIp(ip)
      .thenApplyAsync(cityResponse -> asGeolocation(cityResponse, ip), HttpExecution.defaultContext());
  }

  private Geolocation asGeolocation(CityResponse response, String ip) {
    if (null == response) {
      return null;
    }

    Country country = response.getCountry();
    City city = response.getCity();
    Subdivision subdivision = response.getMostSpecificSubdivision();
    Location location = response.getLocation();

    return new Geolocation(
      ip,
      country.getIsoCode(),
      country.getName(),
      subdivision.getIsoCode(),
      subdivision.getName(),
      city.getName(),
      location.getLatitude(),
      location.getLongitude(),
      location.getTimeZone()
    );
  }
}
