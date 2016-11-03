package com.edulify.modules.geolocation.infrastructure.maxmind.geolite.database;

import scala.concurrent.duration.FiniteDuration;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

/**
 *  {@see https://dev.maxmind.com/geoip/geoip2/geolite2/#Databases}
 *  GeoLite2 databases are updated on the first Tuesday of each month.
 */
public class DurationToFirstWednesdayOfNextMonth implements DurationToUpdateProvider
{
  @Override
  public FiniteDuration getDurationToNextUpdate() {

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime firstWednesdayOfNextMonth = now
      .with(TemporalAdjusters.firstDayOfNextMonth())
      .with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY));
    Duration duration = Duration.between(now, firstWednesdayOfNextMonth);

    return scala.concurrent.duration.Duration.fromNanos(duration.toNanos());
  }
}
