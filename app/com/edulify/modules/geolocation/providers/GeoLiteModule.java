package com.edulify.modules.geolocation.providers;

import akka.actor.ActorSystem;
import akka.actor.TypedActor;
import akka.actor.TypedProps;
import akka.japi.Creator;
import com.edulify.modules.geolocation.GeolocationProvider;
import com.edulify.modules.geolocation.infrastructure.maxmind.geolite.DatabaseReaderSupplier;
import com.edulify.modules.geolocation.infrastructure.maxmind.geolite.GeoLite2;
import com.edulify.modules.geolocation.infrastructure.maxmind.geolite.GeoLite2Impl;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import play.inject.Injector;
import play.libs.akka.AkkaGuiceSupport;

import java.util.function.Function;

public class GeoLiteModule extends AbstractModule implements AkkaGuiceSupport {

  @Override
  protected void configure() {
    bindActor(DatabaseReaderSupplier.class, "database-reader-supplier");
    bindTypedActor(
      GeoLite2.class,
      GeoLite2Impl.class,
      GeoLite2Impl.GeoLite2ImplCreator.class,
      "geo-lite-2",
      Function.identity());
    bind(GeolocationProvider.class).to(GeoLiteProvider.class);
  }

  public <TypedActorT, TypedActorI extends TypedActorT> void bindTypedActor(
    Class<TypedActorT> interFace,
    Class<TypedActorI> implementationClass,
    Class<? extends Creator<TypedActorI>> implementationCreatorClass,
    String name,
    Function<TypedProps<TypedActorI>, TypedProps<TypedActorI>> actorPropsProvider
  ) {
    bind(interFace)
      .annotatedWith(Names.named(name))
      .toProvider(new TypedActorProvider<>(implementationClass, implementationCreatorClass, name, actorPropsProvider))
      .asEagerSingleton();
  }

  public static class TypedActorProvider<TypedActorT, TypedActorI extends TypedActorT> implements Provider<TypedActorT>
  {
    @Inject
    private ActorSystem actorSystem;
    @Inject
    private Injector injector;

    final private String name;
    final private Function<TypedProps<TypedActorI>, TypedProps<TypedActorI>> actorPropsProvider;
    final private Class<TypedActorI> implementationClass;
    final private Class<? extends Creator<TypedActorI>> implementationCreatorClass;

    public TypedActorProvider(
      Class<TypedActorI> implementationClass,
      Class<? extends Creator<TypedActorI>> implementationCreatorClass,
      String name,
      Function<TypedProps<TypedActorI>, TypedProps<TypedActorI>> actorPropsProvider
    ) {
      this.implementationCreatorClass = implementationCreatorClass;
      this.name = name;
      this.implementationClass = implementationClass;
      this.actorPropsProvider = actorPropsProvider;
    }

    @Override
    public TypedActorT get() {
      Creator<TypedActorI> creator = injector.instanceOf(implementationCreatorClass);
      TypedProps<TypedActorI> props = new TypedProps<>(implementationClass, creator);
      return TypedActor.get(actorSystem).typedActorOf(actorPropsProvider.apply(props), name);
    }
  }
}
