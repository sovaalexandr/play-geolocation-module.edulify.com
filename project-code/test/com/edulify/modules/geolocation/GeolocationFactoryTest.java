package com.edulify.modules.geolocation;

import org.junit.Test;

import static org.junit.Assert.*;

public class GeolocationFactoryTest
{
    private GeolocationFactory geolocationFactory;

    @Test
    public void testCreate() throws Exception
    {
        Geolocation actual = geolocationFactory.create("198.252.206.140","US","United States","","","",38.,-97.);
        assertNotNull(actual);
    }

    @Test
    public void testCreate1() throws Exception
    {
        Geolocation actual = geolocationFactory.create("198.252.206.140","US");
        assertNotNull(actual);
    }
}