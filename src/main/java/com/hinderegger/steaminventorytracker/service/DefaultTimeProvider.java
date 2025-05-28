package com.hinderegger.steaminventorytracker.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

/**
 * Default implementation of the TimeProvider interface. Uses system time and standard sleep
 * functionality.
 */
@Service
public class DefaultTimeProvider implements TimeProvider {

  private final Clock clock;

  public DefaultTimeProvider() {
    this.clock = Clock.systemDefaultZone();
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
    return clock.millis();
  }

  @Override
  public void sleep(int seconds) throws InterruptedException {
    TimeUnit.SECONDS.sleep(seconds);
  }
}
