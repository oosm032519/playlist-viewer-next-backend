package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.service.analytics.PlaylistAnalyticsService;
import com.github.oosm032519.playlistviewernext.service.playlist.PlaylistDetailsRetrievalService;
import com.github.oosm032519.playlistviewernext.service.recommendation.TrackRecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistDetailsController.class);

    private final PlaylistDetailsRetrievalService playlistDetailsRetrievalService;
    private final PlaylistAnalyticsService playlistAnalyticsService;
    private final TrackRecommendationService trackRecommendationService;

    @Autowired
    public PlaylistDetailsController(
            PlaylistDetailsRetrievalService playlistDetailsRetrievalService,
            PlaylistAnalyticsService playlistAnalyticsService,
            TrackRecommendationService trackRecommendationService
    ) {
        this.playlistDetailsRetrievalService = playlistDetailsRetrievalService;
        this.playlistAnalyticsService = playlistAnalyticsService;
        this.trackRecommendationService = trackRecommendationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPlaylistById(@PathVariable String id) {
        logger.info("getPlaylistById: プレイリストID: {}", id);
        try {
            Map<String, Object> response = new HashMap<>(playlistDetailsRetrievalService.getPlaylistDetails(id));
            logger.info("getPlaylistById: プレイリスト詳細取得成功");

            // 全てのジャンルとその数を取得
            Map<String, Integer> genreCounts = playlistAnalyticsService.getGenreCountsForPlaylist(id);
            logger.info("getPlaylistById: ジャンル数: {}", genreCounts);

            // トップ5ジャンルを取得
            List<String> top5Genres = playlistAnalyticsService.getTop5GenresForPlaylist(id);
            logger.info("getPlaylistById: トップ5ジャンル: {}", top5Genres);

            // 最大オーディオフィーチャーを取得
            Map<String, Float> maxAudioFeatures = (Map<String, Float>) response.get("maxAudioFeatures");
            logger.info("getPlaylistById: 最大オーディオフィーチャー: {}", maxAudioFeatures);

            // 最小オーディオフィーチャーを取得
            Map<String, Float> minAudioFeatures = (Map<String, Float>) response.get("minAudioFeatures");
            logger.info("getPlaylistById: 最小オーディオフィーチャー: {}", minAudioFeatures);

            // おすすめトラックはトップ5ジャンルと最大・最小オーディオフィーチャーで取得
            List<Track> recommendations = trackRecommendationService.getRecommendations(top5Genres, maxAudioFeatures, minAudioFeatures);
            logger.info("getPlaylistById: 推奨トラック数: {}", recommendations.size());

            response.put("genreCounts", genreCounts); // 全てのジャンルと数を返す
            response.put("recommendations", recommendations); // おすすめトラックはトップ5に基づく

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("getPlaylistById: プレイリストの取得中にエラーが発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
