package com.github.oosm032519.playlistviewernext.service.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ModeValuesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(ModeValuesCalculator.class);

    public Map<String, Object> calculateModeValues(List<Map<String, Object>> trackList) {
        logger.info("calculateModeValues: 計算開始");

        Map<String, List<Integer>> numericFeatureValues = initializeNumericFeatureValues();
        Map<String, List<String>> stringFeatureValues = initializeStringFeatureValues();

        trackList.forEach(trackData -> {
            AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
            if (audioFeatures != null) {
                collectNumericFeatures(numericFeatureValues, audioFeatures);
                collectStringFeatures(stringFeatureValues, audioFeatures);
            }
        });

        Map<String, Object> modeValues = new HashMap<>();
        calculateModeValues(numericFeatureValues, modeValues, this::calculateNumericMode);
        calculateModeValues(stringFeatureValues, modeValues, this::calculateStringMode);

        logger.info("calculateModeValues: 最頻値計算完了: {}", modeValues);
        return modeValues;
    }

    private Map<String, List<Integer>> initializeNumericFeatureValues() {
        Map<String, List<Integer>> numericFeatureValues = new HashMap<>();
        numericFeatureValues.put("key", new ArrayList<>());
        numericFeatureValues.put("time_signature", new ArrayList<>());
        return numericFeatureValues;
    }

    private Map<String, List<String>> initializeStringFeatureValues() {
        Map<String, List<String>> stringFeatureValues = new HashMap<>();
        stringFeatureValues.put("mode", new ArrayList<>());
        return stringFeatureValues;
    }

    private void collectNumericFeatures(Map<String, List<Integer>> numericFeatureValues, AudioFeatures audioFeatures) {
        numericFeatureValues.get("key").add(audioFeatures.getKey());
        numericFeatureValues.get("time_signature").add(audioFeatures.getTimeSignature());
    }

    private void collectStringFeatures(Map<String, List<String>> stringFeatureValues, AudioFeatures audioFeatures) {
        stringFeatureValues.get("mode").add(audioFeatures.getMode().toString());
    }

    private <T> void calculateModeValues(Map<String, List<T>> featureValues, Map<String, Object> modeValues, ModeCalculator<T> calculator) {
        featureValues.forEach((key, values) -> {
            if (!values.isEmpty()) {
                modeValues.put(key, calculator.calculateMode(values));
            }
        });
    }

    private Integer calculateNumericMode(List<Integer> values) {
        return values.stream()
                .collect(Collectors.groupingBy(value -> value, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private String calculateStringMode(List<String> values) {
        return values.stream()
                .collect(Collectors.groupingBy(value -> value, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @FunctionalInterface
    private interface ModeCalculator<T> {
        T calculateMode(List<T> values);
    }
}
