// SpotifyClientCredentialsAuthentication.java

package com.github.oosm032519.playlistviewernext.controller.auth;

import com.github.oosm032519.playlistviewernext.service.auth.SpotifyAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spotifyのクライアントクレデンシャル認証を行うコントローラークラス
 */
@RestController
public class SpotifyClientCredentialsAuthentication {

    // ロガーのインスタンスを生成
    private static final Logger logger = LoggerFactory.getLogger(SpotifyClientCredentialsAuthentication.class);

    // SpotifyAuthServiceのインスタンスをインジェクション
    @Autowired
    private final SpotifyAuthService authService;

    /**
     * コンストラクタでSpotifyAuthServiceをインジェクション
     *
     * @param authService Spotifyの認証サービス
     */
    @Autowired
    public SpotifyClientCredentialsAuthentication(SpotifyAuthService authService) {
        this.authService = authService;
    }

    /**
     * Spotifyのクライアントクレデンシャル認証を実行するメソッド
     */
    public void authenticate() {
        try {
            // クライアントクレデンシャルトークンを取得
            authService.getClientCredentialsToken();
        } catch (Exception e) {
            // 認証中にエラーが発生した場合、エラーログを出力し、RuntimeExceptionをスロー
            logger.error("SpotifyClientCredentialsAuthentication: 認証中にエラーが発生しました", e);
            throw new RuntimeException("認証エラー", e);
        }
    }
}
