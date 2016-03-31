package com.edulify.modules.geolocation.infrastructure.maxmind.geolite;

import com.maxmind.geoip2.model.CityResponse;

import java.util.concurrent.CompletionStage;

public interface GeoLite2 {
  CompletionStage<CityResponse> locateCityByIp(String ipAddress);
}
