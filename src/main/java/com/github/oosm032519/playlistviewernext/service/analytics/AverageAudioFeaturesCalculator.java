package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * プレイリストのトラックのAudioFeaturesの平均値を計算するサービスクラス
 * トラックリストから各AudioFeaturesの平均値を算出する
 */
@Service
public class AverageAudioFeaturesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(AverageAudioFeaturesCalculator.class);

    /**
     * トラックリストのAudioFeaturesの平均値を計算するメソッド
     *
     * @param trackList トラックのリスト（各トラックはAudioFeaturesを含むマップ）
     * @return 各AudioFeaturesの平均値を含むマップ
     * @throws InvalidRequestException トラックリストが空、またはAudioFeaturesがnullの場合
     * @throws RuntimeException        予期しないエラーが発生した場合
     */
    public Map<String, Float> calculateAverageAudioFeatures(List<Map<String, Object>> trackList) {
        logger.info("calculateAverageAudioFeatures: 計算開始");

        // トラックリストの有効性チェック
        if (trackList == null || trackList.isEmpty()) {
            logger.warn("トラックリストが空です。");
            throw new InvalidRequestException(
                    HttpStatus.BAD_REQUEST,
                    "EMPTY_TRACK_LIST",
                    "トラックリストが空です。"
            );
        }

        try {
            // 各AudioFeaturesの合計値を格納するマップを初期化
            Map<AudioFeature, Double> audioFeaturesSum = new EnumMap<>(AudioFeature.class);
            for (AudioFeature feature : AudioFeature.values()) {
                audioFeaturesSum.put(feature, 0.0);
            }

            // 各トラックのAudioFeaturesを合計
            for (Map<String, Object> trackData : trackList) {
                AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
                if (audioFeatures == null) {
                    logger.warn("トラックデータにAudioFeaturesが含まれていません。 trackData: {}", trackData);
                    throw new InvalidRequestException(
                            HttpStatus.BAD_REQUEST,
                            "NULL_AUDIO_FEATURES",
                            "AudioFeaturesがnullです。"
                    );
                }

                for (AudioFeature feature : AudioFeature.values()) {
                    audioFeaturesSum.put(feature, audioFeaturesSum.get(feature) + feature.extract(audioFeatures));
                }
            }

            // 平均値を計算
            Map<String, Float> averageAudioFeatures = audioFeaturesSum.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey().name().toLowerCase(),
                            entry -> (float) (entry.getValue() / trackList.size())
                    ));

            logger.info("calculateAverageAudioFeatures: 平均AudioFeatures計算完了: {}", averageAudioFeatures);

            return averageAudioFeatures;
        } catch (InvalidRequestException e) {
            // InvalidRequestException はそのまま再スロー
            throw e;
        } catch (Exception e) {
            // 予期しないエラーが発生した場合は、RuntimeExceptionをスロー
            logger.error("平均AudioFeaturesの計算中に予期しないエラーが発生しました。", e);
            throw new RuntimeException("平均AudioFeaturesの計算中にエラーが発生しました。", e);
        }
    }

    /**
     * AudioFeaturesの種類を定義する列挙型
     * 各AudioFeaturesにはAudioFeaturesオブジェクトから値を抽出する関数が関連付けられている
     */
    private enum AudioFeature {
        DANCEABILITY(AudioFeatures::getDanceability),
        ENERGY(AudioFeatures::getEnergy),
        VALENCE(AudioFeatures::getValence),
        TEMPO(AudioFeatures::getTempo),
        ACOUSTICNESS(AudioFeatures::getAcousticness),
        INSTRUMENTALNESS(AudioFeatures::getInstrumentalness),
        LIVENESS(AudioFeatures::getLiveness),
        SPEECHINESS(AudioFeatures::getSpeechiness);

        private final ToDoubleFunction<AudioFeatures> extractor;

        /**
         * AudioFeature列挙型のコンストラクタ
         *
         * @param extractor AudioFeaturesオブジェクトから特定のフィーチャー値を抽出する関数
         */
        AudioFeature(ToDoubleFunction<AudioFeatures> extractor) {
            this.extractor = extractor;
        }

        /**
         * 指定されたAudioFeaturesオブジェクトから、このフィーチャーの値を抽出する
         *
         * @param audioFeatures 値を抽出するAudioFeaturesオブジェクト
         * @return 抽出されたフィーチャーの値
         */
        public double extract(AudioFeatures audioFeatures) {
            return extractor.applyAsDouble(audioFeatures);
        }
    }
}
