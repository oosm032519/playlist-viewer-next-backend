package com.github.oosm032519.playlistviewernext.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

/**
 * サーブレットに関連するユーティリティ機能を提供するクラス
 * HTTPリクエストからの情報抽出などの共通処理を実装する
 */
public class ServletUtil {

    /**
     * クラスのロガーインスタンス
     * デバッグ情報やエラー情報のログ出力に使用する
     */
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ServletUtil.class);

    /**
     * HTTPリクエストからセッションIDを抽出する
     *
     * @param request HTTPリクエスト。Cookieの情報を含む
     * @return セッションIDの文字列。セッションIDが見つからない場合はnull
     */
    public static String extractSessionIdFromRequest(HttpServletRequest request) {
        logger.debug("extractSessionIdFromRequest 開始");

        // リクエストからCookieの配列を取得
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            // Cookieの配列をループして"sessionId"という名前のCookieを探す
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
