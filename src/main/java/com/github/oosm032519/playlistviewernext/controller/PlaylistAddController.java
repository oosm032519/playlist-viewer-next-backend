package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.model.AddTrackRequest;
import com.github.oosm032519.playlistviewernext.security.AuthService;
import com.github.oosm032519.playlistviewernext.service.PlaylistAddSearvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@RestController
@RequestMapping("/api/playlist")
public class PlaylistAddController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistAddController.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private PlaylistAddSearvice spotifyService;

    @PostMapping("/add-track")
    public ResponseEntity<String> addTrackToPlaylist(@RequestBody AddTrackRequest request, @AuthenticationPrincipal OAuth2User principal) {
        logger.info("トラック追加リクエストを受信しました。プレイリストID: {}, トラックID: {}", request.getPlaylistId(), request.getTrackId());

        String accessToken = authService.getAccessToken(principal);
        if (accessToken == null) {
            logger.error("ユーザーが認証されていないか、アクセストークンが見つかりません。");
            return ResponseEntity.status(401).body("認証が必要です。");
        }

        try {
            SnapshotResult snapshotResult = spotifyService.addTrackToPlaylist(accessToken, request.getPlaylistId(), request.getTrackId());
            logger.info("トラックが正常に追加されました。Snapshot ID: {}", snapshotResult.getSnapshotId());
            return ResponseEntity.ok("トラックが正常に追加されました。Snapshot ID: " + snapshotResult.getSnapshotId());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("トラックの追加中にエラーが発生しました。", e);
            return ResponseEntity.internalServerError().body("エラー: " + e.getMessage());
        }
    }
}
