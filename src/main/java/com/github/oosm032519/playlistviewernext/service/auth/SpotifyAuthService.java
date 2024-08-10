package com.github.oosm032519.playlistviewernext.service.auth;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
     * SpotifyAuthServiceのコンストラクタ。
     *
     * @param spotifyApi Spotify APIのインスタンス
     */
    @Autowired
    public SpotifyAuthService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * クライアントクレデンシャルフローを使用してSpotify APIのアクセストークンを取得する。
     *
     * @throws PlaylistViewerNextException アクセストークンの取得中にエラーが発生した場合
     */
    public void getClientCredentialsToken() {
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
        try {
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            logger.info("クライアントクレデンシャルトークンが正常に取得されました");
        } catch (Exception e) {
            // アクセストークンの取得中にエラーが発生した場合は PlaylistViewerNextException をスロー
            logger.error("クライアントクレデンシャルトークンの取得中にエラーが発生しました", e);
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "CLIENT_CREDENTIALS_AUTH_ERROR",
                    "クライアントクレデンシャルトークンの取得中にエラーが発生しました。",
                    e
            );
        }
    }
}
