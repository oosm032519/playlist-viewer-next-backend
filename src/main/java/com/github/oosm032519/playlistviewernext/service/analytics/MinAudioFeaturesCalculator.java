package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.*;

/**
 * プレイリストの楽曲の最小オーディオフィーチャーを計算するサービスクラス
 * 与えられた楽曲リストから各オーディオフィーチャーの下限値を算出する
 */
@Service
public class MinAudioFeaturesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(MinAudioFeaturesCalculator.class);

    /**
     * 楽曲リストから最小オーディオフィーチャーを計算する
     *
     * @param trackList 楽曲データのリスト。各楽曲はMap形式で、"audioFeatures"キーにAudioFeaturesオブジェクトを含む
     * @return 各オーディオフィーチャーの下限値を含むMap。キーはフィーチャー名（小文字）、値は下限値
     * @throws PlaylistViewerNextException 計算中にエラーが発生した場合
     */
    public Map<String, Float> calculateMinAudioFeatures(List<Map<String, Object>> trackList) {
        logger.info("calculateMinAudioFeatures: 計算開始");

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

            Map<String, Float> result = calculateLowerBounds(audioFeatureValues);

            logger.info("calculateMinAudioFeatures: 下限オーディオフィーチャー計算完了: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("最小オーディオフィーチャーの計算中にエラーが発生しました。", e);
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "MIN_AUDIO_FEATURES_CALCULATION_ERROR",
                    "最小オーディオフィーチャーの計算中にエラーが発生しました。",
                    e
            );
        }
    }

    /**
     * AudioFeaturesオブジェクトから各オーディオフィーチャーの値を収集する
     *
     * @param audioFeatureValues 各フィーチャーの値を格納するMap
     * @param audioFeatures      楽曲のオーディオフィーチャー情報
     */
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

    /**
     * 各オーディオフィーチャーの下限値を計算する
     * 四分位数範囲（IQR）法を使用して外れ値を考慮した下限を算出する
     *
     * @param audioFeatureValues 各フィーチャーの値のリストを含むMap
     * @return 各フィーチャーの下限値を含むMap
     */
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

    /**
     * ソートされた値のリストから指定された四分位数を計算する
     *
     * @param sortedValues ソートされた値のリスト
     * @param quartile     計算する四分位数（0.25 for Q1, 0.75 for Q3）
     * @return 計算された四分位数
     */
    private float calculateQuartile(List<Float> sortedValues, double quartile) {
        if (sortedValues.isEmpty()) {
            return 0f;  // 空のリストの場合は0を返す
        }
        int index = (int) Math.ceil(sortedValues.size() * quartile) - 1;
        return sortedValues.get(Math.max(0, Math.min(sortedValues.size() - 1, index)));
    }

    /**
     * オーディオフィーチャーの種類を表す列挙型
     */
    private enum AudioFeatureType {
        DANCEABILITY, ENERGY, VALENCE, TEMPO, ACOUSTICNESS, INSTRUMENTALNESS, LIVENESS, SPEECHINESS
    }
}
