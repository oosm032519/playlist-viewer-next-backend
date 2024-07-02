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
import se.michaelthelin.spotify.model_objects.specification.*;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistController.class);

    private final SpotifyService spotifyService;

    @Autowired
    public PlaylistController(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<PlaylistSimplified>> searchPlaylists(@RequestParam String query) {
        logger.info("PlaylistController: searchPlaylists メソッドが呼び出されました。クエリ: {}", query);
        try {
            spotifyService.getClientCredentialsToken();
            List<PlaylistSimplified> playlists = spotifyService.searchPlaylists(query);
            return ResponseEntity.ok(playlists);
        } catch (Exception e) {
            logger.error("PlaylistController: プレイリストの検索中にエラーが発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPlaylistById(@PathVariable String id) {
        logger.info("PlaylistController: getPlaylistById メソッドが呼び出されました。プレイリストID: {}", id);
        try {
            spotifyService.getClientCredentialsToken();

            PlaylistTrack[] tracks = spotifyService.getPlaylistTracks(id);
            List<Map<String, Object>> trackList = getTrackListData(tracks);

            Map<String, Integer> genreCounts = spotifyService.getGenreCountsForPlaylist(id);
            List<String> top5Genres = spotifyService.getTop5GenresForPlaylist(id);

            List<Track> recommendations = getRecommendations(top5Genres);

            String playlistName = spotifyService.getPlaylistName(id);
            User owner = spotifyService.getPlaylistOwner(id);

            Map<String, Object> response = createPlaylistResponse(trackList, genreCounts, recommendations, playlistName, owner);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("PlaylistController: プレイリストの取得中にエラーが発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    private List<Map<String, Object>> getTrackListData(PlaylistTrack[] tracks) throws SpotifyWebApiException, IOException, ParseException {
        List<Map<String, Object>> trackList = new ArrayList<>();
        for (PlaylistTrack track : tracks) {
            Map<String, Object> trackData = new HashMap<>();
            Track fullTrack = (Track) track.getTrack();
            trackData.put("track", fullTrack);

            String trackId = fullTrack.getId();
            trackData.put("audioFeatures", spotifyService.getAudioFeaturesForTrack(trackId));
            trackList.add(trackData);
        }
        return trackList;
    }

    private List<Track> getRecommendations(List<String> top5Genres) {
        List<Track> recommendations = new ArrayList<>();
        try {
            if (!top5Genres.isEmpty()) {
                recommendations = spotifyService.getRecommendations(top5Genres);
            }
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("PlaylistController: Spotify APIの呼び出し中にエラーが発生しました。", e);
        }
        return recommendations;
    }

    private Map<String, Object> createPlaylistResponse(List<Map<String, Object>> trackList,
                                                       Map<String, Integer> genreCounts,
                                                       List<Track> recommendations,
                                                       String playlistName,
                                                       User owner) {
        Map<String, Object> response = new HashMap<>();
        response.put("tracks", Map.of("items", trackList));
        response.put("genreCounts", genreCounts);
        response.put("recommendations", recommendations);
        response.put("playlistName", playlistName);
        response.put("ownerId", owner.getId());
        response.put("ownerName", owner.getDisplayName());
        return response;
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
