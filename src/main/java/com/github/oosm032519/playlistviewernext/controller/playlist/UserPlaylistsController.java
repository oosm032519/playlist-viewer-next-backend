package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyUserPlaylistsService;
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
        return ResponseEntity.ok(userPlaylistsService.getCurrentUsersPlaylists());
    }
}
