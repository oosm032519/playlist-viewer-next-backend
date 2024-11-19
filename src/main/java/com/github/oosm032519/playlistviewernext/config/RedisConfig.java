package com.github.oosm032519.playlistviewernext.config;

import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisの設定を管理するコンフィグレーションクラス。
 * Lettuceクライアントの動作をカスタマイズし、SSLの設定を行う。
 */
@Configuration
public class RedisConfig {

    /**
     * Lettuceクライアントの設定をカスタマイズするBeanを提供する。
     * SSLが有効な場合、ピア検証を無効化する設定を適用する。
     *
     * @return LettuceClientConfigurationBuilderCustomizerのインスタンス
     */
    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationBuilderCustomizer() {
        return clientConfigurationBuilder -> {
            // SSL接続が設定されている場合、ピア検証を無効化
            if (clientConfigurationBuilder.build().isUseSsl()) {
                clientConfigurationBuilder.useSsl().disablePeerVerification();
            }
        };
    }
}
