package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
     * @throws PlaylistViewerNextException 平均オーディオフィーチャーの計算中にエラーが発生した場合
     */
    public Map<String, Float> calculateAverageAudioFeatures(List<Map<String, Object>> trackList) {
        logger.info("calculateAverageAudioFeatures: 計算開始");

        try {
            Map<AudioFeature, Double> audioFeaturesSum = new EnumMap<>(AudioFeature.class);
            for (AudioFeature feature : AudioFeature.values()) {
                audioFeaturesSum.put(feature, 0.0);
            }

            trackList.stream()
                    .map(trackData -> (AudioFeatures) trackData.get("audioFeatures"))
                    .filter(Objects::nonNull)
                    .forEach(audioFeatures -> {
                        for (AudioFeature feature : AudioFeature.values()) {
                            audioFeaturesSum.put(feature, audioFeaturesSum.get(feature) + feature.extract(audioFeatures));
                        }
                    });

            Map<String, Float> averageAudioFeatures = audioFeaturesSum.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey().name().toLowerCase(),
                            entry -> (float) (entry.getValue() / trackList.size())
                    ));

            logger.info("calculateAverageAudioFeatures: 平均オーディオフィーチャー計算完了: {}", averageAudioFeatures);

            return averageAudioFeatures;
        } catch (Exception e) {
            // 平均オーディオフィーチャーの計算中にエラーが発生した場合は PlaylistViewerNextException をスロー
            logger.error("平均オーディオフィーチャーの計算中にエラーが発生しました。", e);
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "AVERAGE_AUDIO_FEATURES_CALCULATION_ERROR",
                    "平均オーディオフィーチャーの計算中にエラーが発生しました。",
                    e
            );
        }
    }
}
