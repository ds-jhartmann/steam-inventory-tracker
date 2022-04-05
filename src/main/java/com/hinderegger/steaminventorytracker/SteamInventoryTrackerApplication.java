package com.hinderegger.steaminventorytracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class SteamInventoryTrackerApplication {
    private static final String BASEURL = "https://steamcommunity.com";

    public static void main(String[] args) {
        SpringApplication.run(SteamInventoryTrackerApplication.class, args);
    }

    @Bean(name = "steamWebClient")
    public WebClient steamWebClient() {
        return WebClient.builder().baseUrl(BASEURL).defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build();
    }
}
