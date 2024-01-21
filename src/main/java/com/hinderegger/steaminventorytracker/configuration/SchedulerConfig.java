package com.hinderegger.steaminventorytracker.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "scheduler")
@Data
public class SchedulerConfig {
  private String request;
}
