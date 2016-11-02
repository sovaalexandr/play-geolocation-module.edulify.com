package com.edulify.modules.geolocation.infrastructure.maxmind.geolite;

import akka.actor.ActorRef;
import akka.actor.TypedActor;
import akka.japi.Creator;
import com.edulify.modules.geolocation.infrastructure.maxmind.geolite.database.DatabaseReaderSupplier;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;
import scala.concurrent.ExecutionContextExecutor;
import static akka.pattern.Patterns.ask;
import static scala.compat.java8.FutureConverters.toJava;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.InetAddress;
import java.util.concurrent.CompletionStage;

public class GeoLite2Impl implements GeoLite2
{
  final private ActorRef dataBaseSupplier;

  static
  public class GeoLite2ImplCreator implements Creator<GeoLite2Impl> {
    private ActorRef dataBaseSupplier;

    @Inject
    public GeoLite2ImplCreator(@Named("database-reader-supplier") ActorRef dataBaseSupplier) {
      this.dataBaseSupplier = dataBaseSupplier;
    }

    @Override
    public GeoLite2Impl create() throws Exception {
      return new GeoLite2Impl(dataBaseSupplier);
    }
  }

  public GeoLite2Impl(ActorRef dataBaseSupplier) {
    this.dataBaseSupplier = dataBaseSupplier;
  }

  @Override
  public CompletionStage<CityResponse> locateCityByIp(String ipAddress) {
    ExecutionContextExecutor executor = TypedActor.context().dispatcher();

    return toJava(ask(dataBaseSupplier, DatabaseReaderSupplier.GetCurrent$.MODULE$, 50l))
      .thenApplyAsync(reader -> getFromReader(mapToReader(reader), ipAddress), executor);
  }

  private DatabaseReader mapToReader(Object reader) {
    return (DatabaseReader) reader;
  }

  private CityResponse getFromReader(DatabaseReader reader, String ipAddress)
  {
    try {
      final InetAddress address = InetAddress.getByName(ipAddress);
      return reader.city(address);
    } catch (Throwable e) {
      return null;
    }
  }
}
