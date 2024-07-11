// SpotifyAuthService.java

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

@Service
public class SpotifyAuthService {
    private final SpotifyApi spotifyApi;
    private final OAuth2AuthorizedClientService authorizedClientService;

    /**
     * SpotifyAuthServiceのコンストラクタ。
     * SpotifyApiとOAuth2AuthorizedClientServiceのインスタンスを注入する。
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
     */
    public void setAccessToken(OAuth2AuthenticationToken authentication) {
        // 認証されたクライアントからアクセストークンを取得
        String accessToken = authorizedClientService.loadAuthorizedClient("spotify", authentication.getName())
                .getAccessToken().getTokenValue();
        // Spotify APIにアクセストークンを設定
        spotifyApi.setAccessToken(accessToken);
    }

    /**
     * クライアントクレデンシャルフローを使用してSpotify APIのアクセストークンを取得する。
     *
     * @throws IOException                             入出力例外
     * @throws SpotifyWebApiException                  Spotify Web API例外
     * @throws org.apache.hc.core5.http.ParseException HTTPパース例外
     */
    public void getClientCredentialsToken() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        // クライアントクレデンシャルリクエストを作成
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
        // リクエストを実行してクライアントクレデンシャルを取得
        ClientCredentials clientCredentials = clientCredentialsRequest.execute();
        // Spotify APIにアクセストークンを設定
        spotifyApi.setAccessToken(clientCredentials.getAccessToken());
    }
}
