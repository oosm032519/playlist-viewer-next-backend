package com.github.oosm032519.playlistviewernext.service.recommendation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackRecommendationServiceTest {

    @Mock
    private SpotifyRecommendationService recommendationService;

    @InjectMocks
    private TrackRecommendationService trackRecommendationService;

    private Map<String, Float> maxAudioFeatures;
    private Map<String, Float> minAudioFeatures;
    private Map<String, Float> medianAudioFeatures;
    private Map<String, Object> modeValues;
    private List<String> genres;

    @BeforeEach
    void setUp() {
        maxAudioFeatures = Map.of("danceability", 0.9f);
        minAudioFeatures = Map.of("danceability", 0.1f);
        medianAudioFeatures = Map.of("danceability", 0.5f);
        modeValues = Map.of("key", 1);
        genres = List.of("pop", "rock");
    }

    @Test
    void getRecommendations_ReturnsRecommendationsSuccessfully() throws Exception {
        List<Track> expectedRecommendations = List.of(
                new Track.Builder().setName("Recommended Track 1").build(),
                new Track.Builder().setName("Recommended Track 2").build()
        );
        when(recommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues))
                .thenReturn(expectedRecommendations);

        List<Track> result = trackRecommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);

        assertThat(result).isEqualTo(expectedRecommendations);
        verify(recommendationService).getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);
    }

    @Test
    void getRecommendations_ReturnsEmptyListWhenGenresAreEmpty() {
        List<Track> result = trackRecommendationService.getRecommendations(Collections.emptyList(), maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);

        assertThat(result).isEmpty();
        verifyNoInteractions(recommendationService);
    }

    @Test
    void getRecommendations_HandlesExceptionGracefully() throws Exception {
        when(recommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues))
                .thenThrow(new SpotifyWebApiException("API Error"));

        List<Track> result = trackRecommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);

        assertThat(result).isEmpty();
        verify(recommendationService).getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);
    }

    @Test
    void getRecommendations_HandlesIOExceptionGracefully() throws Exception {
        when(recommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues))
                .thenThrow(new IOException("IO Error"));

        List<Track> result = trackRecommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);

        assertThat(result).isEmpty();
        verify(recommendationService).getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);
    }
}
