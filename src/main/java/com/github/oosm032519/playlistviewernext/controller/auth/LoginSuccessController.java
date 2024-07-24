package com.github.oosm032519.playlistviewernext.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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
     * フロントエンドアプリケーションにリダイレクトする。
     *
     * @param request HTTPリクエスト
     * @return ログイン成功後にリダイレクトするURL
     */
    @GetMapping("/loginSuccess")
    public String loginSuccess(HttpServletRequest request) {
        logger.info("loginSuccessメソッドが呼び出されました。処理を開始します。");

        // セッションからユーザーIDを取得 (SecurityConfig で設定済み)
        String userId = (String) request.getSession().getAttribute("userId");

        if (userId == null) {
            logger.error("認証に失敗しました: セッションにユーザーIDがありません。ログインページにリダイレクトします。");
            return "redirect:/login?error";
        }

        logger.info("ユーザーが正常に認証されました。UserId: {}", userId);
        logger.info("フロントエンドURL: {}", frontendUrl);

        // フロントエンドアプリケーションへのリダイレクト
        logger.info("フロントエンドアプリケーションへリダイレクトします。");
        return "redirect:" + frontendUrl;
    }
}
