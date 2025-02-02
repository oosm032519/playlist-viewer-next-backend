package com.github.oosm032519.playlistviewernext.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebClientConfig.class);

    @Value("${spotify.mock-api.url}")
    private String mockApiUrl; // mockApiUrl を注入

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        logger.info("mockApiUrlの値: {}", mockApiUrl); // ログ出力追加
        return builder.baseUrl(mockApiUrl).build(); // baseUrl を設定
    }
}
