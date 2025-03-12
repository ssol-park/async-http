package com.psr.webclient17;

import com.psr.webclient17.service.ApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApiServiceTest {

    @Autowired
    private ApiService apiService;

    @ParameterizedTest
    @ValueSource(ints = {1})
    void 단건_조회_테스트(int postId) {
        Mono<String> singlePost = apiService.getPost(postId);

        StepVerifier.create(singlePost)
                .assertNext(body -> {
                    System.out.println("body = " + body);
                    assertThat(body).isNotNull();
                })
                .verifyComplete();
    }

    /**
     * `Mono<Void>`를 반환하며, 현재 `Flux`가 완료될 때 완료된다.
     * 이 메서드는 기존 `Flux`의 모든 데이터를 무시하고,
     * 오직 완료(`onComplete()`) 또는 에러(`onError()`) 신호만 전달한다.
     *
     * <p><strong>Discard Support:</strong> 이 연산자는 원본 `Flux`의 모든 요소를 무시한다.</p>
     *
     * @return `Flux`의 완료 신호를 나타내는 새로운 `Mono<Void>`
     */
    @Test
    void Flux_Then() {
        Flux<String> posts = apiService.getPosts();

        Mono<Void> mono = posts.then();

        StepVerifier.create(mono).verifyComplete();
    }

    /**
     * 현재 `Flux`가 완료된 후 주어진 `Mono<V>`의 신호를 방출한다.
     * 즉, 기존 `Flux`의 데이터를 무시하고 완료 신호를 받아 `Mono<V>`를 실행한다.
     * `Mono<V>`는 원본 `Flux`의 완료 신호를 트리거로 실행됨.
     *
     * <p><strong>Discard Support:</strong> 이 연산자는 원본 `Flux`의 모든 요소를 무시한다.</p>
     *
     * @param other `Flux`가 완료된 후 실행할 `Mono`
     * @param <V> 실행할 `Mono`의 데이터 타입
     *
     * @return 원본 `Flux`가 완료된 후 실행되는 새로운 `Mono<V>`
     */
    @ParameterizedTest
    @ValueSource(strings = "완료 후 실행")
    void Flux_Then_Mono(String expected) {
        Flux<String> posts = apiService.getPosts();

        Mono<String> mono = posts.then(Mono.just("완료 후 실행"));

        StepVerifier.create(mono)
                .expectNext(expected)
                .verifyComplete();
    }

    /**
     * 현재 `Flux`가 완료된 후 주어진 `Publisher<Void>`의 완료 신호를 기다린다.
     * 즉, 원본 `Flux`가 완료된 후 추가적인 `Publisher<Void>` 작업이 끝날 때까지 기다린다.
     *
     * <p><strong>Discard Support:</strong> 이 연산자는 원본 `Flux`의 모든 요소를 무시한다.</p>
     *
     * @param other `Flux`가 완료된 후 실행할 `Publisher<Void>`
     *
     * @return `Flux`와 `Publisher<Void>`가 모두 완료될 때 완료되는 새로운 `Mono<Void>`
     */
    @Test
    void Flux_Then_Mono() {
        AtomicBoolean wasExecuted = new AtomicBoolean(false);

        Flux<String> posts = apiService.getPosts();

//        Mono<Void> mono = posts.thenEmpty(Mono.fromRunnable(() -> System.out.println("모든 데이터 처리 완료")));
        Mono<Void> mono = posts.thenEmpty(Mono.fromRunnable(() -> {
            System.out.println("모든 데이터 처리 완료");
            wasExecuted.set(true);
        }));

        StepVerifier.create(mono).verifyComplete();

        assert wasExecuted.get();
    }

    /**
     * 현재 `Flux`가 완료된 후 제공된 `Publisher<V>`를 실행한다.
     * 즉, 원본 `Flux`의 데이터를 무시하고, 완료 신호를 받아 새로운 `Publisher<V>`를 실행한다.
     *
     * <p><strong>Discard Support:</strong> 이 연산자는 원본 `Flux`의 모든 요소를 무시한다.</p>
     *
     * @param other `Flux`가 완료된 후 실행할 `Publisher<V>`
     * @param <V> 실행할 `Publisher`의 데이터 타입
     *
     * @return 원본 `Flux`가 완료된 후 실행되는 새로운 `Flux<V>`
     */
    @Test
    void Flux_Then_Many() {
        Flux<String> posts = apiService.getPosts();

        Flux<Integer> result = posts.thenMany(Flux.just(1, 2, 3));

        StepVerifier.create(result)
                .expectNext(1, 2, 3)
                .verifyComplete();
    }
}
