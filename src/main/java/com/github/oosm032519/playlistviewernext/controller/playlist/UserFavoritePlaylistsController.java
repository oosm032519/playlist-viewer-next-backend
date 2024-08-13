package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.DatabaseAccessException;
import com.github.oosm032519.playlistviewernext.model.FavoritePlaylistResponse;
import com.github.oosm032519.playlistviewernext.service.playlist.UserFavoritePlaylistsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
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
        LOGGER.info("お気に入りプレイリスト一覧取得リクエストを受信しました。 ユーザー情報: {}", principal);

        try {
            String userId = principal.getAttribute("id");
            // ハッシュ値生成
            String hashedUserId = hashUserId(Objects.requireNonNull(userId));
            LOGGER.debug("ユーザーID: {} のお気に入りプレイリストを取得します。", hashedUserId);

            List<FavoritePlaylistResponse> favoritePlaylists = userFavoritePlaylistsService.getFavoritePlaylists(hashedUserId); // ハッシュ化されたIDを使用
            LOGGER.info("ユーザーID: {} のお気に入りプレイリストを {} 件取得しました。", hashedUserId, favoritePlaylists.size());
            return ResponseEntity.ok(favoritePlaylists);
        } catch (Exception e) {
            // エラーが発生した場合は DatabaseAccessException をスロー
            LOGGER.error("ユーザーID: {} のお気に入りプレイリスト一覧の取得中にエラーが発生しました。", principal.getAttribute("id"), e);
            throw new DatabaseAccessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "FAVORITE_PLAYLISTS_RETRIEVAL_ERROR",
                    "お気に入りプレイリスト一覧の取得中にエラーが発生しました。",
                    e
            );
        }
    }

    // hashUserIdメソッドを追加
    public String hashUserId(String userId) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(userId.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            // ハッシュアルゴリズムが見つからない場合は DatabaseAccessException をスロー
            throw new DatabaseAccessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "HASHING_ALGORITHM_ERROR",
                    "ハッシュアルゴリズムが見つかりません。",
                    e
            );
        }
    }
}
