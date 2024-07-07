package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.model.PlaylistTrackRemovalRequest;
import com.github.oosm032519.playlistviewernext.service.SpotifyPlaylistTrackRemovalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playlist")
public class PlaylistTrackRemovalController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistTrackRemovalController.class);

    @Autowired
    private SpotifyPlaylistTrackRemovalService spotifyPlaylistTrackRemovalService;

    @PostMapping("/remove-track")
    public ResponseEntity<String> removeTrackFromPlaylist(
            @RequestBody PlaylistTrackRemovalRequest request,
            @AuthenticationPrincipal OAuth2User principal) {
        logger.info("removeTrackFromPlaylist メソッドが呼び出されました。リクエスト: {}", request);

        if (principal == null) {
            logger.warn("認証されていないユーザーがアクセスしようとしました。");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("認証が必要です。");
        }

        return spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(request, principal);
    }
}
