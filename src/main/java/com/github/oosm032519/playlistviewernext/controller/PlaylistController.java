// PlaylistController.java

package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.service.SpotifyService;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class);

    @Autowired
    private SpotifyService spotifyService;

    @GetMapping("/search")
    public ResponseEntity<List<PlaylistSimplified>> searchPlaylists(@RequestParam String query) {
        logger.info("PlaylistController: searchPlaylists メソッドが呼び出されました。クエリ: {}", query);
        try {
            logger.info("PlaylistController: クライアントクレデンシャルトークンを取得します");
            spotifyService.getClientCredentialsToken();
            logger.info("PlaylistController: クライアントクレデンシャルトークンの取得に成功しました");

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
        public ResponseEntity<Map<String, Object>> getPlaylistById(@PathVariable String id) {
            logger.info("PlaylistController: getPlaylistById メソッドが呼び出されました。プレイリストID: {}", id);
            try {
                logger.info("PlaylistController: クライアントクレデンシャルトークンを取得します");
                spotifyService.getClientCredentialsToken();
                logger.info("PlaylistController: クライアントクレデンシャルトークンの取得に成功しました");

                logger.info("PlaylistController: プレイリストのトラック情報とaudio featuresを取得します");
                PlaylistTrack[] tracks = spotifyService.getPlaylistTracks(id);
                logger.info("PlaylistController: プレイリストのトラック情報とaudio featuresの取得が完了しました。トラック数: {}", tracks.length);

                List<Map<String, Object>> trackList = new ArrayList<>();
                for (PlaylistTrack track : tracks) {
                    Map<String, Object> trackData = new HashMap<>();
                    Track fullTrack = (Track) track.getTrack();
                    trackData.put("track", fullTrack);

                    // アーティストIDの取得とログ出力
                    ArtistSimplified[] artists = fullTrack.getArtists();
                    for (ArtistSimplified artist : artists) {
                        logger.info("PlaylistController: トラック '{}' のアーティストID: {}", fullTrack.getName(), artist.getId());
                    }

                    String trackId = fullTrack.getId();
                    trackData.put("audioFeatures", spotifyService.getAudioFeaturesForTrack(trackId));
                    trackList.add(trackData);
                }

                // ジャンルの出現回数を取得
                Map<String, Integer> genreCounts = spotifyService.getGenreCountsForPlaylist(id);

                // 上位5つのジャンルを取得
                List<String> top5Genres = spotifyService.getTop5GenresForPlaylist(id);

                // Spotify APIを呼び出してオススメ楽曲を取得
                try {
                    logger.info("PlaylistController: 上位5つのジャンルをシード値としてSpotify APIを呼び出します。ジャンル: {}", top5Genres);
                    spotifyService.getRecommendations(top5Genres);
                } catch (IOException | SpotifyWebApiException | ParseException e) {
                    logger.error("PlaylistController: Spotify APIの呼び出し中にエラーが発生しました。", e);
                    // エラー処理
                }

                // レスポンスの作成
                Map<String, Object> response = new HashMap<>();
                response.put("tracks", Map.of("items", trackList));
                response.put("genreCounts", genreCounts);

                logger.info("PlaylistController: トラック情報とジャンルの出現回数を返却します");
                return ResponseEntity.ok(response);
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

    @GetMapping("/followed")
    public ResponseEntity<?> getFollowedPlaylists(OAuth2AuthenticationToken authentication) {
        try {
            return ResponseEntity.ok(spotifyService.getCurrentUsersPlaylists(authentication));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
