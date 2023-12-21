package com.hinderegger.steaminventorytracker.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "steam")
@Data
public class SteamConfiguration {
  private String baseurl;
  private String path;
}
