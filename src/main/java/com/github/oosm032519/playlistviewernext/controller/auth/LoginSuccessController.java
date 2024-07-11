package com.github.oosm032519.playlistviewernext.controller.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ログイン成功時の処理を担当するコントローラークラス。
 * OAuth2認証後のユーザー情報の取得とリダイレクト処理を行う。
 */
@Controller
public class LoginSuccessController {

    private static final Logger logger = LoggerFactory.getLogger(LoginSuccessController.class);

    @Value("${frontend.url}")
    private String frontendUrl;

    /**
     * ログイン成功時に呼び出されるメソッド。
     * ユーザー情報を取得し、フロントエンドアプリケーションにリダイレクトする。
     *
     * @param principal 認証されたOAuth2ユーザーの情報を含むオブジェクト
     * @return ログイン成功後にリダイレクトするURL
     */
    @GetMapping("/loginSuccess")
    public String loginSuccess(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            logger.error("Authentication failed: OAuth2User is null");
            return "redirect:/login?error";
        }

        String userId = principal.getAttribute("id");
        String accessToken = principal.getAttribute("access_token");

        if (userId == null || accessToken == null) {
            logger.warn("Missing user information. UserId: {}, AccessToken: {}", userId, accessToken);
        } else {
            logger.info("User successfully authenticated. UserId: {}", userId);
            logger.debug("Access token: {}", accessToken);
        }

        return "redirect:" + frontendUrl;
    }
}
