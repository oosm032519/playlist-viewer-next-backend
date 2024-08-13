package com.github.oosm032519.playlistviewernext.service.recommendation;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
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
    void givenValidParameters_whenGetRecommendations_thenReturnsRecommendationsSuccessfully() throws Exception {
        // Arrange
        List<Track> expectedRecommendations = List.of(
                new Track.Builder().setName("Recommended Track 1").build(),
                new Track.Builder().setName("Recommended Track 2").build()
        );
        when(recommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues))
                .thenReturn(expectedRecommendations);

        // Act
        List<Track> result = trackRecommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);

        // Assert
        assertThat(result).isEqualTo(expectedRecommendations);
        verify(recommendationService).getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);
    }

    @Test
    void givenEmptyGenres_whenGetRecommendations_thenReturnsEmptyList() {
        // Arrange
        List<String> emptyGenres = Collections.emptyList();

        // Act
        List<Track> result = trackRecommendationService.getRecommendations(emptyGenres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);

        // Assert
        assertThat(result).isEmpty();
        verifyNoInteractions(recommendationService);
    }

    @Test
    void givenSpotifyWebApiException_whenGetRecommendations_thenThrowsPlaylistViewerNextException() throws Exception {
        // Arrange
        when(recommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues))
                .thenThrow(new RuntimeException(new SpotifyWebApiException("API Error")));

        // Act & Assert
        assertThrows(PlaylistViewerNextException.class, () ->
                trackRecommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues)
        );
        verify(recommendationService).getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);
    }

    @Test
    void givenIOException_whenGetRecommendations_thenThrowsPlaylistViewerNextException() throws Exception {
        // Arrange
        when(recommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues))
                .thenThrow(new RuntimeException(new IOException("IO Error")));

        // Act & Assert
        assertThrows(PlaylistViewerNextException.class, () ->
                trackRecommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues)
        );
        verify(recommendationService).getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);
    }
}
