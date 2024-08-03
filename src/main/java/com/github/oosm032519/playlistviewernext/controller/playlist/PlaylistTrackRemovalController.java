package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.PlaylistTrackRemovalRequest;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackRemovalService;
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

import java.util.Map;

/**
 * プレイリストからトラックを削除するためのコントローラークラス
 */
@RestController
@RequestMapping("/api/playlist")
public class PlaylistTrackRemovalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistTrackRemovalController.class);

    private final SpotifyPlaylistTrackRemovalService spotifyPlaylistTrackRemovalService;

    public PlaylistTrackRemovalController(SpotifyPlaylistTrackRemovalService spotifyPlaylistTrackRemovalService) {
        this.spotifyPlaylistTrackRemovalService = spotifyPlaylistTrackRemovalService;
    }

    /**
     * プレイリストからトラックを削除するエンドポイント
     *
     * @param request   トラック削除リクエストの詳細を含むオブジェクト
     * @param principal 認証されたユーザー情報
     * @return トラック削除の結果を含むResponseEntity
     */
    @PostMapping("/remove-track")
    public ResponseEntity<Map<String, String>> removeTrackFromPlaylist(
            @RequestBody PlaylistTrackRemovalRequest request,
            @AuthenticationPrincipal OAuth2User principal) {
        LOGGER.info("removeTrackFromPlaylist メソッドが呼び出されました。リクエスト: {}", request);

        if (principal == null) {
            LOGGER.warn("認証されていないユーザーがアクセスしようとしました。");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "認証が必要です。"));
        }

        ResponseEntity<String> response = spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(request, principal);
        if (response.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.ok(Map.of("message", "トラックが正常に削除されました。"));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "トラックの削除に失敗しました。"));
        }
    }
}
