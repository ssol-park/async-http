package com.psr.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${cert.crt}")
    private Resource certFile;

    @Value("${cert.key}")
    private Resource keyFile;

    @Value("${cert.ca}")
    private Resource caFile;

    @Value("${cert.password}")
    private String certPassword;

    @Bean
    public WebClient webClient() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("connection-pool")
                .maxConnections(100)
                .pendingAcquireTimeout(Duration.ofSeconds(10))
                .maxIdleTime(Duration.ofSeconds(30))
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    public WebClient customWebClient() {
        // 커넥션 풀
        ConnectionProvider connectionProvider = ConnectionProvider.builder("custom-connection-pool")
                .maxConnections(100)
                .pendingAcquireTimeout(Duration.ofSeconds(10))
                .maxIdleTime(Duration.ofSeconds(30))
                .build();

        // netty 기반 HttpClient
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .compress(true) // gzip 압축 지원
                .responseTimeout(Duration.ofSeconds(10)) // 응답 시간 제한 (헤더 + 바디)
                .tcpConfiguration(tcp -> tcp
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000) // 커넥션 수립 제한 (3 way handshake)
                        .doOnConnected(conn -> conn
                                .addHandlerLast(new ReadTimeoutHandler(5))
                                .addHandlerLast(new WriteTimeoutHandler(5))
                        )
                );

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(config -> config
                        .defaultCodecs()
                        .maxInMemorySize(2 * 1024 * 1024)
                )
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(req -> {
            log.info("[customWebClient] {} {}", req.method(), req.url());
            return Mono.just(req);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(res -> {
            log.info("[customWebClient] status: {}", res.statusCode());
            return Mono.just(res);
        });
    }

    @Bean
    public WebClient mtlsWebClient() {
        try {
            SslContext sslContext = SslContextBuilder.forClient()
                    .keyManager(certFile.getFile(), keyFile.getFile(), certPassword)
                    .trustManager(caFile.getFile())
                    .clientAuth(ClientAuth.REQUIRE)
                    .build();

            ConnectionProvider connectionProvider = ConnectionProvider.builder("mtls-pool")
                    .maxConnections(100)
                    .pendingAcquireTimeout(Duration.ofSeconds(10))
                    .maxIdleTime(Duration.ofSeconds(30))
                    .build();

            HttpClient httpClient = HttpClient.create(connectionProvider)
                    .secure(ssl -> ssl.sslContext(sslContext));

            return WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .build();

        } catch (Exception e) {
            log.error("WebClient mTLS 초기화 실패", e);
            throw new IllegalStateException("mTLS 인증서 초기화 실패", e);
        }
    }
}
