package com.psr.webclient17.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.KeyStore;
import java.security.KeyStoreException;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }

    /* mTls 사용 시
    public WebClient webClientSsl() throws Exception {
        SslContext sslContext = createSslContext();

        HttpClient httpClient = HttpClient.create()
                .secure(ssl -> ssl.sslContext(sslContext));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    private SslContext createSslContext() throws Exception {
        // KeyStore 로드 (JKS or PKCS12)
        String keyStorePath = "keyPath..";
        String keyStorePwd = "password..";

        KeyStore keyStore = KeyStore.getInstance("pkcs12");
        keyStore.load(new FileInputStream(keyStorePath), keyStorePwd.toCharArray());

        TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        factory.init(keyStore);

        return SslContextBuilder.forClient()
                .protocols("TLSv1.3")
                .trustManager(factory)
                .build();
    }*/
}
