// GenreAggregatorServiceTest.java

package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyArtistService;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;

import static org.mockito.Mockito.lenient;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * GenreAggregatorServiceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
class GenreAggregatorServiceTest {

    @Mock
    private SpotifyArtistService artistService;

    @InjectMocks
    private GenreAggregatorService genreAggregatorService;

    /**
     * ジャンルを集計し、ソートされたジャンルのカウントを返すテスト
     */
    @Test
    void aggregateGenres_ShouldReturnSortedGenreCounts() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        PlaylistTrack[] playlistTracks = createMockPlaylistTracks();
        when(artistService.getArtistGenres("artist1")).thenReturn(Arrays.asList("rock", "pop"));
        when(artistService.getArtistGenres("artist2")).thenReturn(Arrays.asList("rock", "jazz"));

        // Act
        Map<String, Integer> result = genreAggregatorService.aggregateGenres(playlistTracks);

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
    void aggregateGenres_ShouldHandleEmptyPlaylist() {
        // Arrange
        PlaylistTrack[] playlistTracks = new PlaylistTrack[0];

        // Act
        Map<String, Integer> result = genreAggregatorService.aggregateGenres(playlistTracks);

        // Assert
        assertThat(result).isEmpty();
    }

    /**
     * ジャンルがないアーティストを処理するテスト
     */
    @Test
    void aggregateGenres_ShouldHandleArtistsWithNoGenres() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        PlaylistTrack[] playlistTracks = createMockPlaylistTracks();
        when(artistService.getArtistGenres("artist1")).thenReturn(Collections.emptyList());
        when(artistService.getArtistGenres("artist2")).thenReturn(Collections.emptyList());

        // Act
        Map<String, Integer> result = genreAggregatorService.aggregateGenres(playlistTracks);

