package com.github.oosm032519.playlistviewernext.service.auth;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

@Service
public class SpotifyAuthService {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyAuthService.class);

    private final SpotifyApi spotifyApi;

    /**
     * SpotifyAuthServiceのコンストラクタ
     *
     * @param spotifyApi Spotify APIのインスタンス
     */
    public SpotifyAuthService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * クライアントクレデンシャルフローを使用してSpotify APIのアクセストークンを取得する
     *
     * @throws SpotifyApiException アクセストークンの取得中にエラーが発生した場合
     */
    public void getClientCredentialsToken() {
        RetryUtil.executeWithRetry(() -> {
            try {
                ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
                ClientCredentials clientCredentials = clientCredentialsRequest.execute();
                spotifyApi.setAccessToken(clientCredentials.getAccessToken());
                logger.info("クライアントクレデンシャルトークンが正常に取得されました");
                return null; // void メソッドなので null を返す
            } catch (Exception e) {
                // アクセストークンの取得中にエラーが発生した場合は SpotifyApiException をスロー
                logger.error("クライアントクレデンシャルトークンの取得中にエラーが発生しました", e);
                throw new InternalServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "クライアントクレデンシャルトークンの取得中にエラーが発生しました。",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS); // 最大3回再試行、初期間隔は RetryUtil のデフォルト値
    }
}
