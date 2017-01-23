package com.edulify.modules.geolocation.providers;

import com.edulify.modules.geolocation.Geolocation;
import com.edulify.modules.geolocation.GeolocationBuilder;
import com.edulify.modules.geolocation.GeolocationProvider;
import com.edulify.modules.geolocation.infrastructure.maxmind.geolite.GeoLite2;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Country;
import play.libs.concurrent.HttpExecution;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.CompletionStage;

import static java.util.Optional.ofNullable;

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

    final GeolocationBuilder geolocationBuilder = new GeolocationBuilder(ip);
    final Country locatedCountry = ofNullable(response.getCountry()).filter(country -> null != country.getIsoCode())
      .orElseGet(response::getRegisteredCountry);
    ofNullable(locatedCountry)
      .ifPresent(country -> geolocationBuilder.withIsoCode(country.getIsoCode()).withCountryName(country.getName()));
    ofNullable(response.getCity())
      .ifPresent(city -> geolocationBuilder.withCityName(city.getName()));
    ofNullable(response.getMostSpecificSubdivision())
      .ifPresent(subdivision -> geolocationBuilder.withRegionCode(subdivision.getIsoCode()).withRegionName(subdivision.getName()));
    ofNullable(response.getLocation())
      .ifPresent(location -> geolocationBuilder.withLatitude(location.getLatitude())
        .withLongitude(location.getLongitude()).withTimeZone(location.getTimeZone())
      );

    return geolocationBuilder.build();
  }
}
