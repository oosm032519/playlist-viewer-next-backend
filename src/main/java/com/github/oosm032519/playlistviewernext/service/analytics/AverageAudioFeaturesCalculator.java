package com.github.oosm032519.playlistviewernext.service.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AverageAudioFeaturesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(AverageAudioFeaturesCalculator.class);

    public Map<String, Float> calculateAverageAudioFeatures(List<Map<String, Object>> trackList) {
        logger.info("calculateAverageAudioFeatures: 計算開始");
        Map<String, Float> audioFeaturesSum = new HashMap<>();
        audioFeaturesSum.put("danceability", 0.0f);
        audioFeaturesSum.put("energy", 0.0f);
        audioFeaturesSum.put("valence", 0.0f);
        audioFeaturesSum.put("tempo", 0.0f);
        audioFeaturesSum.put("acousticness", 0.0f);
        audioFeaturesSum.put("instrumentalness", 0.0f);
        audioFeaturesSum.put("liveness", 0.0f);
        audioFeaturesSum.put("speechiness", 0.0f);

        for (Map<String, Object> trackData : trackList) {
            AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
            if (audioFeatures != null) {
                audioFeaturesSum.put("danceability", audioFeaturesSum.get("danceability") + audioFeatures.getDanceability());
                audioFeaturesSum.put("energy", audioFeaturesSum.get("energy") + audioFeatures.getEnergy());
                audioFeaturesSum.put("valence", audioFeaturesSum.get("valence") + audioFeatures.getValence());
                audioFeaturesSum.put("tempo", audioFeaturesSum.get("tempo") + audioFeatures.getTempo());
                audioFeaturesSum.put("acousticness", audioFeaturesSum.get("acousticness") + audioFeatures.getAcousticness());
                audioFeaturesSum.put("instrumentalness", audioFeaturesSum.get("instrumentalness") + audioFeatures.getInstrumentalness());
                audioFeaturesSum.put("liveness", audioFeaturesSum.get("liveness") + audioFeatures.getLiveness());
                audioFeaturesSum.put("speechiness", audioFeaturesSum.get("speechiness") + audioFeatures.getSpeechiness());
            }
        }

        Map<String, Float> averageAudioFeatures = new HashMap<>();
        for (Map.Entry<String, Float> entry : audioFeaturesSum.entrySet()) {
            averageAudioFeatures.put(entry.getKey(), entry.getValue() / trackList.size());
        }
        logger.info("calculateAverageAudioFeatures: 平均オーディオフィーチャー計算完了: {}", averageAudioFeatures);
        return averageAudioFeatures;
    }
}
