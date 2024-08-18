package com.github.oosm032519.playlistviewernext.controller.auth;

import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
import com.github.oosm032519.playlistviewernext.service.auth.SpotifyAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spotifyのクライアントクレデンシャル認証を処理するコントローラークラス
 * Spotify APIとの認証プロセスを管理し、エラーハンドリングを行う
 */
@RestController
public class SpotifyClientCredentialsAuthentication {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpotifyClientCredentialsAuthentication.class);

    private final SpotifyAuthService authService;
    private final HttpServletRequest request;

    /**
     * SpotifyClientCredentialsAuthenticationクラスのコンストラクタ
     *
     * @param authService Spotifyの認証サービス。クライアントクレデンシャルトークンの取得に使用される
     * @param request     HTTPリクエスト。エラー発生時のリクエストパラメータ取得に使用される
     */
    @Autowired
    public SpotifyClientCredentialsAuthentication(SpotifyAuthService authService, HttpServletRequest request) {
        this.authService = authService;
        this.request = request;
    }

    /**
     * Spotifyのクライアントクレデンシャル認証を実行するメソッド
     * 認証に成功した場合はログを出力し、エラーが発生した場合は適切な例外をスローする
     *
     * @throws SpotifyApiException 認証プロセス中にエラーが発生した場合
     */
    public void authenticate() {
        try {
            authService.getClientCredentialsToken();
            LOGGER.info("クライアントクレデンシャル認証が成功しました。");
        } catch (SpotifyApiException e) {
            // Spotify API固有のエラーはそのまま再スロー
            throw e;
        } catch (Exception e) {
            // 予期しないエラーの場合、カスタムのSpotifyApiExceptionを生成してスロー
            LOGGER.error("クライアントクレデンシャル認証中に予期しないエラーが発生しました", e);
            String requestParams = getRequestParams();
            throw new SpotifyApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "CLIENT_CREDENTIALS_AUTH_ERROR",
                    "Spotify APIへの接続中にエラーが発生しました。しばらく時間をおいてから再度お試しください。",
                    "リクエストパラメータ: " + requestParams,
                    e
            );
        }
    }

    /**
     * 現在のHTTPリクエストからすべてのパラメータを取得し、文字列として整形するヘルパーメソッド
     *
     * @return リクエストパラメータを含む文字列
     */
    private String getRequestParams() {
        StringBuilder params = new StringBuilder();
        request.getParameterMap().forEach((key, values) -> {
            params.append(key).append("=").append(String.join(",", values)).append("&");
        });
        // 最後の '&' を削除
        if (params.length() > 0) {
            params.deleteCharAt(params.length() - 1);
        }
        return params.toString();
    }
}
