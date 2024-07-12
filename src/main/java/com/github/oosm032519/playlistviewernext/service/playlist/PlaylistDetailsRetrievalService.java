package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.service.analytics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.util.*;

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
     * @throws Exception 認証やデータ取得に失敗した場合にスローされる例外
     */
    public Map<String, Object> getPlaylistDetails(String id) throws Exception {
        logger.info("getPlaylistDetails: プレイリストID: {}", id);

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

        return createResponse(trackList, playlistName, owner, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, averageAudioFeatures, modeValues);
    }

    private void logAudioFeatures(Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures, Map<String, Float> medianAudioFeatures, Map<String, Float> averageAudioFeatures, Map<String, Object> modeValues) {
        logger.info("getPlaylistDetails: 最大オーディオフィーチャー: {}", maxAudioFeatures);
        logger.info("getPlaylistDetails: 最小オーディオフィーチャー: {}", minAudioFeatures);
        logger.info("getPlaylistDetails: 中央オーディオフィーチャー: {}", medianAudioFeatures);
        logger.info("getPlaylistDetails: 平均オーディオフィーチャー: {}", averageAudioFeatures);
        logger.info("getPlaylistDetails: 最頻値: {}", modeValues);
    }

    private Map<String, Object> createResponse(List<Map<String, Object>> trackList, String playlistName, User owner, Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures, Map<String, Float> medianAudioFeatures, Map<String, Float> averageAudioFeatures, Map<String, Object> modeValues) {
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
        return response;
    }
}
