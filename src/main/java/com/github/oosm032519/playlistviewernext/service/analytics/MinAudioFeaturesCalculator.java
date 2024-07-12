package com.github.oosm032519.playlistviewernext.service.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * トラックリストから各オーディオフィーチャーの最小値を計算するサービスクラス
 */
@Service
public class MinAudioFeaturesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(MinAudioFeaturesCalculator.class);

    /**
     * オーディオフィーチャーの種類を列挙する列挙型
     */
    private enum AudioFeatureType {
        DANCEABILITY, ENERGY, VALENCE, TEMPO, ACOUSTICNESS, INSTRUMENTALNESS, LIVENESS, SPEECHINESS
    }

    /**
     * トラックリストから各オーディオフィーチャーの最小値を計算するメソッド
     *
     * @param trackList トラックのリスト。各トラックはオーディオフィーチャーを含むマップで表される
     * @return 各オーディオフィーチャーの最小値を含むマップ
     */
    public Map<String, Float> calculateMinAudioFeatures(List<Map<String, Object>> trackList) {
        logger.info("calculateMinAudioFeatures: 計算開始");

        // 最小オーディオフィーチャーを格納するマップの初期化
        Map<AudioFeatureType, Float> minAudioFeatures = new EnumMap<>(AudioFeatureType.class);
        for (AudioFeatureType featureType : AudioFeatureType.values()) {
            minAudioFeatures.put(featureType, Float.MAX_VALUE);
        }

        // トラックリストをループして各オーディオフィーチャーの最小値を計算
        for (Map<String, Object> trackData : trackList) {
            AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
            if (audioFeatures != null) {
                updateMinAudioFeature(minAudioFeatures, AudioFeatureType.DANCEABILITY, audioFeatures.getDanceability());
                updateMinAudioFeature(minAudioFeatures, AudioFeatureType.ENERGY, audioFeatures.getEnergy());
                updateMinAudioFeature(minAudioFeatures, AudioFeatureType.VALENCE, audioFeatures.getValence());
                updateMinAudioFeature(minAudioFeatures, AudioFeatureType.TEMPO, audioFeatures.getTempo());
                updateMinAudioFeature(minAudioFeatures, AudioFeatureType.ACOUSTICNESS, audioFeatures.getAcousticness());
                updateMinAudioFeature(minAudioFeatures, AudioFeatureType.INSTRUMENTALNESS, audioFeatures.getInstrumentalness());
                updateMinAudioFeature(minAudioFeatures, AudioFeatureType.LIVENESS, audioFeatures.getLiveness());
                updateMinAudioFeature(minAudioFeatures, AudioFeatureType.SPEECHINESS, audioFeatures.getSpeechiness());
            }
        }

        // 結果をマップに変換
        Map<String, Float> result = new HashMap<>();
        for (Map.Entry<AudioFeatureType, Float> entry : minAudioFeatures.entrySet()) {
            result.put(entry.getKey().name().toLowerCase(), entry.getValue());
        }

        logger.info("calculateMinAudioFeatures: 最小オーディオフィーチャー計算完了: {}", result);
        return result;
    }

    /**
     * 指定されたオーディオフィーチャーの最小値を更新するヘルパーメソッド
     *
     * @param minAudioFeatures 最小オーディオフィーチャーを格納するマップ
     * @param featureType      オーディオフィーチャーの種類
     * @param newValue         新しい値
     */
    private void updateMinAudioFeature(Map<AudioFeatureType, Float> minAudioFeatures, AudioFeatureType featureType, float newValue) {
        minAudioFeatures.put(featureType, Math.min(minAudioFeatures.get(featureType), newValue));
    }
}
