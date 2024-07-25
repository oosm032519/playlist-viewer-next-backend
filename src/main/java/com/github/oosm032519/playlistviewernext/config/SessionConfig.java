package com.github.oosm032519.playlistviewernext.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(SessionConfig.class);

    @Value("${REDIS_TLS_URL}")
    private String redisUrl;

    @Bean
    public LettuceConnectionFactory connectionFactory() throws URISyntaxException {
        logger.info("Redisコネクションファクトリの初期化を開始します。Redis URL: {}", redisUrl);

        URI redisUri;
        try {
            redisUri = new URI(redisUrl);
            logger.debug("Redis URIの解析に成功しました。ホスト: {}, ポート: {}", redisUri.getHost(), redisUri.getPort());
        } catch (URISyntaxException e) {
            logger.error("Redis URLの解析に失敗しました。URL: {}", redisUrl, e);
            throw e;
        }

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisUri.getHost());
        redisConfig.setPort(redisUri.getPort());

        String[] userInfo = redisUri.getUserInfo().split(":", 2);
        if (userInfo.length > 1) {
            redisConfig.setPassword(userInfo[1]);
            logger.debug("Redisパスワードが設定されました。");
        } else {
            logger.warn("Redisパスワードが設定されていません。セキュリティリスクの可能性があります。");
        }

        logger.info("Redis設定が完了しました。ホスト: {}, ポート: {}", redisConfig.getHostName(), redisConfig.getPort());

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .useSsl().disablePeerVerification()
                .build();
        logger.debug("Lettuceクライアント設定が完了しました。SSL: 有効, ピア検証: 無効");

        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig, clientConfig);
        logger.info("Lettuceコネクションファクトリが正常に作成されました。");

        return factory;
    }
}
