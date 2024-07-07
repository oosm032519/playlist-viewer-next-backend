package com.github.oosm032519.playlistviewernext.service.analytics;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistAnalyticsServiceTest {

    @Mock
    private SpotifyPlaylistAnalyticsService analyticsService;

    @InjectMocks
    private PlaylistAnalyticsService playlistAnalyticsServiceWrapper;

    @BeforeEach
    void setUp() {
        // 各テストメソッドの前に実行される設定
    }

    @Test
    void getGenreCountsForPlaylist_ReturnsGenreCountsSuccessfully() throws IOException, ParseException, SpotifyWebApiException {
        // Given
        String playlistId = "testPlaylistId";
        Map<String, Integer> genreCounts = Map.of("pop", 2, "rock", 1);

        when(analyticsService.getGenreCountsForPlaylist(playlistId)).thenReturn(genreCounts);

        // When
        Map<String, Integer> result = playlistAnalyticsServiceWrapper.getGenreCountsForPlaylist(playlistId);

        // Then
        assertThat(result).isEqualTo(genreCounts);

        verify(analyticsService).getGenreCountsForPlaylist(playlistId);
    }

    @Test
    void getTop5GenresForPlaylist_ReturnsTop5GenresSuccessfully() throws IOException, ParseException, SpotifyWebApiException {
        // Given
        String playlistId = "testPlaylistId";
        List<String> top5Genres = List.of("pop", "rock");

        when(analyticsService.getTop5GenresForPlaylist(playlistId)).thenReturn(top5Genres);

        // When
        List<String> result = playlistAnalyticsServiceWrapper.getTop5GenresForPlaylist(playlistId);

        // Then
        assertThat(result).isEqualTo(top5Genres);

        verify(analyticsService).getTop5GenresForPlaylist(playlistId);
    }
}
