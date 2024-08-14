package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.ErrorResponse;
import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyUserPlaylistsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ユーザーのフォロー中のプレイリストを管理するコントローラークラス
 */
@RestController
@RequestMapping("/api/playlists/followed")
public class UserPlaylistsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserPlaylistsController.class);

    private final SpotifyUserPlaylistsService userPlaylistsService;

    /**
     * コンストラクタ
     *
     * @param userPlaylistsService Spotifyのユーザープレイリストサービス
     */
    public UserPlaylistsController(SpotifyUserPlaylistsService userPlaylistsService) {
        this.userPlaylistsService = userPlaylistsService;
    }

    /**
     * フォロー中のプレイリストを取得するエンドポイント
     *
     * @return フォロー中のプレイリストのリストを含むResponseEntity
     */
    @GetMapping
    public ResponseEntity<?> getFollowedPlaylists() {
        try {
            return ResponseEntity.ok(userPlaylistsService.getCurrentUsersPlaylists()); // authentication を渡さない
        } catch (AuthenticationException e) {
            // AuthenticationException はそのまま再スロー
            HttpStatus status = e.getHttpStatus();
            String errorCode = e.getErrorCode();
            String message = e.getMessage();
            String details = e.getDetails();

            // エラーログに記録
            LOGGER.error("Authentication error occurred while retrieving followed playlists: {} - {} - {}", status, errorCode, message, e);

            // エラーレスポンスを返す
            ErrorResponse errorResponse = new ErrorResponse(status, errorCode, message, details);
            return new ResponseEntity<>(errorResponse, status);
        } catch (SpotifyApiException e) {
            // SpotifyApiException はそのまま再スロー
            HttpStatus status = e.getHttpStatus();
            String errorCode = e.getErrorCode();
            String message = e.getMessage();
            String details = e.getDetails();

            // エラーログに記録
            LOGGER.error("Spotify API error occurred while retrieving followed playlists: {} - {} - {}", status, errorCode, message, e);

            // エラーレスポンスを返す
            ErrorResponse errorResponse = new ErrorResponse(status, errorCode, message, details);
            return new ResponseEntity<>(errorResponse, status);
        } catch (Exception e) {
            // 予期しないエラーを処理
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            String errorCode = "SYSTEM_UNEXPECTED_ERROR";
            String message = "システムエラーが発生しました。しばらく時間をおいてから再度お試しください。";

            // エラーログに記録
            LOGGER.error("Unexpected error occurred while retrieving followed playlists: {} - {} - {}", status, errorCode, message, e);

            // エラーレスポンスを返す
            ErrorResponse errorResponse = new ErrorResponse(status, errorCode, message);
            return new ResponseEntity<>(errorResponse, status);
        }
    }
}
