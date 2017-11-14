package com.edulify.modules.geolocation;

import akka.actor.ActorRef;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Country;
import com.sovaalexandr.maxmind.geoip2.City;

import java.net.InetAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.pattern.Patterns.ask;
import static java.util.Optional.ofNullable;
import static scala.compat.java8.FutureConverters.toJava;

public class MaxmindDBProvider implements GeolocationProvider
{
  private final ActorRef geolocation;

  public MaxmindDBProvider(ActorRef geolocation)
  {
    this.geolocation = geolocation;
  }

  @Override
  public CompletionStage<Geolocation> get(String ip)
  {
    try {
      return toJava(ask(geolocation, City.apply(InetAddress.getByName(ip)), 5000L))
        .thenApply(this::toCityResponse)
        .thenApply(city -> asGeolocation(city, ip));
    } catch (Throwable e) {
      final CompletableFuture<Geolocation> future = new CompletableFuture<>();
      future.completeExceptionally(e);
      return future;
    }
  }

  private CityResponse toCityResponse(Object response) {
    try {
      return (CityResponse)response;
    } catch (ClassCastException e) {
      throw new RuntimeException(e);
    }
  }

  private Geolocation asGeolocation(CityResponse response, String ip) {
    if (null == response) {
      return Geolocation.empty();
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
