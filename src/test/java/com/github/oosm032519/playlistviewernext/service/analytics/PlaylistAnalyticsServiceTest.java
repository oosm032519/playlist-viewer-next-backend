package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
class PlaylistAnalyticsServiceTest {

    @Mock
    private SpotifyPlaylistAnalyticsService analyticsService;

    private static final String PLAYLIST_ID = "testPlaylistId";
    @InjectMocks
    private PlaylistAnalyticsService playlistAnalyticsService;

    @BeforeEach
    void setUp() {
        // テストごとにモックをリセット
    }

    @Test
    void getGenreCountsForPlaylist_Success() {
        // Arrange
        Map<String, Integer> expectedGenreCounts = new HashMap<>();
        expectedGenreCounts.put("Rock", 5);
        expectedGenreCounts.put("Pop", 3);
        when(analyticsService.getGenreCountsForPlaylist(PLAYLIST_ID)).thenReturn(expectedGenreCounts);

        // Act
        Map<String, Integer> result = playlistAnalyticsService.getGenreCountsForPlaylist(PLAYLIST_ID);

        // Assert
        assertThat(result).isEqualTo(expectedGenreCounts);
    }

    @Test
    void getGenreCountsForPlaylist_ThrowsException() {
        // Arrange
        when(analyticsService.getGenreCountsForPlaylist(PLAYLIST_ID)).thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        assertThatThrownBy(() -> playlistAnalyticsService.getGenreCountsForPlaylist(PLAYLIST_ID))
                .isInstanceOf(PlaylistViewerNextException.class)
                .hasMessageContaining("プレイリストのジャンルごとの曲数の取得中にエラーが発生しました。")
                .satisfies(thrown -> {
                    PlaylistViewerNextException exception = (PlaylistViewerNextException) thrown;
                    assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    @Test
    void getTop5GenresForPlaylist_Success() {
        // Arrange
        List<String> expectedTopGenres = Arrays.asList("Rock", "Pop", "Jazz", "Blues", "Classical");
        when(analyticsService.getTop5GenresForPlaylist(PLAYLIST_ID)).thenReturn(expectedTopGenres);

        // Act
        List<String> result = playlistAnalyticsService.getTop5GenresForPlaylist(PLAYLIST_ID);

        // Assert
        assertThat(result).isEqualTo(expectedTopGenres);
    }

    @Test
    void getTop5GenresForPlaylist_ThrowsException() {
        // Arrange
        when(analyticsService.getTop5GenresForPlaylist(PLAYLIST_ID)).thenThrow(new RuntimeException("Test exception"));

        // Act & Assert
        assertThatThrownBy(() -> playlistAnalyticsService.getTop5GenresForPlaylist(PLAYLIST_ID))
                .isInstanceOf(PlaylistViewerNextException.class)
                .hasMessageContaining("プレイリストのトップ5ジャンルの取得中にエラーが発生しました。")
                .satisfies(thrown -> {
                    PlaylistViewerNextException exception = (PlaylistViewerNextException) thrown;
                    assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }
}
