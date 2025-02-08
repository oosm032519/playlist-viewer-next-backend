package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaylistAnalyticsServiceTest {

    @Mock
    private SpotifyPlaylistAnalyticsService analyticsService;

    private static final String PLAYLIST_ID = "testPlaylistId";
    @InjectMocks
    private PlaylistAnalyticsService playlistAnalyticsService;

    /**
     * プレイリストIDを指定して、ジャンルごとの曲数が正常に取得できることを確認する。
     */
    @Test
    void getGenreCountsForPlaylist_Success() {
        // Arrange: テストデータの準備とモックの設定
        Map<String, Integer> expectedGenreCounts = new HashMap<>();
        expectedGenreCounts.put("Rock", 5);
        expectedGenreCounts.put("Pop", 3);
        when(analyticsService.getGenreCountsForPlaylist(PLAYLIST_ID)).thenReturn(expectedGenreCounts);

        // Act: テスト対象メソッドの実行
        Map<String, Integer> result = playlistAnalyticsService.getGenreCountsForPlaylist(PLAYLIST_ID);

        // Assert: 結果の検証
        assertThat(result).isEqualTo(expectedGenreCounts);
    }

    /**
     * プレイリストのジャンルごとの曲数取得時に例外が発生した場合、PlaylistViewerNextExceptionがスローされることを確認する。
     */
    @Test
    void getGenreCountsForPlaylist_ThrowsException() {
        // Arrange: モックの設定（例外をスロー）
        when(analyticsService.getGenreCountsForPlaylist(PLAYLIST_ID)).thenThrow(new RuntimeException("Test exception"));

        // Act & Assert: 例外がスローされることの確認
        assertThatThrownBy(() -> playlistAnalyticsService.getGenreCountsForPlaylist(PLAYLIST_ID))
                .isInstanceOf(PlaylistViewerNextException.class)
                .hasMessageContaining("プレイリストのジャンルごとの曲数の取得中にエラーが発生しました。")
                .satisfies(thrown -> {
                    PlaylistViewerNextException exception = (PlaylistViewerNextException) thrown;
                    assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }

    /**
     * プレイリストIDを指定して、上位5つのジャンルが正常に取得できることを確認する。
     */
    @Test
    void getTop5GenresForPlaylist_Success() {
        // Arrange: テストデータの準備とモックの設定
        List<String> expectedTopGenres = Arrays.asList("Rock", "Pop", "Jazz", "Blues", "Classical");
        when(analyticsService.getTop5GenresForPlaylist(PLAYLIST_ID)).thenReturn(expectedTopGenres);

        // Act: テスト対象メソッドの実行
        List<String> result = playlistAnalyticsService.getTop5GenresForPlaylist(PLAYLIST_ID);

        // Assert: 結果の検証
        assertThat(result).isEqualTo(expectedTopGenres);
    }

    /**
     * プレイリストの上位5つのジャンル取得時に例外が発生した場合、PlaylistViewerNextExceptionがスローされることを確認する。
     */
    @Test
    void getTop5GenresForPlaylist_ThrowsException() {
        // Arrange: モックの設定（例外をスロー）
        when(analyticsService.getTop5GenresForPlaylist(PLAYLIST_ID)).thenThrow(new RuntimeException("Test exception"));

        // Act & Assert: 例外がスローされることの確認
        assertThatThrownBy(() -> playlistAnalyticsService.getTop5GenresForPlaylist(PLAYLIST_ID))
                .isInstanceOf(PlaylistViewerNextException.class)
                .hasMessageContaining("プレイリストのトップ5ジャンルの取得中にエラーが発生しました。")
                .satisfies(thrown -> {
                    PlaylistViewerNextException exception = (PlaylistViewerNextException) thrown;
                    assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                });
    }
}
