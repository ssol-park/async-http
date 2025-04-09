package com.psr.http;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.Collections;

public class HeaderBuilder {

    private final HttpHeaders headers = new HttpHeaders();

    private HeaderBuilder() {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    public static HeaderBuilder create() {
        return new HeaderBuilder();
    }

    public HeaderBuilder contentType(MediaType mediaType) {
        if (mediaType != null) {
            headers.setContentType(mediaType);
        }
        return this;
    }

    public HeaderBuilder accept(MediaType... mediaTypes) {
        if (mediaTypes != null && mediaTypes.length > 0) {
            headers.setAccept(Arrays.asList(mediaTypes));
        }
        return this;
    }

    public HeaderBuilder authBearer(String token) {
        if (token != null && !token.trim().isEmpty()) {
            headers.setBearerAuth(token.trim());
        }
        return this;
    }

    public HeaderBuilder authCustom(String scheme, String token) {
        if (scheme != null && token != null) {
            headers.set(HttpHeaders.AUTHORIZATION, scheme.trim() + " " + token.trim());
        }
        return this;
    }

    public HeaderBuilder custom(String key, String value) {
        if (key != null && value != null) {
            headers.set(key.trim(), value.trim());
        }
        return this;
    }

    public HttpHeaders build() {
        return headers;
    }
}
