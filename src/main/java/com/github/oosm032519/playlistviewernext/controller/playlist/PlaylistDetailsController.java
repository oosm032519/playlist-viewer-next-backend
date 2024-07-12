package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.service.analytics.PlaylistAnalyticsService;
import com.github.oosm032519.playlistviewernext.service.playlist.PlaylistDetailsRetrievalService;
import com.github.oosm032519.playlistviewernext.service.recommendation.TrackRecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * プレイリストの詳細情報を提供するコントローラークラス
 */
@RestController
@RequestMapping("/api/playlists")
public class PlaylistDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistDetailsController.class);

    private final PlaylistDetailsRetrievalService playlistDetailsRetrievalService;
    private final PlaylistAnalyticsService playlistAnalyticsService;
    private final TrackRecommendationService trackRecommendationService;

    /**
     * コンストラクタでサービスを注入
     */
    public PlaylistDetailsController(
            PlaylistDetailsRetrievalService playlistDetailsRetrievalService,
            PlaylistAnalyticsService playlistAnalyticsService,
            TrackRecommendationService trackRecommendationService
    ) {
        this.playlistDetailsRetrievalService = playlistDetailsRetrievalService;
        this.playlistAnalyticsService = playlistAnalyticsService;
        this.trackRecommendationService = trackRecommendationService;
    }

    /**
     * プレイリストIDに基づいてプレイリストの詳細情報を取得
     *
     * @param id プレイリストID
     * @return プレイリストの詳細情報を含むレスポンスエンティティ
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPlaylistById(@PathVariable String id) {
        logger.info("プレイリストID: {} の詳細情報を取得中", id);
        try {
            Map<String, Object> response = fetchPlaylistDetails(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("プレイリストの取得中にエラーが発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> fetchPlaylistDetails(String id) throws Exception {
        Map<String, Object> response = new HashMap<>(playlistDetailsRetrievalService.getPlaylistDetails(id));

        Map<String, Integer> genreCounts = playlistAnalyticsService.getGenreCountsForPlaylist(id);
        List<String> top5Genres = playlistAnalyticsService.getTop5GenresForPlaylist(id);

        Map<String, Float> maxAudioFeatures = (Map<String, Float>) response.get("maxAudioFeatures");
        Map<String, Float> minAudioFeatures = (Map<String, Float>) response.get("minAudioFeatures");
        Map<String, Float> medianAudioFeatures = (Map<String, Float>) response.get("medianAudioFeatures");
        Map<String, Object> modeValues = (Map<String, Object>) response.get("modeValues");

        List<Track> recommendations = trackRecommendationService.getRecommendations(
                top5Genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);

        response.put("genreCounts", genreCounts);
        response.put("recommendations", recommendations);

        logPlaylistDetails(genreCounts, top5Genres, maxAudioFeatures, minAudioFeatures,
                medianAudioFeatures, modeValues, recommendations);

        return response;
    }

    private void logPlaylistDetails(Map<String, Integer> genreCounts, List<String> top5Genres,
                                    Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures,
                                    Map<String, Float> medianAudioFeatures, Map<String, Object> modeValues,
                                    List<Track> recommendations) {
        logger.info("ジャンル数: {}", genreCounts);
        logger.info("トップ5ジャンル: {}", top5Genres);
        logger.info("最大オーディオフィーチャー: {}", maxAudioFeatures);
        logger.info("最小オーディオフィーチャー: {}", minAudioFeatures);
        logger.info("中央オーディオフィーチャー: {}", medianAudioFeatures);
        logger.info("最頻値: {}", modeValues);
        logger.info("推奨トラック数: {}", recommendations.size());
    }
}
