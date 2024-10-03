package com.github.oosm032519.playlistviewernext.service.auth;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

/**
 * Spotify の認証サービス。クライアントクレデンシャルトークンの取得に使用される
 */
@Service
public class SpotifyAuthService {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyAuthService.class);

    private final SpotifyApi spotifyApi;

    /**
     * SpotifyAuthService のコンストラクタ
     *
     * @param spotifyApi Spotify API のインスタンス
     */
    public SpotifyAuthService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * クライアントクレデンシャルフローを使用して Spotify API のアクセストークンを取得する
     *
     * @throws InternalServerException アクセストークンの取得中にエラーが発生した場合
     */
    public void getClientCredentialsToken() throws SpotifyWebApiException {
        RetryUtil.executeWithRetry(() -> {
            try {
                ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
                ClientCredentials clientCredentials = clientCredentialsRequest.execute();
                spotifyApi.setAccessToken(clientCredentials.getAccessToken());
                logger.info("クライアントクレデンシャルトークンが正常に取得されました");
                return null; // void メソッドなので null を返す
            } catch (SpotifyWebApiException e) {
                // SpotifyWebApiException はそのまま再スロー
                logger.error("Spotify API エラー: {}", e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                // その他の例外は InternalServerException にラップしてスロー
                logger.error("クライアントクレデンシャルトークンの取得中にエラーが発生しました", e);
                throw new InternalServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "クライアントクレデンシャルトークンの取得中にエラーが発生しました。",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS);
    }
}
