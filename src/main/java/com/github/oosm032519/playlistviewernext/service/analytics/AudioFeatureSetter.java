// AudioFeatureSetter.java

package com.github.oosm032519.playlistviewernext.service.analytics;

import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;

import java.util.Map;

@Component
public class AudioFeatureSetter {

    /**
     * GetRecommendationsRequest.Builderに最大オーディオ特徴量を設定します。
     *
     * @param builder          GetRecommendationsRequest.Builderオブジェクト
     * @param maxAudioFeatures 最大オーディオ特徴量のマップ
     */
    public void setMaxAudioFeatures(GetRecommendationsRequest.Builder builder, Map<String, Float> maxAudioFeatures) {
        if (maxAudioFeatures.containsKey("danceability")) {
            builder.max_danceability(maxAudioFeatures.get("danceability"));
        }
        if (maxAudioFeatures.containsKey("energy")) {
            builder.max_energy(maxAudioFeatures.get("energy"));
        }
        if (maxAudioFeatures.containsKey("valence")) {
            builder.max_valence(maxAudioFeatures.get("valence"));
        }
        if (maxAudioFeatures.containsKey("tempo")) {
            builder.max_tempo(maxAudioFeatures.get("tempo"));
        }
        if (maxAudioFeatures.containsKey("acousticness")) {
            builder.max_acousticness(maxAudioFeatures.get("acousticness"));
        }
        if (maxAudioFeatures.containsKey("instrumentalness")) {
            builder.max_instrumentalness(maxAudioFeatures.get("instrumentalness"));
        }
        if (maxAudioFeatures.containsKey("liveness")) {
            builder.max_liveness(maxAudioFeatures.get("liveness"));
        }
        if (maxAudioFeatures.containsKey("speechiness")) {
            builder.max_speechiness(maxAudioFeatures.get("speechiness"));
        }
    }

    /**
     * GetRecommendationsRequest.Builderに最小オーディオ特徴量を設定します。
     *
     * @param builder          GetRecommendationsRequest.Builderオブジェクト
     * @param minAudioFeatures 最小オーディオ特徴量のマップ
     */
    public void setMinAudioFeatures(GetRecommendationsRequest.Builder builder, Map<String, Float> minAudioFeatures) {
        if (minAudioFeatures.containsKey("danceability")) {
            builder.min_danceability(minAudioFeatures.get("danceability"));
        }
        if (minAudioFeatures.containsKey("energy")) {
            builder.min_energy(minAudioFeatures.get("energy"));
        }
        if (minAudioFeatures.containsKey("valence")) {
            builder.min_valence(minAudioFeatures.get("valence"));
        }
        if (minAudioFeatures.containsKey("tempo")) {
            builder.min_tempo(minAudioFeatures.get("tempo"));
        }
        if (minAudioFeatures.containsKey("acousticness")) {
            builder.min_acousticness(minAudioFeatures.get("acousticness"));
        }
        if (minAudioFeatures.containsKey("instrumentalness")) {
            builder.min_instrumentalness(minAudioFeatures.get("instrumentalness"));
        }
        if (minAudioFeatures.containsKey("liveness")) {
            builder.min_liveness(minAudioFeatures.get("liveness"));
        }
        if (minAudioFeatures.containsKey("speechiness")) {
            builder.min_speechiness(minAudioFeatures.get("speechiness"));
        }
    }

    /**
     * GetRecommendationsRequest.Builderに中央値のオーディオ特徴量を設定します。
     *
     * @param builder             GetRecommendationsRequest.Builderオブジェクト
     * @param medianAudioFeatures 中央値のオーディオ特徴量のマップ
     */
    public void setMedianAudioFeatures(GetRecommendationsRequest.Builder builder, Map<String, Float> medianAudioFeatures) {
        if (medianAudioFeatures.containsKey("danceability")) {
            builder.target_danceability(medianAudioFeatures.get("danceability"));
        }
        if (medianAudioFeatures.containsKey("energy")) {
            builder.target_energy(medianAudioFeatures.get("energy"));
        }
        if (medianAudioFeatures.containsKey("valence")) {
            builder.target_valence(medianAudioFeatures.get("valence"));
        }
        if (medianAudioFeatures.containsKey("tempo")) {
            builder.target_tempo(medianAudioFeatures.get("tempo"));
        }
        if (medianAudioFeatures.containsKey("acousticness")) {
            builder.target_acousticness(medianAudioFeatures.get("acousticness"));
        }
        if (medianAudioFeatures.containsKey("instrumentalness")) {
            builder.target_instrumentalness(medianAudioFeatures.get("instrumentalness"));
        }
        if (medianAudioFeatures.containsKey("liveness")) {
            builder.target_liveness(medianAudioFeatures.get("liveness"));
        }
        if (medianAudioFeatures.containsKey("speechiness")) {
            builder.target_speechiness(medianAudioFeatures.get("speechiness"));
        }
    }

    /**
     * GetRecommendationsRequest.Builderにモード値を設定します。
     *
     * @param builder    GetRecommendationsRequest.Builderオブジェクト
     * @param modeValues モード値のマップ
     */
    public void setModeValues(GetRecommendationsRequest.Builder builder, Map<String, Object> modeValues) {
        if (modeValues.containsKey("key")) {
            builder.target_key((Integer) modeValues.get("key"));
        }
        if (modeValues.containsKey("mode")) {
            builder.target_mode("MAJOR".equals(modeValues.get("mode")) ? 1 : 0);
        }
        if (modeValues.containsKey("time_signature")) {
            builder.target_time_signature((Integer) modeValues.get("time_signature"));
        }
    }
}
