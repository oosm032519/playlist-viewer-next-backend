package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyUserPlaylistCreationService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * プレイリスト作成のためのコントローラークラス。
 * このクラスはSpotify APIを使用してユーザーのプレイリストを作成するエンドポイントを提供します。
 */
@RestController
@RequestMapping("/api/playlists")
public class PlaylistCreationController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistCreationController.class);
    private static final String PLAYLIST_NAME_FORMAT = "%s さんへのおすすめ楽曲 %s";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");

    private final SpotifyUserPlaylistCreationService spotifyUserPlaylistCreationService;

    /**
     * PlaylistCreationControllerのコンストラクタ。
     *
     * @param spotifyUserPlaylistCreationService Spotifyユーザープレイリスト作成サービス
     */
    public PlaylistCreationController(SpotifyUserPlaylistCreationService spotifyUserPlaylistCreationService) {
        this.spotifyUserPlaylistCreationService = spotifyUserPlaylistCreationService;
    }

    /**
     * プレイリストを作成するエンドポイント。
     * 指定されたトラックIDのリストを使用して新しいプレイリストを作成します。
     *
     * @param trackIds プレイリストに追加するトラックIDのリスト
     * @param session  現在のセッション
     * @return プレイリスト作成の結果を含むResponseEntity
     */
    @PostMapping("/create")
    public ResponseEntity<String> createPlaylist(@RequestBody List<String> trackIds, HttpSession session) {
        logger.info("プレイリスト作成リクエストを受信しました。トラックID数: {}", trackIds.size());

        String accessToken = (String) session.getAttribute("accessToken");
        String userId = (String) session.getAttribute("userId");
        String displayName = (String) session.getAttribute("displayName");

        if (accessToken == null || userId == null) {
            return handleAuthenticationError();
        }

        if (displayName == null) {
            displayName = "あなた"; // デフォルト値
        }

        String playlistName = generatePlaylistName(displayName);

        try {
            String playlistId = spotifyUserPlaylistCreationService.createPlaylist(accessToken, userId, playlistName, trackIds);
            logger.info("プレイリストが正常に作成されました。プレイリストID: {}", playlistId);
            return ResponseEntity.ok(String.format("{\"playlistId\": \"%s\"}", playlistId));
        } catch (Exception e) {
            return handlePlaylistCreationError(e);
        }
    }

    /**
     * 認証エラーを処理するプライベートメソッド。
     *
     * @return 認証エラーを示すResponseEntity
     */
    private ResponseEntity<String> handleAuthenticationError() {
        logger.error("ユーザーが認証されていないか、アクセストークンが見つかりません。");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"認証が必要です。\"}");
    }

    /**
     * プレイリスト名を生成するプライベートメソッド。
     *
     * @param userName ユーザー名
     * @return 生成されたプレイリスト名
     */
    private String generatePlaylistName(String userName) {
        return String.format(PLAYLIST_NAME_FORMAT, userName, LocalDateTime.now().format(DATE_TIME_FORMATTER));
    }

    /**
     * プレイリスト作成エラーを処理するプライベートメソッド。
     *
     * @param e 発生した例外
     * @return エラー情報を含むResponseEntity
     */
    private ResponseEntity<String> handlePlaylistCreationError(Exception e) {
        logger.error("プレイリストの作成中にエラーが発生しました。", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(String.format("{\"error\": \"エラー: %s\"}", e.getMessage()));
    }
}
