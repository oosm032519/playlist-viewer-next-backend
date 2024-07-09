package com.github.oosm032519.playlistviewernext.service.recommendation;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Recommendations;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class SpotifyRecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyRecommendationService.class);
    private final SpotifyApi spotifyApi;

    @Autowired
    public SpotifyRecommendationService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    public List<Track> getRecommendations(List<String> seedGenres, Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures, Map<String, Float> medianAudioFeatures, Map<String, Object> modeValues) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("getRecommendations: seedGenres: {}, maxAudioFeatures: {}, minAudioFeatures: {}, medianAudioFeatures: {}, modeValues: {}", seedGenres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);
        if (seedGenres.isEmpty()) {
            return Collections.emptyList();
        }

        String genres = String.join(",", seedGenres);
        GetRecommendationsRequest.Builder recommendationsRequestBuilder = spotifyApi.getRecommendations()
                .seed_genres(genres)
                .limit(20);

        // 最大オーディオフィーチャーを設定
        if (maxAudioFeatures.containsKey("danceability")) {
            recommendationsRequestBuilder.max_danceability(maxAudioFeatures.get("danceability"));
        }
        if (maxAudioFeatures.containsKey("energy")) {
            recommendationsRequestBuilder.max_energy(maxAudioFeatures.get("energy"));
        }
        if (maxAudioFeatures.containsKey("valence")) {
            recommendationsRequestBuilder.max_valence(maxAudioFeatures.get("valence"));
        }
        if (maxAudioFeatures.containsKey("tempo")) {
            recommendationsRequestBuilder.max_tempo(maxAudioFeatures.get("tempo"));
        }
        if (maxAudioFeatures.containsKey("acousticness")) {
            recommendationsRequestBuilder.max_acousticness(maxAudioFeatures.get("acousticness"));
        }
        if (maxAudioFeatures.containsKey("instrumentalness")) {
            recommendationsRequestBuilder.max_instrumentalness(maxAudioFeatures.get("instrumentalness"));
        }
        if (maxAudioFeatures.containsKey("liveness")) {
            recommendationsRequestBuilder.max_liveness(maxAudioFeatures.get("liveness"));
        }
        if (maxAudioFeatures.containsKey("speechiness")) {
            recommendationsRequestBuilder.max_speechiness(maxAudioFeatures.get("speechiness"));
        }

        // 最小オーディオフィーチャーを設定
        if (minAudioFeatures.containsKey("danceability")) {
            recommendationsRequestBuilder.min_danceability(minAudioFeatures.get("danceability"));
        }
        if (minAudioFeatures.containsKey("energy")) {
            recommendationsRequestBuilder.min_energy(minAudioFeatures.get("energy"));
        }
        if (minAudioFeatures.containsKey("valence")) {
            recommendationsRequestBuilder.min_valence(minAudioFeatures.get("valence"));
        }
        if (minAudioFeatures.containsKey("tempo")) {
            recommendationsRequestBuilder.min_tempo(minAudioFeatures.get("tempo"));
        }
        if (minAudioFeatures.containsKey("acousticness")) {
            recommendationsRequestBuilder.min_acousticness(minAudioFeatures.get("acousticness"));
        }
        if (minAudioFeatures.containsKey("instrumentalness")) {
            recommendationsRequestBuilder.min_instrumentalness(minAudioFeatures.get("instrumentalness"));
        }
        if (minAudioFeatures.containsKey("liveness")) {
            recommendationsRequestBuilder.min_liveness(minAudioFeatures.get("liveness"));
        }
        if (minAudioFeatures.containsKey("speechiness")) {
            recommendationsRequestBuilder.min_speechiness(minAudioFeatures.get("speechiness"));
        }

        // 中央オーディオフィーチャーを設定
        if (medianAudioFeatures.containsKey("danceability")) {
            recommendationsRequestBuilder.target_danceability(medianAudioFeatures.get("danceability"));
        }
        if (medianAudioFeatures.containsKey("energy")) {
            recommendationsRequestBuilder.target_energy(medianAudioFeatures.get("energy"));
        }
        if (medianAudioFeatures.containsKey("valence")) {
            recommendationsRequestBuilder.target_valence(medianAudioFeatures.get("valence"));
        }
        if (medianAudioFeatures.containsKey("tempo")) {
            recommendationsRequestBuilder.target_tempo(medianAudioFeatures.get("tempo"));
        }
        if (medianAudioFeatures.containsKey("acousticness")) {
            recommendationsRequestBuilder.target_acousticness(medianAudioFeatures.get("acousticness"));
        }
        if (medianAudioFeatures.containsKey("instrumentalness")) {
            recommendationsRequestBuilder.target_instrumentalness(medianAudioFeatures.get("instrumentalness"));
        }
        if (medianAudioFeatures.containsKey("liveness")) {
            recommendationsRequestBuilder.target_liveness(medianAudioFeatures.get("liveness"));
        }
        if (medianAudioFeatures.containsKey("speechiness")) {
            recommendationsRequestBuilder.target_speechiness(medianAudioFeatures.get("speechiness"));
        }

        // modeValuesの設定
        if (modeValues.containsKey("key")) {
            recommendationsRequestBuilder.target_key((Integer) modeValues.get("key"));
        }
        if (modeValues.containsKey("mode")) {
            recommendationsRequestBuilder.target_mode("MAJOR".equals(modeValues.get("mode")) ? 1 : 0);
        }
        if (modeValues.containsKey("time_signature")) {
            recommendationsRequestBuilder.target_time_signature((Integer) modeValues.get("time_signature"));
        }

        GetRecommendationsRequest recommendationsRequest = recommendationsRequestBuilder.build();
        Recommendations recommendations = recommendationsRequest.execute();

        if (recommendations == null || recommendations.getTracks() == null) {
            logger.info("getRecommendations: 推奨トラックが見つかりませんでした");
            return Collections.emptyList();
        }

        logger.info("getRecommendations: 推奨トラック数: {}", recommendations.getTracks().length);
        return Arrays.asList(recommendations.getTracks());
    }
}
