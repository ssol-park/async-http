package com.psr.http;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class ApiClient {

    private final WebClient webClient;

    public ApiClient(@Qualifier("customWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public <T> Mono<T> get(String url, Class<T> responseType) {
        return webClient.get()
                .uri(url)
                .retrieve()
//                .onStatus(HttpStatus::is4xxClientError, res -> Mono.error())
//                .onStatus(HttpStatus::is5xxServerError, res -> Mono.error())
                .bodyToMono(responseType);
    }

    public <T> Mono<T> post(String url, Map<String, Object> body, Map<String, String> headers, ParameterizedTypeReference<T> typeReference) {
        return webClient.post()
                .uri(url)
                .headers(h -> {
                    if (headers != null) headers.forEach(h::set);
                    h.setContentType(MediaType.APPLICATION_JSON);
                })
                .bodyValue(body)
                .retrieve()
                .bodyToMono(typeReference);
    }
}
