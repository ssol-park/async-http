package com.psr.webclient17.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ApiService {

    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    private static final String POSTS_ENDPOINT = "/posts";

    private final WebClient webClient;

    public ApiService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<String> getSinglePost(int postId) {
        return webClient.get()
                .uri(BASE_URL + POSTS_ENDPOINT + "/{id}", postId)
                .retrieve()
                .bodyToMono(String.class);
    }
}
