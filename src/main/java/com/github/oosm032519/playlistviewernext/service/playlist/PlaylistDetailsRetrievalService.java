package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.exception.InvalidRequestException;
import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import com.github.oosm032519.playlistviewernext.exception.ResourceNotFoundException;
import com.github.oosm032519.playlistviewernext.service.analytics.AverageAudioFeaturesCalculator;
import com.github.oosm032519.playlistviewernext.service.analytics.MaxAudioFeaturesCalculator;
import com.github.oosm032519.playlistviewernext.service.analytics.MinAudioFeaturesCalculator;
import com.github.oosm032519.playlistviewernext.service.analytics.SpotifyPlaylistAnalyticsService;
import com.github.oosm032519.playlistviewernext.service.recommendation.SpotifyRecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class PlaylistDetailsRetrievalService {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistDetailsRetrievalService.class);

    private final SpotifyPlaylistDetailsService playlistDetailsService;
    private final SpotifyClientCredentialsAuthentication authController;
    private final MaxAudioFeaturesCalculator maxAudioFeaturesCalculator;
    private final MinAudioFeaturesCalculator minAudioFeaturesCalculator;
    private final AverageAudioFeaturesCalculator averageAudioFeaturesCalculator;
    private final TrackDataRetriever trackDataRetriever;
    private final SpotifyPlaylistAnalyticsService playlistAnalyticsService;

    @Autowired
    public PlaylistDetailsRetrievalService(
            SpotifyPlaylistDetailsService playlistDetailsService,
            SpotifyClientCredentialsAuthentication authController,
            MaxAudioFeaturesCalculator maxAudioFeaturesCalculator,
            MinAudioFeaturesCalculator minAudioFeaturesCalculator,
            AverageAudioFeaturesCalculator averageAudioFeaturesCalculator,
            TrackDataRetriever trackDataRetriever,
            SpotifyPlaylistAnalyticsService playlistAnalyticsService,
            SpotifyRecommendationService trackRecommendationService) {
        this.playlistDetailsService = playlistDetailsService;
        this.authController = authController;
        this.maxAudioFeaturesCalculator = maxAudioFeaturesCalculator;
        this.minAudioFeaturesCalculator = minAudioFeaturesCalculator;
        this.averageAudioFeaturesCalculator = averageAudioFeaturesCalculator;
        this.trackDataRetriever = trackDataRetriever;
        this.playlistAnalyticsService = playlistAnalyticsService;
    }

    /**
     * プレイリストの詳細情報を取得するメソッド
     *
     * @param id プレイリストのID
     * @return プレイリストの詳細情報を含むマップ
     * @throws PlaylistViewerNextException 認証やデータ取得に失敗した場合にスローされる例外
     */
    public Map<String, Object> getPlaylistDetails(String id) {
        logger.info("getPlaylistDetails: プレイリストID: {}", id);

        try {
            authController.authenticate();

            Playlist playlist = playlistDetailsService.getPlaylist(id);
            if (playlist == null) {
                throw new ResourceNotFoundException(
                        HttpStatus.NOT_FOUND,
                        "指定されたプレイリストが見つかりません。"
                );
            }

            String playlistName = playlist.getName();
            User owner = playlist.getOwner();

            PlaylistTrack[] tracks = playlistDetailsService.getPlaylistTracks(id);
            List<Map<String, Object>> trackList = trackDataRetriever.getTrackListData(tracks);


            CompletableFuture<Map<String, Float>> maxFuture = CompletableFuture.supplyAsync(() -> maxAudioFeaturesCalculator.calculateMaxAudioFeatures(trackList));
            CompletableFuture<Map<String, Float>> minFuture = CompletableFuture.supplyAsync(() -> minAudioFeaturesCalculator.calculateMinAudioFeatures(trackList));
            CompletableFuture<Map<String, Float>> averageFuture = CompletableFuture.supplyAsync(() -> averageAudioFeaturesCalculator.calculateAverageAudioFeatures(trackList));

            // 全てのFutureが完了するのを待つ
            CompletableFuture.allOf(maxFuture, minFuture, averageFuture).join();

            Map<String, Float> maxAudioFeatures = maxFuture.join();
            Map<String, Float> minAudioFeatures = minFuture.join();
            Map<String, Float> averageAudioFeatures = averageFuture.join();

            // アーティスト出現頻度上位5件を取得
            List<String> seedArtists = playlistAnalyticsService.getTop5ArtistsForPlaylist(id);

            logAudioFeatures(maxAudioFeatures, minAudioFeatures, averageAudioFeatures);

            long totalDuration = calculateTotalDuration(tracks);

            return createResponse(trackList, playlistName, owner, maxAudioFeatures, minAudioFeatures, averageAudioFeatures, totalDuration, seedArtists);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("プレイリストの詳細情報の取得中に予期しないエラーが発生しました。", e);
            throw new InvalidRequestException(HttpStatus.INTERNAL_SERVER_ERROR, "プレイリストの詳細情報の取得中にエラーが発生しました。", e);
        }
    }

    /**
     * プレイリストの総再生時間を計算するメソッド
     *
     * @param tracks プレイリストのトラック配列
     * @return 総再生時間（ミリ秒）
     */
    public long calculateTotalDuration(PlaylistTrack[] tracks) {
        long totalDuration = 0;
        for (PlaylistTrack track : tracks) {
            totalDuration += track.getTrack().getDurationMs();
        }
        return totalDuration;
    }

    private void logAudioFeatures(Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures, Map<String, Float> averageAudioFeatures) {
        logger.info("getPlaylistDetails: 最大AudioFeatures: {}", maxAudioFeatures);
        logger.info("getPlaylistDetails: 最小AudioFeatures: {}", minAudioFeatures);
        logger.info("getPlaylistDetails: 平均AudioFeatures: {}", averageAudioFeatures);
    }

    private Map<String, Object> createResponse(List<Map<String, Object>> trackList, String playlistName, User owner, Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures, Map<String, Float> averageAudioFeatures, long totalDuration, final List<String> seedArtists) {
        Map<String, Object> response = new HashMap<>();
        response.put("tracks", Map.of("items", trackList));
        response.put("playlistName", playlistName);
        response.put("ownerId", owner.getId());
        response.put("ownerName", owner.getDisplayName());
        response.put("maxAudioFeatures", maxAudioFeatures);
        response.put("minAudioFeatures", minAudioFeatures);
        response.put("averageAudioFeatures", averageAudioFeatures);
        response.put("totalDuration", totalDuration);
        response.put("seedArtists", seedArtists);
        return response;
    }
}
