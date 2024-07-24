package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.PlaylistTrackRemovalRequest;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackRemovalService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * プレイリストからトラックを削除するためのコントローラークラス
 */
@RestController
@RequestMapping("/api/playlist")
public class PlaylistTrackRemovalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistTrackRemovalController.class);
    private static final String JSON_CONTENT_TYPE = MediaType.APPLICATION_JSON_VALUE;

    private final SpotifyPlaylistTrackRemovalService spotifyPlaylistTrackRemovalService;

    public PlaylistTrackRemovalController(SpotifyPlaylistTrackRemovalService spotifyPlaylistTrackRemovalService) {
        this.spotifyPlaylistTrackRemovalService = spotifyPlaylistTrackRemovalService;
    }

    /**
     * プレイリストからトラックを削除するエンドポイント
     *
     * @param request トラック削除リクエストの詳細を含むオブジェクト
     * @param session 現在のセッション
     * @return トラック削除の結果を含むResponseEntity
     */
    @PostMapping("/remove-track")
    public ResponseEntity<String> removeTrackFromPlaylist(
            @RequestBody PlaylistTrackRemovalRequest request, HttpSession session) {
        LOGGER.info("removeTrackFromPlaylist メソッドが呼び出されました。リクエスト: {}", request);

        String accessToken = (String) session.getAttribute("accessToken");
        if (accessToken == null) {
            LOGGER.warn("認証されていないユーザーがアクセスしようとしました。");
            return createJsonResponse(HttpStatus.UNAUTHORIZED, "{\"error\": \"認証が必要です。\"}");
        }

        boolean success = spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(request, accessToken).hasBody();
        return success
                ? createJsonResponse(HttpStatus.OK, "{\"message\": \"トラックが正常に削除されました。\"}")
                : createJsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, "{\"error\": \"トラックの削除に失敗しました。\"}");
    }

    private ResponseEntity<String> createJsonResponse(HttpStatus status, String body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.parseMediaType(JSON_CONTENT_TYPE))
                .body(body);
    }
}
