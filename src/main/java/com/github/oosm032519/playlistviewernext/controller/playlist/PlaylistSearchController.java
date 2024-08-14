package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.exception.ErrorResponse;
import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

import java.util.List;

/**
 * Spotifyのプレイリスト検索機能を提供するRESTコントローラー
 * このクラスは、クライアントからのプレイリスト検索リクエストを処理し、
 * SpotifyAPIを使用して検索結果を返します。
 */
@RestController
@RequestMapping("/api/playlists/search")
public class PlaylistSearchController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistSearchController.class);

    private final SpotifyPlaylistSearchService playlistSearchService;
    private final SpotifyClientCredentialsAuthentication authController;

    /**
     * PlaylistSearchControllerのコンストラクタ
     *
     * @param playlistSearchService Spotifyプレイリスト検索サービス
     * @param authController        Spotify認証コントローラー
     */
    public PlaylistSearchController(SpotifyPlaylistSearchService playlistSearchService,
                                    SpotifyClientCredentialsAuthentication authController) {
        this.playlistSearchService = playlistSearchService;
        this.authController = authController;
    }

    /**
     * プレイリストを検索するエンドポイント
     *
     * @param query  検索クエリ
     * @param offset 検索結果のオフセット (デフォルトは0)
     * @param limit  検索結果のリミット (デフォルトは20)
     * @return 検索結果のプレイリストのリストを含むResponseEntity
     */
    @GetMapping
    public ResponseEntity<?> searchPlaylists(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {
        logger.info("Searching playlists. Query: {}, Offset: {}, Limit: {}", query, offset, limit);
        try {
            // Spotify APIの認証を行う
            authController.authenticate();

            // プレイリストの検索を実行
            List<PlaylistSimplified> playlists = playlistSearchService.searchPlaylists(query, offset, limit);

            // 検索結果を返す
            return ResponseEntity.ok(playlists);
        } catch (SpotifyApiException e) {
            // Spotify API エラーを処理
            HttpStatus status = e.getHttpStatus();
            String errorCode = e.getErrorCode();
            String message = e.getMessage();
            String details = e.getDetails();

            // レート制限を超過した場合の処理
            if (errorCode.equals("SPOTIFY_API_RATE_LIMIT_EXCEEDED")) {
                // 一定時間待機してから再度リクエストを送信する
                // ...
                message = "Spotify API のレート制限を超過しました。しばらく時間をおいてから再度お試しください。";
            }

            // エラーログに記録
            logger.error("Spotify API error occurred while searching playlists: {} - {} - {}", status, errorCode, message, e);

            // エラーレスポンスを返す
            ErrorResponse errorResponse = new ErrorResponse(status, errorCode, message, details);
            return new ResponseEntity<>(errorResponse, status);
        } catch (Exception e) {
            // 予期しないエラーを処理
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            String errorCode = "SYSTEM_UNEXPECTED_ERROR";
            String message = "システムエラーが発生しました。しばらく時間をおいてから再度お試しください。";

            // エラーログに記録
            logger.error("Unexpected error occurred while searching playlists: {} - {} - {}", status, errorCode, message, e);

            // エラーレスポンスを返す
            ErrorResponse errorResponse = new ErrorResponse(status, errorCode, message);
            return new ResponseEntity<>(errorResponse, status);
        }
    }
}
