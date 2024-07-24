package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.entity.UserFavoritePlaylist;
import com.github.oosm032519.playlistviewernext.repository.UserFavoritePlaylistRepository;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Map<String, Object>> favoritePlaylist(HttpSession session,
                                                                @RequestParam String playlistId,
                                                                @RequestParam String playlistName,
                                                                @RequestParam int totalTracks,
                                                                @RequestParam String playlistOwnerName
    ) {
        logger.info("プレイリストお気に入り登録リクエストを受信しました。プレイリストID: {}, プレイリスト名: {}, 楽曲数: {}", playlistId, playlistName, totalTracks);

        // セッションからユーザーIDを取得
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return handleAuthenticationError();
        }

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
            logger.error("プレイリストのお気に入り登録中にエラーが発生しました。", e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "プレイリストのお気に入り登録中にエラーが発生しました。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    public ResponseEntity<Map<String, Object>> handleAuthenticationError() {
        logger.error("ユーザーが認証されていないか、セッションが見つかりません。");
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "認証が必要です。");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    public String hashUserId(String userId) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(userId.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("ハッシュアルゴリズムが見つかりません。", e);
        }
    }

    @DeleteMapping("/favorite")
    @Transactional
    public ResponseEntity<Map<String, Object>> unfavoritePlaylist(HttpSession session,
                                                                  @RequestParam String playlistId) {
        logger.info("プレイリストお気に入り解除リクエストを受信しました。プレイリストID: {}", playlistId);

        // セッションからユーザーIDを取得
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return handleAuthenticationError();
        }

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
            logger.error("プレイリストのお気に入り解除中にエラーが発生しました。", e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "プレイリストのお気に入り解除中にエラーが発生しました。");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/favorite")
    public ResponseEntity<List<Map<String, Object>>> getFavoritePlaylists(HttpSession session) {
        logger.info("お気に入りプレイリスト一覧取得リクエストを受信しました。");

        // セッションからユーザーIDを取得
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            logger.error("ユーザーが認証されていないか、セッションが見つかりません。");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        }

        // ハッシュ値生成
        String hashedUserId = hashUserId(Objects.requireNonNull(userId));

        try {
            // お気に入りプレイリストID一覧を取得
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
        } catch (Exception e) {
            logger.error("お気に入りプレイリスト一覧の取得中にエラーが発生しました。", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/favoriteCheck")
    public ResponseEntity<Boolean> checkFavorite(HttpSession session,
                                                 @RequestParam String playlistId) {
        logger.info("プレイリストお気に入り確認リクエストを受信しました。プレイリストID: {}", playlistId);

        // セッションからユーザーIDを取得
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
        }

        // ハッシュ値生成
        String hashedUserId = hashUserId(Objects.requireNonNull(userId));

        // お気に入り登録状況を確認
        boolean isFavorited = userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(hashedUserId, playlistId);

        return ResponseEntity.ok(isFavorited);
    }
}
