package com.github.oosm032519.playlistviewernext.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisConfigTest {

    @Autowired
    private RedisConfig redisConfig;

    @Test
    void testLettuceClientConfigurationBuilderCustomizer() {
        // Given
        LettuceClientConfigurationBuilderCustomizer customizer = redisConfig.lettuceClientConfigurationBuilderCustomizer();
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder();

        // When
        builder.useSsl(); // SSL を有効にする
        customizer.customize(builder);
        LettuceClientConfiguration configuration = builder.build();

        // Then
        assertThat(configuration.isUseSsl()).isTrue();
        assertThat(configuration.isVerifyPeer()).isFalse();
    }
}
