package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.FavoritePlaylistResponse;
import com.github.oosm032519.playlistviewernext.service.playlist.UserFavoritePlaylistsService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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
     * @param session セッション情報
     * @return お気に入りプレイリストのリストを含むResponseEntity
     */
    @GetMapping
    public ResponseEntity<List<FavoritePlaylistResponse>> getFavoritePlaylists(HttpSession session) {
        LOGGER.info("お気に入りプレイリスト一覧取得リクエストを受信しました。");

        try {
            // セッションからユーザーIDを取得
            String userId = (String) session.getAttribute("userId");
            if (userId == null) {
                LOGGER.warn("認証されていないユーザーがアクセスしようとしました。");
                return ResponseEntity.status(401).build();
            }

            List<FavoritePlaylistResponse> favoritePlaylists = userFavoritePlaylistsService.getFavoritePlaylists(userId);
            return ResponseEntity.ok(favoritePlaylists);
        } catch (Exception e) {
            LOGGER.error("お気に入りプレイリスト一覧の取得中にエラーが発生しました。", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
