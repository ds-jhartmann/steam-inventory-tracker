package com.hinderegger.steaminventorytracker;

import com.hinderegger.steaminventorytracker.model.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class SteamMarketAPICaller {

    private static final String PATH = "/market/priceoverview/?appid=730&currency=3&market_hash_name=";

    @Resource(name = "steamWebClient")
    private WebClient client;

    public Mono<String> getPriceForItem(Item item) {
        return getFromApi(item.getItemName());
    }

    public Mono<String> getFromApi(String itemName) {
        log.info("Starting api request for item: " + itemName);

        return client
                .get()
                .uri(PATH + URLEncoder.encode(itemName, StandardCharsets.UTF_8))
                .exchangeToMono(clientResponse -> {
                    if (clientResponse.statusCode().is2xxSuccessful()) {
                        return clientResponse.bodyToMono(String.class);
                    } else if (clientResponse.statusCode().is4xxClientError()) {
                        throw new IllegalStateException("error while accessing SteamAPI for item: " + itemName);
                    } else {
                        return clientResponse.createException().flatMap(Mono::error);
                    }
                });
    }


}
