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
 * プレイリストの詳細情報を管理するRESTコントローラー
 * プレイリストの詳細情報の取得、ジャンル分析、楽曲推薦機能を提供する
 */
@RestController
@RequestMapping("/api/playlists")
public class PlaylistDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistDetailsController.class);

    private final PlaylistDetailsRetrievalService playlistDetailsRetrievalService;
    private final PlaylistAnalyticsService playlistAnalyticsService;
    private final TrackRecommendationService trackRecommendationService;

    /**
     * PlaylistDetailsControllerのコンストラクタ
     *
     * @param playlistDetailsRetrievalService プレイリスト詳細情報取得サービス
     * @param playlistAnalyticsService        プレイリスト分析サービス
     * @param trackRecommendationService      楽曲推薦サービス
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
     * 指定されたプレイリストの詳細情報とジャンル分析結果を取得する
     *
     * @param id プレイリストID
     * @return プレイリストの詳細情報とジャンル分析結果を含むResponseEntity
     */
    @GetMapping("/{id}/details")
    public ResponseEntity<Map<String, Object>> getPlaylistDetails(@PathVariable String id) {
        logger.info("プレイリストID: {} の詳細情報を取得中", id);

        // プレイリストの基本情報を取得
        Map<String, Object> playlistDetails = playlistDetailsRetrievalService.getPlaylistDetails(id);

        // ジャンル分析を実行し、結果を追加
        Map<String, Integer> genreCounts = playlistAnalyticsService.getGenreCountsForPlaylist(id);
        playlistDetails.put("genreCounts", genreCounts);

        return ResponseEntity.ok(playlistDetails);
    }

    /**
     * 指定された条件に基づいて楽曲推薦を提供する
     *
     * @param request 推薦リクエスト（アーティスト情報と音楽特徴の制約を含む）
     * @return 推薦された楽曲のリストを含むResponseEntity
     */
    @PostMapping("/recommendations")
    public ResponseEntity<List<Track>> getRecommendations(
            @RequestBody RecommendationRequest request
    ) {
        List<String> seedArtists = request.getSeedArtists();
        Map<String, Float> maxAudioFeatures = request.getMaxAudioFeatures();
        Map<String, Float> minAudioFeatures = request.getMinAudioFeatures();
        logger.info("seedArtists: {}, maxAudioFeatures: {}, minAudioFeatures: {}",
                seedArtists, maxAudioFeatures, minAudioFeatures);

        // 指定された条件に基づいて楽曲推薦を取得
        List<Track> recommendations = trackRecommendationService.getRecommendations(
                seedArtists, maxAudioFeatures, minAudioFeatures);
        return ResponseEntity.ok(recommendations);
    }
}
