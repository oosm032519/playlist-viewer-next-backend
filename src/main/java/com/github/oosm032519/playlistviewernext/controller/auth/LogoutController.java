package com.github.oosm032519.playlistviewernext.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogoutController {

    private static final Logger logger = LoggerFactory.getLogger(LogoutController.class);
    private final SecurityContextLogoutHandler securityContextLogoutHandler;

    public LogoutController() {
        this.securityContextLogoutHandler = new SecurityContextLogoutHandler();
    }

    // テスト用のコンストラクタ
    LogoutController(SecurityContextLogoutHandler securityContextLogoutHandler) {
        this.securityContextLogoutHandler = securityContextLogoutHandler;
    }

    @PostMapping("/api/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        logger.info("ログアウト処理を開始します。リクエスト元IP: {}", request.getRemoteAddr());

        try {
            logger.debug("セキュリティコンテキストのクリアを実行します");
            securityContextLogoutHandler.logout(request, response, null);
            logger.info("セキュリティコンテキストのクリアが完了しました");

            logger.debug("JWTクッキーの無効化を開始します");
            ResponseCookie cookie = ResponseCookie.from("JWT", "")
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(0)
                    .sameSite("None")
                    .build();
            logger.info("JWTクッキーの無効化が完了しました。クッキー設定: {}", cookie);

            logger.debug("ログアウト成功レスポンスを作成します");
            ResponseEntity<String> responseEntity = ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body("Logged out successfully");
            logger.info("ログアウト処理が正常に完了しました");

            return responseEntity;
        } catch (Exception e) {
            logger.error("ログアウト処理中にエラーが発生しました", e);
            return ResponseEntity.internalServerError().body("ログアウト処理中にエラーが発生しました");
        }
    }
}
