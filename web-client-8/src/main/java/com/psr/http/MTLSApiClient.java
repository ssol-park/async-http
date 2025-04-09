package com.psr.http;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Component
public class MTLSApiClient {

    private final WebClient webClient;

    public MTLSApiClient(@Qualifier("mtlsWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public <T, R> Mono<R> postJson(String url,
                                   HttpHeaders headers,
                                   T requestBody,
                                   ParameterizedTypeReference<R> responseType) {

        return webClient.post()
                .uri(url)
                .headers(h -> h.addAll(headers))
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType);
    }
}
