package com.edulify.modules.geolocation;

import play.libs.ws.WS;
import play.libs.ws.WSRequestHolder;

/**
 * Created by sovaalexandr
 */
public class RequestBuilder {
  public WSRequestHolder buildRequest(String url) {
    return WS.url(url);
  }
}
