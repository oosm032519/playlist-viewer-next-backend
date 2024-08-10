package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import com.github.oosm032519.playlistviewernext.service.analytics.PlaylistAnalyticsService;
import com.github.oosm032519.playlistviewernext.service.playlist.PlaylistDetailsRetrievalService;
import com.github.oosm032519.playlistviewernext.service.recommendation.TrackRecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * プレイリストの詳細情報を提供するコントローラークラス。
 * このクラスは、プレイリストの詳細情報の取得、分析、および推奨トラックの提供を行います。
 */
@RestController
@RequestMapping("/api/playlists")
public class PlaylistDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistDetailsController.class);

    private final PlaylistDetailsRetrievalService playlistDetailsRetrievalService;
    private final PlaylistAnalyticsService playlistAnalyticsService;
    private final TrackRecommendationService trackRecommendationService;

    /**
     * PlaylistDetailsControllerのコンストラクタ。
     * 必要なサービスを注入します。
     *
     * @param playlistDetailsRetrievalService プレイリスト詳細取得サービス
     * @param playlistAnalyticsService        プレイリスト分析サービス
     * @param trackRecommendationService      トラック推奨サービス
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
     * 指定されたプレイリストIDに基づいてプレイリストの詳細情報を取得します。
     *
     * @param id 取得するプレイリストのID
     * @return プレイリストの詳細情報を含むResponseEntity
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPlaylistById(@PathVariable String id) {
        logger.info("プレイリストID: {} の詳細情報を取得中", id);
        try {
            Map<String, Object> response = fetchPlaylistDetails(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // エラーが発生した場合は PlaylistViewerNextException をスロー
            logger.error("プレイリストの取得中にエラーが発生しました", e);
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "PLAYLIST_DETAILS_ERROR",
                    "プレイリストの詳細情報の取得中にエラーが発生しました。",
                    e
            );
        }
    }

    /**
     * プレイリストの詳細情報を取得し、分析結果と推奨トラックを含めて返します。
     *
     * @param id プレイリストID
     * @return プレイリストの詳細情報、分析結果、推奨トラックを含むMap
     * @throws Exception プレイリスト情報の取得や分析中に発生した例外
     */
    private Map<String, Object> fetchPlaylistDetails(String id) throws Exception {
        // プレイリストの基本情報を取得
        Map<String, Object> response = new HashMap<>(playlistDetailsRetrievalService.getPlaylistDetails(id));

        // ジャンル分析を実行
        Map<String, Integer> genreCounts = playlistAnalyticsService.getGenreCountsForPlaylist(id);
        List<String> top5Genres = playlistAnalyticsService.getTop5GenresForPlaylist(id);

        // オーディオフィーチャーの統計情報を取得
        Map<String, Float> maxAudioFeatures = (Map<String, Float>) response.get("maxAudioFeatures");
        Map<String, Float> minAudioFeatures = (Map<String, Float>) response.get("minAudioFeatures");
        Map<String, Float> medianAudioFeatures = (Map<String, Float>) response.get("medianAudioFeatures");
        Map<String, Object> modeValues = (Map<String, Object>) response.get("modeValues");

        // 推奨トラックを取得
        List<Track> recommendations = trackRecommendationService.getRecommendations(
                top5Genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);

        // レスポンスに追加情報を設定
        response.put("genreCounts", genreCounts);
        response.put("recommendations", recommendations);

        // 詳細情報をログに記録
        logPlaylistDetails(genreCounts, top5Genres, maxAudioFeatures, minAudioFeatures,
                medianAudioFeatures, modeValues, recommendations);

        return response;
    }

    /**
     * プレイリストの詳細情報をログに記録します。
     *
     * @param genreCounts         ジャンルごとの曲数
     * @param top5Genres          上位5つのジャンル
     * @param maxAudioFeatures    最大オーディオフィーチャー値
     * @param minAudioFeatures    最小オーディオフィーチャー値
     * @param medianAudioFeatures 中央値のオーディオフィーチャー
     * @param modeValues          最頻値のオーディオフィーチャー
     * @param recommendations     推奨トラックリスト
     */
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
