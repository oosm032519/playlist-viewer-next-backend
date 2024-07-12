package com.github.oosm032519.playlistviewernext.service.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.*;

@Service
public class MedianAudioFeaturesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(MedianAudioFeaturesCalculator.class);

    private static final List<String> FEATURE_KEYS = Arrays.asList(
            "danceability", "energy", "valence", "tempo",
            "acousticness", "instrumentalness", "liveness", "speechiness"
    );

    /**
     * トラックリストのオーディオフィーチャーの中央値を計算するメソッド
     *
     * @param trackList 各トラックのオーディオフィーチャーを含むリスト
     * @return 各オーディオフィーチャーの中央値を含むマップ
     */
    public Map<String, Float> calculateMedianAudioFeatures(List<Map<String, Object>> trackList) {
        logger.info("calculateMedianAudioFeatures: 計算開始");

        Map<String, List<Float>> featureValues = initializeFeatureValues();

        for (Map<String, Object> trackData : trackList) {
            AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
            if (audioFeatures != null) {
                addFeatureValues(featureValues, audioFeatures);
            }
        }

        Map<String, Float> medianAudioFeatures = calculateMedians(featureValues);

        logger.info("calculateMedianAudioFeatures: 中央オーディオフィーチャー計算完了: {}", medianAudioFeatures);
        return medianAudioFeatures;
    }

    private Map<String, List<Float>> initializeFeatureValues() {
        Map<String, List<Float>> featureValues = new HashMap<>();
        for (String key : FEATURE_KEYS) {
            featureValues.put(key, new ArrayList<>());
        }
        return featureValues;
    }

    private void addFeatureValues(Map<String, List<Float>> featureValues, AudioFeatures audioFeatures) {
        featureValues.get("danceability").add(audioFeatures.getDanceability());
        featureValues.get("energy").add(audioFeatures.getEnergy());
        featureValues.get("valence").add(audioFeatures.getValence());
        featureValues.get("tempo").add(audioFeatures.getTempo());
        featureValues.get("acousticness").add(audioFeatures.getAcousticness());
        featureValues.get("instrumentalness").add(audioFeatures.getInstrumentalness());
        featureValues.get("liveness").add(audioFeatures.getLiveness());
        featureValues.get("speechiness").add(audioFeatures.getSpeechiness());
    }

    private Map<String, Float> calculateMedians(Map<String, List<Float>> featureValues) {
        Map<String, Float> medianAudioFeatures = new HashMap<>();
        for (Map.Entry<String, List<Float>> entry : featureValues.entrySet()) {
            List<Float> values = entry.getValue();
            if (!values.isEmpty()) {
                Collections.sort(values);
                int size = values.size();
                float median = (size % 2 == 0) ?
                        (values.get(size / 2 - 1) + values.get(size / 2)) / 2 :
                        values.get(size / 2);
                medianAudioFeatures.put(entry.getKey(), median);
            }
        }
        return medianAudioFeatures;
    }
}
