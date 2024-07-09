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
    private final AudioFeatureSetter audioFeatureSetter;

    @Autowired
    public SpotifyRecommendationService(SpotifyApi spotifyApi, AudioFeatureSetter audioFeatureSetter) {
        this.spotifyApi = spotifyApi;
        this.audioFeatureSetter = audioFeatureSetter;
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

        audioFeatureSetter.setMaxAudioFeatures(recommendationsRequestBuilder, maxAudioFeatures);
        audioFeatureSetter.setMinAudioFeatures(recommendationsRequestBuilder, minAudioFeatures);
        audioFeatureSetter.setMedianAudioFeatures(recommendationsRequestBuilder, medianAudioFeatures);
        audioFeatureSetter.setModeValues(recommendationsRequestBuilder, modeValues);

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
