package com.github.oosm032519.playlistviewernext.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class SpotifyAuthService {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyAuthService.class);
    private static final String SPOTIFY_CLIENT_ID = "spotify";

    private final SpotifyApi spotifyApi;
    private final OAuth2AuthorizedClientService authorizedClientService;

    /**
     * SpotifyAuthServiceのコンストラクタ。
     *
     * @param spotifyApi              Spotify APIのインスタンス
     * @param authorizedClientService OAuth2認証クライアントサービスのインスタンス
     */
    @Autowired
    public SpotifyAuthService(SpotifyApi spotifyApi, OAuth2AuthorizedClientService authorizedClientService) {
        this.spotifyApi = spotifyApi;
        this.authorizedClientService = authorizedClientService;
    }

    /**
     * 認証情報を使用してSpotify APIのアクセストークンを設定する。
     *
     * @param authentication OAuth2認証トークン
     * @throws NullPointerException 認証されたクライアントが見つからない場合
     */
    public void setAccessToken(OAuth2AuthenticationToken authentication) {
        var authorizedClient = authorizedClientService.loadAuthorizedClient(SPOTIFY_CLIENT_ID, authentication.getName());
        if (authorizedClient == null) {
            logger.error("認証されたクライアントが見つかりません");
            throw new NullPointerException("認証されたクライアントが見つかりません");
        }
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        spotifyApi.setAccessToken(accessToken);
        logger.info("アクセストークンが正常に設定されました");
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
