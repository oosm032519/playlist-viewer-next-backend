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

@Service
public class AverageAudioFeaturesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(AverageAudioFeaturesCalculator.class);

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

        AudioFeature(ToDoubleFunction<AudioFeatures> extractor) {
            this.extractor = extractor;
        }

        public double extract(AudioFeatures audioFeatures) {
            return extractor.applyAsDouble(audioFeatures);
        }
    }

    /**
     * トラックリストのオーディオフィーチャーの平均値を計算するメソッド
     *
     * @param trackList トラックのリスト（各トラックはオーディオフィーチャーを含むマップ）
     * @return 各オーディオフィーチャーの平均値を含むマップ
     * @throws InvalidRequestException トラックリストが空、またはオーディオフィーチャーがnullの場合
     */
    public Map<String, Float> calculateAverageAudioFeatures(List<Map<String, Object>> trackList) {
        logger.info("calculateAverageAudioFeatures: 計算開始");

        if (trackList == null || trackList.isEmpty()) {
            logger.warn("トラックリストが空です。");
            throw new InvalidRequestException(
                    HttpStatus.BAD_REQUEST,
                    "EMPTY_TRACK_LIST",
                    "トラックリストが空です。"
            );
        }

        try {
            Map<AudioFeature, Double> audioFeaturesSum = new EnumMap<>(AudioFeature.class);
            for (AudioFeature feature : AudioFeature.values()) {
                audioFeaturesSum.put(feature, 0.0);
            }

            for (Map<String, Object> trackData : trackList) {
                AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
                if (audioFeatures == null) {
                    logger.warn("トラックデータにオーディオフィーチャーが含まれていません。 trackData: {}", trackData);
                    throw new InvalidRequestException(
                            HttpStatus.BAD_REQUEST,
                            "NULL_AUDIO_FEATURES",
                            "オーディオフィーチャーがnullです。"
                    );
                }

                for (AudioFeature feature : AudioFeature.values()) {
                    audioFeaturesSum.put(feature, audioFeaturesSum.get(feature) + feature.extract(audioFeatures));
                }
            }

            Map<String, Float> averageAudioFeatures = audioFeaturesSum.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey().name().toLowerCase(),
                            entry -> (float) (entry.getValue() / trackList.size())
                    ));

            logger.info("calculateAverageAudioFeatures: 平均オーディオフィーチャー計算完了: {}", averageAudioFeatures);

            return averageAudioFeatures;
        } catch (InvalidRequestException e) {
            // InvalidRequestException はそのまま再スロー
            throw e;
        } catch (Exception e) {
            // 予期しないエラーが発生した場合は、RuntimeExceptionをスロー
            logger.error("平均オーディオフィーチャーの計算中に予期しないエラーが発生しました。", e);
            throw new RuntimeException("平均オーディオフィーチャーの計算中にエラーが発生しました。", e);
        }
    }
}
