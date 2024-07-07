package com.github.oosm032519.playlistviewernext.service;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private SpotifyRecommendationService recommendationService;

    @InjectMocks
    private RecommendationService recommendationServiceWrapper;

    @BeforeEach
    void setUp() {
        // 各テストメソッドの前に実行される設定
    }

    @Test
    void getRecommendations_ReturnsRecommendationsSuccessfully() throws IOException, ParseException, SpotifyWebApiException {
        // Given
        List<String> top5Genres = List.of("pop", "rock");
        List<Track> recommendations = List.of(
                new Track.Builder().setName("Recommended Track 1").build(),
                new Track.Builder().setName("Recommended Track 2").build()
        );

        when(recommendationService.getRecommendations(top5Genres)).thenReturn(recommendations);

        // When
        List<Track> result = recommendationServiceWrapper.getRecommendations(top5Genres);

        // Then
        assertThat(result).isEqualTo(recommendations);

        verify(recommendationService).getRecommendations(top5Genres);
    }
}
