// UserPlaylistsController.java

package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyUserPlaylistsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ユーザーのフォロー中のプレイリストを管理するコントローラークラス
 */
@RestController
@RequestMapping("/api/playlists/followed")
public class UserPlaylistsController {

    // ロガーの初期化
    private static final Logger logger = LoggerFactory.getLogger(UserPlaylistsController.class);

    // SpotifyUserPlaylistsServiceのインスタンス
    @Autowired
    private final SpotifyUserPlaylistsService userPlaylistsService;

    /**
     * コンストラクタ
     *
     * @param userPlaylistsService Spotifyのユーザープレイリストサービス
     */
    @Autowired
    public UserPlaylistsController(SpotifyUserPlaylistsService userPlaylistsService) {
        this.userPlaylistsService = userPlaylistsService;
    }

    /**
     * フォロー中のプレイリストを取得するエンドポイント
     *
     * @param authentication OAuth2認証トークン
     * @return フォロー中のプレイリストのリストを含むResponseEntity
     */
    @GetMapping
    public ResponseEntity<?> getFollowedPlaylists(OAuth2AuthenticationToken authentication) {
        try {
            // フォロー中のプレイリストを取得し、200 OKレスポンスを返す
            return ResponseEntity.ok(userPlaylistsService.getCurrentUsersPlaylists(authentication));
        } catch (Exception e) {
            // エラーメッセージをログに記録し、500 Internal Server Errorレスポンスを返す
            logger.error("UserPlaylistsController: フォロー中のプレイリストの取得中にエラーが発生しました", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
