// PlaylistSearchController.java

package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

import java.util.List;

@RestController
@RequestMapping("/api/playlists/search")
public class PlaylistSearchController {

    // ロガーのインスタンスを生成
    private static final Logger logger = LoggerFactory.getLogger(PlaylistSearchController.class);

    // SpotifyPlaylistSearchServiceのインスタンスを自動注入
    @Autowired
    private final SpotifyPlaylistSearchService playlistSearchService;

    // SpotifyClientCredentialsAuthenticationのインスタンスを自動注入
    @Autowired
    private final SpotifyClientCredentialsAuthentication authController;

    /**
     * コンストラクタ
     *
     * @param playlistSearchService プレイリスト検索サービス
     * @param authController        認証コントローラー
     */
    @Autowired
    public PlaylistSearchController(SpotifyPlaylistSearchService playlistSearchService, SpotifyClientCredentialsAuthentication authController) {
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
    public ResponseEntity<List<PlaylistSimplified>> searchPlaylists(@RequestParam String query,
                                                                    @RequestParam(defaultValue = "0") int offset,
                                                                    @RequestParam(defaultValue = "20") int limit) {
        // メソッドが呼び出されたことをログに記録
        logger.info("PlaylistSearchController: searchPlaylists メソッドが呼び出されました。クエリ: {}, オフセット: {}, リミット: {}", query, offset, limit);
        try {
            // 認証を実行
            authController.authenticate();
            // プレイリストを検索
            List<PlaylistSimplified> playlists = playlistSearchService.searchPlaylists(query, offset, limit);
            // 検索結果を含むレスポンスを返す
            return ResponseEntity.ok(playlists);
        } catch (Exception e) {
            // エラーが発生した場合はログに記録し、500エラーを返す
            logger.error("PlaylistSearchController: プレイリストの検索中にエラーが発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
