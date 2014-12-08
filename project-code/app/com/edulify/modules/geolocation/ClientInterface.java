package com.edulify.modules.geolocation;

import play.libs.F;

/**
 * Created by sovaalexandr
 */
public interface ClientInterface {
  F.Promise<Geolocation> getGeolocation(String ip);
}
