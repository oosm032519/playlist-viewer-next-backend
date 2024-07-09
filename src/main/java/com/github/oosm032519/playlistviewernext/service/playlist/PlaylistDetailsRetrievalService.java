package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
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

    public Map<String, Object> getPlaylistDetails(String id) throws Exception {
        logger.info("getPlaylistDetails: プレイリストID: {}", id);
        authController.authenticate();

        PlaylistTrack[] tracks = playlistDetailsService.getPlaylistTracks(id);
        List<Map<String, Object>> trackList = getTrackListData(tracks);

        String playlistName = playlistDetailsService.getPlaylistName(id);
        User owner = playlistDetailsService.getPlaylistOwner(id);

        // 最大オーディオフィーチャーを計算
        Map<String, Float> maxAudioFeatures = calculateMaxAudioFeatures(trackList);
        logger.info("getPlaylistDetails: 最大オーディオフィーチャー: {}", maxAudioFeatures);

        // 最小オーディオフィーチャーを計算
        Map<String, Float> minAudioFeatures = calculateMinAudioFeatures(trackList);
        logger.info("getPlaylistDetails: 最小オーディオフィーチャー: {}", minAudioFeatures);

        // 中央オーディオフィーチャーを計算
        Map<String, Float> medianAudioFeatures = calculateMedianAudioFeatures(trackList);
        logger.info("getPlaylistDetails: 中央オーディオフィーチャー: {}", medianAudioFeatures);

        // key, mode, time_signatureの最頻値を計算
        Map<String, Object> modeValues = calculateModeValues(trackList);
        logger.info("getPlaylistDetails: 最頻値: {}", modeValues);

        Map<String, Object> response = new HashMap<>();
        response.put("tracks", Map.of("items", trackList));
        response.put("playlistName", playlistName);
        response.put("ownerId", owner.getId());
        response.put("ownerName", owner.getDisplayName());
        response.put("maxAudioFeatures", maxAudioFeatures); // 最大オーディオフィーチャーを追加
        response.put("minAudioFeatures", minAudioFeatures); // 最小オーディオフィーチャーを追加
        response.put("medianAudioFeatures", medianAudioFeatures); // 中央オーディオフィーチャーを追加
        response.put("modeValues", modeValues);
        return response;
    }


    private List<Map<String, Object>> getTrackListData(PlaylistTrack[] tracks) throws Exception {
        logger.info("getTrackListData: トラック数: {}", tracks.length);
        List<Map<String, Object>> trackList = new ArrayList<>();
        for (PlaylistTrack track : tracks) {
            Map<String, Object> trackData = new HashMap<>();
            Track fullTrack = (Track) track.getTrack();
            trackData.put("track", fullTrack);

            String trackId = fullTrack.getId();
            AudioFeatures audioFeatures = trackService.getAudioFeaturesForTrack(trackId);
            trackData.put("audioFeatures", audioFeatures);
            trackList.add(trackData);
        }
        logger.info("getTrackListData: トラックデータリスト作成完了");
        return trackList;
    }

    private Map<String, Float> calculateMaxAudioFeatures(List<Map<String, Object>> trackList) {
        logger.info("calculateMaxAudioFeatures: 計算開始");
        Map<String, Float> maxAudioFeatures = new HashMap<>();
        maxAudioFeatures.put("danceability", 0.0f);
        maxAudioFeatures.put("energy", 0.0f);
        maxAudioFeatures.put("valence", 0.0f);
        maxAudioFeatures.put("tempo", 0.0f);
        maxAudioFeatures.put("acousticness", 0.0f);
        maxAudioFeatures.put("instrumentalness", 0.0f);
        maxAudioFeatures.put("liveness", 0.0f);
        maxAudioFeatures.put("speechiness", 0.0f);

        for (Map<String, Object> trackData : trackList) {
            AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
            if (audioFeatures != null) {
                maxAudioFeatures.put("danceability", Math.max(maxAudioFeatures.get("danceability"), audioFeatures.getDanceability()));
                maxAudioFeatures.put("energy", Math.max(maxAudioFeatures.get("energy"), audioFeatures.getEnergy()));
                maxAudioFeatures.put("valence", Math.max(maxAudioFeatures.get("valence"), audioFeatures.getValence()));
                maxAudioFeatures.put("tempo", Math.max(maxAudioFeatures.get("tempo"), audioFeatures.getTempo()));
                maxAudioFeatures.put("acousticness", Math.max(maxAudioFeatures.get("acousticness"), audioFeatures.getAcousticness()));
                maxAudioFeatures.put("instrumentalness", Math.max(maxAudioFeatures.get("instrumentalness"), audioFeatures.getInstrumentalness()));
                maxAudioFeatures.put("liveness", Math.max(maxAudioFeatures.get("liveness"), audioFeatures.getLiveness()));
                maxAudioFeatures.put("speechiness", Math.max(maxAudioFeatures.get("speechiness"), audioFeatures.getSpeechiness()));
            }
        }
        logger.info("calculateMaxAudioFeatures: 最大オーディオフィーチャー計算完了: {}", maxAudioFeatures);
        return maxAudioFeatures;
    }

    private Map<String, Float> calculateMinAudioFeatures(List<Map<String, Object>> trackList) {
        logger.info("calculateMinAudioFeatures: 計算開始");
        Map<String, Float> minAudioFeatures = new HashMap<>();
        minAudioFeatures.put("danceability", Float.MAX_VALUE);
        minAudioFeatures.put("energy", Float.MAX_VALUE);
        minAudioFeatures.put("valence", Float.MAX_VALUE);
        minAudioFeatures.put("tempo", Float.MAX_VALUE);
        minAudioFeatures.put("acousticness", Float.MAX_VALUE);
        minAudioFeatures.put("instrumentalness", Float.MAX_VALUE);
        minAudioFeatures.put("liveness", Float.MAX_VALUE);
        minAudioFeatures.put("speechiness", Float.MAX_VALUE);

        for (Map<String, Object> trackData : trackList) {
            AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
            if (audioFeatures != null) {
                minAudioFeatures.put("danceability", Math.min(minAudioFeatures.get("danceability"), audioFeatures.getDanceability()));
                minAudioFeatures.put("energy", Math.min(minAudioFeatures.get("energy"), audioFeatures.getEnergy()));
                minAudioFeatures.put("valence", Math.min(minAudioFeatures.get("valence"), audioFeatures.getValence()));
                minAudioFeatures.put("tempo", Math.min(minAudioFeatures.get("tempo"), audioFeatures.getTempo()));
                minAudioFeatures.put("acousticness", Math.min(minAudioFeatures.get("acousticness"), audioFeatures.getAcousticness()));
                minAudioFeatures.put("instrumentalness", Math.min(minAudioFeatures.get("instrumentalness"), audioFeatures.getInstrumentalness()));
                minAudioFeatures.put("liveness", Math.min(minAudioFeatures.get("liveness"), audioFeatures.getLiveness()));
                minAudioFeatures.put("speechiness", Math.min(minAudioFeatures.get("speechiness"), audioFeatures.getSpeechiness()));
            }
        }
        logger.info("calculateMinAudioFeatures: 最小オーディオフィーチャー計算完了: {}", minAudioFeatures);
        return minAudioFeatures;
    }

    private Map<String, Float> calculateMedianAudioFeatures(List<Map<String, Object>> trackList) {
        logger.info("calculateMedianAudioFeatures: 計算開始");
        Map<String, List<Float>> featureValues = new HashMap<>();
        featureValues.put("danceability", new ArrayList<>());
        featureValues.put("energy", new ArrayList<>());
        featureValues.put("valence", new ArrayList<>());
        featureValues.put("tempo", new ArrayList<>());
        featureValues.put("acousticness", new ArrayList<>());
        featureValues.put("instrumentalness", new ArrayList<>());
        featureValues.put("liveness", new ArrayList<>());
        featureValues.put("speechiness", new ArrayList<>());

        for (Map<String, Object> trackData : trackList) {
            AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
            if (audioFeatures != null) {
                featureValues.get("danceability").add(audioFeatures.getDanceability());
                featureValues.get("energy").add(audioFeatures.getEnergy());
                featureValues.get("valence").add(audioFeatures.getValence());
                featureValues.get("tempo").add(audioFeatures.getTempo());
                featureValues.get("acousticness").add(audioFeatures.getAcousticness());
                featureValues.get("instrumentalness").add(audioFeatures.getInstrumentalness());
                featureValues.get("liveness").add(audioFeatures.getLiveness());
                featureValues.get("speechiness").add(audioFeatures.getSpeechiness());
            }
        }

        Map<String, Float> medianAudioFeatures = new HashMap<>();
        for (Map.Entry<String, List<Float>> entry : featureValues.entrySet()) {
            List<Float> values = entry.getValue();
            Collections.sort(values);
            int size = values.size();
            float median;
            if (size % 2 == 0) {
                median = (values.get(size / 2 - 1) + values.get(size / 2)) / 2;
            } else {
                median = values.get(size / 2);
            }
            medianAudioFeatures.put(entry.getKey(), median);
        }
        logger.info("calculateMedianAudioFeatures: 中央オーディオフィーチャー計算完了: {}", medianAudioFeatures);
        return medianAudioFeatures;
    }

    private Map<String, Object> calculateModeValues(List<Map<String, Object>> trackList) {
        logger.info("calculateModeValues: 計算開始");
        Map<String, List<Integer>> numericFeatureValues = new HashMap<>();
        numericFeatureValues.put("key", new ArrayList<>());
        numericFeatureValues.put("time_signature", new ArrayList<>());

        Map<String, List<String>> stringFeatureValues = new HashMap<>();
        stringFeatureValues.put("mode", new ArrayList<>());

        for (Map<String, Object> trackData : trackList) {
            AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
            if (audioFeatures != null) {
                numericFeatureValues.get("key").add(audioFeatures.getKey());
                numericFeatureValues.get("time_signature").add(audioFeatures.getTimeSignature());
                stringFeatureValues.get("mode").add(audioFeatures.getMode().toString());
            }
        }

        Map<String, Object> modeValues = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : numericFeatureValues.entrySet()) {
            modeValues.put(entry.getKey(), calculateNumericMode(entry.getValue()));
        }
        for (Map.Entry<String, List<String>> entry : stringFeatureValues.entrySet()) {
            modeValues.put(entry.getKey(), calculateStringMode(entry.getValue()));
        }
        logger.info("calculateModeValues: 最頻値計算完了: {}", modeValues);
        return modeValues;
    }

    private int calculateNumericMode(List<Integer> values) {
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        for (int value : values) {
            frequencyMap.put(value, frequencyMap.getOrDefault(value, 0) + 1);
        }
        return Collections.max(frequencyMap.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    private String calculateStringMode(List<String> values) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String value : values) {
            frequencyMap.put(value, frequencyMap.getOrDefault(value, 0) + 1);
        }
        return Collections.max(frequencyMap.entrySet(), Map.Entry.comparingByValue()).getKey();
    }
}
