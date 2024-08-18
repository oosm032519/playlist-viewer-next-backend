package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import com.github.oosm032519.playlistviewernext.exception.ResourceNotFoundException;
import com.github.oosm032519.playlistviewernext.service.analytics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlaylistDetailsRetrievalService {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistDetailsRetrievalService.class);

    private final SpotifyPlaylistDetailsService playlistDetailsService;
    private final SpotifyClientCredentialsAuthentication authController;
    private final MaxAudioFeaturesCalculator maxAudioFeaturesCalculator;
    private final MinAudioFeaturesCalculator minAudioFeaturesCalculator;
    private final MedianAudioFeaturesCalculator medianAudioFeaturesCalculator;
    private final AverageAudioFeaturesCalculator averageAudioFeaturesCalculator;
    private final TrackDataRetriever trackDataRetriever;
    private final ModeValuesCalculator modeValuesCalculator;

    @Autowired
    public PlaylistDetailsRetrievalService(
            SpotifyPlaylistDetailsService playlistDetailsService,
            SpotifyTrackService trackService,
            SpotifyClientCredentialsAuthentication authController,
            MaxAudioFeaturesCalculator maxAudioFeaturesCalculator,
            MinAudioFeaturesCalculator minAudioFeaturesCalculator,
            MedianAudioFeaturesCalculator medianAudioFeaturesCalculator,
            AverageAudioFeaturesCalculator averageAudioFeaturesCalculator,
            TrackDataRetriever trackDataRetriever,
            ModeValuesCalculator modeValuesCalculator) {
        this.playlistDetailsService = playlistDetailsService;
        this.authController = authController;
        this.maxAudioFeaturesCalculator = maxAudioFeaturesCalculator;
        this.minAudioFeaturesCalculator = minAudioFeaturesCalculator;
        this.medianAudioFeaturesCalculator = medianAudioFeaturesCalculator;
        this.averageAudioFeaturesCalculator = averageAudioFeaturesCalculator;
        this.trackDataRetriever = trackDataRetriever;
        this.modeValuesCalculator = modeValuesCalculator;
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

            PlaylistTrack[] tracks = playlistDetailsService.getPlaylistTracks(id);
            List<Map<String, Object>> trackList = trackDataRetriever.getTrackListData(tracks);
            String playlistName = playlistDetailsService.getPlaylistName(id);
            User owner = playlistDetailsService.getPlaylistOwner(id);

            Map<String, Float> maxAudioFeatures = maxAudioFeaturesCalculator.calculateMaxAudioFeatures(trackList);
            Map<String, Float> minAudioFeatures = minAudioFeaturesCalculator.calculateMinAudioFeatures(trackList);
            Map<String, Float> medianAudioFeatures = medianAudioFeaturesCalculator.calculateMedianAudioFeatures(trackList);
            Map<String, Float> averageAudioFeatures = averageAudioFeaturesCalculator.calculateAverageAudioFeatures(trackList);
            Map<String, Object> modeValues = modeValuesCalculator.calculateModeValues(trackList);

            logAudioFeatures(maxAudioFeatures, minAudioFeatures, medianAudioFeatures, averageAudioFeatures, modeValues);

            long totalDuration = calculateTotalDuration(tracks);

            return createResponse(trackList, playlistName, owner, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, averageAudioFeatures, modeValues, totalDuration);

        } catch (ResourceNotFoundException e) {
            // リソースが見つからないエラーはそのまま再スロー
            logger.error("リソースが見つかりません。", e);
            throw e;
        } catch (Exception e) {
            // その他のエラー
            logger.error("プレイリストの詳細情報の取得中に予期しないエラーが発生しました。", e);
            throw new PlaylistViewerNextException(HttpStatus.INTERNAL_SERVER_ERROR, "PLAYLIST_DETAILS_RETRIEVAL_ERROR", "プレイリストの詳細情報の取得中にエラーが発生しました。", e);
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

    private void logAudioFeatures(Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures, Map<String, Float> medianAudioFeatures, Map<String, Float> averageAudioFeatures, Map<String, Object> modeValues) {
        logger.info("getPlaylistDetails: 最大AudioFeatures: {}", maxAudioFeatures);
        logger.info("getPlaylistDetails: 最小AudioFeatures: {}", minAudioFeatures);
        logger.info("getPlaylistDetails: 中央AudioFeatures: {}", medianAudioFeatures);
        logger.info("getPlaylistDetails: 平均AudioFeatures: {}", averageAudioFeatures);
        logger.info("getPlaylistDetails: 最頻値: {}", modeValues);
    }

    private Map<String, Object> createResponse(List<Map<String, Object>> trackList, String playlistName, User owner, Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures, Map<String, Float> medianAudioFeatures, Map<String, Float> averageAudioFeatures, Map<String, Object> modeValues, long totalDuration) {
        Map<String, Object> response = new HashMap<>();
        response.put("tracks", Map.of("items", trackList));
        response.put("playlistName", playlistName);
        response.put("ownerId", owner.getId());
        response.put("ownerName", owner.getDisplayName());
        response.put("maxAudioFeatures", maxAudioFeatures);
        response.put("minAudioFeatures", minAudioFeatures);
        response.put("medianAudioFeatures", medianAudioFeatures);
        response.put("averageAudioFeatures", averageAudioFeatures);
        response.put("modeValues", modeValues);
        response.put("totalDuration", totalDuration);
        return response;
    }
}
