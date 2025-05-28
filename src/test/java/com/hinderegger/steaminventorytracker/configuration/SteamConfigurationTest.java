package com.hinderegger.steaminventorytracker.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class SteamConfigurationTest {

  @Test
  void shouldHaveSettersForAllProperties() {
    // Arrange
    SteamConfiguration config = new SteamConfiguration();
    
    // Act
    config.setBaseurl("https://new.example.com");
    config.setPath("/api/new");
    config.setTimeoutDuration(Duration.ofSeconds(10));
    config.setLimitRefreshPeriod(Duration.ofMinutes(2));
    config.setLimitForPeriod(20);
    config.setSleepDuration(1000);
    config.setMaxRetryAttempts(5);
    config.setRetryInitialBackoff(Duration.ofSeconds(2));
    config.setRetryMaxBackoff(Duration.ofSeconds(20));
    config.setRetryJitter(0.75);
    
    // Assert
    assertThat(config.getBaseurl()).isEqualTo("https://new.example.com");
    assertThat(config.getPath()).isEqualTo("/api/new");
    assertThat(config.getTimeoutDuration()).isEqualTo(Duration.ofSeconds(10));
    assertThat(config.getLimitRefreshPeriod()).isEqualTo(Duration.ofMinutes(2));
    assertThat(config.getLimitForPeriod()).isEqualTo(20);
    assertThat(config.getSleepDuration()).isEqualTo(1000);
    assertThat(config.getMaxRetryAttempts()).isEqualTo(5);
    assertThat(config.getRetryInitialBackoff()).isEqualTo(Duration.ofSeconds(2));
    assertThat(config.getRetryMaxBackoff()).isEqualTo(Duration.ofSeconds(20));
    assertThat(config.getRetryJitter()).isEqualTo(0.75);
  }
  
  @Test
  void shouldImplementEqualsAndHashCode() {
    // Arrange
    SteamConfiguration config1 = new SteamConfiguration();
    config1.setBaseurl("https://test.example.com");
    config1.setPath("/api/test");
    
    SteamConfiguration config2 = new SteamConfiguration();
    config2.setBaseurl("https://test.example.com");
    config2.setPath("/api/test");
    
    SteamConfiguration config3 = new SteamConfiguration();
    config3.setBaseurl("https://different.example.com");
    config3.setPath("/api/test");
    
    // Assert
    assertThat(config1).isEqualTo(config2);
    assertThat(config1).isNotEqualTo(config3);
    assertThat(config1.hashCode()).isEqualTo(config2.hashCode());
    assertThat(config1.hashCode()).isNotEqualTo(config3.hashCode());
  }
  
  @Test
  void shouldImplementToString() {
    // Arrange
    SteamConfiguration config = new SteamConfiguration();
    config.setBaseurl("https://test.example.com");
    config.setPath("/api/test");
    
    // Assert
    assertThat(config.toString()).contains("baseurl=https://test.example.com");
    assertThat(config.toString()).contains("path=/api/test");
  }
}