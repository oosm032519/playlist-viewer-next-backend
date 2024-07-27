package com.github.oosm032519.playlistviewernext.service.auth;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;

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
     * @throws IOException            入出力例外
     * @throws SpotifyWebApiException Spotify Web API例外
     * @throws ParseException         HTTPパース例外
     */
    public void getClientCredentialsToken() throws IOException, SpotifyWebApiException, ParseException {
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
        try {
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());
            logger.info("クライアントクレデンシャルトークンが正常に取得されました");
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("クライアントクレデンシャルトークンの取得中にエラーが発生しました", e);
            throw e;
        }
    }
}
