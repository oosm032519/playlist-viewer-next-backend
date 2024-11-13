package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.*;
import java.util.function.ToDoubleFunction;

public class AudioFeaturesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(AudioFeaturesCalculator.class);

    // 平均値計算
    public static Map<String, Float> calculateAverageAudioFeatures(List<Map<String, Object>> trackList) {
        if (trackList == null || trackList.isEmpty()) {
            String message = "トラックリストが空です。";
            logger.warn(message);
            throw new InvalidRequestException(HttpStatus.BAD_REQUEST, message);
        }

        Map<AudioFeatureType, Double> sums = new EnumMap<>(AudioFeatureType.class);
        for (AudioFeatureType type : AudioFeatureType.values()) {
            sums.put(type, 0.0);
        }

        for (Map<String, Object> trackData : trackList) {
            AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
            if (audioFeatures == null) {
                String message = "AudioFeaturesがnullです。";
                logger.warn(message + " trackData: {}", trackData);
                throw new InvalidRequestException(HttpStatus.BAD_REQUEST, message);
            }
            for (AudioFeatureType type : AudioFeatureType.values()) {
                sums.put(type, sums.get(type) + type.extractor.applyAsDouble(audioFeatures));
            }
        }

        Map<String, Float> averages = new HashMap<>();
        for (AudioFeatureType type : AudioFeatureType.values()) {
            averages.put(type.name().toLowerCase(), (float) (sums.get(type) / trackList.size()));
        }

        return averages;
    }

    // 最大値計算
    public static Map<String, Float> calculateMaxAudioFeatures(List<Map<String, Object>> trackList) {
        return calculateExtremeAudioFeatures(trackList, true);
    }

    // 最大値/最小値計算の共通処理
    private static Map<String, Float> calculateExtremeAudioFeatures(List<Map<String, Object>> trackList, boolean isMax) {
        if (trackList == null || trackList.isEmpty()) {
            String message = "トラックリストが空です。";
            logger.warn(message);
            throw new InvalidRequestException(HttpStatus.BAD_REQUEST, message);
        }

        Map<AudioFeatureType, List<Float>> values = new EnumMap<>(AudioFeatureType.class);
        for (AudioFeatureType type : AudioFeatureType.values()) {
            values.put(type, new ArrayList<>());
        }

        for (Map<String, Object> trackData : trackList) {
            AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
            if (audioFeatures != null) {
                for (AudioFeatureType type : AudioFeatureType.values()) {
                    values.get(type).add((float) type.extractor.applyAsDouble(audioFeatures));
                }
            }
        }

        Map<String, Float> result = new HashMap<>();
        for (Map.Entry<AudioFeatureType, List<Float>> entry : values.entrySet()) {
            if (entry.getValue().isEmpty()) continue;

            float extremeValue;
            if (isMax) {
                extremeValue = Collections.max(entry.getValue());
            } else {
                extremeValue = Collections.min(entry.getValue());
            }

            result.put(entry.getKey().name().toLowerCase(), extremeValue);
        }
        return result;
    }

    // 最小値計算
    public static Map<String, Float> calculateMinAudioFeatures(List<Map<String, Object>> trackList) {
        return calculateExtremeAudioFeatures(trackList, false);
    }


    private enum AudioFeatureType {
        DANCEABILITY(AudioFeatures::getDanceability),
        ENERGY(AudioFeatures::getEnergy),
        VALENCE(AudioFeatures::getValence),
        TEMPO(AudioFeatures::getTempo),
        ACOUSTICNESS(AudioFeatures::getAcousticness),
        INSTRUMENTALNESS(AudioFeatures::getInstrumentalness),
        LIVENESS(AudioFeatures::getLiveness),
        SPEECHINESS(AudioFeatures::getSpeechiness);

        private final ToDoubleFunction<AudioFeatures> extractor;

        AudioFeatureType(ToDoubleFunction<AudioFeatures> extractor) {
            this.extractor = extractor;
        }
    }
}
