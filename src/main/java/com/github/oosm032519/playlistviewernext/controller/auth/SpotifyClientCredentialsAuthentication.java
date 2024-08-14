package com.github.oosm032519.playlistviewernext.controller.auth;

import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
import com.github.oosm032519.playlistviewernext.service.auth.SpotifyAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spotifyのクライアントクレデンシャル認証を行うコントローラークラス。
 */
@RestController
public class SpotifyClientCredentialsAuthentication {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyClientCredentialsAuthentication.class);

    private final SpotifyAuthService authService;

    /**
     * コンストラクタでSpotifyAuthServiceをインジェクション。
     *
     * @param authService Spotifyの認証サービス
     */
    @Autowired
    public SpotifyClientCredentialsAuthentication(SpotifyAuthService authService) {
        this.authService = authService;
    }

    /**
     * Spotifyのクライアントクレデンシャル認証を実行。
     *
     * @throws SpotifyApiException 認証中にエラーが発生した場合
     */
    public void authenticate() {
        try {
            authService.getClientCredentialsToken();
            LOGGER.info("クライアントクレデンシャル認証が成功しました。");
        } catch (SpotifyApiException e) {
            // Spotify API エラーはそのまま再スロー
            throw e;
        } catch (Exception e) {
            // 認証中に予期しないエラーが発生した場合は SpotifyApiException をスロー
            LOGGER.error("クライアントクレデンシャル認証中に予期しないエラーが発生しました", e);
            throw new SpotifyApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "CLIENT_CREDENTIALS_AUTH_ERROR",
                    "Spotify APIへの接続中にエラーが発生しました。しばらく時間をおいてから再度お試しください。",
                    e
            );
        }
    }
}
