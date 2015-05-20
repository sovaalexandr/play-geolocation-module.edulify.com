package com.edulify.modules.geolocation;

import static play.libs.F.Promise;

/**
 * Created by sovaalexandr
 */
public interface ClientInterface {
  Promise<Geolocation> getGeolocation(String ip) throws Throwable;
}
