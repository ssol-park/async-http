package com.psr;


import com.psr.config.WebClientConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

    // TODO ::  Connection Pool, Backpressure, Pending
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

}
