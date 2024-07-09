package com.github.oosm032519.playlistviewernext.service.playlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.*;

@Service
public class AudioFeaturesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(AudioFeaturesCalculator.class);

    public Map<String, Float> calculateMaxAudioFeatures(List<Map<String, Object>> trackList) {
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

    public Map<String, Float> calculateMinAudioFeatures(List<Map<String, Object>> trackList) {
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

    public Map<String, Float> calculateMedianAudioFeatures(List<Map<String, Object>> trackList) {
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
}
