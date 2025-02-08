package com.github.oosm032519.playlistviewernext.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

class RedisConfigTest {

    private final RedisConfig redisConfig = new RedisConfig();

    /**
     * Lettuceクライアントの設定が正しくカスタマイズされることを確認する。
     * SSLが有効な場合に、ピア検証が無効化されることを検証する。
     */
    @Test
    void testLettuceClientConfigurationBuilderCustomizer() {
        // Arrange: テストデータの準備
        LettuceClientConfigurationBuilderCustomizer customizer = redisConfig.lettuceClientConfigurationBuilderCustomizer();
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder();

        // Act: SSLを有効にしてカスタマイザを適用
        builder.useSsl();
        customizer.customize(builder);
        LettuceClientConfiguration configuration = builder.build();

        // Assert: SSLが有効で、ピア検証が無効化されていることを確認
        assertThat(configuration.isUseSsl()).isTrue();
        assertThat(configuration.isVerifyPeer()).isFalse();
    }
}
