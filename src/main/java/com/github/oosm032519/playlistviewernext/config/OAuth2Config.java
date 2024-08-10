package com.github.oosm032519.playlistviewernext.config;

import com.github.oosm032519.playlistviewernext.service.auth.SpotifyOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OAuth2Config {

    @Bean
    public SpotifyOAuth2UserService spotifyOAuth2UserService() {
        return new SpotifyOAuth2UserService();
    }
}
