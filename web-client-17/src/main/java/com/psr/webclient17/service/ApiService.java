package com.psr.webclient17.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ApiService {
    private final WebClient webClient;

    public ApiService(WebClient webClient) {
        this.webClient = webClient;
    }

}
