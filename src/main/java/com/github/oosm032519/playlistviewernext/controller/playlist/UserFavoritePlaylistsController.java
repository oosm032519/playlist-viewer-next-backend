package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.FavoritePlaylistResponse;
import com.github.oosm032519.playlistviewernext.service.playlist.UserFavoritePlaylistsService;
import com.github.oosm032519.playlistviewernext.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

/**
 * ログインユーザーのお気に入りプレイリストを取得するAPIエンドポイントを提供するコントローラークラス
 */
@RestController
@RequestMapping("/api/playlists/favorites")
public class UserFavoritePlaylistsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserFavoritePlaylistsController.class);

    private final UserFavoritePlaylistsService userFavoritePlaylistsService;

    @Autowired
    private HashUtil hashUtil;

    /**
     * コンストラクタ
     *
     * @param userFavoritePlaylistsService お気に入りプレイリストサービス
     */
    public UserFavoritePlaylistsController(UserFavoritePlaylistsService userFavoritePlaylistsService, HashUtil hashUtil) {
        this.userFavoritePlaylistsService = userFavoritePlaylistsService;
        this.hashUtil = hashUtil;
    }

    /**
     * ログインユーザーのお気に入りプレイリストを取得するエンドポイント
     *
     * @param principal 認証されたユーザー情報
     * @return お気に入りプレイリストのリストを含むResponseEntity
     */
    @GetMapping
    public ResponseEntity<?> getFavoritePlaylists(@AuthenticationPrincipal OAuth2User principal) throws NoSuchAlgorithmException {
        LOGGER.info("お気に入りプレイリスト一覧取得リクエストを受信しました。 ユーザー情報: {}", principal);

        String userId = principal.getAttribute("id");
        // ハッシュ値生成
        String hashedUserId = hashUtil.hashUserId(Objects.requireNonNull(userId));
        LOGGER.debug("ユーザーID: {} のお気に入りプレイリストを取得します。", hashedUserId);

        List<FavoritePlaylistResponse> favoritePlaylists = userFavoritePlaylistsService.getFavoritePlaylists(hashedUserId); // ハッシュ化されたIDを使用
        LOGGER.info("ユーザーID: {} のお気に入りプレイリストを {} 件取得しました。", hashedUserId, favoritePlaylists.size());
        return ResponseEntity.ok(favoritePlaylists);
    }
}
