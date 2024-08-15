package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.ErrorResponse;
import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
import com.github.oosm032519.playlistviewernext.model.PlaylistTrackRemovalRequest;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackRemovalService;
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

import java.util.Map;

/**
 * プレイリストからトラックを削除するためのコントローラークラス
 */
@RestController
@RequestMapping("/api/playlist")
@Validated
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
    public ResponseEntity<?> removeTrackFromPlaylist(
            @Valid @RequestBody PlaylistTrackRemovalRequest request,
            @AuthenticationPrincipal OAuth2User principal) {
        LOGGER.info("removeTrackFromPlaylist メソッドが呼び出されました。リクエスト: {}", request);

        if (principal == null) {
            throw new AuthenticationException(
                    HttpStatus.UNAUTHORIZED,
                    "AUTHENTICATION_ERROR",
                    "認証されていないユーザーがアクセスしようとしました。"
            );
        }

        try {
            ResponseEntity<String> response = spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(request, principal);
            if (response.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok(Map.of("message", "トラックが正常に削除されました。"));
            } else {
                throw new SpotifyApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "TRACK_REMOVAL_ERROR",
                        "Spotify APIでトラックの削除中にエラーが発生しました。しばらく時間をおいてから再度お試しください。",
                        "Spotify APIからのエラーレスポンス: " + response.getBody()
                );
            }
        } catch (SpotifyApiException e) {
            // SpotifyApiException はそのまま再スロー
            HttpStatus status = e.getHttpStatus();
            String errorCode = e.getErrorCode();
            String message = e.getMessage();
            String details = e.getDetails();

            // エラーログに記録
            LOGGER.error("Spotify API error occurred while removing track from playlist: {} - {} - {}", status, errorCode, message, e);

            // エラーレスポンスを返す
            ErrorResponse errorResponse = new ErrorResponse(status, errorCode, message, details);
            return new ResponseEntity<>(errorResponse, status);
        } catch (Exception e) {
            // 予期しない例外が発生した場合は、一般的な SpotifyApiException をスロー
            LOGGER.error("トラックの削除中に予期しないエラーが発生しました。", e);
            throw new SpotifyApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "TRACK_REMOVAL_ERROR",
                    "トラックの削除中にエラーが発生しました。しばらく時間をおいてから再度お試しください。",
                    e
            );
        }
    }
}
