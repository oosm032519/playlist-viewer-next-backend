package com.github.oosm032519.playlistviewernext.config;

import com.github.oosm032519.playlistviewernext.service.auth.SpotifyOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OAuth2認証に関する設定を提供するConfiguration クラスである。
 * Spotify APIとの認証統合のための設定を含む。
 */
@Configuration
public class OAuth2Config {

    /**
     * SpotifyOAuth2UserService のインスタンスを生成し、Bean として登録する。
     * このサービスは Spotify の OAuth2 認証フローでユーザー情報を処理する。
     *
     * @return 新しい SpotifyOAuth2UserService インスタンス
     */
    @Bean
    public SpotifyOAuth2UserService spotifyOAuth2UserService() {
        return new SpotifyOAuth2UserService();
    }
}
