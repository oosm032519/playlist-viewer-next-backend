package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyArtistService;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GenreAggregatorServiceTest {

    @Mock
    private SpotifyArtistService artistService;

    @InjectMocks
    private GenreAggregatorService genreAggregatorService;

    @Test
    void aggregateGenres_ShouldReturnSortedGenreCounts() throws Exception {
        PlaylistTrack[] playlistTracks = createMockPlaylistTracks();
        when(artistService.getArtistGenres("artist1")).thenReturn(Arrays.asList("rock", "pop"));
        when(artistService.getArtistGenres("artist2")).thenReturn(Arrays.asList("rock", "jazz"));

        Map<String, Integer> result = genreAggregatorService.aggregateGenres(playlistTracks);

        assertThat(result).isNotEmpty();
        assertThat(result).containsOnlyKeys("rock", "pop", "jazz");
        assertThat(result).containsEntry("rock", 2);
        assertThat(result).containsEntry("pop", 1);
        assertThat(result).containsEntry("jazz", 1);

        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(result.entrySet());
        sortedEntries.sort(Map.Entry.<String, Integer>comparingByValue().reversed());
        assertThat(new ArrayList<>(result.entrySet())).isEqualTo(sortedEntries);
    }

    @Test
    void aggregateGenres_ShouldHandleEmptyPlaylist() {
        PlaylistTrack[] playlistTracks = new PlaylistTrack[0];

        Map<String, Integer> result = genreAggregatorService.aggregateGenres(playlistTracks);

        assertThat(result).isEmpty();
    }

    @Test
    void aggregateGenres_ShouldHandleArtistsWithNoGenres() throws Exception {
        PlaylistTrack[] playlistTracks = createMockPlaylistTracks();
        when(artistService.getArtistGenres("artist1")).thenReturn(Collections.emptyList());
        when(artistService.getArtistGenres("artist2")).thenReturn(Collections.emptyList());

        Map<String, Integer> result = genreAggregatorService.aggregateGenres(playlistTracks);

        assertThat(result).isEmpty();
    }

    @Test
    void aggregateGenres_ShouldHandleTracksWithNoArtists() {
        PlaylistTrack[] playlistTracks = createMockPlaylistTracksWithNoArtists();

        Map<String, Integer> result = genreAggregatorService.aggregateGenres(playlistTracks);

        assertThat(result).isEmpty();
    }

    @Test
    void aggregateGenres_ShouldHandleException() throws Exception {
        PlaylistTrack[] playlistTracks = createMockPlaylistTracks();
        when(artistService.getArtistGenres("artist1")).thenThrow(new RuntimeException("Test Exception"));

        PlaylistViewerNextException exception = assertThrows(PlaylistViewerNextException.class, () -> {
            genreAggregatorService.aggregateGenres(playlistTracks);
        });
        assertThat(exception).hasMessageContaining("ジャンルの集計中にエラーが発生しました。");
    }

    @Test
    void aggregateGenres_ShouldHandleSpotifyWebApiException() throws Exception {
        PlaylistTrack[] playlistTracks = createMockPlaylistTracks();
        when(artistService.getArtistGenres("artist1")).thenThrow(new RuntimeException(new SpotifyWebApiException("Test Exception")));

        PlaylistViewerNextException exception = assertThrows(PlaylistViewerNextException.class, () -> {
            genreAggregatorService.aggregateGenres(playlistTracks);
        });
        assertThat(exception).hasMessageContaining("ジャンルの集計中にエラーが発生しました。");
    }

    @Test
    void aggregateGenres_ShouldHandleParseException() throws Exception {
        PlaylistTrack[] playlistTracks = createMockPlaylistTracks();
        when(artistService.getArtistGenres("artist1")).thenThrow(new RuntimeException(new ParseException("Test Exception")));

        PlaylistViewerNextException exception = assertThrows(PlaylistViewerNextException.class, () -> {
            genreAggregatorService.aggregateGenres(playlistTracks);
        });
        assertThat(exception).hasMessageContaining("ジャンルの集計中にエラーが発生しました。");
    }

    // 他のテストメソッドは変更なし

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
