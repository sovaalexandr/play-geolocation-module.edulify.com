package com.edulify.modules.geolocation;

import akka.actor.ActorRef;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.sovaalexandr.maxmind.geoip2.GeoIP2DBModule;
import com.typesafe.config.Config;
import play.Environment;

import javax.inject.Named;

public class MaxmindDBModule extends AbstractModule {

  private final Environment environment;
  private final Config config;

  public MaxmindDBModule(Environment environment, Config config)
  {
    this.environment = environment;
    this.config = config;
  }

  @Override
  protected void configure() {
    install(new GeoIP2DBModule(environment.asScala(), config));
    install(new GeolocationModule(environment, config));
  }

  @Provides
  public GeolocationProvider geolocationProvider(@Named("geolocation") final ActorRef geolocation) {
    return new MaxmindDBProvider(geolocation);
  }
}
