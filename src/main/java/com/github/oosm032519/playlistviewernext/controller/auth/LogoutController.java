// LogoutController.java

package com.github.oosm032519.playlistviewernext.controller.auth;

import com.github.oosm032519.playlistviewernext.service.auth.LogoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class LogoutController {

    // ロガーの初期化
    private static final Logger logger = LoggerFactory.getLogger(LogoutController.class);

    // ログアウト成功メッセージ
    private static final String LOGOUT_SUCCESS_MESSAGE = "ログアウトしました";

    // ログアウトエラーメッセージ
    private static final String LOGOUT_ERROR_MESSAGE = "ログアウト処理中にエラーが発生しました";

    // LogoutServiceのインスタンス
    private final LogoutService logoutService;

    /**
     * コンストラクタ
     *
     * @param authorizedClientService OAuth2AuthorizedClientServiceのインスタンス
     */
    public LogoutController(OAuth2AuthorizedClientService authorizedClientService) {
        this.logoutService = createLogoutService(authorizedClientService);
        logger.debug("LogoutController initialized");
    }

    /**
     * LogoutServiceを生成するメソッド
     *
     * @param authorizedClientService OAuth2AuthorizedClientServiceのインスタンス
     * @return 新しいLogoutServiceのインスタンス
     */
    protected LogoutService createLogoutService(OAuth2AuthorizedClientService authorizedClientService) {
        return new LogoutService(authorizedClientService, new SecurityContextLogoutHandler());
    }

    /**
     * ログアウト処理を行うエンドポイント
     *
     * @param request  HttpServletRequestのインスタンス
     * @param response HttpServletResponseのインスタンス
     * @return ログアウト結果のResponseEntity
     */
    @PostMapping("/api/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Logout process started");
        try {
            // ログアウト処理の実行
            logoutService.processLogout(request, response);
            return ResponseEntity.ok(LOGOUT_SUCCESS_MESSAGE);
        } catch (Exception e) {
            // ログアウト処理中のエラーハンドリング
            logger.error("Logout process failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(LOGOUT_ERROR_MESSAGE);
        }
    }
}
