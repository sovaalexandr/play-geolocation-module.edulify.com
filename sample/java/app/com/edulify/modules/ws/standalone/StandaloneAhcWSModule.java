package com.edulify.modules.ws.standalone;

import akka.stream.Materializer;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import play.libs.ws.StandaloneWSClient;
import play.libs.ws.ahc.StandaloneAhcWSClient;
import play.shaded.ahc.org.asynchttpclient.AsyncHttpClient;
import scala.collection.Seq;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Play not supplies such a module by default.
 */
public class StandaloneAhcWSModule extends Module
{

  @Override
  public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
    return seq(
      // AsyncHttpClientProvider is added by the Scala API
      bind(StandaloneWSClient.class).toProvider(StandaloneAhcWSModule.AhcWSClientProvider.class)
    );
  }

  @Singleton
  public static class AhcWSClientProvider implements Provider<StandaloneWSClient>
  {
    private final StandaloneWSClient client;

    @Inject
    public AhcWSClientProvider(AsyncHttpClient asyncHttpClient, Materializer materializer) {
      client = new StandaloneAhcWSClient(asyncHttpClient, materializer);
    }

    @Override
    public StandaloneWSClient get() {
      return client;
    }
  }
}
