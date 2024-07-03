package com.github.oosm032519.playlistviewernext.service;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SpotifyAnalyticsServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private SpotifyService spotifyService;

    @InjectMocks
    private SpotifyAnalyticsService spotifyAnalyticsService;

    @Test
    void getGenreCountsForPlaylist_ShouldReturnSortedGenreCounts() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "testPlaylistId";
        PlaylistTrack[] playlistTracks = createMockPlaylistTracks();
        when(spotifyService.getPlaylistTracks(playlistId)).thenReturn(playlistTracks);
        when(spotifyService.getArtistGenres("artist1")).thenReturn(Arrays.asList("rock", "pop"));
        when(spotifyService.getArtistGenres("artist2")).thenReturn(Arrays.asList("rock", "jazz"));

        // Act
        Map<String, Integer> result = spotifyAnalyticsService.getGenreCountsForPlaylist(playlistId);

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

    @Test
    void getGenreCountsForPlaylist_ShouldHandleEmptyPlaylist() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "emptyPlaylistId";
        when(spotifyService.getPlaylistTracks(playlistId)).thenReturn(new PlaylistTrack[0]);

        // Act
        Map<String, Integer> result = spotifyAnalyticsService.getGenreCountsForPlaylist(playlistId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getGenreCountsForPlaylist_ShouldHandleException() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "errorPlaylistId";
        when(spotifyService.getPlaylistTracks(playlistId)).thenThrow(new IOException("API error"));

        // Act & Assert
        assertThatThrownBy(() -> spotifyAnalyticsService.getGenreCountsForPlaylist(playlistId))
                .isInstanceOf(IOException.class)
                .hasMessage("API error");
    }

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

        SpotifyAnalyticsService spyService = spy(spotifyAnalyticsService);
        doReturn(genreCounts).when(spyService).getGenreCountsForPlaylist(playlistId);

        // Act
        List<String> result = spyService.getTop5GenresForPlaylist(playlistId);

        // Assert
        assertThat(result).hasSize(5);
        assertThat(result).containsExactly("rock", "pop", "jazz", "blues", "classical");
    }

    @Test
    void getRecommendations_ShouldReturnRecommendedTracks() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        List<String> seedGenres = Arrays.asList("rock", "pop");
        Track[] mockTracks = createMockTracks();
        Recommendations mockRecommendations = mock(Recommendations.class);
        when(mockRecommendations.getTracks()).thenReturn(mockTracks);

        GetRecommendationsRequest.Builder recommendationsBuilder = mock(GetRecommendationsRequest.Builder.class);
        GetRecommendationsRequest recommendationsRequest = mock(GetRecommendationsRequest.class);

        when(spotifyApi.getRecommendations()).thenReturn(recommendationsBuilder);
        when(recommendationsBuilder.seed_genres(anyString())).thenReturn(recommendationsBuilder);
        when(recommendationsBuilder.limit(anyInt())).thenReturn(recommendationsBuilder);
        when(recommendationsBuilder.build()).thenReturn(recommendationsRequest);
        when(recommendationsRequest.execute()).thenReturn(mockRecommendations);

        // Act
        List<Track> result = spotifyAnalyticsService.getRecommendations(seedGenres);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Track 1");
        assertThat(result.get(1).getName()).isEqualTo("Track 2");
        verify(recommendationsBuilder).seed_genres("rock,pop");
        verify(recommendationsBuilder).limit(20);
    }

    @Test
    void getRecommendations_ShouldHandleEmptySeedGenres() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        List<String> seedGenres = Collections.emptyList();

        // Act
        List<Track> result = spotifyAnalyticsService.getRecommendations(seedGenres);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getGenreCountsForPlaylist_ShouldHandleNullTracks() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "nullTracksPlaylistId";
        when(spotifyService.getPlaylistTracks(playlistId)).thenReturn(null);

        // Act
        Map<String, Integer> result = spotifyAnalyticsService.getGenreCountsForPlaylist(playlistId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getRecommendations_ShouldHandleApiException() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        List<String> seedGenres = Arrays.asList("rock", "pop");
        GetRecommendationsRequest.Builder recommendationsBuilder = mock(GetRecommendationsRequest.Builder.class);
        GetRecommendationsRequest recommendationsRequest = mock(GetRecommendationsRequest.class);

        when(spotifyApi.getRecommendations()).thenReturn(recommendationsBuilder);
        when(recommendationsBuilder.seed_genres(anyString())).thenReturn(recommendationsBuilder);
        when(recommendationsBuilder.limit(anyInt())).thenReturn(recommendationsBuilder);
        when(recommendationsBuilder.build()).thenReturn(recommendationsRequest);
        when(recommendationsRequest.execute()).thenThrow(new SpotifyWebApiException("API error"));

        // Act & Assert
        assertThatThrownBy(() -> spotifyAnalyticsService.getRecommendations(seedGenres))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("API error");
    }

    @Test
    void getTop5GenresForPlaylist_ShouldHandleLessThan5Genres() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "fewGenresPlaylistId";
        Map<String, Integer> genreCounts = new LinkedHashMap<>();
        genreCounts.put("rock", 10);
        genreCounts.put("pop", 8);
        genreCounts.put("jazz", 6);

        SpotifyAnalyticsService spyService = spy(spotifyAnalyticsService);
        doReturn(genreCounts).when(spyService).getGenreCountsForPlaylist(playlistId);

        // Act
        List<String> result = spyService.getTop5GenresForPlaylist(playlistId);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result).containsExactly("rock", "pop", "jazz");
    }

    @Test
    void getGenreCountsForPlaylist_ShouldHandleArtistsWithNoGenres() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "noGenresPlaylistId";
        PlaylistTrack[] playlistTracks = createMockPlaylistTracks();
        when(spotifyService.getPlaylistTracks(playlistId)).thenReturn(playlistTracks);
        when(spotifyService.getArtistGenres("artist1")).thenReturn(Collections.emptyList());
        when(spotifyService.getArtistGenres("artist2")).thenReturn(Collections.emptyList());

        // Act
        Map<String, Integer> result = spotifyAnalyticsService.getGenreCountsForPlaylist(playlistId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getRecommendations_ShouldHandleNullRecommendations() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        List<String> seedGenres = Arrays.asList("rock", "pop");
        GetRecommendationsRequest.Builder recommendationsBuilder = mock(GetRecommendationsRequest.Builder.class);
        GetRecommendationsRequest recommendationsRequest = mock(GetRecommendationsRequest.class);

        when(spotifyApi.getRecommendations()).thenReturn(recommendationsBuilder);
        when(recommendationsBuilder.seed_genres(anyString())).thenReturn(recommendationsBuilder);
        when(recommendationsBuilder.limit(anyInt())).thenReturn(recommendationsBuilder);
        when(recommendationsBuilder.build()).thenReturn(recommendationsRequest);
        when(recommendationsRequest.execute()).thenReturn(null);

        // Act
        List<Track> result = spotifyAnalyticsService.getRecommendations(seedGenres);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getGenreCountsForPlaylist_ShouldHandleTracksWithNoArtists() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "noArtistsPlaylistId";
        PlaylistTrack[] playlistTracks = createMockPlaylistTracksWithNoArtists();
        when(spotifyService.getPlaylistTracks(playlistId)).thenReturn(playlistTracks);

        // Act
        Map<String, Integer> result = spotifyAnalyticsService.getGenreCountsForPlaylist(playlistId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getTop5GenresForPlaylist_ShouldHandleEmptyPlaylist() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "emptyPlaylistId";
        when(spotifyService.getPlaylistTracks(playlistId)).thenReturn(new PlaylistTrack[0]);

        // Act
        List<String> result = spotifyAnalyticsService.getTop5GenresForPlaylist(playlistId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getTop5GenresForPlaylist_ShouldHandleException() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "errorPlaylistId";
        when(spotifyService.getPlaylistTracks(playlistId)).thenThrow(new IOException("API error"));

        // Act & Assert
        assertThatThrownBy(() -> spotifyAnalyticsService.getTop5GenresForPlaylist(playlistId))
                .isInstanceOf(IOException.class)
                .hasMessage("API error");
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

    private Track[] createMockTracks() {
        Track track1 = mock(Track.class);
        Track track2 = mock(Track.class);
        ArtistSimplified artist1 = mock(ArtistSimplified.class);
        ArtistSimplified artist2 = mock(ArtistSimplified.class);

        when(track1.getName()).thenReturn("Track 1");
        when(track2.getName()).thenReturn("Track 2");
        when(track1.getArtists()).thenReturn(new ArtistSimplified[]{artist1});
        when(track2.getArtists()).thenReturn(new ArtistSimplified[]{artist2});
        when(artist1.getName()).thenReturn("Artist 1");
        when(artist2.getName()).thenReturn("Artist 2");

        return new Track[]{track1, track2};
    }
}
