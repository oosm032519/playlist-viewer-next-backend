// ModeValuesCalculator.java

package com.github.oosm032519.playlistviewernext.service.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.*;

@Service
public class ModeValuesCalculator {

    // ロガーの初期化
    private static final Logger logger = LoggerFactory.getLogger(ModeValuesCalculator.class);

    /**
     * トラックリストから各オーディオフィーチャーの最頻値を計算するメソッド
     *
     * @param trackList トラックデータのリスト
     * @return 各フィーチャーの最頻値を含むマップ
     */
    public Map<String, Object> calculateModeValues(List<Map<String, Object>> trackList) {
        logger.info("calculateModeValues: 計算開始");

        // 数値フィーチャーの値を格納するマップの初期化
        Map<String, List<Integer>> numericFeatureValues = new HashMap<>();
        numericFeatureValues.put("key", new ArrayList<>());
        numericFeatureValues.put("time_signature", new ArrayList<>());

        // 文字列フィーチャーの値を格納するマップの初期化
        Map<String, List<String>> stringFeatureValues = new HashMap<>();
        stringFeatureValues.put("mode", new ArrayList<>());

        // トラックリストをループし、各フィーチャーの値を収集
        for (Map<String, Object> trackData : trackList) {
            AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
            if (audioFeatures != null) {
                numericFeatureValues.get("key").add(audioFeatures.getKey());
                numericFeatureValues.get("time_signature").add(audioFeatures.getTimeSignature());
                stringFeatureValues.get("mode").add(audioFeatures.getMode().toString());
            }
        }

        // 最頻値を格納するマップの初期化
        Map<String, Object> modeValues = new HashMap<>();

        // 数値フィーチャーの最頻値を計算
        for (Map.Entry<String, List<Integer>> entry : numericFeatureValues.entrySet()) {
            modeValues.put(entry.getKey(), calculateNumericMode(entry.getValue()));
        }

        // 文字列フィーチャーの最頻値を計算
        for (Map.Entry<String, List<String>> entry : stringFeatureValues.entrySet()) {
            modeValues.put(entry.getKey(), calculateStringMode(entry.getValue()));
        }

        logger.info("calculateModeValues: 最頻値計算完了: {}", modeValues);
        return modeValues;
    }

    /**
     * 数値リストから最頻値を計算するヘルパーメソッド
     *
     * @param values 数値のリスト
     * @return 最頻値
     */
    private int calculateNumericMode(List<Integer> values) {
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        for (int value : values) {
            frequencyMap.put(value, frequencyMap.getOrDefault(value, 0) + 1);
        }
        return Collections.max(frequencyMap.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    /**
     * 文字列リストから最頻値を計算するヘルパーメソッド
     *
     * @param values 文字列のリスト
     * @return 最頻値
     */
    private String calculateStringMode(List<String> values) {
        Map<String, Integer> frequencyMap = new HashMap<>();
        for (String value : values) {
            frequencyMap.put(value, frequencyMap.getOrDefault(value, 0) + 1);
        }
        return Collections.max(frequencyMap.entrySet(), Map.Entry.comparingByValue()).getKey();
    }
}
