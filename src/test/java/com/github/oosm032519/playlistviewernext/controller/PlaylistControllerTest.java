package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.service.SpotifyAnalyticsService;
import com.github.oosm032519.playlistviewernext.service.SpotifyService;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistControllerTest {

    @Mock
    private SpotifyService spotifyService;

    @Mock
    private SpotifyAnalyticsService spotifyAnalyticsService;

    @InjectMocks
    private PlaylistController playlistController;

    @BeforeEach
    void setUp() {
        // 各テストメソッドの前に実行される設定
    }

    @Test
    void searchPlaylists_ReturnsPlaylistsSuccessfully() throws IOException, ParseException, SpotifyWebApiException {
        // Given
        String query = "test query";
        List<PlaylistSimplified> expectedPlaylists = Arrays.asList(
                new PlaylistSimplified.Builder().setName("Playlist 1").build(),
                new PlaylistSimplified.Builder().setName("Playlist 2").build()
        );

        when(spotifyService.searchPlaylists(query)).thenReturn(expectedPlaylists);

        // When
        ResponseEntity<List<PlaylistSimplified>> response = playlistController.searchPlaylists(query);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedPlaylists);
        verify(spotifyService).searchPlaylists(query);
    }

    @Test
    void searchPlaylists_HandlesExceptionGracefully() throws IOException, ParseException, SpotifyWebApiException {
        // Given
        String query = "test query";
        when(spotifyService.searchPlaylists(query)).thenThrow(new RuntimeException("API error"));

        // When
        ResponseEntity<List<PlaylistSimplified>> response = playlistController.searchPlaylists(query);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
        verify(spotifyService).searchPlaylists(query);
    }

    @Test
    void getPlaylistById_ReturnsPlaylistDetailsSuccessfully() throws IOException, ParseException, SpotifyWebApiException {
        // Given
        String playlistId = "testPlaylistId";
        PlaylistTrack[] tracks = new PlaylistTrack[]{
                new PlaylistTrack.Builder().setTrack(new Track.Builder().setId("track1").build()).build(),
                new PlaylistTrack.Builder().setTrack(new Track.Builder().setId("track2").build()).build()
        };
        Map<String, Integer> genreCounts = Map.of("pop", 2, "rock", 1);
        List<String> top5Genres = Arrays.asList("pop", "rock");
        List<Track> recommendations = Arrays.asList(
                new Track.Builder().setName("Recommended Track 1").build(),
                new Track.Builder().setName("Recommended Track 2").build()
        );
        String playlistName = "Test Playlist";
        User owner = new User.Builder().setId("ownerId").setDisplayName("Owner Name").build();

        when(spotifyService.getPlaylistTracks(playlistId)).thenReturn(tracks);
        when(spotifyAnalyticsService.getGenreCountsForPlaylist(playlistId)).thenReturn(genreCounts);
        when(spotifyAnalyticsService.getTop5GenresForPlaylist(playlistId)).thenReturn(top5Genres);
        when(spotifyAnalyticsService.getRecommendations(top5Genres)).thenReturn(recommendations);
        when(spotifyService.getPlaylistName(playlistId)).thenReturn(playlistName);
        when(spotifyService.getPlaylistOwner(playlistId)).thenReturn(owner);
        when(spotifyService.getAudioFeaturesForTrack(anyString())).thenReturn(new AudioFeatures.Builder().build());

        // When
        ResponseEntity<Map<String, Object>> response = playlistController.getPlaylistById(playlistId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        Map<String, Object> responseBody = response.getBody();
        assertThat(responseBody.get("tracks")).isInstanceOf(Map.class);
        assertThat(responseBody.get("genreCounts")).isEqualTo(genreCounts);
        assertThat(responseBody.get("recommendations")).isEqualTo(recommendations);
        assertThat(responseBody.get("playlistName")).isEqualTo(playlistName);
        assertThat(responseBody.get("ownerId")).isEqualTo(owner.getId());
        assertThat(responseBody.get("ownerName")).isEqualTo(owner.getDisplayName());

        verify(spotifyService).getPlaylistTracks(playlistId);
        verify(spotifyAnalyticsService).getGenreCountsForPlaylist(playlistId);
        verify(spotifyAnalyticsService).getTop5GenresForPlaylist(playlistId);
        verify(spotifyAnalyticsService).getRecommendations(top5Genres);
        verify(spotifyService).getPlaylistName(playlistId);
        verify(spotifyService).getPlaylistOwner(playlistId);
        verify(spotifyService, times(tracks.length)).getAudioFeaturesForTrack(anyString());
    }

    @Test
    void getPlaylistById_HandlesExceptionGracefully() throws IOException, ParseException, SpotifyWebApiException {
        // Given
        String playlistId = "testPlaylistId";
        when(spotifyService.getPlaylistTracks(playlistId)).thenThrow(new RuntimeException("API error"));

        // When
        ResponseEntity<Map<String, Object>> response = playlistController.getPlaylistById(playlistId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("API error");

        verify(spotifyService).getPlaylistTracks(playlistId);
    }

    @Test
    void getFollowedPlaylists_ReturnsPlaylistsSuccessfully() throws IOException, ParseException, SpotifyWebApiException {
        // Given
        OAuth2AuthenticationToken authToken = mock(OAuth2AuthenticationToken.class);
        List<PlaylistSimplified> expectedPlaylists = Arrays.asList(
                new PlaylistSimplified.Builder().setName("Followed Playlist 1").build(),
                new PlaylistSimplified.Builder().setName("Followed Playlist 2").build()
        );

        when(spotifyService.getCurrentUsersPlaylists(authToken)).thenReturn(expectedPlaylists);

        // When
        ResponseEntity<?> response = playlistController.getFollowedPlaylists(authToken);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedPlaylists);
        verify(spotifyService).getCurrentUsersPlaylists(authToken);
    }

    @Test
    void getFollowedPlaylists_HandlesExceptionGracefully() throws IOException, ParseException, SpotifyWebApiException {
        // Given
        OAuth2AuthenticationToken authToken = mock(OAuth2AuthenticationToken.class);
        when(spotifyService.getCurrentUsersPlaylists(authToken)).thenThrow(new RuntimeException("Authentication error"));

        // When
        ResponseEntity<?> response = playlistController.getFollowedPlaylists(authToken);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Error: Authentication error");
        verify(spotifyService).getCurrentUsersPlaylists(authToken);
    }
}
