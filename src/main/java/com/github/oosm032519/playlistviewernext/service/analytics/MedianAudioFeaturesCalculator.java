package com.github.oosm032519.playlistviewernext.service.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.*;

@Service
public class MedianAudioFeaturesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(MedianAudioFeaturesCalculator.class);

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
            if (!values.isEmpty()) {
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
        }
        logger.info("calculateMedianAudioFeatures: 中央オーディオフィーチャー計算完了: {}", medianAudioFeatures);
        return medianAudioFeatures;
    }
}
