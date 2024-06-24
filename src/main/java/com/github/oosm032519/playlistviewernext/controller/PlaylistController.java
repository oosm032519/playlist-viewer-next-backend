// PlaylistController.java

package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.service.SpotifyService;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
@CrossOrigin(origins = "*")
public class PlaylistController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class); // ロガーを追加

    @Autowired
    private SpotifyService spotifyService;

    @GetMapping("/search")
    @CrossOrigin(origins = "*")
    public ResponseEntity<List<PlaylistSimplified>> searchPlaylists(@RequestParam String query) {
        logger.info("PlaylistController: searchPlaylists called with query: {}", query); // クエリをログ出力
        try {
            spotifyService.getAccessToken();
            List<PlaylistSimplified> playlists = spotifyService.searchPlaylists(query);
            logger.info("PlaylistController: Found {} playlists", playlists.size()); // プレイリスト数をログ出力
            return ResponseEntity.ok(playlists);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("PlaylistController: Error searching playlists", e); // エラーをログ出力
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/{id}") // 新しいエンドポイントを追加
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> getPlaylistById(@PathVariable String id) {
        try {
            spotifyService.getAccessToken();
            PlaylistTrack[] tracks = spotifyService.getPlaylistTracks(id);
            logger.info("PlaylistController: Found {} tracks", tracks.length);
            return ResponseEntity.ok(Map.of("tracks", tracks));
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("PlaylistController: Error getting playlist", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
