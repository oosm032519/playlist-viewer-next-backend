package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
import com.github.oosm032519.playlistviewernext.model.PlaylistTrackAdditionRequest;
import com.github.oosm032519.playlistviewernext.security.UserAuthenticationService;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackAdditionService;
import jakarta.servlet.http.HttpServletRequest;
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
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;

import java.util.HashMap;
import java.util.Map;

/**
 * プレイリストトラック追加操作を処理するRESTコントローラ
 * プレイリストにトラックを追加する機能を提供する
 */
@RestController
@RequestMapping("/api/playlist")
@Validated
public class PlaylistTrackAdditionController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistTrackAdditionController.class);

    private final UserAuthenticationService userAuthenticationService;
    private final SpotifyPlaylistTrackAdditionService spotifyService;
    private final HttpServletRequest request;

    /**
     * PlaylistTrackAdditionControllerのコンストラクタ
     *
     * @param userAuthenticationService ユーザー認証サービス
     * @param spotifyService            Spotifyプレイリストトラック追加サービス
     * @param request                   HTTPサーブレットリクエスト
     */
    public PlaylistTrackAdditionController(UserAuthenticationService userAuthenticationService,
                                           SpotifyPlaylistTrackAdditionService spotifyService,
                                           HttpServletRequest request) {
        this.userAuthenticationService = userAuthenticationService;
        this.spotifyService = spotifyService;
        this.request = request;
    }

    /**
     * プレイリストにトラックを追加するエンドポイント
     *
     * @param request   プレイリストIDとトラックIDを含むリクエストボディ
     * @param principal 認証されたユーザー情報
     * @return トラック追加の結果を含むレスポンスエンティティ
     * @throws AuthenticationException ユーザーが認証されていない場合
     * @throws SpotifyApiException     Spotify APIでエラーが発生した場合
     */
    @PostMapping("/add-track")
    public ResponseEntity<Map<String, String>> addTrackToPlaylist(@Valid @RequestBody PlaylistTrackAdditionRequest request,
                                                                  @AuthenticationPrincipal OAuth2User principal) {
        logger.info("トラック追加リクエストを受信しました。プレイリストID: {}, トラックID: {}", request.getPlaylistId(), request.getTrackId());

        // アクセストークンの取得と検証
        String accessToken = userAuthenticationService.getAccessToken(principal);
        if (accessToken == null) {
            throw new AuthenticationException(
                    HttpStatus.UNAUTHORIZED,
                    "AUTHENTICATION_ERROR",
                    "ユーザーが認証されていないか、アクセストークンが見つかりません。"
            );
        }

        try {
            // Spotify APIを使用してトラックを追加
            SnapshotResult snapshotResult = spotifyService.addTrackToPlaylist(accessToken, request.getPlaylistId(), request.getTrackId());
            logger.info("トラックが正常に追加されました。Snapshot ID: {}", snapshotResult.getSnapshotId());

            // レスポンスの作成
            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "トラックが正常に追加されました。");
            responseBody.put("snapshot_id", snapshotResult.getSnapshotId());

            return ResponseEntity.ok(responseBody);
        } catch (SpotifyApiException e) {
            // Spotify API エラーはそのまま再スロー
            throw e;
        } catch (Exception e) {
            // 予期しないエラーの処理
            logger.error("トラックの追加中に予期しないエラーが発生しました。", e);
            String requestParams = getRequestParams();
            throw new SpotifyApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "TRACK_ADDITION_ERROR",
                    "Spotify APIでトラックの追加中にエラーが発生しました。しばらく時間をおいてから再度お試しください。",
                    "リクエストパラメータ: " + requestParams,
                    e
            );
        }
    }

    /**
     * リクエストパラメータを取得するヘルパーメソッド
     *
     * @return リクエストパラメータを文字列として
     */
    private String getRequestParams() {
        StringBuilder params = new StringBuilder();
        request.getParameterMap().forEach((key, values) -> {
            params.append(key).append("=").append(String.join(",", values)).append("&");
        });
        if (params.length() > 0) {
            params.deleteCharAt(params.length() - 1);
        }
        return params.toString();
    }
}
