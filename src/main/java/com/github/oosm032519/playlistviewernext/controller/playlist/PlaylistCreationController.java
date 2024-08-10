package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import com.github.oosm032519.playlistviewernext.security.UserAuthenticationService;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyUserPlaylistCreationService;
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

    private final UserAuthenticationService userAuthenticationService;
    private final SpotifyUserPlaylistCreationService spotifyUserPlaylistCreationService;

    /**
     * PlaylistCreationControllerのコンストラクタ。
     *
     * @param userAuthenticationService          ユーザー認証サービス
     * @param spotifyUserPlaylistCreationService Spotifyユーザープレイリスト作成サービス
     */
    public PlaylistCreationController(UserAuthenticationService userAuthenticationService,
                                      SpotifyUserPlaylistCreationService spotifyUserPlaylistCreationService) {
        this.userAuthenticationService = userAuthenticationService;
        this.spotifyUserPlaylistCreationService = spotifyUserPlaylistCreationService;
    }

    /**
     * プレイリストを作成するエンドポイント。
     * 指定されたトラックIDのリストを使用して新しいプレイリストを作成します。
     *
     * @param trackIds  プレイリストに追加するトラックIDのリスト
     * @param principal 認証されたユーザー情報
     * @return プレイリスト作成の結果を含むResponseEntity
     */
    @PostMapping("/create")
    public ResponseEntity<String> createPlaylist(@RequestBody List<String> trackIds,
                                                 @AuthenticationPrincipal OAuth2User principal) {
        logger.info("プレイリスト作成リクエストを受信しました。トラックID数: {}", trackIds.size());

        // ユーザーのアクセストークンを取得
        String accessToken = userAuthenticationService.getAccessToken(principal);
        if (accessToken == null) {
            throw new PlaylistViewerNextException(
                    HttpStatus.UNAUTHORIZED,
                    "UNAUTHORIZED_ACCESS",
                    "ユーザーが認証されていないか、アクセストークンが見つかりません。"
            );
        }

        // ユーザー情報を取得
        String userId = principal.getAttribute("id");
        String userName = principal.getAttribute("name");
        String playlistName = generatePlaylistName(userName);

        try {
            // プレイリストを作成
            String playlistId = spotifyUserPlaylistCreationService.createPlaylist(accessToken, userId, playlistName, trackIds);
            logger.info("プレイリストが正常に作成されました。プレイリストID: {}", playlistId);
            return ResponseEntity.ok(String.format("{\"playlistId\": \"%s\"}", playlistId));
        } catch (Exception e) {
            // プレイリスト作成中にエラーが発生した場合は PlaylistViewerNextException をスロー
            logger.error("プレイリストの作成中にエラーが発生しました。", e);
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "PLAYLIST_CREATION_ERROR",
                    "プレイリストの作成中にエラーが発生しました。",
                    e
            );
        }
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
}
