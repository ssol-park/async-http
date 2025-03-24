package com.psr;


import com.psr.config.WebClientConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/*
* curl -X GET https://jsonplaceholder.typicode.com/posts/2 -H "Accept: application/json"
* */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WebClientConfig.class)
class AppTest {
    private static final Logger logger = LoggerFactory.getLogger(AppTest.class);
    private static final String BASE_URL = "https://jsonplaceholder.typicode.com";
    private static final String DELAY_URL = "https://httpbin.org/delay/%d";
    private static final AtomicInteger counter = new AtomicInteger(0);

    @Autowired
    private WebClient webClient;

    static Stream<String> providePostUrls() {
        return IntStream.rangeClosed(1, 10)
                .mapToObj(i -> BASE_URL + "/posts/" + i);
    }

    @ParameterizedTest
    @MethodSource("providePostUrls")
    void WebClient_테스트(String url) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        logger.info("요청 시작: {}", url);

        webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSubscribe(subscription -> logger.info("요청 등록: {}", url))
                .doOnNext(response -> logger.info("응답 수신: {} (Counter: {})", url, counter.incrementAndGet()))
                .doOnSuccess(response -> {
                    logger.info("요청 완료: {}", url);
                    latch.countDown();
                })
                .subscribe();

        logger.info("메인 스레드 실행 중...");
        latch.await();
    }

    @Test
    void WebClient_지연_테스트() throws InterruptedException {
        int reqSize = 5;
        CountDownLatch latch = new CountDownLatch(reqSize);
        logger.info("#### 요청 시작");

        IntStream.rangeClosed(1, reqSize).forEach(i -> {
            String traceId = "REQ-" + i;

            webClient.get()
                    .uri(String.format(DELAY_URL, i))
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSubscribe(subscription -> logger.info("[{}] 요청 등록 @", traceId))
                    .doOnNext(response -> logger.info("[{}] 응답 수신 #", traceId))
                    .doOnSuccess(response -> {
                        logger.info("[{}] 요청 완료 %", traceId);
                        latch.countDown();
                    })
                    .subscribe();
        });
        logger.info("메인 스레드 실행 중...");

        latch.await();
    }


    // TODO ::  Connection Pool, Backpressure, Pending
    @Test
    void WebClient_커넥션풀_확장_테스트() throws InterruptedException {
        int reqSize = 5;
        logger.info("#### 요청 시작");

        // Connection Pool 확장
        ConnectionProvider provider = ConnectionProvider.builder("custom")
                .maxConnections(3) // 기본은 2
                .pendingAcquireTimeout(Duration.ofSeconds(5)) // 커넥션 풀이 가득 찬 상태에서 새 요청이 커넥션을 얻기 위해 기다릴 수 있는 최대 시간. 5초 동안 커넥션이 반납되지 않으면 → 타임아웃 발생 + 예외 throw
                .build();

        WebClient customWebClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.create(provider)))
                .build();

        logger.info("#### Delay 시작");
        IntStream.rangeClosed(1, reqSize).forEach(i -> {
            String traceId = "POOL-" + i;

            customWebClient.get()
                    .uri(String.format(DELAY_URL, 5))
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSubscribe(sub -> logger.info("[{}] 요청 등록 @ [{}]", traceId, Thread.currentThread().getName()))
                    .doOnNext(res -> logger.info("[{}] 응답 수신 # [{}]", traceId, Thread.currentThread().getName()))
                    .doOnSuccess(res -> {
                        logger.info("[{}] 요청 완료 % [{}]", traceId, Thread.currentThread().getName());
                    })
                    .log()
                    .subscribe();
        });

        logger.info("#### Get Post 시작");
        IntStream.rangeClosed(1, reqSize).forEach(i -> {
            String url = BASE_URL + "/posts/" + i;

            customWebClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .doOnSubscribe(subscription -> logger.info("Get Post 요청 등록: {}", url))
                    .doOnNext(response -> logger.info("Get Post 응답 수신: {} (Counter: {})", url, counter.incrementAndGet()))
                    .doOnSuccess(response -> {
                        logger.info("Get Post 요청 완료: {}", url);
                    })
                    .log()
                    .subscribe();
        });

        Thread.sleep(10000);
    }
}
