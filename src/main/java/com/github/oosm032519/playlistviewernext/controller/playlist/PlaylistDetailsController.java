package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import com.github.oosm032519.playlistviewernext.exception.ResourceNotFoundException;
import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
import com.github.oosm032519.playlistviewernext.service.analytics.PlaylistAnalyticsService;
import com.github.oosm032519.playlistviewernext.service.analytics.SpotifyPlaylistAnalyticsService;
import com.github.oosm032519.playlistviewernext.service.playlist.PlaylistDetailsRetrievalService;
import com.github.oosm032519.playlistviewernext.service.recommendation.TrackRecommendationService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final HttpServletRequest request;
    private final SpotifyPlaylistAnalyticsService spotifyPlaylistAnalyticsService;

    /**
     * PlaylistDetailsControllerのコンストラクタ
     *
     * @param playlistDetailsRetrievalService プレイリスト詳細取得サービス
     * @param playlistAnalyticsService        プレイリスト分析サービス
     * @param trackRecommendationService      トラック推奨サービス
     * @param request                         HTTPリクエスト
     */
    public PlaylistDetailsController(
            PlaylistDetailsRetrievalService playlistDetailsRetrievalService,
            PlaylistAnalyticsService playlistAnalyticsService,
            TrackRecommendationService trackRecommendationService,
            HttpServletRequest request,
            SpotifyPlaylistAnalyticsService spotifyPlaylistAnalyticsService
    ) {
        this.playlistDetailsRetrievalService = playlistDetailsRetrievalService;
        this.playlistAnalyticsService = playlistAnalyticsService;
        this.trackRecommendationService = trackRecommendationService;
        this.request = request;
        this.spotifyPlaylistAnalyticsService = spotifyPlaylistAnalyticsService;
    }

    /**
     * 指定されたプレイリストIDに基づいてプレイリストの詳細情報を取得する
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
        } catch (ResourceNotFoundException e) {
            // リソースが見つからない場合はそのまま例外をスロー
            logger.error("プレイリストが見つかりませんでした: {}", e.getMessage(), e);
            throw e;
        } catch (SpotifyApiException e) {
            // Spotify APIのエラーはそのまま例外をスロー
            logger.error("Spotify APIとの通信中にエラーが発生しました: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // 予期しないエラーが発生した場合は PlaylistViewerNextException をスロー
            logger.error("プレイリストの取得中に予期しないエラーが発生しました: {}", e.getMessage(), e);
            String requestParams = getRequestParams();
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "PLAYLIST_DETAILS_ERROR",
                    "プレイリスト情報の取得中にエラーが発生しました。URLが正しいか確認し、しばらく時間をおいてから再度お試しください。",
                    "リクエストパラメータ: " + requestParams,
                    e
            );
        }
    }

    /**
     * プレイリストの詳細情報を取得し、分析結果と推奨トラックを含めて返す
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

        // アーティスト分析を実行
        List<String> top5Artists = spotifyPlaylistAnalyticsService.getTop5ArtistsForPlaylist(id);

        // AudioFeaturesの統計情報を取得
        Map<String, Float> maxAudioFeatures = (Map<String, Float>) response.get("maxAudioFeatures");
        Map<String, Float> minAudioFeatures = (Map<String, Float>) response.get("minAudioFeatures");

        // 推奨トラックを取得
        List<Track> recommendations = trackRecommendationService.getRecommendations(
                top5Artists, maxAudioFeatures, minAudioFeatures);

        // レスポンスに追加情報を設定
        response.put("genreCounts", genreCounts);
        response.put("recommendations", recommendations);

        // 詳細情報をログに記録
        logPlaylistDetails(genreCounts, top5Artists, maxAudioFeatures, minAudioFeatures, recommendations);

        return response;
    }

    /**
     * プレイリストの詳細情報をログに記録する
     *
     * @param genreCounts      ジャンルごとの曲数
     * @param top5Artists       上位5つのアーティスト
     * @param maxAudioFeatures 最大AudioFeatures
     * @param minAudioFeatures 最小AudioFeatures
     * @param recommendations  推奨トラックリスト
     */
    private void logPlaylistDetails(Map<String, Integer> genreCounts, List<String> top5Artists,
                                    Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures,
                                    List<Track> recommendations) {
        logger.info("ジャンル数: {}", genreCounts);
        logger.info("トップ5アーティスト: {}", top5Artists);
        logger.info("最大AudioFeatures: {}", maxAudioFeatures);
        logger.info("最小AudioFeatures: {}", minAudioFeatures);
        logger.info("推奨トラック数: {}", recommendations.size());
    }

    // リクエストパラメータを取得するヘルパーメソッド
    private String getRequestParams() {
        StringBuilder params = new StringBuilder();
        request.getParameterMap().forEach((key, values) -> {
            params.append(key).append("=").append(String.join(",", values)).append("&");
        });
        if (params.length() > 0) {
            params.deleteCharAt(params.length() - 1);
        }
        return params.toString();
    }
}
