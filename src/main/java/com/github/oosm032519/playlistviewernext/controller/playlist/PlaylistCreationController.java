// PlaylistCreationController.java

package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.security.UserAuthenticationService;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyUserPlaylistCreationService;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * プレイリスト作成のためのコントローラークラス
 */
@RestController
@RequestMapping("/api/playlists")
public class PlaylistCreationController {

    // ロガーの初期化
    private static final Logger logger = LoggerFactory.getLogger(PlaylistCreationController.class);

    // ユーザー認証サービスの自動注入
    @Autowired
    private UserAuthenticationService userAuthenticationService;

    // Spotifyユーザープレイリスト作成サービスの自動注入
    @Autowired
    private SpotifyUserPlaylistCreationService spotifyUserPlaylistCreationService;

    /**
     * プレイリストを作成するエンドポイント
     *
     * @param trackIds  プレイリストに追加するトラックIDのリスト
     * @param principal 認証されたユーザー情報
     * @return プレイリスト作成の結果を含むレスポンスエンティティ
     */
    @PostMapping("/create")
    public ResponseEntity<String> createPlaylist(
            @RequestBody List<String> trackIds,
            @AuthenticationPrincipal OAuth2User principal
    ) {
        // プレイリスト作成リクエストの受信をログに記録
        logger.info("プレイリスト作成リクエストを受信しました。トラックID数: {}", trackIds.size());

        // アクセストークンの取得
        String accessToken = userAuthenticationService.getAccessToken(principal);
        if (accessToken == null) {
            // 認証エラーのログ記録とレスポンスの返却
            logger.error("ユーザーが認証されていないか、アクセストークンが見つかりません。");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"認証が必要です。\"}");
        }

        // ユーザーIDと表示名の取得
        String userId = principal.getAttribute("id");
        String userName = (String) principal.getAttributes().get("display_name");

        // プレイリスト名の生成
        String playlistName = String.format("%s さんへのおすすめ楽曲 %s", userName, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss")));

        try {
            // プレイリストの作成
            String playlistId = spotifyUserPlaylistCreationService.createPlaylist(accessToken, userId, playlistName, trackIds);
            // プレイリスト作成成功のログ記録とレスポンスの返却
            logger.info("プレイリストが正常に作成されました。プレイリストID: {}", playlistId);
            return ResponseEntity.ok("{\"playlistId\": \"" + playlistId + "\"}");
        } catch (Exception e) {
            // プレイリスト作成中のエラーをログに記録し、エラーレスポンスを返却
            logger.error("プレイリストの作成中にエラーが発生しました。", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("{\"error\": \"エラー: " + e.getMessage() + "\"}");
        }
    }
}
