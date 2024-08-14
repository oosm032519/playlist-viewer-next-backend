package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.entity.UserFavoritePlaylist;
import com.github.oosm032519.playlistviewernext.exception.DatabaseAccessException;
import com.github.oosm032519.playlistviewernext.repository.UserFavoritePlaylistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistFavoriteController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistFavoriteController.class);

    private final UserFavoritePlaylistRepository userFavoritePlaylistRepository;

    public PlaylistFavoriteController(UserFavoritePlaylistRepository userFavoritePlaylistRepository) {
        this.userFavoritePlaylistRepository = userFavoritePlaylistRepository;
    }

    @PostMapping("/favorite")
    public ResponseEntity<Map<String, Object>> favoritePlaylist(@AuthenticationPrincipal OAuth2User principal,
                                                                @RequestParam String playlistId,
                                                                @RequestParam String playlistName,
                                                                @RequestParam int totalTracks,
                                                                @RequestParam String playlistOwnerName
    ) {
        logger.info("プレイリストお気に入り登録リクエストを受信しました。プレイリストID: {}, プレイリスト名: {}, 楽曲数: {}", playlistId, playlistName, totalTracks);

        String userId = principal.getAttribute("id");

        // ハッシュ値生成
        String hashedUserId = hashUserId(Objects.requireNonNull(userId));

        // 既に登録されているかチェック
        if (userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(hashedUserId, playlistId)) {
            logger.info("プレイリストは既にお気に入りに登録されています。ユーザーID: {}, プレイリストID: {}", userId, playlistId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "warning");
            response.put("message", "このプレイリストは既にお気に入りに登録されています。");
            return ResponseEntity.ok(response);
        }

        // 登録処理
        UserFavoritePlaylist userFavoritePlaylist = new UserFavoritePlaylist();
        userFavoritePlaylist.setUserId(hashedUserId);
        userFavoritePlaylist.setPlaylistId(playlistId);
        userFavoritePlaylist.setPlaylistName(playlistName);
        userFavoritePlaylist.setTotalTracks(totalTracks);
        userFavoritePlaylist.setPlaylistOwnerName(playlistOwnerName);

        try {
            userFavoritePlaylistRepository.save(userFavoritePlaylist);
            logger.info("プレイリストをお気に入りに登録しました。ユーザーID: {}, プレイリストID: {}, プレイリスト名: {}", userId, playlistId, playlistName);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "プレイリストをお気に入りに登録しました。");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // エラーが発生した場合は DatabaseAccessException をスロー
            logger.error("プレイリストのお気に入り登録中にエラーが発生しました。", e);
            throw new DatabaseAccessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "PLAYLIST_FAVORITE_ERROR",
                    "プレイリストをお気に入りに登録できませんでした。しばらく時間をおいてから再度お試しください。",
                    e
            );
        }
    }

    private String hashUserId(String userId) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(userId.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            // ハッシュアルゴリズムが見つからない場合は IllegalStateException をスロー
            throw new IllegalStateException("SHA-256 ハッシュアルゴリズムが見つかりません。", e);
        }
    }

    @DeleteMapping("/favorite")
    @Transactional
    public ResponseEntity<Map<String, Object>> unfavoritePlaylist(@AuthenticationPrincipal OAuth2User principal,
                                                                  @RequestParam String playlistId) {
        logger.info("プレイリストお気に入り解除リクエストを受信しました。プレイリストID: {}", playlistId);

        String userId = principal.getAttribute("id");

        // ハッシュ値生成
        String hashedUserId = hashUserId(Objects.requireNonNull(userId));

        try {
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
        } catch (Exception e) {
            // エラーが発生した場合は DatabaseAccessException をスロー
            logger.error("プレイリストのお気に入り解除中にエラーが発生しました。", e);
            throw new DatabaseAccessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "PLAYLIST_UNFAVORITE_ERROR",
                    "プレイリストをお気に入りから解除できませんでした。しばらく時間をおいてから再度お試しください。",
                    e
            );
        }
    }

    @GetMapping("/favorite")
    public ResponseEntity<List<Map<String, Object>>> getFavoritePlaylists(@AuthenticationPrincipal OAuth2User principal) {
        logger.info("お気に入りプレイリスト一覧取得リクエストを受信しました。");

        String userId = principal.getAttribute("id");

        // ハッシュ値生成
        String hashedUserId = hashUserId(Objects.requireNonNull(userId));

        try {
            // お気に入りプレイリストID一覧を取得
            List<UserFavoritePlaylist> favoritePlaylists = userFavoritePlaylistRepository.findByUserId(hashedUserId); // ハッシュ化されたIDを使用

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
        } catch (Exception e) {
            // エラーが発生した場合は DatabaseAccessException をスロー
            logger.error("お気に入りプレイリスト一覧の取得中にエラーが発生しました。", e);
            throw new DatabaseAccessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "FAVORITE_PLAYLISTS_RETRIEVAL_ERROR",
                    "お気に入りプレイリスト一覧を取得できませんでした。しばらく時間をおいてから再度お試しください。",
                    e
            );
        }
    }

    @GetMapping("/favoriteCheck")
    public ResponseEntity<Boolean> checkFavorite(@AuthenticationPrincipal OAuth2User principal,
                                                 @RequestParam String playlistId) {
        logger.info("プレイリストお気に入り確認リクエストを受信しました。プレイリストID: {}", playlistId);

        String userId = principal.getAttribute("id");

        // ハッシュ値生成
        String hashedUserId = hashUserId(Objects.requireNonNull(userId));

        try {
            // お気に入り登録状況を確認
            boolean isFavorited = userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(hashedUserId, playlistId);
            return ResponseEntity.ok(isFavorited);
        } catch (Exception e) {
            // エラーが発生した場合は DatabaseAccessException をスロー
            logger.error("プレイリストのお気に入り確認中にエラーが発生しました。", e);
            throw new DatabaseAccessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "PLAYLIST_FAVORITE_CHECK_ERROR",
                    "プレイリストのお気に入り状態を確認できませんでした。しばらく時間をおいてから再度お試しください。",
                    e
            );
        }
    }
}
