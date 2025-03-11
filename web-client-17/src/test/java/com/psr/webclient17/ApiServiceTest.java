package com.psr.webclient17;

import com.psr.webclient17.service.ApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApiServiceTest {

    @Autowired
    private ApiService apiService;

    @ParameterizedTest
    @ValueSource(ints = {1})
    void 단건_조회_테스트(int postId) {
        Mono<String> singlePost = apiService.getSinglePost(postId);

        StepVerifier.create(singlePost)
                .assertNext(body -> {
                    System.out.println("body = " + body);
                    assertThat(body).isNotNull();
                })
                .verifyComplete();
    }
}
