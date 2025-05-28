package com.hinderegger.steaminventorytracker.service;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Interface for providing time-related functionality. This allows for better testability by mocking
 * time in tests.
 */
public interface TimeProvider {
  /**
   * Returns the clock used by this time provider.
   *
   * @return the clock
   */
  Clock getClock();

  /**
   * Returns the current date and time.
   *
   * @return the current date and time
   */
  LocalDateTime now();

  /**
   * Returns the current time in milliseconds.
   *
   * @return the current time in milliseconds
   */
  long currentTimeMillis();

  /**
   * Sleeps for the specified number of seconds.
   *
   * @param seconds the number of seconds to sleep
   * @throws InterruptedException if the thread is interrupted while sleeping
   */
  void sleep(int seconds) throws InterruptedException;
}
