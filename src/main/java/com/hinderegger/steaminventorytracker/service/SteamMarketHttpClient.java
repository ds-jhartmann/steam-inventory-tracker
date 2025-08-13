package com.hinderegger.steaminventorytracker.service;

import com.hinderegger.steaminventorytracker.configuration.SteamConfiguration;
import com.hinderegger.steaminventorytracker.model.Item;
import com.hinderegger.steaminventorytracker.service.strategy.SteamApiQueryException;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class SteamMarketHttpClient {

  private final HttpClient httpClient;
  private final SteamConfiguration steamConfig;

  public String getStringHttpResponse(final Item item)
      throws InterruptedException, SteamApiQueryException {
    String url =
        steamConfig.getPath() + URLEncoder.encode(item.getItemName(), StandardCharsets.UTF_8);
    log.info(url);
    final HttpRequest httpRequest =
        HttpRequest.newBuilder().uri(URI.create(steamConfig.getBaseurl() + url)).GET().build();
    try {
      final HttpResponse<String> response =
          httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        return response.body();
      } else {
        throw new SteamApiQueryException("Error while querying steam API");
      }
    } catch (final IOException e) {
      throw new SteamApiQueryException(e.getMessage());
    }
  }
}
