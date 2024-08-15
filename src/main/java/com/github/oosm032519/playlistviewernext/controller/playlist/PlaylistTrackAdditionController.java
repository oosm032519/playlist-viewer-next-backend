package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
import com.github.oosm032519.playlistviewernext.model.PlaylistTrackAdditionRequest;
import com.github.oosm032519.playlistviewernext.security.UserAuthenticationService;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackAdditionService;
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

@RestController
@RequestMapping("/api/playlist")
@Validated
public class PlaylistTrackAdditionController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistTrackAdditionController.class);

    private final UserAuthenticationService userAuthenticationService;
    private final SpotifyPlaylistTrackAdditionService spotifyService;

    public PlaylistTrackAdditionController(UserAuthenticationService userAuthenticationService,
                                           SpotifyPlaylistTrackAdditionService spotifyService) {
        this.userAuthenticationService = userAuthenticationService;
        this.spotifyService = spotifyService;
    }

    /**
     * プレイリストにトラックを追加するエンドポイント
     *
     * @param request   プレイリストIDとトラックIDを含むリクエストボディ
     * @param principal 認証されたユーザー情報
     * @return トラック追加の結果を含むレスポンスエンティティ
     */
    @PostMapping("/add-track")
    public ResponseEntity<Map<String, String>> addTrackToPlaylist(@Valid @RequestBody PlaylistTrackAdditionRequest request,
                                                                  @AuthenticationPrincipal OAuth2User principal) {
        logger.info("トラック追加リクエストを受信しました。プレイリストID: {}, トラックID: {}", request.getPlaylistId(), request.getTrackId());

        String accessToken = userAuthenticationService.getAccessToken(principal);
        if (accessToken == null) {
            throw new AuthenticationException(
                    HttpStatus.UNAUTHORIZED,
                    "AUTHENTICATION_ERROR",
                    "ユーザーが認証されていないか、アクセストークンが見つかりません。"
            );
        }

        try {
            SnapshotResult snapshotResult = spotifyService.addTrackToPlaylist(accessToken, request.getPlaylistId(), request.getTrackId());
            logger.info("トラックが正常に追加されました。Snapshot ID: {}", snapshotResult.getSnapshotId());

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "トラックが正常に追加されました。");
            responseBody.put("snapshot_id", snapshotResult.getSnapshotId());

            return ResponseEntity.ok(responseBody);
        } catch (SpotifyApiException e) {
            // Spotify API エラーはそのまま再スロー
            throw e;
        } catch (Exception e) {
            // トラックの追加中に予期しないエラーが発生した場合は SpotifyApiException をスロー
            logger.error("トラックの追加中に予期しないエラーが発生しました。", e);
            throw new SpotifyApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "TRACK_ADDITION_ERROR",
                    "Spotify APIでトラックの追加中にエラーが発生しました。しばらく時間をおいてから再度お試しください。",
                    e
            );
        }
    }
}
