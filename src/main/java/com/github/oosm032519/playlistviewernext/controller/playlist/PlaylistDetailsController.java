package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.RecommendationRequest;
import com.github.oosm032519.playlistviewernext.service.analytics.PlaylistAnalyticsService;
import com.github.oosm032519.playlistviewernext.service.playlist.PlaylistDetailsRetrievalService;
import com.github.oosm032519.playlistviewernext.service.recommendation.TrackRecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.List;
import java.util.Map;

/**
 * プレイリストの詳細情報を提供するコントローラークラス
 * プレイリストの詳細情報の取得、分析、および推奨トラックの提供を行う
 */
@RestController
@RequestMapping("/api/playlists")
public class PlaylistDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistDetailsController.class);

    private final PlaylistDetailsRetrievalService playlistDetailsRetrievalService;
    private final PlaylistAnalyticsService playlistAnalyticsService;
    private final TrackRecommendationService trackRecommendationService;


    public PlaylistDetailsController(
            PlaylistDetailsRetrievalService playlistDetailsRetrievalService,
            PlaylistAnalyticsService playlistAnalyticsService,
            TrackRecommendationService trackRecommendationService
    ) {
        this.playlistDetailsRetrievalService = playlistDetailsRetrievalService;
        this.playlistAnalyticsService = playlistAnalyticsService;
        this.trackRecommendationService = trackRecommendationService;
    }


    @GetMapping("/{id}/details")
    public ResponseEntity<Map<String, Object>> getPlaylistDetails(@PathVariable String id) {
        logger.info("プレイリストID: {} の詳細情報を取得中", id);

        Map<String, Object> playlistDetails = playlistDetailsRetrievalService.getPlaylistDetails(id);

        Map<String, Integer> genreCounts = playlistAnalyticsService.getGenreCountsForPlaylist(id);
        playlistDetails.put("genreCounts", genreCounts);

        return ResponseEntity.ok(playlistDetails);
    }


    @PostMapping("/recommendations")
    public ResponseEntity<List<Track>> getRecommendations(
            @RequestBody RecommendationRequest request
    ) {
        List<String> seedArtists = request.getSeedArtists();
        Map<String, Float> maxAudioFeatures = request.getMaxAudioFeatures
                ();
        Map<String, Float> minAudioFeatures = request.getMinAudioFeatures();
        logger.info("seedArtists: {}, maxAudioFeatures: {}, minAudioFeatures: {}", seedArtists, maxAudioFeatures, minAudioFeatures);

        List<Track> recommendations = trackRecommendationService.getRecommendations(
                seedArtists, maxAudioFeatures, minAudioFeatures);
        return ResponseEntity.ok(recommendations);
    }
}
