package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyUserPlaylistsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        } catch (Exception e) {
            LOGGER.error("フォロー中のプレイリストの取得中にエラーが発生しました", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
