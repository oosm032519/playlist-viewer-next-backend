package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.PlaylistTrackAdditionRequest;
import com.github.oosm032519.playlistviewernext.security.UserAuthenticationService;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackAdditionService;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/playlist")
public class PlaylistTrackAdditionController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistTrackAdditionController.class);

    private final UserAuthenticationService userAuthenticationService;
    private final SpotifyPlaylistTrackAdditionService spotifyService;

    public PlaylistTrackAdditionController(UserAuthenticationService userAuthenticationService,
                                           SpotifyPlaylistTrackAdditionService spotifyService) {
        this.userAuthenticationService = userAuthenticationService;
        this.spotifyService = spotifyService;
    }

    /**
     * プレイリストにトラックを追加するエンドポイント
     *
     * @param request   プレイリストIDとトラックIDを含むリクエストボディ
     * @param principal 認証されたユーザー情報
     * @return トラック追加の結果を含むレスポンスエンティティ
     */
    @PostMapping("/add-track")
    public ResponseEntity<Map<String, String>> addTrackToPlaylist(@RequestBody PlaylistTrackAdditionRequest request,
                                                                  @AuthenticationPrincipal OAuth2User principal) {
        logger.info("トラック追加リクエストを受信しました。プレイリストID: {}, トラックID: {}", request.getPlaylistId(), request.getTrackId());

        String accessToken = userAuthenticationService.getAccessToken(principal);
        if (accessToken == null) {
            logger.error("ユーザーが認証されていないか、アクセストークンが見つかりません。");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "認証が必要です。"));
        }

        try {
            SnapshotResult snapshotResult = spotifyService.addTrackToPlaylist(accessToken, request.getPlaylistId(), request.getTrackId());
            logger.info("トラックが正常に追加されました。Snapshot ID: {}", snapshotResult.getSnapshotId());

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "トラックが正常に追加されました。");
            responseBody.put("snapshot_id", snapshotResult.getSnapshotId());

            return ResponseEntity.ok(responseBody);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("トラックの追加中にエラーが発生しました。", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "エラー: " + e.getMessage()));
        }
    }
}
