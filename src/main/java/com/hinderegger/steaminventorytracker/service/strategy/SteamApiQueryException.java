package com.hinderegger.steaminventorytracker.service.strategy;

public class SteamApiQueryException extends Exception {
  public SteamApiQueryException(final String message) {
    super(message);
  }

  public SteamApiQueryException(final Throwable e) {
    super(e);
  }
}
