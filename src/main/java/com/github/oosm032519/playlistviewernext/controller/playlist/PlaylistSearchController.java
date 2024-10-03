package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.exception.ErrorResponse;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistSearchService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * プレイリスト検索機能を提供するRESTコントローラ
 * クライアントからのプレイリスト検索リクエストを処理し、SpotifyAPIを使用して検索結果を返す
 */
@RestController
@RequestMapping("/api/playlists/search")
@Validated
public class PlaylistSearchController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistSearchController.class);

    private final SpotifyPlaylistSearchService playlistSearchService;
    private final SpotifyClientCredentialsAuthentication authController;
    private final HttpServletRequest request; // リクエスト情報を取得

    /**
     * PlaylistSearchControllerのコンストラクタ
     *
     * @param playlistSearchService Spotifyプレイリスト検索サービス
     * @param authController        Spotify認証コントローラ
     * @param request               HTTPリクエスト
     */
    public PlaylistSearchController(SpotifyPlaylistSearchService playlistSearchService,
                                    SpotifyClientCredentialsAuthentication authController,
                                    HttpServletRequest request) {
        this.playlistSearchService = playlistSearchService;
        this.authController = authController;
        this.request = request;
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
            @RequestParam @NotBlank String query,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit) {

        if (query.isBlank()) {
            ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_QUERY", "検索クエリは必須です。");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        logger.info("Searching playlists. Query: {}, Offset: {}, Limit: {}", query, offset, limit);

        // Spotify APIの認証を行う
        authController.authenticate();

        // プレイリストの検索を実行し、検索結果と総数を取得
        Map<String, Object> searchResult = playlistSearchService.searchPlaylists(query, offset, limit);

        // 検索結果を返す
        return ResponseEntity.ok(searchResult);
    }

    // リクエストパラメータを取得するヘルパーメソッド
    public String getRequestParams() {
        StringBuilder params = new StringBuilder();
        request.getParameterMap().forEach((key, values) -> params.append(key).append("=").append(String.join(",", values)).append("&"));
        if (!params.isEmpty()) {
            params.deleteCharAt(params.length() - 1);
        }
        return params.toString();
    }
}
