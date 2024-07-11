// LoginSuccessController.java

package com.github.oosm032519.playlistviewernext.controller.auth;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class LoginSuccessController {

    // ロガーのインスタンスを作成
    private static final Logger logger = LoggerFactory.getLogger(LoginSuccessController.class);

    /**
     * ログイン成功時に呼び出されるメソッド
     *
     * @param principal 認証されたOAuth2ユーザーの情報を含むオブジェクト
     * @return ログイン成功後にリダイレクトするURL
     */
    @GetMapping("/loginSuccess")
    public String loginSuccess(@AuthenticationPrincipal OAuth2User principal) {
        // ユーザーIDを取得
        String userId = principal.getAttribute("id");
        // アクセストークンを取得
        String accessToken = principal.getAttribute("access_token");
        // 認証成功のログを記録
        logger.info("User successfully authenticated: {}", userId);
        // アクセストークンのログを記録
        logger.info("Access token: {}", accessToken);

        // フロントエンドアプリケーションにリダイレクト
        return "redirect:http://localhost:3000/";
    }
}
