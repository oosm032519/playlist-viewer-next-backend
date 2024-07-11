// LogoutService.java

package com.github.oosm032519.playlistviewernext.service.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Optional;

public class LogoutService {

    // ロガーのインスタンスを生成
    private static final Logger logger = LoggerFactory.getLogger(LogoutService.class);
    // Spotifyのクライアント登録ID
    private static final String SPOTIFY_CLIENT_REGISTRATION_ID = "spotify";

    // OAuth2AuthorizedClientServiceのインスタンス
    private final OAuth2AuthorizedClientService authorizedClientService;
    // SecurityContextLogoutHandlerのインスタンス
    private final SecurityContextLogoutHandler logoutHandler;

    /**
     * コンストラクタ
     *
     * @param authorizedClientService OAuth2AuthorizedClientServiceのインスタンス
     * @param logoutHandler           SecurityContextLogoutHandlerのインスタンス
     */
    public LogoutService(OAuth2AuthorizedClientService authorizedClientService, SecurityContextLogoutHandler logoutHandler) {
        this.authorizedClientService = authorizedClientService;
        this.logoutHandler = logoutHandler;
    }

    /**
     * ログアウト処理を行うメソッド
     *
     * @param request  HttpServletRequestのインスタンス
     * @param response HttpServletResponseのインスタンス
     */
    public void processLogout(HttpServletRequest request, HttpServletResponse response) {
        // 認証情報が存在する場合はログアウト処理を実行
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .ifPresentOrElse(
                        auth -> performLogout(auth, request, response),
                        () -> logger.debug("No authentication found in SecurityContext")
                );
        // 全てのクッキーを削除
        removeAllCookies(request, response);
        // セキュリティコンテキストをクリア
        SecurityContextHolder.clearContext();
        logger.info("Logout process completed successfully");
    }

    /**
     * ログアウト処理を実行するメソッド
     *
     * @param auth     認証情報
     * @param request  HttpServletRequestのインスタンス
     * @param response HttpServletResponseのインスタンス
     */
    private void performLogout(Authentication auth, HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Performing logout for authentication: {}", auth);
        // OAuth2の認証クライアントを削除
        removeOAuth2AuthorizedClient(auth);
        // ログアウトハンドラを実行
        logoutHandler.logout(request, response, auth);
        logger.debug("Logout handler executed");
    }

    /**
     * OAuth2認証クライアントを削除するメソッド
     *
     * @param auth 認証情報
     */
    private void removeOAuth2AuthorizedClient(Authentication auth) {
        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
            logger.debug("Removing OAuth2 authorized client for user: {}", oauthToken.getName());
            authorizedClientService.removeAuthorizedClient(SPOTIFY_CLIENT_REGISTRATION_ID, oauthToken.getName());
            logger.debug("OAuth2 authorized client removed");
        } else {
            logger.debug("Authentication is not an OAuth2AuthenticationToken");
        }
    }

    /**
     * 全てのクッキーを削除するメソッド
     *
     * @param request  HttpServletRequestのインスタンス
     * @param response HttpServletResponseのインスタンス
     */
    private void removeAllCookies(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Removing all cookies");
        Optional.ofNullable(request.getCookies())
                .ifPresent(cookies -> {
                    logger.debug("Found {} cookies to remove", cookies.length);
                    Arrays.stream(cookies).forEach(cookie -> expireCookie(cookie, response));
                });
        logger.debug("All cookies removed");
    }

    /**
     * クッキーを無効にするメソッド
     *
     * @param cookie   クッキーのインスタンス
     * @param response HttpServletResponseのインスタンス
     */
    private void expireCookie(Cookie cookie, HttpServletResponse response) {
        logger.debug("Expiring cookie: {}", cookie.getName());
        cookie.setValue("");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        logger.debug("Cookie expired: {}", cookie.getName());
    }
}
