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

    @Autowired
    private SpotifyPlaylistDetailsService playlistDetailsService;
    @Autowired
    private SpotifyTrackService trackService;
    @Autowired
    private SpotifyClientCredentialsAuthentication authController;
    @Autowired
    private MaxAudioFeaturesCalculator maxAudioFeaturesCalculator;
    @Autowired
    private MinAudioFeaturesCalculator minAudioFeaturesCalculator;
    @Autowired
    private MedianAudioFeaturesCalculator medianAudioFeaturesCalculator;
    @Autowired
    private AverageAudioFeaturesCalculator averageAudioFeaturesCalculator;
    @Autowired
    private TrackDataRetriever trackDataRetriever;
    @Autowired
    private ModeValuesCalculator modeValuesCalculator;

    public Map<String, Object> getPlaylistDetails(String id) throws Exception {
        logger.info("getPlaylistDetails: プレイリストID: {}", id);
        authController.authenticate();

        PlaylistTrack[] tracks = playlistDetailsService.getPlaylistTracks(id);
        List<Map<String, Object>> trackList = trackDataRetriever.getTrackListData(tracks);

        String playlistName = playlistDetailsService.getPlaylistName(id);
        User owner = playlistDetailsService.getPlaylistOwner(id);

        Map<String, Float> maxAudioFeatures = maxAudioFeaturesCalculator.calculateMaxAudioFeatures(trackList);
        logger.info("getPlaylistDetails: 最大オーディオフィーチャー: {}", maxAudioFeatures);

        Map<String, Float> minAudioFeatures = minAudioFeaturesCalculator.calculateMinAudioFeatures(trackList);
        logger.info("getPlaylistDetails: 最小オーディオフィーチャー: {}", minAudioFeatures);

        Map<String, Float> medianAudioFeatures = medianAudioFeaturesCalculator.calculateMedianAudioFeatures(trackList);
        logger.info("getPlaylistDetails: 中央オーディオフィーチャー: {}", medianAudioFeatures);

        Map<String, Float> averageAudioFeatures = averageAudioFeaturesCalculator.calculateAverageAudioFeatures(trackList);
        logger.info("getPlaylistDetails: 平均オーディオフィーチャー: {}", averageAudioFeatures);

        Map<String, Object> modeValues = modeValuesCalculator.calculateModeValues(trackList);
        logger.info("getPlaylistDetails: 最頻値: {}", modeValues);

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
