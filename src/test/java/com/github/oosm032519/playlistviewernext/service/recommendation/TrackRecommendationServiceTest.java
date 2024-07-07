package com.github.oosm032519.playlistviewernext.service.recommendation;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackRecommendationServiceTest {

    @Mock
    private SpotifyRecommendationService recommendationService;

    @InjectMocks
    private TrackRecommendationService trackRecommendationServiceWrapper;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        // LoggerのモックをTrackRecommendationServiceに設定
        TrackRecommendationService.setLogger(logger);
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
        List<Track> result = trackRecommendationServiceWrapper.getRecommendations(top5Genres);

        // Then
        assertThat(result).isEqualTo(recommendations);
        verify(recommendationService).getRecommendations(top5Genres);
    }

    @Test
    void getRecommendations_ReturnsEmptyListWhenGenresAreEmpty() throws IOException, ParseException, SpotifyWebApiException {
        // Given
        List<String> top5Genres = Collections.emptyList();

        // When
        List<Track> result = trackRecommendationServiceWrapper.getRecommendations(top5Genres);

        // Then
        assertThat(result).isEmpty();
        verify(recommendationService, never()).getRecommendations(anyList());
    }

    @Test
    void getRecommendations_HandlesExceptionGracefully() throws IOException, ParseException, SpotifyWebApiException {
        // Given
        List<String> top5Genres = List.of("pop", "rock");

        when(recommendationService.getRecommendations(top5Genres)).thenThrow(new SpotifyWebApiException("API Error"));

        // When
        List<Track> result = trackRecommendationServiceWrapper.getRecommendations(top5Genres);

        // Then
        assertThat(result).isEmpty();
        verify(recommendationService).getRecommendations(top5Genres);
        verify(logger).error(eq("TrackRecommendationService: Spotify APIの呼び出し中にエラーが発生しました。"), any(SpotifyWebApiException.class));
    }

    @Test
    void getRecommendations_HandlesIOExceptionGracefully() throws IOException, ParseException, SpotifyWebApiException {
        // Given
        List<String> top5Genres = List.of("pop", "rock");

        when(recommendationService.getRecommendations(top5Genres)).thenThrow(new IOException("IO Error"));

        // When
        List<Track> result = trackRecommendationServiceWrapper.getRecommendations(top5Genres);

        // Then
        assertThat(result).isEmpty();
        verify(recommendationService).getRecommendations(top5Genres);
        verify(logger).error(eq("TrackRecommendationService: Spotify APIの呼び出し中にエラーが発生しました。"), any(IOException.class));
    }

    @Test
    void getRecommendations_HandlesParseExceptionGracefully() throws IOException, ParseException, SpotifyWebApiException {
        // Given
        List<String> top5Genres = List.of("pop", "rock");

        when(recommendationService.getRecommendations(top5Genres)).thenThrow(new ParseException("Parse Error"));

        // When
        List<Track> result = trackRecommendationServiceWrapper.getRecommendations(top5Genres);

        // Then
        assertThat(result).isEmpty();
        verify(recommendationService).getRecommendations(top5Genres);
        verify(logger).error(eq("TrackRecommendationService: Spotify APIの呼び出し中にエラーが発生しました。"), any(ParseException.class));
    }
}
