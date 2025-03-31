package com.psr.http;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ApiClient {

    private final WebClient webClient;

    public ApiClient(@Qualifier("customWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public <T> Mono<T> get(String url, Class<T> responseClass) {
        return webClient.get()
                .uri(url)
                .retrieve()
//                .onStatus(HttpStatus::is4xxClientError, res -> Mono.error())
//                .onStatus(HttpStatus::is5xxServerError, res -> Mono.error())
                .bodyToMono(responseClass);
    }
}
