// MinAudioFeaturesCalculator.java

package com.github.oosm032519.playlistviewernext.service.analytics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MinAudioFeaturesCalculator {

    // ロガーの初期化
    private static final Logger logger = LoggerFactory.getLogger(MinAudioFeaturesCalculator.class);

    /**
     * トラックリストから各オーディオフィーチャーの最小値を計算するメソッド
     *
     * @param trackList トラックのリスト。各トラックはオーディオフィーチャーを含むマップで表される
     * @return 各オーディオフィーチャーの最小値を含むマップ
     */
    public Map<String, Float> calculateMinAudioFeatures(List<Map<String, Object>> trackList) {
        logger.info("calculateMinAudioFeatures: 計算開始");

        // 最小オーディオフィーチャーを格納するマップの初期化
        Map<String, Float> minAudioFeatures = new HashMap<>();
        minAudioFeatures.put("danceability", Float.MAX_VALUE);
        minAudioFeatures.put("energy", Float.MAX_VALUE);
        minAudioFeatures.put("valence", Float.MAX_VALUE);
        minAudioFeatures.put("tempo", Float.MAX_VALUE);
        minAudioFeatures.put("acousticness", Float.MAX_VALUE);
        minAudioFeatures.put("instrumentalness", Float.MAX_VALUE);
        minAudioFeatures.put("liveness", Float.MAX_VALUE);
        minAudioFeatures.put("speechiness", Float.MAX_VALUE);

        // トラックリストをループして各オーディオフィーチャーの最小値を計算
        for (Map<String, Object> trackData : trackList) {
            AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
            if (audioFeatures != null) {
                minAudioFeatures.put("danceability", Math.min(minAudioFeatures.get("danceability"), audioFeatures.getDanceability()));
                minAudioFeatures.put("energy", Math.min(minAudioFeatures.get("energy"), audioFeatures.getEnergy()));
                minAudioFeatures.put("valence", Math.min(minAudioFeatures.get("valence"), audioFeatures.getValence()));
                minAudioFeatures.put("tempo", Math.min(minAudioFeatures.get("tempo"), audioFeatures.getTempo()));
                minAudioFeatures.put("acousticness", Math.min(minAudioFeatures.get("acousticness"), audioFeatures.getAcousticness()));
                minAudioFeatures.put("instrumentalness", Math.min(minAudioFeatures.get("instrumentalness"), audioFeatures.getInstrumentalness()));
                minAudioFeatures.put("liveness", Math.min(minAudioFeatures.get("liveness"), audioFeatures.getLiveness()));
                minAudioFeatures.put("speechiness", Math.min(minAudioFeatures.get("speechiness"), audioFeatures.getSpeechiness()));
            }
        }

        logger.info("calculateMinAudioFeatures: 最小オーディオフィーチャー計算完了: {}", minAudioFeatures);
        return minAudioFeatures;
    }
}
