package com.github.oosm032519.playlistviewernext.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@EnableRedisHttpSession
public class SessionConfig {

    @Value("${REDIS_TLS_URL}")
    private String redisUrl;

    @Bean
    public LettuceConnectionFactory connectionFactory() throws URISyntaxException {
        URI redisUri = new URI(redisUrl);

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisUri.getHost());
        redisConfig.setPort(redisUri.getPort());
        redisConfig.setPassword(redisUri.getUserInfo().split(":", 2)[1]);

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .useSsl().disablePeerVerification()
                .build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }
}