        // Assert
        assertThat(result).isEmpty();
    }

    /**
     * アーティストがいないトラックを処理するテスト
     */
    @Test
    void aggregateGenres_ShouldHandleTracksWithNoArtists() {
        // Arrange
        PlaylistTrack[] playlistTracks = createMockPlaylistTracksWithNoArtists();

        // Act
        Map<String, Integer> result = genreAggregatorService.aggregateGenres(playlistTracks);

        // Assert
        assertThat(result).isEmpty();
    }

    /**
     * 例外を処理するテスト
     */
    @Test
    void aggregateGenres_ShouldHandleException() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        PlaylistTrack[] playlistTracks = createMockPlaylistTracks();
        when(artistService.getArtistGenres("artist1")).thenThrow(new IOException("Test Exception"));

        // Act & Assert
        try {
            genreAggregatorService.aggregateGenres(playlistTracks);
        } catch (RuntimeException e) {
            assertThat(e).hasMessageContaining("Test Exception");
        }
    }

    /**
     * SpotifyWebApiExceptionを処理するテスト
     */
    @Test
    void aggregateGenres_ShouldHandleSpotifyWebApiException() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        PlaylistTrack[] playlistTracks = createMockPlaylistTracks();
        when(artistService.getArtistGenres("artist1")).thenThrow(new SpotifyWebApiException("Test Exception"));

        // Act & Assert
        try {
            genreAggregatorService.aggregateGenres(playlistTracks);
        } catch (RuntimeException e) {
            assertThat(e).hasMessageContaining("Test Exception");
        }
    }

    /**
     * ParseExceptionを処理するテスト
     */
    @Test
    void aggregateGenres_ShouldHandleParseException() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        PlaylistTrack[] playlistTracks = createMockPlaylistTracks();
        when(artistService.getArtistGenres("artist1")).thenThrow(new ParseException("Test Exception"));

        // Act & Assert
        try {
            genreAggregatorService.aggregateGenres(playlistTracks);
        } catch (RuntimeException e) {
            assertThat(e).hasMessageContaining("Test Exception");
        }
    }

    /**
     * nullのPlaylistTrackを処理するテスト
     */
    @Test
    void aggregateGenres_ShouldHandleNullPlaylistTrack() {
        // Arrange
        PlaylistTrack[] playlistTracks = new PlaylistTrack[]{null};

        // Act
        Map<String, Integer> result = genreAggregatorService.aggregateGenres(playlistTracks);

        // Assert
        assertThat(result).isEmpty();
    }

    /**
     * nullのTrackを処理するテスト
     */
    @Test
    void aggregateGenres_ShouldHandleNullTrack() {
        // Arrange
        PlaylistTrack track = mock(PlaylistTrack.class);
        when(track.getTrack()).thenReturn(null);
        PlaylistTrack[] playlistTracks = new PlaylistTrack[]{track};

        // Act
        Map<String, Integer> result = genreAggregatorService.aggregateGenres(playlistTracks);

        // Assert
        assertThat(result).isEmpty();
    }

    /**
     * 重複するジャンルを処理するテスト
     */
    @Test
    void aggregateGenres_ShouldHandleDuplicateGenres() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        PlaylistTrack[] playlistTracks = createMockPlaylistTracks();
        when(artistService.getArtistGenres("artist1")).thenReturn(Arrays.asList("rock", "pop"));
        when(artistService.getArtistGenres("artist2")).thenReturn(Arrays.asList("rock", "pop"));

        // Act
        Map<String, Integer> result = genreAggregatorService.aggregateGenres(playlistTracks);

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).containsOnlyKeys("rock", "pop");
        assertThat(result).containsEntry("rock", 2);
        assertThat(result).containsEntry("pop", 2);
    }

    /**
     * 上位のジャンルを返すテスト
     */
    @Test
    void getTopGenres_ShouldReturnTopGenres() {
        // Arrange
        Map<String, Integer> genreCounts = new LinkedHashMap<>();
        genreCounts.put("rock", 10);
        genreCounts.put("pop", 8);
        genreCounts.put("jazz", 6);
        genreCounts.put("blues", 4);
        genreCounts.put("classical", 2);
        genreCounts.put("country", 1);

        // Act
        List<String> result = genreAggregatorService.getTopGenres(genreCounts, 5);

        // Assert
        assertThat(result).hasSize(5);
        assertThat(result).containsExactly("rock", "pop", "jazz", "blues", "classical");
    }

    /**
     * 5つ未満のジャンルを処理するテスト
     */
    @Test
    void getTopGenres_ShouldHandleLessThan5Genres() {
        // Arrange
        Map<String, Integer> genreCounts = new LinkedHashMap<>();
        genreCounts.put("rock", 10);
        genreCounts.put("pop", 8);
        genreCounts.put("jazz", 6);

        // Act
        List<String> result = genreAggregatorService.getTopGenres(genreCounts, 5);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("rock", "pop", "jazz");
    }

    /**
     * 制限がジャンル数より多い場合の処理をテスト
     */
    @Test
    void getTopGenres_ShouldHandleLimitGreaterThanGenreCount() {
        // Arrange
        Map<String, Integer> genreCounts = new LinkedHashMap<>();
        genreCounts.put("rock", 10);
        genreCounts.put("pop", 8);

        // Act
        List<String> result = genreAggregatorService.getTopGenres(genreCounts, 5);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly("rock", "pop");
    }

    /**
     * 制限がゼロの場合の処理をテスト
     */
    @Test
    void getTopGenres_ShouldHandleZeroLimit() {
        // Arrange
        Map<String, Integer> genreCounts = new LinkedHashMap<>();
        genreCounts.put("rock", 10);
        genreCounts.put("pop", 8);

        // Act
        List<String> result = genreAggregatorService.getTopGenres(genreCounts, 0);

        // Assert
        assertThat(result).isEmpty();
    }

    /**
     * モックのPlaylistTrack配列を作成するヘルパーメソッド
     */
    private PlaylistTrack[] createMockPlaylistTracks() {
        PlaylistTrack track1 = mock(PlaylistTrack.class);
        PlaylistTrack track2 = mock(PlaylistTrack.class);
        Track fullTrack1 = mock(Track.class);
        Track fullTrack2 = mock(Track.class);
        ArtistSimplified artist1 = mock(ArtistSimplified.class);
        ArtistSimplified artist2 = mock(ArtistSimplified.class);

        lenient().when(track1.getTrack()).thenReturn(fullTrack1);
        lenient().when(track2.getTrack()).thenReturn(fullTrack2);
        lenient().when(fullTrack1.getArtists()).thenReturn(new ArtistSimplified[]{artist1});
        lenient().when(fullTrack2.getArtists()).thenReturn(new ArtistSimplified[]{artist2});
        lenient().when(artist1.getId()).thenReturn("artist1");
        lenient().when(artist2.getId()).thenReturn("artist2");

        return new PlaylistTrack[]{track1, track2};
    }

    /**
     * アーティストがいないモックのPlaylistTrack配列を作成するヘルパーメソッド
     */
    private PlaylistTrack[] createMockPlaylistTracksWithNoArtists() {
        PlaylistTrack track1 = mock(PlaylistTrack.class);
        PlaylistTrack track2 = mock(PlaylistTrack.class);
        Track fullTrack1 = mock(Track.class);
        Track fullTrack2 = mock(Track.class);

        when(track1.getTrack()).thenReturn(fullTrack1);
        when(track2.getTrack()).thenReturn(fullTrack2);
        when(fullTrack1.getArtists()).thenReturn(new ArtistSimplified[]{});
        when(fullTrack2.getArtists()).thenReturn(new ArtistSimplified[]{});

        return new PlaylistTrack[]{track1, track2};
    }
}
