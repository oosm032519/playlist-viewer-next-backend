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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackRecommendationServiceTest {

    @Mock
    private SpotifyRecommendationService recommendationService;

    @InjectMocks
    private TrackRecommendationService trackRecommendationService;

    private Map<String, Float> maxAudioFeatures;
    private Map<String, Float> minAudioFeatures;
    private List<String> genres;

    @BeforeEach
    void setUp() {
        maxAudioFeatures = Map.of("danceability", 0.9f);
        minAudioFeatures = Map.of("danceability", 0.1f);
        genres = List.of("pop", "rock");
    }

    @Test
    void givenValidParameters_whenGetRecommendations_thenReturnsRecommendationsSuccessfully() throws Exception {
        // Arrange
        List<Track> expectedRecommendations = List.of(
                new Track.Builder().setName("Recommended Track 1").build(),
                new Track.Builder().setName("Recommended Track 2").build()
        );
        when(recommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures))
                .thenReturn(expectedRecommendations);

        // Act
        List<Track> result = trackRecommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures);

        // Assert
        assertThat(result).isEqualTo(expectedRecommendations);
        verify(recommendationService).getRecommendations(genres, maxAudioFeatures, minAudioFeatures);
    }

    @Test
    void givenEmptyGenres_whenGetRecommendations_thenReturnsEmptyList() {
        // Arrange
        List<String> emptyGenres = Collections.emptyList();

        // Act
        List<Track> result = trackRecommendationService.getRecommendations(emptyGenres, maxAudioFeatures, minAudioFeatures);

        // Assert
        assertThat(result).isEmpty();
        verifyNoInteractions(recommendationService);
    }

    @Test
    void givenSpotifyWebApiException_whenGetRecommendations_thenThrowsSpotifyApiException() throws Exception {
        // Arrange
        when(recommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures))
                .thenThrow(new RuntimeException(new SpotifyWebApiException("API Error")));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                trackRecommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures)
        );
        verify(recommendationService).getRecommendations(genres, maxAudioFeatures, minAudioFeatures);
    }

    @Test
    void givenIOException_whenGetRecommendations_thenThrowsSpotifyApiException() throws Exception {
        // Arrange
        when(recommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures))
                .thenThrow(new RuntimeException(new IOException("IO Error")));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                trackRecommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures)
        );
        verify(recommendationService).getRecommendations(genres, maxAudioFeatures, minAudioFeatures);
    }
}
