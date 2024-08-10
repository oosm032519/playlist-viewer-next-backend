package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.*;

@Service
public class MaxAudioFeaturesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(MaxAudioFeaturesCalculator.class);

    public Map<String, Float> calculateMaxAudioFeatures(List<Map<String, Object>> trackList) {
        logger.info("calculateMaxAudioFeatures: 計算開始");

        try {
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

            Map<String, Float> result = calculateUpperBounds(audioFeatureValues);

            logger.info("calculateMaxAudioFeatures: 上限オーディオフィーチャー計算完了: {}", result);
            return result;
        } catch (Exception e) {
            // 最大オーディオフィーチャーの計算中にエラーが発生した場合は PlaylistViewerNextException をスロー
            logger.error("最大オーディオフィーチャーの計算中にエラーが発生しました。", e);
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "MAX_AUDIO_FEATURES_CALCULATION_ERROR",
                    "最大オーディオフィーチャーの計算中にエラーが発生しました。",
                    e
            );
        }
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

    private Map<String, Float> calculateUpperBounds(Map<AudioFeatureType, List<Float>> audioFeatureValues) {
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
            float upperBound = q3 + 1.5f * iqr;
            if (entry.getKey() != AudioFeatureType.TEMPO) {
                upperBound = Math.min(upperBound, 1f);
            }
            result.put(entry.getKey().name().toLowerCase(), upperBound);
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

    private enum AudioFeatureType {
        DANCEABILITY, ENERGY, VALENCE, TEMPO, ACOUSTICNESS, INSTRUMENTALNESS, LIVENESS, SPEECHINESS
    }
}
