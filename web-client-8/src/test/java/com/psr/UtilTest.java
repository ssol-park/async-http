package com.psr;

import com.psr.config.WebClientConfig;
import com.psr.util.WebClientUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

import java.util.Map;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {WebClientConfig.class, WebClientUtil.class})
class UtilTest {

    @Autowired
    private WebClientUtil webClientUtil;


    @Test
    void getTest() {
        String url = "";

        Mono<Map> mono = webClientUtil.get(url, Map.class);
    }
}
