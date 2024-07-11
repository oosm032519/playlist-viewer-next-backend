// PlaylistAnalyticsServiceTest.java

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

/**
 * PlaylistAnalyticsServiceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
class PlaylistAnalyticsServiceTest {

    /**
     * SpotifyPlaylistAnalyticsServiceのモックオブジェクト
     */
    @Mock
    private SpotifyPlaylistAnalyticsService analyticsService;

    /**
     * テスト対象のPlaylistAnalyticsServiceのインスタンス
     */
    @InjectMocks
    private PlaylistAnalyticsService playlistAnalyticsServiceWrapper;

    /**
     * 各テストメソッドの前に実行される設定メソッド
     */
    @BeforeEach
    void setUp() {
        // 各テストメソッドの前に実行される設定
    }

    /**
     * getGenreCountsForPlaylistメソッドのテスト
     *
     * @throws IOException            入出力例外
     * @throws ParseException         パース例外
     * @throws SpotifyWebApiException Spotify API例外
     */
    @Test
    void getGenreCountsForPlaylist_ReturnsGenreCountsSuccessfully() throws IOException, ParseException, SpotifyWebApiException {
        // Given: テストの前提条件を設定
        String playlistId = "testPlaylistId";
        Map<String, Integer> genreCounts = Map.of("pop", 2, "rock", 1);

        // モックの振る舞いを設定
        when(analyticsService.getGenreCountsForPlaylist(playlistId)).thenReturn(genreCounts);

        // When: テスト対象メソッドを実行
        Map<String, Integer> result = playlistAnalyticsServiceWrapper.getGenreCountsForPlaylist(playlistId);

        // Then: 結果を検証
        assertThat(result).isEqualTo(genreCounts);

        // モックが正しく呼び出されたかを検証
        verify(analyticsService).getGenreCountsForPlaylist(playlistId);
    }

    /**
     * getTop5GenresForPlaylistメソッドのテスト
     *
     * @throws IOException            入出力例外
     * @throws ParseException         パース例外
     * @throws SpotifyWebApiException Spotify API例外
     */
    @Test
    void getTop5GenresForPlaylist_ReturnsTop5GenresSuccessfully() throws IOException, ParseException, SpotifyWebApiException {
        // Given: テストの前提条件を設定
        String playlistId = "testPlaylistId";
        List<String> top5Genres = List.of("pop", "rock");

        // モックの振る舞いを設定
        when(analyticsService.getTop5GenresForPlaylist(playlistId)).thenReturn(top5Genres);

        // When: テスト対象メソッドを実行
        List<String> result = playlistAnalyticsServiceWrapper.getTop5GenresForPlaylist(playlistId);

        // Then: 結果を検証
        assertThat(result).isEqualTo(top5Genres);

        // モックが正しく呼び出されたかを検証
        verify(analyticsService).getTop5GenresForPlaylist(playlistId);
    }
}
