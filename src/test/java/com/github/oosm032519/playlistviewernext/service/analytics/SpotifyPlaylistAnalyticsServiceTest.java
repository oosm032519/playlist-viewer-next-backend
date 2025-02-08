package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.InvalidRequestException;
import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SpotifyPlaylistAnalyticsServiceTest {

    @Mock
    private SpotifyPlaylistDetailsService playlistDetailsService;

    @Mock
    private GenreAggregatorService genreAggregatorService;

    @InjectMocks
    private SpotifyPlaylistAnalyticsService spotifyPlaylistAnalyticsService;

    /**
     * プレイリストのジャンルカウントが正常に取得・集計され、出現回数で降順にソートされたマップが返されることを確認する。
     */
    @Test
    void getGenreCountsForPlaylist_ShouldReturnSortedGenreCounts() throws PlaylistViewerNextException, SpotifyWebApiException {
        // Arrange: テストデータの準備とモックの設定
        String playlistId = "testPlaylistId";
        PlaylistTrack[] playlistTracks = createMockPlaylistTracks();
        when(playlistDetailsService.getPlaylistTracks(playlistId)).thenReturn(playlistTracks);
        Map<String, Integer> genreCounts = new LinkedHashMap<>();
        genreCounts.put("rock", 2);
        genreCounts.put("pop", 1);
        genreCounts.put("jazz", 1);
        when(genreAggregatorService.aggregateGenres(playlistTracks)).thenReturn(genreCounts);

        // Act: テスト対象メソッドの実行
        Map<String, Integer> result = spotifyPlaylistAnalyticsService.getGenreCountsForPlaylist(playlistId);

        // Assert: 結果の検証
        assertThat(result).isNotEmpty()
                .containsOnlyKeys("rock", "pop", "jazz")
                .containsEntry("rock", 2)
                .containsEntry("pop", 1)
                .containsEntry("jazz", 1);

        // ソートの検証
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(result.entrySet());
        sortedEntries.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        assertThat(new ArrayList<>(result.entrySet())).isEqualTo(sortedEntries);
    }

    /**
     * 空のプレイリストが与えられた場合、空のジャンルカウントマップが返されることを確認する。
     */
    @Test
    void getGenreCountsForPlaylist_ShouldHandleEmptyPlaylist() throws PlaylistViewerNextException, SpotifyWebApiException {
        // Arrange: 空のプレイリストに対するモックの設定
        String playlistId = "emptyPlaylistId";
        when(playlistDetailsService.getPlaylistTracks(playlistId)).thenReturn(new PlaylistTrack[0]);
        when(genreAggregatorService.aggregateGenres(new PlaylistTrack[0])).thenReturn(Collections.emptyMap());

        // Act: テスト対象メソッドの実行
        Map<String, Integer> result = spotifyPlaylistAnalyticsService.getGenreCountsForPlaylist(playlistId);

        // Assert: 結果の検証
        assertThat(result).isEmpty();
    }

    /**
     * プレイリストのトラック取得時に例外が発生した場合、InvalidRequestExceptionがスローされることを確認する。
     */
    @Test
    void getGenreCountsForPlaylist_ShouldHandleException() throws PlaylistViewerNextException, SpotifyWebApiException {
        // Arrange: 例外をスローするモックの設定
        String playlistId = "errorPlaylistId";
        when(playlistDetailsService.getPlaylistTracks(playlistId))
                .thenThrow(new InvalidRequestException(HttpStatus.INTERNAL_SERVER_ERROR, "API error"));

        // Act & Assert: 例外がスローされることの確認
        assertThatThrownBy(() -> spotifyPlaylistAnalyticsService.getGenreCountsForPlaylist(playlistId))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessage("プレイリストのジャンルごとのトラック数の取得中にエラーが発生しました。");
    }

    /**
     * プレイリストのトラックがnullの場合、空のジャンルカウントマップが返されることを確認する。
     */
    @Test
    void getGenreCountsForPlaylist_ShouldHandleNullTracks() throws PlaylistViewerNextException, SpotifyWebApiException {
        // Arrange: nullのトラックを返すモックの設定
        String playlistId = "nullTracksPlaylistId";
        when(playlistDetailsService.getPlaylistTracks(playlistId)).thenReturn(null);

        // Act: テスト対象メソッドの実行
        Map<String, Integer> result = spotifyPlaylistAnalyticsService.getGenreCountsForPlaylist(playlistId);

        // Assert: 結果の検証
        assertThat(result).isEmpty();
    }

    /**
     * プレイリストの上位5ジャンルが正常に取得できることを確認する。
     */
    @Test
    void getTop5GenresForPlaylist_ShouldReturnTop5Genres() throws PlaylistViewerNextException {
        // Arrange: テストデータの準備とモックの設定
        String playlistId = "testPlaylistId";
        Map<String, Integer> genreCounts = new LinkedHashMap<>();
        genreCounts.put("rock", 10);
        genreCounts.put("pop", 8);
        genreCounts.put("jazz", 6);
        genreCounts.put("blues", 4);
        genreCounts.put("classical", 2);
        genreCounts.put("country", 1);

        SpotifyPlaylistAnalyticsService spyService = spy(spotifyPlaylistAnalyticsService);
        doReturn(genreCounts).when(spyService).getGenreCountsForPlaylist(playlistId);
        when(genreAggregatorService.getTopGenres(genreCounts, 5)).thenReturn(Arrays.asList("rock", "pop", "jazz", "blues", "classical"));

        // Act: テスト対象メソッドの実行
        List<String> result = spyService.getTop5GenresForPlaylist(playlistId);

        // Assert: 結果の検証
        assertThat(result).hasSize(5)
                .containsExactly("rock", "pop", "jazz", "blues", "classical");
    }

    /**
     * プレイリストのジャンル数が5未満の場合、存在するジャンルのみが返されることを確認する。
     */
    @Test
    void getTop5GenresForPlaylist_ShouldHandleLessThan5Genres() throws PlaylistViewerNextException {
        // Arrange: テストデータの準備とモックの設定
        String playlistId = "fewGenresPlaylistId";
        Map<String, Integer> genreCounts = new LinkedHashMap<>();
        genreCounts.put("rock", 10);
        genreCounts.put("pop", 8);
        genreCounts.put("jazz", 6);

        SpotifyPlaylistAnalyticsService spyService = spy(spotifyPlaylistAnalyticsService);
        doReturn(genreCounts).when(spyService).getGenreCountsForPlaylist(playlistId);
        when(genreAggregatorService.getTopGenres(genreCounts, 5)).thenReturn(Arrays.asList("rock", "pop", "jazz"));

        // Act: テスト対象メソッドの実行
        List<String> result = spyService.getTop5GenresForPlaylist(playlistId);

        // Assert: 結果の検証
        assertThat(result).hasSize(3)
                .containsExactly("rock", "pop", "jazz");
    }

    /**
     * 空のプレイリストが与えられた場合、上位5ジャンルとして空のリストが返されることを確認する。
     */
    @Test
    void getTop5GenresForPlaylist_ShouldHandleEmptyPlaylist() throws PlaylistViewerNextException, SpotifyWebApiException {
        // Arrange: 空のプレイリストに対するモックの設定
        String playlistId = "emptyPlaylistId";
        when(playlistDetailsService.getPlaylistTracks(playlistId)).thenReturn(new PlaylistTrack[0]);
        when(genreAggregatorService.aggregateGenres(new PlaylistTrack[0])).thenReturn(Collections.emptyMap());

        // Act: テスト対象メソッドの実行
        List<String> result = spotifyPlaylistAnalyticsService.getTop5GenresForPlaylist(playlistId);

        // Assert: 結果の検証
        assertThat(result).isEmpty();
    }

    /**
     * プレイリストの上位5ジャンル取得時に例外が発生した場合、PlaylistViewerNextExceptionがスローされることを確認する。
     */
    @Test
    void getTop5GenresForPlaylist_ShouldHandleException() throws PlaylistViewerNextException, SpotifyWebApiException {
        // Arrange: 例外をスローするモックの設定
        String playlistId = "errorPlaylistId";
        when(playlistDetailsService.getPlaylistTracks(playlistId))
                .thenThrow(new InvalidRequestException(HttpStatus.INTERNAL_SERVER_ERROR, "API error"));

        // Act & Assert: 例外がスローされることの確認
        assertThatThrownBy(() -> spotifyPlaylistAnalyticsService.getTop5GenresForPlaylist(playlistId))
                .isInstanceOf(PlaylistViewerNextException.class)
                .hasMessage("プレイリストのジャンル出現頻度上位5つの取得中にエラーが発生しました。");
    }

    private PlaylistTrack[] createMockPlaylistTracks() {
        PlaylistTrack track1 = mock(PlaylistTrack.class);
        PlaylistTrack track2 = mock(PlaylistTrack.class);
        Track fullTrack1 = mock(Track.class);
        Track fullTrack2 = mock(Track.class);
        ArtistSimplified artist1 = mock(ArtistSimplified.class);
        ArtistSimplified artist2 = mock(ArtistSimplified.class);

        when(track1.getTrack()).thenReturn(fullTrack1);
        when(track2.getTrack()).thenReturn(fullTrack2);
        when(fullTrack1.getArtists()).thenReturn(new ArtistSimplified[]{artist1});
        when(fullTrack2.getArtists()).thenReturn(new ArtistSimplified[]{artist2});
        when(artist1.getId()).thenReturn("artist1");
        when(artist2.getId()).thenReturn("artist2");

        return new PlaylistTrack[]{track1, track2};
    }
}
