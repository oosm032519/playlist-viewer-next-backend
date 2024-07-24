package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.PlaylistTrackAdditionRequest;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackAdditionService;
import jakarta.servlet.http.HttpSession;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;

import java.io.IOException;

@RestController
@RequestMapping("/api/playlist")
public class PlaylistTrackAdditionController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistTrackAdditionController.class);

    private final SpotifyPlaylistTrackAdditionService spotifyService;

    public PlaylistTrackAdditionController(SpotifyPlaylistTrackAdditionService spotifyService) {
        this.spotifyService = spotifyService;
    }

    /**
     * プレイリストにトラックを追加するエンドポイント
     *
     * @param request プレイリストIDとトラックIDを含むリクエストボディ
     * @param session 現在のセッション
     * @return トラック追加の結果を含むレスポンスエンティティ
     */
    @PostMapping("/add-track")
    public ResponseEntity<String> addTrackToPlaylist(@RequestBody PlaylistTrackAdditionRequest request, HttpSession session) {
        logger.info("トラック追加リクエストを受信しました。プレイリストID: {}, トラックID: {}", request.getPlaylistId(), request.getTrackId());

        // セッションからアクセストークンを取得
        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            logger.error("ユーザーが認証されていないか、アクセストークンが見つかりません。");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("認証が必要です。");
        }

        try {
            SnapshotResult snapshotResult = spotifyService.addTrackToPlaylist(accessToken, request.getPlaylistId(), request.getTrackId());
            logger.info("トラックが正常に追加されました。Snapshot ID: {}", snapshotResult.getSnapshotId());
            return ResponseEntity.ok("トラックが正常に追加されました。Snapshot ID: " + snapshotResult.getSnapshotId());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("トラックの追加中にエラーが発生しました。", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("エラー: " + e.getMessage());
        }
    }
}
