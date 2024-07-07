package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.service.SpotifyAuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlaylistAuthController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistAuthController.class);

    @Autowired
    private final SpotifyAuthService authService;

    @Autowired
    public PlaylistAuthController(SpotifyAuthService authService) {
        this.authService = authService;
    }

    public void authenticate() {
        try {
            authService.getClientCredentialsToken();
        } catch (Exception e) {
            logger.error("PlaylistAuthController: 認証中にエラーが発生しました", e);
            throw new RuntimeException("認証エラー", e);
        }
    }
}
