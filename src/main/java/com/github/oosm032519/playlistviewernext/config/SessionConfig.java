package com.github.oosm032519.playlistviewernext.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@Configuration
@EnableJdbcHttpSession
public class SessionConfig {

    private static final Logger logger = LoggerFactory.getLogger(SessionConfig.class);

    public SessionConfig() {
        logger.info("JDBCセッション設定が初期化されました。");
    }
}
