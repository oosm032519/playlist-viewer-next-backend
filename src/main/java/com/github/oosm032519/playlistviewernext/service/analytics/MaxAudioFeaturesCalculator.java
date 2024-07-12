package com.github.oosm032519.playlistviewernext.service.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MaxAudioFeaturesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(MaxAudioFeaturesCalculator.class);

    private static final String DANCEABILITY = "danceability";
    private static final String ENERGY = "energy";
    private static final String VALENCE = "valence";
    private static final String TEMPO = "tempo";
    private static final String ACOUSTICNESS = "acousticness";
    private static final String INSTRUMENTALNESS = "instrumentalness";
    private static final String LIVENESS = "liveness";
    private static final String SPEECHINESS = "speechiness";

    private static final String CALCULATION_START = "calculateMaxAudioFeatures: 計算開始";
    private static final String CALCULATION_COMPLETE = "calculateMaxAudioFeatures: 最大オーディオフィーチャー計算完了: {}";

    /**
     * トラックリストから最大のオーディオフィーチャーを計算するメソッド
     *
     * @param trackList トラックデータのリスト
     * @return 各オーディオフィーチャーの最大値を含むマップ
     */
    public Map<String, Float> calculateMaxAudioFeatures(List<Map<String, Object>> trackList) {
        logger.info(CALCULATION_START);

        Map<String, Float> maxAudioFeatures = initializeMaxAudioFeatures();

        for (Map<String, Object> trackData : trackList) {
            AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
            if (audioFeatures != null) {
                updateMaxAudioFeatures(maxAudioFeatures, audioFeatures);
            }
        }

        logger.info(CALCULATION_COMPLETE, maxAudioFeatures);
        return maxAudioFeatures;
    }

    private Map<String, Float> initializeMaxAudioFeatures() {
        Map<String, Float> maxAudioFeatures = new HashMap<>(8);
        maxAudioFeatures.put(DANCEABILITY, 0.0f);
        maxAudioFeatures.put(ENERGY, 0.0f);
        maxAudioFeatures.put(VALENCE, 0.0f);
        maxAudioFeatures.put(TEMPO, 0.0f);
        maxAudioFeatures.put(ACOUSTICNESS, 0.0f);
        maxAudioFeatures.put(INSTRUMENTALNESS, 0.0f);
        maxAudioFeatures.put(LIVENESS, 0.0f);
        maxAudioFeatures.put(SPEECHINESS, 0.0f);
        return maxAudioFeatures;
    }

    private void updateMaxAudioFeatures(Map<String, Float> maxAudioFeatures, AudioFeatures audioFeatures) {
        maxAudioFeatures.put(DANCEABILITY, Math.max(maxAudioFeatures.get(DANCEABILITY), audioFeatures.getDanceability()));
        maxAudioFeatures.put(ENERGY, Math.max(maxAudioFeatures.get(ENERGY), audioFeatures.getEnergy()));
        maxAudioFeatures.put(VALENCE, Math.max(maxAudioFeatures.get(VALENCE), audioFeatures.getValence()));
        maxAudioFeatures.put(TEMPO, Math.max(maxAudioFeatures.get(TEMPO), audioFeatures.getTempo()));
        maxAudioFeatures.put(ACOUSTICNESS, Math.max(maxAudioFeatures.get(ACOUSTICNESS), audioFeatures.getAcousticness()));
        maxAudioFeatures.put(INSTRUMENTALNESS, Math.max(maxAudioFeatures.get(INSTRUMENTALNESS), audioFeatures.getInstrumentalness()));
        maxAudioFeatures.put(LIVENESS, Math.max(maxAudioFeatures.get(LIVENESS), audioFeatures.getLiveness()));
        maxAudioFeatures.put(SPEECHINESS, Math.max(maxAudioFeatures.get(SPEECHINESS), audioFeatures.getSpeechiness()));
    }
}
