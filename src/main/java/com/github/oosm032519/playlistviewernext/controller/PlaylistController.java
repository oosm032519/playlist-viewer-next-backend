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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
@CrossOrigin(origins = "*")
public class PlaylistController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class);

    @Autowired
    private SpotifyService spotifyService;

    @GetMapping("/search")
    @CrossOrigin(origins = "*")
    public ResponseEntity<List<PlaylistSimplified>> searchPlaylists(@RequestParam String query) {
        logger.info("PlaylistController: searchPlaylists メソッドが呼び出されました。クエリ: {}", query);
        try {
            logger.info("PlaylistController: アクセストークンを取得します");
            spotifyService.getAccessToken();
            logger.info("PlaylistController: アクセストークンの取得に成功しました");

            logger.info("PlaylistController: プレイリストの検索を開始します");
            List<PlaylistSimplified> playlists = spotifyService.searchPlaylists(query);
            logger.info("PlaylistController: プレイリストの検索が完了しました。見つかったプレイリスト数: {}", playlists.size());

            logger.info("PlaylistController: 検索結果を返却します");
            return ResponseEntity.ok(playlists);
        } catch (IOException e) {
            logger.error("PlaylistController: プレイリストの検索中にIO例外が発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (SpotifyWebApiException e) {
            logger.error("PlaylistController: プレイリストの検索中にSpotify Web API例外が発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (ParseException e) {
            logger.error("PlaylistController: プレイリストの検索中に解析例外が発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } finally {
            logger.info("PlaylistController: searchPlaylists メソッドが終了しました");
        }
    }

    @GetMapping("/{id}")
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> getPlaylistById(@PathVariable String id) {
        logger.info("PlaylistController: getPlaylistById メソッドが呼び出されました。プレイリストID: {}", id);
        try {
            logger.info("PlaylistController: アクセストークンを取得します");
            spotifyService.getAccessToken();
            logger.info("PlaylistController: アクセストークンの取得に成功しました");

            logger.info("PlaylistController: プレイリストのトラック情報を取得します");
            PlaylistTrack[] tracks = spotifyService.getPlaylistTracks(id);
            logger.info("PlaylistController: プレイリストのトラック情報の取得が完了しました。トラック数: {}", tracks.length);

            logger.info("PlaylistController: トラック情報を items キーでラップして返却します");
            return ResponseEntity.ok(Map.of("tracks", Map.of("items", tracks)));
        } catch (IOException e) {
            logger.error("PlaylistController: プレイリストの取得中にIO例外が発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (SpotifyWebApiException e) {
            logger.error("PlaylistController: プレイリストの取得中にSpotify Web API例外が発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } catch (ParseException e) {
            logger.error("PlaylistController: プレイリストの取得中に解析例外が発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        } finally {
            logger.info("PlaylistController: getPlaylistById メソッドが終了しました");
        }
    }
}
