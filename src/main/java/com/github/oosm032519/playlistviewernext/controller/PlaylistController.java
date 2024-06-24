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

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class); // ロガーを追加

    @Autowired
    private SpotifyService spotifyService;

    @GetMapping("/search")
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
}
