package com.github.oosm032519.playlistviewernext.service.playlist;

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

    public Map<String, Float> calculateMaxAudioFeatures(List<Map<String, Object>> trackList) {
        logger.info("calculateMaxAudioFeatures: 計算開始");
        Map<String, Float> maxAudioFeatures = new HashMap<>();
        maxAudioFeatures.put("danceability", 0.0f);
        maxAudioFeatures.put("energy", 0.0f);
        maxAudioFeatures.put("valence", 0.0f);
        maxAudioFeatures.put("tempo", 0.0f);
        maxAudioFeatures.put("acousticness", 0.0f);
        maxAudioFeatures.put("instrumentalness", 0.0f);
        maxAudioFeatures.put("liveness", 0.0f);
        maxAudioFeatures.put("speechiness", 0.0f);

        for (Map<String, Object> trackData : trackList) {
            AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
            if (audioFeatures != null) {
                maxAudioFeatures.put("danceability", Math.max(maxAudioFeatures.get("danceability"), audioFeatures.getDanceability()));
                maxAudioFeatures.put("energy", Math.max(maxAudioFeatures.get("energy"), audioFeatures.getEnergy()));
                maxAudioFeatures.put("valence", Math.max(maxAudioFeatures.get("valence"), audioFeatures.getValence()));
                maxAudioFeatures.put("tempo", Math.max(maxAudioFeatures.get("tempo"), audioFeatures.getTempo()));
                maxAudioFeatures.put("acousticness", Math.max(maxAudioFeatures.get("acousticness"), audioFeatures.getAcousticness()));
                maxAudioFeatures.put("instrumentalness", Math.max(maxAudioFeatures.get("instrumentalness"), audioFeatures.getInstrumentalness()));
                maxAudioFeatures.put("liveness", Math.max(maxAudioFeatures.get("liveness"), audioFeatures.getLiveness()));
                maxAudioFeatures.put("speechiness", Math.max(maxAudioFeatures.get("speechiness"), audioFeatures.getSpeechiness()));
            }
        }
        logger.info("calculateMaxAudioFeatures: 最大オーディオフィーチャー計算完了: {}", maxAudioFeatures);
        return maxAudioFeatures;
    }
}
