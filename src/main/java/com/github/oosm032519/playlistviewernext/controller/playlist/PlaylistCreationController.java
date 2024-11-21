package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.model.CreatePlaylistRequest;
import com.github.oosm032519.playlistviewernext.security.UserAuthenticationService;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyUserPlaylistCreationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * プレイリスト作成のためのコントローラークラス
 * ユーザーのプレイリストを作成するエンドポイントを提供する
 */
@RestController
@RequestMapping("/api/playlists")
@Validated
public class PlaylistCreationController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistCreationController.class);
    private static final String PLAYLIST_NAME_FORMAT = "%s さんへのおすすめ楽曲 %s";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");

    private final UserAuthenticationService userAuthenticationService;
    private final SpotifyUserPlaylistCreationService spotifyUserPlaylistCreationService;

    /**
     * PlaylistCreationControllerのコンストラクタ
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
     * プレイリストを作成するエンドポイント
     * 指定されたトラックIDのリストを使用して新しいプレイリストを作成する
     *
     * @param request   リクエストボディ。トラックIDのリストとプレイリスト名を含む
     * @param principal 認証されたユーザー情報
     * @return プレイリスト作成の結果を含むResponseEntity
     */
    @PostMapping("/create")
    public ResponseEntity<String> createPlaylist(
            @Valid @RequestBody CreatePlaylistRequest request, // リクエストボディからデータを取得
            @AuthenticationPrincipal OAuth2User principal
    ) throws SpotifyWebApiException {
        logger.info("プレイリスト作成リクエストを受信しました。トラックID数: {}", request.getTrackIds().size());

        // ユーザーのアクセストークンを取得
        String accessToken = userAuthenticationService.getAccessToken(principal);
        if (accessToken == null) {
            throw new AuthenticationException(
                    HttpStatus.UNAUTHORIZED,
                    "ユーザーが認証されていないか、アクセストークンが見つかりません。"
            );
        }

        // ユーザー情報を取得
        String userId = principal.getAttribute("id");
        String userName = principal.getAttribute("name");

        // プレイリスト名を設定。リクエストで指定された名前があれば使用、なければデフォルトの名前を生成
        String finalPlaylistName = request.getPlaylistName() != null ? request.getPlaylistName() : generatePlaylistName(userName);


        // プレイリストを作成
        String playlistId = spotifyUserPlaylistCreationService.createPlaylist(accessToken, userId, finalPlaylistName, request.getTrackIds());
        logger.info("プレイリストが正常に作成されました。プレイリストID: {}", playlistId);
        return ResponseEntity.ok(String.format("{\"playlistId\": \"%s\"}", playlistId));
    }

    /**
     * プレイリスト名を生成するプライベートメソッド
     *
     * @param userName ユーザー名
     * @return 生成されたプレイリスト名
     */
    public String generatePlaylistName(String userName) {
        return String.format(PLAYLIST_NAME_FORMAT, userName, LocalDateTime.now().format(DATE_TIME_FORMATTER));
    }
}
