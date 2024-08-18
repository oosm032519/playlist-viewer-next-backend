package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * トラックリストの音楽特性の最頻値を計算するサービスクラス。
 */
@Service
public class ModeValuesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(ModeValuesCalculator.class);

    /**
     * トラックリストから音楽特性の最頻値を計算します。
     *
     * @param trackList 音楽特性を含むトラックのリスト
     * @return 計算された最頻値のマップ
     * @throws PlaylistViewerNextException 計算中にエラーが発生した場合
     */
    public Map<String, Object> calculateModeValues(List<Map<String, Object>> trackList) {
        logger.info("calculateModeValues: 計算開始");

        try {
            Map<String, List<Integer>> numericFeatureValues = initializeNumericFeatureValues();
            Map<String, List<String>> stringFeatureValues = initializeStringFeatureValues();

            // 各トラックの音楽特性を収集
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
        } catch (Exception e) {
            logger.error("最頻値の計算中にエラーが発生しました。", e);
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "MODE_VALUES_CALCULATION_ERROR",
                    "最頻値の計算中にエラーが発生しました。",
                    e
            );
        }
    }

    /**
     * 数値特性の初期マップを作成します。
     *
     * @return 初期化された数値特性のマップ
     */
    private Map<String, List<Integer>> initializeNumericFeatureValues() {
        Map<String, List<Integer>> numericFeatureValues = new HashMap<>();
        numericFeatureValues.put("key", new ArrayList<>());
        numericFeatureValues.put("time_signature", new ArrayList<>());
        return numericFeatureValues;
    }

    /**
     * 文字列特性の初期マップを作成します。
     *
     * @return 初期化された文字列特性のマップ
     */
    private Map<String, List<String>> initializeStringFeatureValues() {
        Map<String, List<String>> stringFeatureValues = new HashMap<>();
        stringFeatureValues.put("mode", new ArrayList<>());
        return stringFeatureValues;
    }

    /**
     * 数値特性を収集します。
     *
     * @param numericFeatureValues 数値特性を格納するマップ
     * @param audioFeatures        音楽特性オブジェクト
     */
    private void collectNumericFeatures(Map<String, List<Integer>> numericFeatureValues, AudioFeatures audioFeatures) {
        numericFeatureValues.get("key").add(audioFeatures.getKey());
        numericFeatureValues.get("time_signature").add(audioFeatures.getTimeSignature());
    }

    /**
     * 文字列特性を収集します。
     *
     * @param stringFeatureValues 文字列特性を格納するマップ
     * @param audioFeatures       音楽特性オブジェクト
     */
    private void collectStringFeatures(Map<String, List<String>> stringFeatureValues, AudioFeatures audioFeatures) {
        stringFeatureValues.get("mode").add(audioFeatures.getMode().toString());
    }

    /**
     * 特性値の最頻値を計算します。
     *
     * @param featureValues 特性値のマップ
     * @param modeValues    結果を格納するマップ
     * @param calculator    最頻値計算のための関数インターフェース
     * @param <T>           特性値の型
     */
    private <T> void calculateModeValues(Map<String, List<T>> featureValues, Map<String, Object> modeValues, ModeCalculator<T> calculator) {
        featureValues.forEach((key, values) -> {
            if (!values.isEmpty()) {
                modeValues.put(key, calculator.calculateMode(values));
            }
        });
    }

    /**
     * 数値リストの最頻値を計算します。
     *
     * @param values 数値のリスト
     * @return 最頻値
     */
    private Integer calculateNumericMode(List<Integer> values) {
        return values.stream()
                .collect(Collectors.groupingBy(value -> value, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * 文字列リストの最頻値を計算します。
     *
     * @param values 文字列のリスト
     * @return 最頻値
     */
    private String calculateStringMode(List<String> values) {
        return values.stream()
                .collect(Collectors.groupingBy(value -> value, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    /**
     * 最頻値計算のための関数インターフェース。
     *
     * @param <T> 特性値の型
     */
    @FunctionalInterface
    private interface ModeCalculator<T> {
        T calculateMode(List<T> values);
    }
}
