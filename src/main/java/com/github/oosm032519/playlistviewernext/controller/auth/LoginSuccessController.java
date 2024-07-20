package com.github.oosm032519.playlistviewernext.controller.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ログイン成功時の処理を担当するコントローラークラス。
 * OAuth2認証後のユーザー情報の取得とリダイレクト処理を行う。
 */
@Controller
public class LoginSuccessController {

    /**
     * このクラスのロガーインスタンス。
     * ログイン処理に関する情報やエラーのログ出力に使用する。
     */
    private static final Logger logger = LoggerFactory.getLogger(LoginSuccessController.class);

    /**
     * フロントエンドアプリケーションのURL。
     * application.propertiesまたはapplication.ymlから注入される。
     */
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
        // 認証が失敗した場合（principalがnullの場合）のエラーハンドリング
        if (principal == null) {
            logger.error("認証に失敗しました: OAuth2Userがnullです");
            return "redirect:/login?error";
        }

        // ユーザーIDとアクセストークンの取得
        String userId = principal.getAttribute("id");
        String accessToken = principal.getAttribute("access_token");

        // ユーザー情報の欠落チェックとログ出力
        if (userId == null || accessToken == null) {
            logger.warn("ユーザー情報が不足しています。UserId: {}, AccessToken: {}", userId, accessToken);
        } else {
            logger.info("ユーザーが正常に認証されました。UserId: {}", userId);
            logger.debug("アクセストークン: {}", accessToken);
        }

        // フロントエンドアプリケーションへのリダイレクト
        return "redirect:" + frontendUrl;
    }
}
