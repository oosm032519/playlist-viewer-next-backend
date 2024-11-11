package com.github.oosm032519.playlistviewernext.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

public class ServletUtil {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ServletUtil.class);

    public static String extractSessionIdFromRequest(HttpServletRequest request) {
        logger.debug("extractSessionIdFromRequest 開始");

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("sessionId".equals(cookie.getName())) {
                    String sessionId = cookie.getValue();
                    logger.debug("extractSessionIdFromRequest 終了 - セッションID: {}", sessionId);
                    return sessionId;
                }
            }
        }

        logger.debug("extractSessionIdFromRequest 終了 - セッションIDが見つかりません");
        return null;
    }
}
