package com.edulify.modules.geolocation.infrastructure.maxmind.geolite.database;

import scala.concurrent.duration.FiniteDuration;

public interface DurationToUpdateProvider
{
  FiniteDuration getDurationToNextUpdate();
}
