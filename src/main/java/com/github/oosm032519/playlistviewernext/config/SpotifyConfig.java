package com.github.oosm032519.playlistviewernext.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.michaelthelin.spotify.SpotifyApi;

/**
 * Spotify APIの設定を管理するConfigurationクラス。
 * アプリケーションのSpotify API認証情報を設定し、SpotifyApiインスタンスを提供する。
 */
@Configuration
public class SpotifyConfig {

    /**
     * Spotify APIのクライアントID。
     * application.propertiesから注入される。
     */
    @Value("${spotify.client.id}")
    private String clientId;

    /**
     * Spotify APIのクライアントシークレット。
     * application.propertiesから注入される。
     */
    @Value("${spotify.client.secret}")
    private String clientSecret;

    /**
     * SpotifyApiインスタンスを生成し、Spring IoCコンテナに登録する。
     * このインスタンスは、アプリケーション全体でSpotify APIとの通信に使用される。
     *
     * @return 設定済みのSpotifyApiインスタンス
     */
    @Bean
    public SpotifyApi spotifyApi() {
        // クライアントIDとシークレットを使用してSpotifyApiインスタンスを構築
        return new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build();
    }
}
