package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.FavoritePlaylistResponse;
import com.github.oosm032519.playlistviewernext.service.playlist.UserFavoritePlaylistsService;
import com.github.oosm032519.playlistviewernext.util.HashUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * ログインユーザーのお気に入りプレイリストを取得するAPIエンドポイントを提供するコントローラークラス
 */
@RestController
@RequestMapping("/api/playlists/favorites")
public class UserFavoritePlaylistsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserFavoritePlaylistsController.class);
    private static final String MOCK_USER_ID = "mock-user-id";

    private final UserFavoritePlaylistsService userFavoritePlaylistsService;

    @Value("${spotify.mock.enabled}")
    private boolean mockEnabled;

    @Autowired
    private HashUtil hashUtil;

    /**
     * コンストラクタ
     *
     * @param userFavoritePlaylistsService お気に入りプレイリストサービス
     * @param hashUtil                     ハッシュユーティリティ
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
        LOGGER.info("お気に入りプレイリスト一覧取得リクエストを受信しました。");

        if (principal == null) {
            LOGGER.error("認証されたユーザー情報が利用できません。 principalがnullです。");
            return ResponseEntity.badRequest().body("認証情報が不足しています。");
        }

        LOGGER.debug("認証情報を検証中。ユーザー情報: {}", principal);

        String userId;
        try {
            userId = mockEnabled ? MOCK_USER_ID : principal.getAttribute("id");
            LOGGER.debug("取得されたユーザーID: {}", userId);
        } catch (Exception e) {
            LOGGER.error("ユーザーIDの取得中にエラーが発生しました。", e);
            return ResponseEntity.status(500).body("ユーザーIDの取得に失敗しました。");
        }

        if (userId == null) {
            LOGGER.error("ユーザーIDがnullです。principalの内容: {}", principal);
            return ResponseEntity.badRequest().body("ユーザーIDを取得できませんでした。");
        }

        String hashedUserId;
        try {
            if (!mockEnabled) {
                LOGGER.debug("ユーザーIDをハッシュ化します。");
                hashedUserId = hashUtil.hashUserId(userId);
                LOGGER.debug("ハッシュ化されたユーザーID: {}", hashedUserId);
            } else {
                LOGGER.debug("モックモードが有効のため、ユーザーIDのハッシュ化をスキップします。ユーザーID: {}", userId);
                hashedUserId = userId;
            }
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("ユーザーIDのハッシュ化中にエラーが発生しました。ユーザーID: {}", userId, e);
            return ResponseEntity.status(500).body("ユーザーIDのハッシュ化に失敗しました。");
        }

        LOGGER.info("ユーザー [{}] のお気に入りプレイリストの取得を開始します。", hashedUserId);

        List<FavoritePlaylistResponse> favoritePlaylists;
        try {
            favoritePlaylists = userFavoritePlaylistsService.getFavoritePlaylists(hashedUserId);
            LOGGER.info("ユーザー [{}] のお気に入りプレイリストを {} 件取得しました。", hashedUserId, favoritePlaylists.size());
        } catch (Exception e) {
            LOGGER.error("お気に入りプレイリストの取得中にエラーが発生しました。ユーザーID: {}", hashedUserId, e);
            return ResponseEntity.status(500).body("お気に入りプレイリストの取得に失敗しました。");
        }

        LOGGER.debug("ユーザー [{}] のお気に入りプレイリスト内容: {}", hashedUserId, favoritePlaylists);

        LOGGER.info("お気に入りプレイリスト一覧取得リクエストが正常に完了しました。");
        return ResponseEntity.ok(favoritePlaylists);
    }
}
