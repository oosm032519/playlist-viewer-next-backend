package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.*;

/**
 * プレイリスト内の楽曲のオーディオ特徴量の最大値を計算するサービスクラス。
 * 外れ値を考慮した上限値を計算します。
 */
@Service
public class MaxAudioFeaturesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(MaxAudioFeaturesCalculator.class);

    /**
     * 楽曲リストからオーディオ特徴量の最大値を計算します。
     *
     * @param trackList オーディオ特徴量を含む楽曲データのリスト
     * @return 各オーディオ特徴量の最大値を含むマップ
     * @throws PlaylistViewerNextException 計算中にエラーが発生した場合
     */
    public Map<String, Float> calculateMaxAudioFeatures(List<Map<String, Object>> trackList) {
        logger.info("calculateMaxAudioFeatures: 計算開始");

        try {
            Map<AudioFeatureType, List<Float>> audioFeatureValues = initializeAudioFeatureValues();
            collectAudioFeatures(trackList, audioFeatureValues);
            Map<String, Float> result = calculateUpperBounds(audioFeatureValues);

            logger.info("calculateMaxAudioFeatures: 上限オーディオフィーチャー計算完了: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("最大オーディオフィーチャーの計算中にエラーが発生しました。", e);
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "MAX_AUDIO_FEATURES_CALCULATION_ERROR",
                    "最大オーディオフィーチャーの計算中にエラーが発生しました。",
                    e
            );
        }
    }

    /**
     * オーディオ特徴量の値を格納するマップを初期化します。
     *
     * @return 初期化されたオーディオ特徴量マップ
     */
    private Map<AudioFeatureType, List<Float>> initializeAudioFeatureValues() {
        Map<AudioFeatureType, List<Float>> audioFeatureValues = new EnumMap<>(AudioFeatureType.class);
        for (AudioFeatureType featureType : AudioFeatureType.values()) {
            audioFeatureValues.put(featureType, new ArrayList<>());
        }
        return audioFeatureValues;
    }

    /**
     * 楽曲リストからオーディオ特徴量を収集します。
     *
     * @param trackList          楽曲データのリスト
     * @param audioFeatureValues 収集したオーディオ特徴量を格納するマップ
     */
    private void collectAudioFeatures(List<Map<String, Object>> trackList, Map<AudioFeatureType, List<Float>> audioFeatureValues) {
        for (Map<String, Object> trackData : trackList) {
            AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
            if (audioFeatures != null) {
                collectAudioFeatureValues(audioFeatureValues, audioFeatures);
            }
        }
    }

    /**
     * 収集したオーディオ特徴量から上限値を計算します。
     *
     * @param audioFeatureValues 収集したオーディオ特徴量
     * @return 各オーディオ特徴量の上限値を含むマップ
     */
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
                upperBound = Math.min(upperBound, 1f);  // TEMPO以外は0-1の範囲に制限
            }
            result.put(entry.getKey().name().toLowerCase(), upperBound);
        }
        return result;
    }

    /**
     * 個々のAudioFeaturesオブジェクトからオーディオ特徴量を収集します。
     *
     * @param audioFeatureValues 収集したオーディオ特徴量を格納するマップ
     * @param audioFeatures      収集対象のAudioFeaturesオブジェクト
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
     * ソートされた値のリストから指定された四分位数を計算します。
     *
     * @param sortedValues ソートされた値のリスト
     * @param quartile     計算する四分位数（0.25 = 第1四分位数, 0.75 = 第3四分位数）
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
     * オーディオ特徴量の種類を表す列挙型。
     */
    private enum AudioFeatureType {
        DANCEABILITY, ENERGY, VALENCE, TEMPO, ACOUSTICNESS, INSTRUMENTALNESS, LIVENESS, SPEECHINESS
    }
}
