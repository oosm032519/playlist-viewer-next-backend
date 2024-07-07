package com.github.oosm032519.playlistviewernext.controller.auth;

import com.github.oosm032519.playlistviewernext.service.auth.SpotifyAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SpotifyClientCredentialsAuthentication {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyClientCredentialsAuthentication.class);

    @Autowired
    private final SpotifyAuthService authService;

    @Autowired
    public SpotifyClientCredentialsAuthentication(SpotifyAuthService authService) {
        this.authService = authService;
    }

    public void authenticate() {
        try {
            authService.getClientCredentialsToken();
        } catch (Exception e) {
            logger.error("SpotifyClientCredentialsAuthentication: 認証中にエラーが発生しました", e);
            throw new RuntimeException("認証エラー", e);
        }
    }
}
