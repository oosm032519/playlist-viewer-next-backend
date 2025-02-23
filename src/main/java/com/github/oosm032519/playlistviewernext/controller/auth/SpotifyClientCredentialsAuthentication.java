package com.github.oosm032519.playlistviewernext.controller.auth;

import com.github.oosm032519.playlistviewernext.service.auth.SpotifyAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

/**
 * Spotifyのクライアントクレデンシャル認証を処理するコントローラークラス
 * Spotify APIとの認証プロセスを管理し、エラーハンドリングを行う
 */
@RestController
@ConditionalOnProperty(name = "spotify.mock.enabled", havingValue = "false", matchIfMissing = true)
public class SpotifyClientCredentialsAuthentication {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyClientCredentialsAuthentication.class);

    private final SpotifyAuthService authService;

    /**
     * SpotifyClientCredentialsAuthenticationクラスのコンストラクタ
     *
     * @param authService Spotifyの認証サービス。クライアントクレデンシャルトークンの取得に使用される
     */
    @Autowired
    public SpotifyClientCredentialsAuthentication(SpotifyAuthService authService) {
        this.authService = authService;
    }

    /**
     * Spotifyのクライアントクレデンシャル認証を実行するメソッド
     * 認証に成功した場合はログを出力し、エラーが発生した場合は適切な例外をスローする
     */
    public void authenticate() throws SpotifyWebApiException {
        authService.getClientCredentialsToken();
        LOGGER.info("クライアントクレデンシャル認証が成功しました。");
    }
}
