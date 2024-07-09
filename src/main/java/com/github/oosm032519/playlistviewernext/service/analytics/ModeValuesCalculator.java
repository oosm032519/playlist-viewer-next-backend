package com.github.oosm032519.playlistviewernext.service.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.*;

@Service
public class ModeValuesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(ModeValuesCalculator.class);

    public Map<String, Object> calculateModeValues(List<Map<String, Object>> trackList) {
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
