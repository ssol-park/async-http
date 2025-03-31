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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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
}
