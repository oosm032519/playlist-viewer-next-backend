// PlaylistDetailsController.java

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

/**
 * プレイリストの詳細情報を提供するコントローラークラス
 */
@RestController
@RequestMapping("/api/playlists")
public class PlaylistDetailsController {

    // ロガーの初期化
    private static final Logger logger = LoggerFactory.getLogger(PlaylistDetailsController.class);

    // サービスの依存性を注入
    private final PlaylistDetailsRetrievalService playlistDetailsRetrievalService;
    private final PlaylistAnalyticsService playlistAnalyticsService;
    private final TrackRecommendationService trackRecommendationService;

    /**
     * コンストラクタでサービスを注入
     *
     * @param playlistDetailsRetrievalService プレイリスト詳細取得サービス
     * @param playlistAnalyticsService        プレイリスト分析サービス
     * @param trackRecommendationService      トラック推薦サービス
     */
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

    /**
     * プレイリストIDに基づいてプレイリストの詳細情報を取得
     *
     * @param id プレイリストID
     * @return プレイリストの詳細情報を含むレスポンスエンティティ
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPlaylistById(@PathVariable String id) {
        logger.info("getPlaylistById: プレイリストID: {}", id);
        try {
            // プレイリストの詳細情報を取得
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

            // 中央オーディオフィーチャーを取得
            Map<String, Float> medianAudioFeatures = (Map<String, Float>) response.get("medianAudioFeatures");
            logger.info("getPlaylistById: 中央オーディオフィーチャー: {}", medianAudioFeatures);

            // modeValuesを取得
            Map<String, Object> modeValues = (Map<String, Object>) response.get("modeValues");
            logger.info("getPlaylistById: 最頻値: {}", modeValues);

            // おすすめトラックはトップ5ジャンルと最大・最小・中央値オーディオフィーチャー、最頻値で取得
            List<Track> recommendations = trackRecommendationService.getRecommendations(top5Genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);
            logger.info("getPlaylistById: 推奨トラック数: {}", recommendations.size());

            // レスポンスにジャンル数と推奨トラックを追加
            response.put("genreCounts", genreCounts); // 全てのジャンルと数を返す
            response.put("recommendations", recommendations); // おすすめトラックはトップ5に基づく

            // 成功レスポンスを返す
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // エラー発生時のログ出力とエラーレスポンスの返却
            logger.error("getPlaylistById: プレイリストの取得中にエラーが発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
