package com.github.oosm032519.playlistviewernext.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@RestController
@RequestMapping("/api/playlist")
public class PlaylistAddController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistAddController.class);

    @Autowired
    private SpotifyApi spotifyApi;

    @PostMapping("/add-track")
    public ResponseEntity<String> addTrackToPlaylist(@RequestBody AddTrackRequest request, @AuthenticationPrincipal OAuth2User principal) {
        logger.info("トラック追加リクエストを受信しました。プレイリストID: {}, トラックID: {}", request.getPlaylistId(), request.getTrackId());

        if (principal == null) {
            logger.error("ユーザーが認証されていません。");
            return ResponseEntity.status(401).body("認証が必要です。");
        }

        String accessToken = (String) principal.getAttributes().get("access_token");
        if (accessToken == null) {
            logger.error("アクセストークンが見つかりません。");
            return ResponseEntity.status(401).body("有効なアクセストークンがありません。");
        }

        spotifyApi.setAccessToken(accessToken);

        try {
            String playlistId = request.getPlaylistId();
            String trackId = request.getTrackId();

            String[] uris = new String[]{"spotify:track:" + trackId};

            AddItemsToPlaylistRequest addItemsToPlaylistRequest = spotifyApi
                    .addItemsToPlaylist(playlistId, uris)
                    .build();

            SnapshotResult snapshotResult = addItemsToPlaylistRequest.execute();

            logger.info("トラックが正常に追加されました。Snapshot ID: {}", snapshotResult.getSnapshotId());
            return ResponseEntity.ok("トラックが正常に追加されました。Snapshot ID: " + snapshotResult.getSnapshotId());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("トラックの追加中にエラーが発生しました。", e);
            return ResponseEntity.internalServerError().body("エラー: " + e.getMessage());
        }
    }
}

    @Setter
    @Getter
    class AddTrackRequest {
        // getterとsetterメソッド
        private String playlistId;
    private String trackId;

    }
