package com.psr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.psr.config.WebClientConfig;
import com.psr.dto.Post;
import com.psr.http.ApiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WebClientConfig.class, ApiClient.class})
class ApiClientTest {
    private static final Logger logger = LoggerFactory.getLogger(ApiClientTest.class);
    private ObjectMapper om = new ObjectMapper();

    @Autowired
    private ApiClient apiClient;

    @Test
    void getPostTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        String url = "https://jsonplaceholder.typicode.com/posts/1";

        apiClient.get(url, Post.class)
                .doOnNext(post -> logger.info("{}", post))
                .doOnTerminate(() -> latch.countDown())
                .subscribe();

        latch.await();
    }

    @Test
    void getPostsTest() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        List<String> urls = IntStream.rangeClosed(1, 10)
                .mapToObj(id -> "https://jsonplaceholder.typicode.com/posts/" + id)
                .collect(Collectors.toList());

        Flux.fromIterable(urls)
                .flatMap(url -> apiClient.get(url, Post.class))
                .collectList()
                .doOnNext(posts -> logger.info("post size: {}", posts.size()))
                .doOnTerminate(() -> latch.countDown())
                .subscribe();

        latch.await();
    }

    @Test
    void 인메모리_사이즈_제한_에러_발생_테스트() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        // 1MB 제한 설정한 WebClient
        WebClient limitedClient = WebClient.builder()
                .codecs(config -> config
                        .defaultCodecs()
                        .maxInMemorySize(1024)) // 1KB 제한
                .defaultHeader(HttpHeaders.ACCEPT_ENCODING, "identity") // gzip 비활성화
                .build();

        String url = "https://httpbin.org/bytes/4097152"; // 2MB 응답

        limitedClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(body -> logger.info("응답 본문 크기: {} bytes", body.length()))
                .doOnError(e -> logger.error("에러 발생 {}", e.toString()))
                .doOnTerminate(latch::countDown)
                .subscribe();

        latch.await();
    }
}
