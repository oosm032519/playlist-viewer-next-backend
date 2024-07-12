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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    void getGenreCountsForPlaylist_ReturnsGenreCountsSuccessfully() throws IOException, ParseException, SpotifyWebApiException, PlaylistAnalyticsException {
        // Arrange
        String playlistId = "testPlaylistId";
        Map<String, Integer> expectedGenreCounts = Map.of("pop", 2, "rock", 1);

        // モックの振る舞いを設定
        when(analyticsService.getGenreCountsForPlaylist(playlistId)).thenReturn(expectedGenreCounts);

        // Act
        Map<String, Integer> actualGenreCounts = playlistAnalyticsServiceWrapper.getGenreCountsForPlaylist(playlistId);

        // Assert
        assertThat(actualGenreCounts).isEqualTo(expectedGenreCounts);

        // モックが正しく呼び出されたかを検証
        verify(analyticsService).getGenreCountsForPlaylist(playlistId);
    }

    @Test
    void getGenreCountsForPlaylist_ThrowsPlaylistAnalyticsException() throws IOException, ParseException, SpotifyWebApiException {
        // Arrange
        String playlistId = "testPlaylistId";
        when(analyticsService.getGenreCountsForPlaylist(playlistId)).thenThrow(new IOException("Test IOException"));

        // Act & Assert
        assertThatThrownBy(() -> playlistAnalyticsServiceWrapper.getGenreCountsForPlaylist(playlistId))
                .isInstanceOf(PlaylistAnalyticsException.class)
                .hasMessageContaining("Failed to get genre counts for playlist: " + playlistId);

        // モックが正しく呼び出されたかを検証
        verify(analyticsService).getGenreCountsForPlaylist(playlistId);
    }

    @Test
    void getTop5GenresForPlaylist_ReturnsTop5GenresSuccessfully() throws IOException, ParseException, SpotifyWebApiException, PlaylistAnalyticsException {
        // Arrange
        String playlistId = "testPlaylistId";
        List<String> expectedTop5Genres = List.of("pop", "rock");

        // モックの振る舞いを設定
        when(analyticsService.getTop5GenresForPlaylist(playlistId)).thenReturn(expectedTop5Genres);

        // Act
        List<String> actualTop5Genres = playlistAnalyticsServiceWrapper.getTop5GenresForPlaylist(playlistId);

        // Assert
        assertThat(actualTop5Genres).isEqualTo(expectedTop5Genres);

        // モックが正しく呼び出されたかを検証
        verify(analyticsService).getTop5GenresForPlaylist(playlistId);
    }

    @Test
    void getTop5GenresForPlaylist_ThrowsPlaylistAnalyticsException() throws IOException, ParseException, SpotifyWebApiException {
        // Arrange
        String playlistId = "testPlaylistId";
        when(analyticsService.getTop5GenresForPlaylist(playlistId)).thenThrow(new ParseException("Test ParseException"));

        // Act & Assert
        assertThatThrownBy(() -> playlistAnalyticsServiceWrapper.getTop5GenresForPlaylist(playlistId))
                .isInstanceOf(PlaylistAnalyticsException.class)
                .hasMessageContaining("Failed to get top 5 genres for playlist: " + playlistId);

        // モックが正しく呼び出されたかを検証
        verify(analyticsService).getTop5GenresForPlaylist(playlistId);
    }
}
