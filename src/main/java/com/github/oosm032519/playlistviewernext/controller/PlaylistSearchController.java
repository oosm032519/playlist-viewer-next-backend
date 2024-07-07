package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.service.SpotifyPlaylistSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

import java.util.List;

@RestController
@RequestMapping("/api/playlists/search")
public class PlaylistSearchController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistSearchController.class);

    @Autowired
    private final SpotifyPlaylistSearchService playlistSearchService;
    @Autowired
    private final PlaylistAuthController authController;

    @Autowired
    public PlaylistSearchController(SpotifyPlaylistSearchService playlistSearchService, PlaylistAuthController authController) {
        this.playlistSearchService = playlistSearchService;
        this.authController = authController;
    }

    @GetMapping
    public ResponseEntity<List<PlaylistSimplified>> searchPlaylists(@RequestParam String query) {
        logger.info("PlaylistSearchController: searchPlaylists メソッドが呼び出されました。クエリ: {}", query);
        try {
            authController.authenticate();
            List<PlaylistSimplified> playlists = playlistSearchService.searchPlaylists(query);
            return ResponseEntity.ok(playlists);
        } catch (Exception e) {
            logger.error("PlaylistSearchController: プレイリストの検索中にエラーが発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}