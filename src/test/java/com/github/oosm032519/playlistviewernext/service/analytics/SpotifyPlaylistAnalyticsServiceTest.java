// SpotifyPlaylistAnalyticsServiceTest.java

package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistDetailsService;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
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
     * プレイリストのジャンルカウントを取得し、ソートされた結果を返すテスト
     */
    @Test
    void getGenreCountsForPlaylist_ShouldReturnSortedGenreCounts() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "testPlaylistId";
        PlaylistTrack[] playlistTracks = createMockPlaylistTracks();
        when(playlistDetailsService.getPlaylistTracks(playlistId)).thenReturn(playlistTracks);
        Map<String, Integer> genreCounts = new LinkedHashMap<>();
        genreCounts.put("rock", 2);
        genreCounts.put("pop", 1);
        genreCounts.put("jazz", 1);
        when(genreAggregatorService.aggregateGenres(playlistTracks)).thenReturn(genreCounts);

        // Act
        Map<String, Integer> result = spotifyPlaylistAnalyticsService.getGenreCountsForPlaylist(playlistId);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).containsOnlyKeys("rock", "pop", "jazz");
        assertThat(result).containsEntry("rock", 2);
        assertThat(result).containsEntry("pop", 1);
        assertThat(result).containsEntry("jazz", 1);

        // ソートの検証
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(result.entrySet());
        sortedEntries.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        assertThat(new ArrayList<>(result.entrySet())).isEqualTo(sortedEntries);
    }

    /**
     * 空のプレイリストを処理するテスト
     */
    @Test
    void getGenreCountsForPlaylist_ShouldHandleEmptyPlaylist() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "emptyPlaylistId";
        when(playlistDetailsService.getPlaylistTracks(playlistId)).thenReturn(new PlaylistTrack[0]);
        when(genreAggregatorService.aggregateGenres(new PlaylistTrack[0])).thenReturn(Collections.emptyMap());

        // Act
        Map<String, Integer> result = spotifyPlaylistAnalyticsService.getGenreCountsForPlaylist(playlistId);

        // Assert
        assertThat(result).isEmpty();
    }

    /**
     * 例外発生時の処理をテスト
     */
    @Test
    void getGenreCountsForPlaylist_ShouldHandleException() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "errorPlaylistId";
        when(playlistDetailsService.getPlaylistTracks(playlistId)).thenThrow(new IOException("API error"));

        // Act & Assert
        assertThatThrownBy(() -> spotifyPlaylistAnalyticsService.getGenreCountsForPlaylist(playlistId))
                .isInstanceOf(IOException.class)
                .hasMessage("API error");
    }

    /**
     * トラックがnullの場合の処理をテスト
     */
    @Test
    void getGenreCountsForPlaylist_ShouldHandleNullTracks() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "nullTracksPlaylistId";
        when(playlistDetailsService.getPlaylistTracks(playlistId)).thenReturn(null);

        // Act
        Map<String, Integer> result = spotifyPlaylistAnalyticsService.getGenreCountsForPlaylist(playlistId);

        // Assert
        assertThat(result).isEmpty();
    }

    /**
     * プレイリストのトップ5ジャンルを取得するテスト
     */
    @Test
    void getTop5GenresForPlaylist_ShouldReturnTop5Genres() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
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

        // Act
        List<String> result = spyService.getTop5GenresForPlaylist(playlistId);

        // Assert
        assertThat(result).hasSize(5);
        assertThat(result).containsExactly("rock", "pop", "jazz", "blues", "classical");
    }

    /**
     * ジャンルが5つ未満の場合の処理をテスト
     */
    @Test
    void getTop5GenresForPlaylist_ShouldHandleLessThan5Genres() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "fewGenresPlaylistId";
        Map<String, Integer> genreCounts = new LinkedHashMap<>();
        genreCounts.put("rock", 10);
        genreCounts.put("pop", 8);
        genreCounts.put("jazz", 6);

        SpotifyPlaylistAnalyticsService spyService = spy(spotifyPlaylistAnalyticsService);
        doReturn(genreCounts).when(spyService).getGenreCountsForPlaylist(playlistId);
        when(genreAggregatorService.getTopGenres(genreCounts, 5)).thenReturn(Arrays.asList("rock", "pop", "jazz"));

        // Act
        List<String> result = spyService.getTop5GenresForPlaylist(playlistId);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("rock", "pop", "jazz");
    }

    /**
     * 空のプレイリストを処理するテスト
     */
    @Test
    void getTop5GenresForPlaylist_ShouldHandleEmptyPlaylist() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "emptyPlaylistId";
        when(playlistDetailsService.getPlaylistTracks(playlistId)).thenReturn(new PlaylistTrack[0]);
        when(genreAggregatorService.aggregateGenres(new PlaylistTrack[0])).thenReturn(Collections.emptyMap());

        // Act
        List<String> result = spotifyPlaylistAnalyticsService.getTop5GenresForPlaylist(playlistId);

        // Assert
        assertThat(result).isEmpty();
    }

    /**
     * 例外発生時の処理をテスト
     */
    @Test
    void getTop5GenresForPlaylist_ShouldHandleException() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "errorPlaylistId";
        when(playlistDetailsService.getPlaylistTracks(playlistId)).thenThrow(new IOException("API error"));

        // Act & Assert
        assertThatThrownBy(() -> spotifyPlaylistAnalyticsService.getTop5GenresForPlaylist(playlistId))
                .isInstanceOf(IOException.class)
                .hasMessage("API error");
    }

    /**
     * モックのプレイリストトラックを作成するヘルパーメソッド
     *
     * @return モックのPlaylistTrack配列
     */
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
