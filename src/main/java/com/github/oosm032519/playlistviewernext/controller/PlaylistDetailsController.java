package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.*;

@RestController
@RequestMapping("/api/playlists/details")
public class PlaylistDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistDetailsController.class);

    @Autowired
    private final SpotifyPlaylistDetailsService playlistDetailsService;
    @Autowired
    private final SpotifyTrackService trackService;
    @Autowired
    private final SpotifyAnalyticsService analyticsService;
    @Autowired
    private final SpotifyRecommendationService recommendationService;
    @Autowired
    private final PlaylistAuthController authController;

    @Autowired
    public PlaylistDetailsController(
            SpotifyPlaylistDetailsService playlistDetailsService,
            SpotifyTrackService trackService,
            SpotifyAnalyticsService analyticsService,
            SpotifyRecommendationService recommendationService,
            PlaylistAuthController authController
    ) {
        this.playlistDetailsService = playlistDetailsService;
        this.trackService = trackService;
        this.analyticsService = analyticsService;
        this.recommendationService = recommendationService;
        this.authController = authController;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPlaylistById(@PathVariable String id) {
        logger.info("PlaylistDetailsController: getPlaylistById メソッドが呼び出されました。プレイリストID: {}", id);
        try {
            authController.authenticate();

            PlaylistTrack[] tracks = playlistDetailsService.getPlaylistTracks(id);
            List<Map<String, Object>> trackList = getTrackListData(tracks);

            Map<String, Integer> genreCounts = analyticsService.getGenreCountsForPlaylist(id);
            List<String> top5Genres = analyticsService.getTop5GenresForPlaylist(id);

            List<Track> recommendations = getRecommendations(top5Genres);

            String playlistName = playlistDetailsService.getPlaylistName(id);
            User owner = playlistDetailsService.getPlaylistOwner(id);

            Map<String, Object> response = createPlaylistResponse(trackList, genreCounts, recommendations, playlistName, owner);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("PlaylistDetailsController: プレイリストの取得中にエラーが発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    private List<Map<String, Object>> getTrackListData(PlaylistTrack[] tracks) throws Exception {
        List<Map<String, Object>> trackList = new ArrayList<>();
        for (PlaylistTrack track : tracks) {
            Map<String, Object> trackData = new HashMap<>();
            Track fullTrack = (Track) track.getTrack();
            trackData.put("track", fullTrack);

            String trackId = fullTrack.getId();
            trackData.put("audioFeatures", trackService.getAudioFeaturesForTrack(trackId));
            trackList.add(trackData);
        }
        return trackList;
    }

    private List<Track> getRecommendations(List<String> top5Genres) {
        List<Track> recommendations = new ArrayList<>();
        try {
            if (!top5Genres.isEmpty()) {
                recommendations = recommendationService.getRecommendations(top5Genres);
            }
        } catch (Exception e) {
            logger.error("PlaylistDetailsController: Spotify APIの呼び出し中にエラーが発生しました。", e);
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
}
