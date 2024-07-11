// PlaylistTrackRemovalController.java

package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.PlaylistTrackRemovalRequest;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackRemovalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
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

    /**
     * ロガーのインスタンス
     */
    private static final Logger logger = LoggerFactory.getLogger(PlaylistTrackRemovalController.class);

    /**
     * SpotifyPlaylistTrackRemovalServiceのインスタンス
     */
    @Autowired
    private SpotifyPlaylistTrackRemovalService spotifyPlaylistTrackRemovalService;

    /**
     * プレイリストからトラックを削除するエンドポイント
     *
     * @param request   トラック削除リクエストの詳細を含むオブジェクト
     * @param principal 認証されたユーザー情報
     * @return トラック削除の結果を含むResponseEntity
     */
    @PostMapping("/remove-track")
    public ResponseEntity<String> removeTrackFromPlaylist(
            @RequestBody PlaylistTrackRemovalRequest request,
            @AuthenticationPrincipal OAuth2User principal) {
        logger.info("removeTrackFromPlaylist メソッドが呼び出されました。リクエスト: {}", request);

        // 認証されていない場合は401エラーを返す
        if (principal == null) {
            logger.warn("認証されていないユーザーがアクセスしようとしました。");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"認証が必要です。\"}");
        }

        // トラック削除サービスを呼び出し、結果を確認する
        boolean success = spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(request, principal).hasBody();
        if (success) {
            // 削除成功時のレスポンス
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"message\": \"トラックが正常に削除されました。\"}");
        } else {
            // 削除失敗時のレスポンス
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"トラックの削除に失敗しました。\"}");
        }
    }
}
