// PlaylistTrackAdditionController.java

package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.PlaylistTrackAdditionRequest;
import com.github.oosm032519.playlistviewernext.security.UserAuthenticationService;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackAdditionService;
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
public class PlaylistTrackAdditionController {

    // ロガーのインスタンスを作成
    private static final Logger logger = LoggerFactory.getLogger(PlaylistTrackAdditionController.class);

    // UserAuthenticationServiceのインスタンスを注入
    @Autowired
    private UserAuthenticationService userAuthenticationService;

    // SpotifyPlaylistTrackAdditionServiceのインスタンスを注入
    @Autowired
    private SpotifyPlaylistTrackAdditionService spotifyService;

    /**
     * プレイリストにトラックを追加するエンドポイント
     *
     * @param request   プレイリストIDとトラックIDを含むリクエストボディ
     * @param principal 認証されたユーザー情報
     * @return トラック追加の結果を含むレスポンスエンティティ
     */
    @PostMapping("/add-track")
    public ResponseEntity<String> addTrackToPlaylist(@RequestBody PlaylistTrackAdditionRequest request, @AuthenticationPrincipal OAuth2User principal) {
        // トラック追加リクエストを受信したことをログに記録
        logger.info("トラック追加リクエストを受信しました。プレイリストID: {}, トラックID: {}", request.getPlaylistId(), request.getTrackId());

        // ユーザーのアクセストークンを取得
        String accessToken = userAuthenticationService.getAccessToken(principal);
        if (accessToken == null) {
            // アクセストークンが取得できない場合はエラーログを記録し、401エラーを返す
            logger.error("ユーザーが認証されていないか、アクセストークンが見つかりません。");
            return ResponseEntity.status(401).body("認証が必要です。");
        }

        try {
            // Spotify APIを使用してプレイリストにトラックを追加
            SnapshotResult snapshotResult = spotifyService.addTrackToPlaylist(accessToken, request.getPlaylistId(), request.getTrackId());
            // トラックが正常に追加されたことをログに記録
            logger.info("トラックが正常に追加されました。Snapshot ID: {}", snapshotResult.getSnapshotId());
            return ResponseEntity.ok("トラックが正常に追加されました。Snapshot ID: " + snapshotResult.getSnapshotId());
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            // トラック追加中にエラーが発生した場合はエラーログを記録し、500エラーを返す
            logger.error("トラックの追加中にエラーが発生しました。", e);
            return ResponseEntity.internalServerError().body("エラー: " + e.getMessage());
        }
    }
}
