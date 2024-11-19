package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.*;
import java.util.function.ToDoubleFunction;

/**
 * SpotifyのAudio Featuresの統計計算を行うユーティリティクラス。
 * Audio Featuresの平均値、最大値、最小値を計算する。
 */
public class AudioFeaturesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(AudioFeaturesCalculator.class);

    /**
     * トラックリストのAudio Features平均値を計算する。
     *
     * @param trackList Audio Featuresを含むトラックデータのリスト
     * @return 各Audio Featuresの平均値をキーと値のペアで返す
     * @throws InvalidRequestException トラックリストが空またはnull、もしくはAudio Featuresがnullの場合
     */
    public static Map<String, Float> calculateAverageAudioFeatures(List<Map<String, Object>> trackList) {
        validateTrackList(trackList);

        // 各特徴量の合計値を保持するマップを初期化
        Map<AudioFeatureType, Double> sums = new EnumMap<>(AudioFeatureType.class);
        for (AudioFeatureType type : AudioFeatureType.values()) {
            sums.put(type, 0.0);
        }

        // 各トラックの特徴量を合計
        for (Map<String, Object> trackData : trackList) {
            AudioFeatures audioFeatures = getAudioFeaturesFromTrack(trackData);
            for (AudioFeatureType type : AudioFeatureType.values()) {
                sums.put(type, sums.get(type) + type.extractor.applyAsDouble(audioFeatures));
            }
        }

        // 平均値を計算
        Map<String, Float> averages = new HashMap<>();
        for (AudioFeatureType type : AudioFeatureType.values()) {
            averages.put(type.name().toLowerCase(), (float) (sums.get(type) / trackList.size()));
        }

        return averages;
    }

    /**
     * トラックリストの妥当性を検証する。
     *
     * @param trackList 検証対象のトラックリスト
     * @throws InvalidRequestException トラックリストが無効な場合
     */
    private static void validateTrackList(List<Map<String, Object>> trackList) {
        if (trackList == null || trackList.isEmpty()) {
            String message = "トラックリストが空です。";
            logger.warn(message);
            throw new InvalidRequestException(HttpStatus.BAD_REQUEST, message);
        }
    }

    /**
     * トラックデータからAudio Featuresを取得する。
     *
     * @param trackData トラックデータ
     * @return AudioFeatures
     * @throws InvalidRequestException Audio Featuresが取得できない場合
     */
    private static AudioFeatures getAudioFeaturesFromTrack(Map<String, Object> trackData) {
        AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
        if (audioFeatures == null) {
            String message = "AudioFeaturesがnullです。";
            logger.warn(message + " trackData: {}", trackData);
            throw new InvalidRequestException(HttpStatus.BAD_REQUEST, message);
        }
        return audioFeatures;
    }

    /**
     * トラックリストのAudio Features最大値を計算する。
     *
     * @param trackList Audio Featuresを含むトラックデータのリスト
     * @return 各Audio Featuresの最大値をキーと値のペアで返す
     * @throws InvalidRequestException トラックリストが空またはnullの場合
     */
    public static Map<String, Float> calculateMaxAudioFeatures(List<Map<String, Object>> trackList) {
        return calculateExtremeAudioFeatures(trackList, true);
    }

    /**
     * トラックリストの最大値または最小値を計算する内部メソッド。
     *
     * @param trackList Audio Featuresを含むトラックデータのリスト
     * @param isMax     true:最大値を計算、false:最小値を計算
     * @return 計算結果をキーと値のペアで返す
     */
    private static Map<String, Float> calculateExtremeAudioFeatures(List<Map<String, Object>> trackList, boolean isMax) {
        validateTrackList(trackList);

        // 各特徴量の値を格納するリストを初期化
        Map<AudioFeatureType, List<Float>> values = new EnumMap<>(AudioFeatureType.class);
        for (AudioFeatureType type : AudioFeatureType.values()) {
            values.put(type, new ArrayList<>());
        }

        // 各トラックの特徴量を収集
        for (Map<String, Object> trackData : trackList) {
            AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
            if (audioFeatures != null) {
                for (AudioFeatureType type : AudioFeatureType.values()) {
                    values.get(type).add((float) type.extractor.applyAsDouble(audioFeatures));
                }
            }
        }

        // 最大値または最小値を計算
        Map<String, Float> result = new HashMap<>();
        for (Map.Entry<AudioFeatureType, List<Float>> entry : values.entrySet()) {
            if (entry.getValue().isEmpty()) continue;
            result.put(entry.getKey().name().toLowerCase(),
                    isMax ? Collections.max(entry.getValue()) : Collections.min(entry.getValue()));
        }
        return result;
    }

    /**
     * トラックリストのAudio Features最小値を計算する。
     *
     * @param trackList Audio Featuresを含むトラックデータのリスト
     * @return 各Audio Featuresの最小値をキーと値のペアで返す
     * @throws InvalidRequestException トラックリストが空またはnullの場合
     */
    public static Map<String, Float> calculateMinAudioFeatures(List<Map<String, Object>> trackList) {
        return calculateExtremeAudioFeatures(trackList, false);
    }

    /**
     * Audio Featuresの種類と値の抽出方法を定義する列挙型。
     */
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
