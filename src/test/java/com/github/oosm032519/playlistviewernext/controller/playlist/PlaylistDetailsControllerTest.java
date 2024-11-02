package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.ResourceNotFoundException;
import com.github.oosm032519.playlistviewernext.service.analytics.PlaylistAnalyticsService;
import com.github.oosm032519.playlistviewernext.service.analytics.SpotifyPlaylistAnalyticsService;
import com.github.oosm032519.playlistviewernext.service.playlist.PlaylistDetailsRetrievalService;
import com.github.oosm032519.playlistviewernext.service.recommendation.TrackRecommendationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistDetailsControllerTest {

    @Mock
    private PlaylistDetailsRetrievalService playlistDetailsRetrievalService;

    @Mock
    private PlaylistAnalyticsService playlistAnalyticsService;

    @Mock
    private SpotifyPlaylistAnalyticsService spotifyPlaylistAnalyticsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private TrackRecommendationService trackRecommendationService;

    @InjectMocks
    private PlaylistDetailsController detailsController;

    @BeforeEach
    void setUp() {
        reset(playlistDetailsRetrievalService, playlistAnalyticsService, spotifyPlaylistAnalyticsService, trackRecommendationService);
    }

    @Test
    void shouldReturnPlaylistDetailsSuccessfully() throws Exception {
        // Given
        String playlistId = "testPlaylistId";
        Map<String, Object> playlistDetails = createTestPlaylistDetails();
        Map<String, Integer> genreCounts = Map.of("pop", 2, "rock", 1);
        List<String> top5Artists = List.of("artist1", "artist2");
        List<Track> recommendations = List.of(mock(Track.class), mock(Track.class));

        when(playlistDetailsRetrievalService.getPlaylistDetails(playlistId)).thenReturn(playlistDetails);
        when(playlistAnalyticsService.getGenreCountsForPlaylist(playlistId)).thenReturn(genreCounts);
        when(spotifyPlaylistAnalyticsService.getTop5ArtistsForPlaylist(playlistId)).thenReturn(top5Artists);
        when(trackRecommendationService.getRecommendations(
                eq(top5Artists),
                eq(Map.of("feature1", 1.0f)),
                eq(Map.of("feature1", 0.1f))
        )).thenReturn(recommendations);

        // When
        ResponseEntity<Map<String, Object>> response = detailsController.getPlaylistById(playlistId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("genreCounts")).isEqualTo(genreCounts);
        assertThat(response.getBody().get("recommendations")).isEqualTo(recommendations);
        assertThat(response.getBody().get("tracks")).isEqualTo(Map.of("items", List.of(Map.of("track", "track1"), Map.of("track", "track2"))));
        assertThat(response.getBody().get("playlistName")).isEqualTo("Test Playlist");
        assertThat(response.getBody().get("ownerId")).isEqualTo("ownerId");
        assertThat(response.getBody().get("ownerName")).isEqualTo("Owner Name");

        verify(playlistDetailsRetrievalService).getPlaylistDetails(playlistId);
        verify(playlistAnalyticsService).getGenreCountsForPlaylist(playlistId);
        verify(spotifyPlaylistAnalyticsService).getTop5ArtistsForPlaylist(playlistId);
        verify(trackRecommendationService).getRecommendations(
                eq(top5Artists),
                eq(Map.of("feature1", 1.0f)),
                eq(Map.of("feature1", 0.1f))
        );
    }

    @Test
    void shouldHandleResourceNotFoundException() throws Exception {
        // Given
        String playlistId = "testPlaylistId";
        ResourceNotFoundException exception = new ResourceNotFoundException(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
        when(playlistDetailsRetrievalService.getPlaylistDetails(playlistId)).thenThrow(exception);

        // When & Then
        Throwable thrown = catchThrowable(() -> detailsController.getPlaylistById(playlistId));
        assertThat(thrown).isSameAs(exception);


        verify(playlistDetailsRetrievalService).getPlaylistDetails(playlistId);
    }

    private Map<String, Object> createTestPlaylistDetails() {
        return Map.of(
                "tracks", Map.of("items", List.of(Map.of("track", "track1"), Map.of("track", "track2"))),
                "playlistName", "Test Playlist",
                "ownerId", "ownerId",
                "ownerName", "Owner Name",
                "maxAudioFeatures", Map.of("feature1", 1.0f),
                "minAudioFeatures", Map.of("feature1", 0.1f),
                "medianAudioFeatures", Map.of("feature1", 0.5f),
                "modeValues", Map.of("feature1", 0.5f)
        );
    }
}
