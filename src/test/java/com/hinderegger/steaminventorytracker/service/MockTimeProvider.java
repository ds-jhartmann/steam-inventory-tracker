package com.hinderegger.steaminventorytracker.service;

import java.time.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mock implementation of the TimeProvider interface for testing purposes. Uses a configurable fixed
 * clock or a tick clock for more advanced testing scenarios.
 */
public class MockTimeProvider implements TimeProvider {
  private final AtomicLong millisCounter = new AtomicLong(0);
  private Clock clock;
  private boolean useTickClock = false;
  private Duration tickDuration = Duration.ofSeconds(1);

  /**
   * Creates a new MockTimeProvider with a default fixed time of 2023-12-20T15:00:00 in the system
   * default time zone.
   */
  public MockTimeProvider() {
    // Default to 2023-12-20T15:00:00 in the system default time zone
    setFixedTime(LocalDateTime.of(2023, 12, 20, 15, 0, 0));
  }

  @Override
  public Clock getClock() {
    return clock;
  }

  @Override
  public LocalDateTime now() {
    return LocalDateTime.now(clock);
  }

  @Override
  public long currentTimeMillis() {
    if (useTickClock) {
      // Simulate incrementing time for each call
      return millisCounter.addAndGet(tickDuration.toMillis());
    }
    return clock.millis();
  }

  @Override
  public void sleep(int seconds) {
    // Do nothing in tests to avoid actual sleeping
  }

  /**
   * Sets a fixed time for this provider.
   *
   * @param fixedTime the fixed time to use
   */
  public void setFixedTime(LocalDateTime fixedTime) {
    ZoneId zoneId = ZoneId.systemDefault();
    Instant instant = fixedTime.atZone(zoneId).toInstant();
    this.clock = Clock.fixed(instant, zoneId);
    this.millisCounter.set(instant.toEpochMilli());
    this.useTickClock = false;
  }

  /**
   * Sets a fixed time for this provider with a specific zone.
   *
   * @param fixedTime the fixed time to use
   * @param zoneId the time zone to use
   */
  public void setFixedTime(LocalDateTime fixedTime, ZoneId zoneId) {
    Instant instant = fixedTime.atZone(zoneId).toInstant();
    this.clock = Clock.fixed(instant, zoneId);
    this.millisCounter.set(instant.toEpochMilli());
    this.useTickClock = false;
  }

  /**
   * Sets a fixed time in milliseconds since the epoch.
   *
   * @param epochMillis the time in milliseconds since the epoch
   */
  public void setFixedTimeMillis(long epochMillis) {
    ZoneId zoneId = ZoneId.systemDefault();
    Instant instant = Instant.ofEpochMilli(epochMillis);
    this.clock = Clock.fixed(instant, zoneId);
    this.millisCounter.set(epochMillis);
    this.useTickClock = false;
  }

  /**
   * Configures this provider to use a tick clock that advances by the specified duration on each
   * call to currentTimeMillis().
   *
   * @param useTickClock whether to use a tick clock
   * @param tickDuration the duration to advance the clock by on each call
   */
  public void setTickClock(boolean useTickClock, Duration tickDuration) {
    this.useTickClock = useTickClock;
    if (tickDuration != null) {
      this.tickDuration = tickDuration;
    }
  }

  /**
   * Configures this provider to increment time on each call to currentTimeMillis(). This is a
   * convenience method that sets up a tick clock with a 1-second tick duration.
   *
   * @param incrementTimeMillis whether to increment time on each call
   */
  public void setIncrementTimeMillis(boolean incrementTimeMillis) {
    setTickClock(incrementTimeMillis, Duration.ofSeconds(1));
  }
}
