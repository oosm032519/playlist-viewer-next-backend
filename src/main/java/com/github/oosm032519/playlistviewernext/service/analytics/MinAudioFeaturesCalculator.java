package com.github.oosm032519.playlistviewernext.service.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.*;

@Service
public class MinAudioFeaturesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(MinAudioFeaturesCalculator.class);

    private enum AudioFeatureType {
        DANCEABILITY, ENERGY, VALENCE, TEMPO, ACOUSTICNESS, INSTRUMENTALNESS, LIVENESS, SPEECHINESS
    }

    public Map<String, Float> calculateMinAudioFeatures(List<Map<String, Object>> trackList) {
        logger.info("calculateMinAudioFeatures: 計算開始");

        Map<AudioFeatureType, List<Float>> audioFeatureValues = new EnumMap<>(AudioFeatureType.class);
        for (AudioFeatureType featureType : AudioFeatureType.values()) {
            audioFeatureValues.put(featureType, new ArrayList<>());
        }

        for (Map<String, Object> trackData : trackList) {
            AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
            if (audioFeatures != null) {
                collectAudioFeatureValues(audioFeatureValues, audioFeatures);
            }
        }

        Map<String, Float> result = calculateLowerBounds(audioFeatureValues);

        logger.info("calculateMinAudioFeatures: 下限オーディオフィーチャー計算完了: {}", result);
        return result;
    }

    private void collectAudioFeatureValues(Map<AudioFeatureType, List<Float>> audioFeatureValues, AudioFeatures audioFeatures) {
        audioFeatureValues.get(AudioFeatureType.DANCEABILITY).add(audioFeatures.getDanceability());
        audioFeatureValues.get(AudioFeatureType.ENERGY).add(audioFeatures.getEnergy());
        audioFeatureValues.get(AudioFeatureType.VALENCE).add(audioFeatures.getValence());
        audioFeatureValues.get(AudioFeatureType.TEMPO).add(audioFeatures.getTempo());
        audioFeatureValues.get(AudioFeatureType.ACOUSTICNESS).add(audioFeatures.getAcousticness());
        audioFeatureValues.get(AudioFeatureType.INSTRUMENTALNESS).add(audioFeatures.getInstrumentalness());
        audioFeatureValues.get(AudioFeatureType.LIVENESS).add(audioFeatures.getLiveness());
        audioFeatureValues.get(AudioFeatureType.SPEECHINESS).add(audioFeatures.getSpeechiness());
    }

    private Map<String, Float> calculateLowerBounds(Map<AudioFeatureType, List<Float>> audioFeatureValues) {
        Map<String, Float> result = new HashMap<>();
        for (Map.Entry<AudioFeatureType, List<Float>> entry : audioFeatureValues.entrySet()) {
            List<Float> values = entry.getValue();
            if (values.isEmpty()) {
                continue;  // 空のリストの場合はスキップ
            }
            Collections.sort(values);
            float q1 = calculateQuartile(values, 0.25);
            float q3 = calculateQuartile(values, 0.75);
            float iqr = q3 - q1;
            float lowerBound = Math.max(q1 - 1.5f * iqr, 0f);
            if (entry.getKey() != AudioFeatureType.TEMPO) {
                lowerBound = Math.min(lowerBound, 1f);
            }
            result.put(entry.getKey().name().toLowerCase(), lowerBound);
        }
        return result;
    }

    private float calculateQuartile(List<Float> sortedValues, double quartile) {
        if (sortedValues.isEmpty()) {
            return 0f;  // 空のリストの場合は0を返す
        }
        int index = (int) Math.ceil(sortedValues.size() * quartile) - 1;
        return sortedValues.get(Math.max(0, Math.min(sortedValues.size() - 1, index)));
    }
}
