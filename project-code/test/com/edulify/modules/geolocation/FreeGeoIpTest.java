package com.edulify.modules.geolocation;

import org.junit.Test;

import static org.junit.Assert.*;
import static play.libs.F.Promise.promise;

public class FreeGeoIpTest {

    @Test
    public void testGetGeolocation() throws Exception
    {
      Object t = promise(() -> null).map(nullVal -> null == nullVal ? "1" : "0").get(5);
    }
}