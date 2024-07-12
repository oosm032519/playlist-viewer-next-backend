package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

import java.util.List;

@RestController
@RequestMapping("/api/playlists/search")
public class PlaylistSearchController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistSearchController.class);

    private final SpotifyPlaylistSearchService playlistSearchService;
    private final SpotifyClientCredentialsAuthentication authController;

    public PlaylistSearchController(SpotifyPlaylistSearchService playlistSearchService,
                                    SpotifyClientCredentialsAuthentication authController) {
        this.playlistSearchService = playlistSearchService;
        this.authController = authController;
    }

    /**
     * プレイリストを検索するエンドポイント
     *
     * @param query  検索クエリ
     * @param offset 検索結果のオフセット (デフォルトは0)
     * @param limit  検索結果のリミット (デフォルトは20)
     * @return 検索結果のプレイリストのリストを含むResponseEntity
     */
    @GetMapping
    public ResponseEntity<List<PlaylistSimplified>> searchPlaylists(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {
        logger.info("Searching playlists. Query: {}, Offset: {}, Limit: {}", query, offset, limit);
        try {
            authController.authenticate();
            List<PlaylistSimplified> playlists = playlistSearchService.searchPlaylists(query, offset, limit);
            return ResponseEntity.ok(playlists);
        } catch (Exception e) {
            logger.error("Error occurred while searching playlists", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
