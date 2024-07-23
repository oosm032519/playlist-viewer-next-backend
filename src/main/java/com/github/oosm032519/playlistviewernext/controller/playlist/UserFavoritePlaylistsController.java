package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.FavoritePlaylistResponse;
import com.github.oosm032519.playlistviewernext.service.playlist.UserFavoritePlaylistsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ログインユーザーのお気に入りプレイリストを取得するAPIエンドポイントを提供するコントローラークラス
 */
@RestController
@RequestMapping("/api/playlists/favorites")
public class UserFavoritePlaylistsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserFavoritePlaylistsController.class);

    private final UserFavoritePlaylistsService userFavoritePlaylistsService;

    /**
     * コンストラクタ
     *
     * @param userFavoritePlaylistsService お気に入りプレイリストサービス
     */
    public UserFavoritePlaylistsController(UserFavoritePlaylistsService userFavoritePlaylistsService) {
        this.userFavoritePlaylistsService = userFavoritePlaylistsService;
    }

    /**
     * ログインユーザーのお気に入りプレイリストを取得するエンドポイント
     *
     * @param principal 認証されたユーザー情報
     * @return お気に入りプレイリストのリストを含むResponseEntity
     */
    @GetMapping
    public ResponseEntity<List<FavoritePlaylistResponse>> getFavoritePlaylists(@AuthenticationPrincipal OAuth2User principal) {
        LOGGER.info("お気に入りプレイリスト一覧取得リクエストを受信しました。");

        try {
            String userId = principal.getAttribute("id");
            List<FavoritePlaylistResponse> favoritePlaylists = userFavoritePlaylistsService.getFavoritePlaylists(userId);
            return ResponseEntity.ok(favoritePlaylists);
        } catch (Exception e) {
            LOGGER.error("お気に入りプレイリスト一覧の取得中にエラーが発生しました。", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
