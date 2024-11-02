package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.entity.UserFavoritePlaylist;
import com.github.oosm032519.playlistviewernext.repository.UserFavoritePlaylistRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * プレイリストのお気に入り機能を管理するコントローラークラス
 * ユーザーのプレイリストのお気に入り登録、解除、一覧取得、お気に入り状態の確認を行う
 */
@RestController
@RequestMapping("/api/playlists")
@Validated
public class PlaylistFavoriteController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistFavoriteController.class);

    private final UserFavoritePlaylistRepository userFavoritePlaylistRepository;

    /**
     * コンストラクタ
     *
     * @param userFavoritePlaylistRepository ユーザーのお気に入りプレイリストを管理するリポジトリ
     */
    public PlaylistFavoriteController(UserFavoritePlaylistRepository userFavoritePlaylistRepository) {
        this.userFavoritePlaylistRepository = userFavoritePlaylistRepository;
    }

    /**
     * プレイリストをお気に入りに登録する
     *
     * @param principal         認証されたユーザー情報
     * @param playlistId        プレイリストID
     * @param playlistName      プレイリスト名
     * @param totalTracks       プレイリストの総トラック数
     * @param playlistOwnerName プレイリスト所有者名
     * @return 登録結果を含むResponseEntity
     */
    @PostMapping("/favorite")
    public ResponseEntity<Map<String, Object>> favoritePlaylist(@AuthenticationPrincipal OAuth2User principal,
                                                                @RequestParam @NotBlank String playlistId,
                                                                @RequestParam @NotBlank String playlistName,
                                                                @RequestParam @NotNull @PositiveOrZero int totalTracks,
                                                                @RequestParam @NotBlank String playlistOwnerName
    ) throws NoSuchAlgorithmException {
        logger.info("プレイリストお気に入り登録リクエストを受信しました。プレイリストID: {}, プレイリスト名: {}, 楽曲数: {}", playlistId, playlistName, totalTracks);

        String userId = principal.getAttribute("id");

        // ユーザーIDをハッシュ化
        String hashedUserId = hashUserId(Objects.requireNonNull(userId));

        // 既に登録されているかチェック
        if (userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(hashedUserId, playlistId)) {
            logger.info("プレイリストは既にお気に入りに登録されています。ユーザーID: {}, プレイリストID: {}", userId, playlistId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "warning");
            response.put("message", "このプレイリストは既にお気に入りに登録されています。");
            return ResponseEntity.ok(response);
        }

        // 新しいお気に入りプレイリストエンティティを作成
        UserFavoritePlaylist userFavoritePlaylist = new UserFavoritePlaylist();
        userFavoritePlaylist.setUserId(hashedUserId);
        userFavoritePlaylist.setPlaylistId(playlistId);
        userFavoritePlaylist.setPlaylistName(playlistName);
        userFavoritePlaylist.setTotalTracks(totalTracks);
        userFavoritePlaylist.setPlaylistOwnerName(playlistOwnerName);

        userFavoritePlaylistRepository.save(userFavoritePlaylist);
        logger.info("プレイリストをお気に入りに登録しました。ユーザーID: {}, プレイリストID: {}, プレイリスト名: {}", userId, playlistId, playlistName);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "プレイリストをお気に入りに登録しました。");
        return ResponseEntity.ok(response);
    }

    /**
     * ユーザーIDをハッシュ化する
     *
     * @param userId ハッシュ化するユーザーID
     * @return ハッシュ化されたユーザーID
     */
    public String hashUserId(String userId) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(userId.getBytes());
        return Base64.getEncoder().encodeToString(hashedBytes);
    }

    /**
     * プレイリストのお気に入り登録を解除する
     *
     * @param principal  認証されたユーザー情報
     * @param playlistId 解除するプレイリストのID
     * @return 解除結果を含むResponseEntity
     */
    @DeleteMapping("/favorite")
    @Transactional
    public ResponseEntity<Map<String, Object>> unfavoritePlaylist(@AuthenticationPrincipal OAuth2User principal,
                                                                  @RequestParam @NotBlank String playlistId) throws NoSuchAlgorithmException {
        logger.info("プレイリストお気に入り解除リクエストを受信しました。プレイリストID: {}", playlistId);

        String userId = principal.getAttribute("id");

        // ユーザーIDをハッシュ化
        String hashedUserId = hashUserId(Objects.requireNonNull(userId));

        // お気に入り解除処理
        boolean deleted = userFavoritePlaylistRepository.deleteByUserIdAndPlaylistId(hashedUserId, playlistId) > 0;

        if (deleted) {
            logger.info("プレイリストをお気に入りから解除しました。ユーザーID: {}, プレイリストID: {}", userId, playlistId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "プレイリストをお気に入りから解除しました。");
            return ResponseEntity.ok(response);
        } else {
            logger.info("プレイリストはお気に入りに登録されていませんでした。ユーザーID: {}, プレイリストID: {}", userId, playlistId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "warning");
            response.put("message", "このプレイリストはお気に入りに登録されていません。");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * ユーザーのお気に入りプレイリスト一覧を取得する
     *
     * @param principal 認証されたユーザー情報
     * @return お気に入りプレイリスト一覧を含むResponseEntity
     */
    @GetMapping("/favorite")
    public ResponseEntity<List<Map<String, Object>>> getFavoritePlaylists(@AuthenticationPrincipal OAuth2User principal) throws NoSuchAlgorithmException {
        logger.info("お気に入りプレイリスト一覧取得リクエストを受信しました。");

        String userId = principal.getAttribute("id");

        // ユーザーIDをハッシュ化
        String hashedUserId = hashUserId(Objects.requireNonNull(userId));

        // お気に入りプレイリスト一覧を取得
        List<UserFavoritePlaylist> favoritePlaylists = userFavoritePlaylistRepository.findByUserId(hashedUserId);

        List<Map<String, Object>> response = favoritePlaylists.stream()
                .map(favorite -> {
                    Map<String, Object> playlistData = new HashMap<>();
                    playlistData.put("playlistId", favorite.getPlaylistId());
                    playlistData.put("playlistName", favorite.getPlaylistName());
                    playlistData.put("totalTracks", favorite.getTotalTracks());
                    playlistData.put("addedAt", favorite.getAddedAt());
                    playlistData.put("playlistOwnerName", favorite.getPlaylistOwnerName());
                    return playlistData;
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * 指定されたプレイリストがお気に入りに登録されているかを確認する
     *
     * @param principal  認証されたユーザー情報
     * @param playlistId 確認するプレイリストのID
     * @return お気に入り登録状態を含むResponseEntity
     */
    @GetMapping("/favoriteCheck")
    public ResponseEntity<Boolean> checkFavorite(@AuthenticationPrincipal OAuth2User principal,
                                                 @RequestParam @NotBlank String playlistId) throws NoSuchAlgorithmException {
        logger.info("プレイリストお気に入り確認リクエストを受信しました。プレイリストID: {}", playlistId);

        String userId = principal.getAttribute("id");

        // ユーザーIDをハッシュ化
        String hashedUserId = hashUserId(Objects.requireNonNull(userId));

        // お気に入り登録状況を確認
        boolean isFavorited = userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(hashedUserId, playlistId);
        return ResponseEntity.ok(isFavorited);
    }
}
