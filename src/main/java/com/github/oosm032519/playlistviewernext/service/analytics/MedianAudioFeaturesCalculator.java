package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.*;

/**
 * プレイリストのオーディオフィーチャーの中央値を計算するサービスクラス。
 * このクラスは、トラックリストから各オーディオフィーチャーの中央値を算出します。
 */
@Service
public class MedianAudioFeaturesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(MedianAudioFeaturesCalculator.class);

    /**
     * 計算対象のオーディオフィーチャーのキーリスト。
     * これらのフィーチャーの中央値が計算されます。
     */
    private static final List<String> FEATURE_KEYS = Arrays.asList(
            "danceability", "energy", "valence", "tempo",
            "acousticness", "instrumentalness", "liveness", "speechiness"
    );

    /**
     * トラックリストのオーディオフィーチャーの中央値を計算します。
     *
     * @param trackList 各トラックのオーディオフィーチャーを含むリスト
     * @return 各オーディオフィーチャーの中央値を含むマップ
     * @throws PlaylistViewerNextException 中央オーディオフィーチャーの計算中にエラーが発生した場合
     */
    public Map<String, Float> calculateMedianAudioFeatures(List<Map<String, Object>> trackList) {
        logger.info("calculateMedianAudioFeatures: 計算開始");

        try {
            Map<String, List<Float>> featureValues = initializeFeatureValues();

            // 各トラックのオーディオフィーチャーを収集
            for (Map<String, Object> trackData : trackList) {
                AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
                if (audioFeatures != null) {
                    addFeatureValues(featureValues, audioFeatures);
                }
            }

            // 中央値を計算
            Map<String, Float> medianAudioFeatures = calculateMedians(featureValues);

            logger.info("calculateMedianAudioFeatures: 中央オーディオフィーチャー計算完了: {}", medianAudioFeatures);
            return medianAudioFeatures;
        } catch (Exception e) {
            logger.error("中央オーディオフィーチャーの計算中にエラーが発生しました。", e);
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "MEDIAN_AUDIO_FEATURES_CALCULATION_ERROR",
                    "中央オーディオフィーチャーの計算中にエラーが発生しました。",
                    e
            );
        }
    }

    /**
     * フィーチャー値を格納するマップを初期化します。
     *
     * @return 初期化されたフィーチャー値マップ
     */
    private Map<String, List<Float>> initializeFeatureValues() {
        Map<String, List<Float>> featureValues = new HashMap<>();
        for (String key : FEATURE_KEYS) {
            featureValues.put(key, new ArrayList<>());
        }
        return featureValues;
    }

    /**
     * 指定されたオーディオフィーチャーの値をフィーチャー値マップに追加します。
     *
     * @param featureValues フィーチャー値を格納するマップ
     * @param audioFeatures 追加するオーディオフィーチャー
     */
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

    /**
     * 各フィーチャーの中央値を計算します。
     *
     * @param featureValues 各フィーチャーの値リストを含むマップ
     * @return 各フィーチャーの中央値を含むマップ
     */
    private Map<String, Float> calculateMedians(Map<String, List<Float>> featureValues) {
        Map<String, Float> medianAudioFeatures = new HashMap<>();
        for (Map.Entry<String, List<Float>> entry : featureValues.entrySet()) {
            List<Float> values = entry.getValue();
            if (!values.isEmpty()) {
                Collections.sort(values);
                int size = values.size();
                // 中央値の計算：要素数が偶数の場合は中央の2つの平均、奇数の場合は中央の値
                float median = (size % 2 == 0) ?
                        (values.get(size / 2 - 1) + values.get(size / 2)) / 2 :
                        values.get(size / 2);
                medianAudioFeatures.put(entry.getKey(), median);
            }
        }
        return medianAudioFeatures;
    }
}
