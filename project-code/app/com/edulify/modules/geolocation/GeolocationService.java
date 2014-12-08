package com.edulify.modules.geolocation;

import play.Play;
import play.cache.Cache;

import java.util.HashMap;

import static play.Logger.ALogger;
import static play.libs.F.Promise;
import static play.libs.F.Promise.promise;
import static play.libs.F.Promise.pure;

public class GeolocationService {

  private static final long DEFAULT_TIMEOUT = 5000l;

  public enum Source {
    FREEGEOIP,
    GEOIP_COUNTRY
  }

  protected     boolean useCache = Play.application().configuration().getBoolean("geolocation.useCache", true);
  protected     int     cacheTTL = Play.application().configuration().getInt("geolocation.cacheTTL", 3600);

  private final  HashMap<String, ClientInterface> clientsMap = new HashMap<>(1);
  private static ALogger                          logger     = play.Logger.of("geolocation");
  private static GeolocationService               instance   = new GeolocationService();

  public static void useCache(boolean useCache) {
    instance.switchCache(useCache);
  }

  public void switchCache(boolean useCache) {
    this.useCache = useCache;
  }

  public static void setCacheTime(int seconds) {
    instance.setCacheTimeout(seconds);
  }

  public void setCacheTimeout(int seconds) {
    this.cacheTTL = seconds;
  }

  public static void setSource(Source source) {
    try {
      instance.addClient(source.toString());
    } catch (InvalidClientException e) {
      throw new Error(e);
    }
  }

  public static void setMaxmindLicense(String license) {
    instance.addClient(Source.GEOIP_COUNTRY.toString(), new GeoIpCountry(license));
  }

  public static Geolocation getGeolocation(String ip) {
    return instance.getGeolocation(ip, Play.application().configuration().getString("geolocation.source", "FREEGEOIP"))
            .get(DEFAULT_TIMEOUT);
  }

  public static Geolocation getGeolocation(String ip, Source source) {
    return instance.getGeolocation(ip, source.toString()).get(DEFAULT_TIMEOUT);
  }

  public Promise<Geolocation> getGeolocation(String ip, String key) {
    return getGeolocation(ip, clientsMap.get(key));
  }

  public Promise<Geolocation> getGeolocation(String ip, ClientInterface source) {
    String cacheKey = String.format("geolocation-cache-%s", ip);
    return promise(() -> useCache ? Cache.get(cacheKey) : null)
            .flatMap(geo -> null == geo ? source.getGeolocation(ip) : pure((Geolocation) geo))
            .transform(
                    geo -> {
                      if (useCache) {
                        Cache.set(cacheKey, geo, cacheTTL);
                      }
                      return (Geolocation) geo;
                    },
                    ex -> {
                      logger.error("Exception ", ex);
                      return null;
                    }
            );
  }

  public void addClient(String key) throws InvalidClientException {
    if (key.equals(Source.FREEGEOIP.toString())) {
      clientsMap.put(key, new FreeGeoIp());
    } else if (key.equals(Source.GEOIP_COUNTRY.toString())) {
      clientsMap.put(key, new GeoIpCountry());
    } else {
      throw new InvalidClientException(key);
    }
  }

  public void addClient(String key, ClientInterface client) {
    clientsMap.put(key, client);
  }

  public ClientInterface removeClient(String key) {
    return clientsMap.remove(key);
  }
}