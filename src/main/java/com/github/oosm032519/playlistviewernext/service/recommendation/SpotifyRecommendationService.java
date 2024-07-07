package com.github.oosm032519.playlistviewernext.service.recommendation;

import org.apache.hc.core5.http.ParseException;
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

@Service
public class SpotifyRecommendationService {
    private final SpotifyApi spotifyApi;

    @Autowired
    public SpotifyRecommendationService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    public List<Track> getRecommendations(List<String> seedGenres) throws IOException, SpotifyWebApiException, ParseException {
        if (seedGenres.isEmpty()) {
            return Collections.emptyList();
        }

        String genres = String.join(",", seedGenres);
        GetRecommendationsRequest recommendationsRequest = spotifyApi.getRecommendations()
                .seed_genres(genres)
                .limit(20)
                .build();

        Recommendations recommendations = recommendationsRequest.execute();

        if (recommendations == null || recommendations.getTracks() == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(recommendations.getTracks());
    }
}
