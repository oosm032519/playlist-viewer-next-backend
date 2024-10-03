package com.github.oosm032519.playlistviewernext.service.analytics;

import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;

import java.util.Map;
import java.util.function.BiConsumer;

@Component
public class AudioFeatureSetter {

    /**
     * GetRecommendationsRequest.Builderに最大AudioFeaturesを設定する
     *
     * @param builder          GetRecommendationsRequest.Builderオブジェクト
     * @param maxAudioFeatures 最大AudioFeaturesのマップ
     */
    public void setMaxAudioFeatures(GetRecommendationsRequest.Builder builder, Map<String, Float> maxAudioFeatures) {
        setAudioFeatures(builder, maxAudioFeatures, this::setMaxFeature);
    }

    /**
     * GetRecommendationsRequest.Builderに最小AudioFeaturesを設定する
     *
     * @param builder          GetRecommendationsRequest.Builderオブジェクト
     * @param minAudioFeatures 最小AudioFeaturesのマップ
     */
    public void setMinAudioFeatures(GetRecommendationsRequest.Builder builder, Map<String, Float> minAudioFeatures) {
        setAudioFeatures(builder, minAudioFeatures, this::setMinFeature);
    }

    private void setAudioFeatures(GetRecommendationsRequest.Builder builder, Map<String, Float> audioFeatures, BiConsumer<GetRecommendationsRequest.Builder, Map.Entry<String, Float>> featureSetter) {
        for (Map.Entry<String, Float> entry : audioFeatures.entrySet()) {
            featureSetter.accept(builder, entry);
        }
    }

    private void setMaxFeature(GetRecommendationsRequest.Builder builder, Map.Entry<String, Float> entry) {
        switch (entry.getKey()) {
            case "danceability":
                builder.max_danceability(entry.getValue());
                break;
            case "energy":
                builder.max_energy(entry.getValue());
                break;
            case "valence":
                builder.max_valence(entry.getValue());
                break;
            case "tempo":
                builder.max_tempo(entry.getValue());
                break;
            case "acousticness":
                builder.max_acousticness(entry.getValue());
                break;
            case "instrumentalness":
                builder.max_instrumentalness(entry.getValue());
                break;
            case "liveness":
                builder.max_liveness(entry.getValue());
                break;
            case "speechiness":
                builder.max_speechiness(entry.getValue());
                break;
        }
    }

    private void setMinFeature(GetRecommendationsRequest.Builder builder, Map.Entry<String, Float> entry) {
        switch (entry.getKey()) {
            case "danceability":
                builder.min_danceability(entry.getValue());
                break;
            case "energy":
                builder.min_energy(entry.getValue());
                break;
            case "valence":
                builder.min_valence(entry.getValue());
                break;
            case "tempo":
                builder.min_tempo(entry.getValue());
                break;
            case "acousticness":
                builder.min_acousticness(entry.getValue());
                break;
            case "instrumentalness":
                builder.min_instrumentalness(entry.getValue());
                break;
            case "liveness":
                builder.min_liveness(entry.getValue());
                break;
            case "speechiness":
                builder.min_speechiness(entry.getValue());
                break;
        }
    }
}
